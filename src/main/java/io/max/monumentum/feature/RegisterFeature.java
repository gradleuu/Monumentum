package io.max.monumentum.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterFeature {
  // the feature's name and also its id and its key in features.json
  String value();

  boolean turnedOnByDefault() default true;
}
