package edu.umass.nlp.exec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD,ElementType.METHOD})
public @interface Opt {
	public abstract String name() default "[unassigned]";
	public abstract String gloss() default "";
	public abstract boolean required() default false;
  public abstract String defaultVal() default "";
}
