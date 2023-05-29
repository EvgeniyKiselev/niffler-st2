package niffler.jupiter.annotation;

import niffler.jupiter.extension.DBType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static niffler.jupiter.extension.DBType.HIBERNATE;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GenerateUserEntity {
    DBType dbType() default HIBERNATE;
}