package com.intellij.codeInspection;

public class InspectingForStatement implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
    return new Class[]{ForStatementConditionInspection.class};
  }
}
