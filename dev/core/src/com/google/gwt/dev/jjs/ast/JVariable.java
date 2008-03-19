/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;

/**
 * Base class for any storage location.
 */
public abstract class JVariable extends JNode implements CanBeSetFinal,
    CanHaveInitializer, HasName, HasSettableType {

  protected JExpression initializer = null;
  private boolean isFinal;
  private final String name;
  private JType type;

  JVariable(JProgram program, SourceInfo info, String name, JType type,
      boolean isFinal) {
    super(program, info);
    this.name = name;
    this.type = type;
    this.isFinal = isFinal;
  }

  public JLiteral getConstInitializer() {
    if (isFinal() && initializer instanceof JLiteral) {
      return (JLiteral) initializer;
    }
    return null;
  }

  public String getName() {
    return name;
  }

  public JType getType() {
    return type;
  }

  public boolean hasInitializer() {
    return initializer != null;
  }

  public boolean isFinal() {
    return isFinal;
  }

  public void setFinal() {
    isFinal = true;
  }

  public void setType(JType newType) {
    type = newType;
  }

}
