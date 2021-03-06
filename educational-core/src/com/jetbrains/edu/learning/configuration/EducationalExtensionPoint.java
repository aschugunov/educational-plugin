package com.jetbrains.edu.learning.configuration;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.util.xmlb.annotations.Attribute;
import com.jetbrains.edu.learning.EduNames;
import org.jetbrains.annotations.NotNull;

public class EducationalExtensionPoint<T> extends AbstractExtensionPointBean {
  public static final ExtensionPointName<EducationalExtensionPoint<EduConfigurator<?>>> EP_NAME =
    ExtensionPointName.create("Educational.configurator");

  @Attribute("implementationClass")
  public String implementationClass;

  @Attribute("language")
  public String language = "";

  @Attribute("courseType")
  public String courseType = EduNames.PYCHARM;

  @Attribute("environment")
  public String environment = "";

  @Attribute("displayName")
  public String displayName = null;

  private final AtomicNotNullLazyValue<T> myInstanceHolder = new AtomicNotNullLazyValue<T>() {
    @NotNull
    @Override
    protected T compute() {
      try {
        return instantiate(implementationClass, ApplicationManager.getApplication().getPicoContainer());
      }
      catch (final ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  };

  @NotNull
  public T getInstance() {
    return myInstanceHolder.getValue();
  }
}
