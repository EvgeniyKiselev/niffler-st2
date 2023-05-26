package niffler.jupiter.annotation;

import niffler.model.DBType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static niffler.model.DBType.HIBERNATE;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GenerateRandomUserEntity {
    DBType dbType() default HIBERNATE;
}