package me.gong.lavarun.plugin.timer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
/*
 * @author WesJD https://github.com/WesJD
 */
public @interface Timer {

    long runEvery();

    boolean millisTime() default false;

}
