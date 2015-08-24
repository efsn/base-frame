package com.codeyn.jfinal.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Extension annotation based on JFinal
 * 
 * @author Codeyn
 *
 */
public class JFinalAnnos {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Route {
        String value(); // path

        String viewPath() default "";
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ModelMapping {
        String value(); // table name

        String primary() default "";
    }

}
