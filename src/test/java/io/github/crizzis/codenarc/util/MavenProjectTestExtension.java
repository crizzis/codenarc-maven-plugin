package io.github.crizzis.codenarc.util;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MavenProjectTestExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {

    private static final String VERIFIER = "verifier";
    private static final String PROJECT_ROOT = "projectRoot";
    public static final String MODEL = "model";

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        ExtensionContext.Store methodScopedStore = getMethodScopedStore(context);
        Verifier verifier = (Verifier) methodScopedStore.remove(VERIFIER);
        removeGeneratedArtifact((Model) methodScopedStore.remove(MODEL), verifier);
        FileUtils.forceDelete(new File((File) methodScopedStore.remove(PROJECT_ROOT), "target"));
        verifier.resetStreams();
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        ExtensionContext.Store methodScopedStore = getMethodScopedStore(context);
        File projectRoot = ResourceExtractor.simpleExtractResources(
                context.getRequiredTestClass(),
                getTestResourceProjectRoot(context));
        methodScopedStore.put(PROJECT_ROOT, projectRoot);
        methodScopedStore.put(VERIFIER, prepareVerifier(projectRoot, context));
        methodScopedStore.put(MODEL, parsePomXml(projectRoot));
    }

    private Verifier prepareVerifier(File projectRoot, ExtensionContext context) throws VerificationException {
        Verifier verifier = new Verifier(projectRoot.getAbsolutePath());
        verifier.setAutoclean(context.getRequiredTestMethod().getAnnotation(MavenProjectTest.class).autoClean());
        return verifier;
    }

    private Model parsePomXml(File projectRoot) throws IOException, XmlPullParserException {
        return new MavenXpp3Reader().read(new FileReader(new File(projectRoot, "pom.xml")));
    }

    private String getTestResourceProjectRoot(ExtensionContext context) {
        return context.getRequiredTestMethod().getAnnotation(MavenProjectTest.class).value();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return isVerifier(parameterContext) || isProjectRoot(parameterContext) || isModel(parameterContext);
    }

    private boolean isModel(ParameterContext parameterContext) {
        return isParameterType(parameterContext, Model.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (isVerifier(parameterContext)) {
            return getMethodScopedStore(extensionContext).get(VERIFIER);
        } else if (isProjectRoot(parameterContext)) {
            return getMethodScopedStore(extensionContext).get(PROJECT_ROOT);
        } else if (isModel(parameterContext)) {
            return getMethodScopedStore(extensionContext).get(MODEL);
        }
        return null;
    }

    private boolean isProjectRoot(ParameterContext parameterContext) {
        return isParameterType(parameterContext, File.class) && parameterContext.isAnnotated(ProjectRoot.class);
    }

    private boolean isVerifier(ParameterContext parameterContext) {
        return isParameterType(parameterContext, Verifier.class);
    }

    private boolean isParameterType(ParameterContext parameterContext, Class<?> type) {
        return parameterContext.getParameter().getType().equals(type);
    }

    private ExtensionContext.Store getMethodScopedStore(ExtensionContext context) {
        return context.getStore(methodScopedNamespace(context));
    }

    private ExtensionContext.Namespace methodScopedNamespace(ExtensionContext extensionContext) {
        return ExtensionContext.Namespace.create(extensionContext.getRequiredTestMethod());
    }

    private void removeGeneratedArtifact(Model model, Verifier verifier) throws IOException {
        verifier.deleteArtifact(model.getGroupId(), model.getArtifactId(), model.getVersion(), model.getPackaging());
    }
}
