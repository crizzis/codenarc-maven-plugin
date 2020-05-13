package com.github.crizzis;

import com.github.crizzis.util.MavenProjectTest;
import com.github.crizzis.util.Phrasify;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Tag;

@Tag("integration-test")
@DisplayNameGeneration(Phrasify.class)
class VerifyGoalIntegrationTest {

    @MavenProjectTest("/projects/verify-minimal-config-no-violations")
    void verifyMinimalConfig_shouldSucceed(Verifier verifier, Model model) throws VerificationException {
        //when
        verifier.executeGoal("verify");

        //then
        verifier.assertFilePresent("target/codenarc.xml");
        verifier.verifyErrorFreeLog();
    }

}
