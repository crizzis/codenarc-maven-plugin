package com.github.crizzis.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Test
@ExtendWith(MavenProjectTestExtension.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface MavenProjectTest {

    String value();

    boolean autoClean() default true;
}
