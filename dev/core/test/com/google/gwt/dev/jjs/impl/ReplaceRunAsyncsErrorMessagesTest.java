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

package com.google.gwt.dev.jjs.impl;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.javac.testing.impl.MockJavaResource;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.util.UnitTestTreeLogger;

/**
 * Tests the error messages generated by {@link ReplaceRunAsyncs}.
 */
public class ReplaceRunAsyncsErrorMessagesTest extends JJSTestBase {
  private UnitTestTreeLogger.Builder testLoggerBuilder;

  @Override
  public void setUp() {
    addCommonTestCode();
    initializeTestLoggerBuilder();
  }

  public void testAmbiguousClassLiteral() {
    sourceOracle.addOrReplace(new MockJavaResource("test.SplitPoint3") {
      @Override
      public CharSequence getContent() {
        StringBuffer code = new StringBuffer();
        code.append("package test;\n");
        code.append("import com.google.gwt.core.client.GWT;\n");
        code.append("public class SplitPoint3 {\n");
        code.append("  void doStuff() {\n");
        // Intentionally reuse SplitPoint1.class
        code.append("    GWT.runAsync(SplitPoint1.class, null);\n");
        code.append("  }\n");
        code.append("}\n");
        return code;
      }
    });
    addSnippetImport("test.SplitPoint3");

    expectError("Line 15: Multiple runAsync calls are named test.SplitPoint1");
    expectError("One call is in test.SplitPoint1.doStuff (/mock/test/SplitPoint1.java:4)");
    expectError("One call is in test.SplitPoint3.doStuff (/mock/test/SplitPoint3.java:4)");

    testSnippet("RunAsyncCode.runAsyncCode(SplitPoint1.class);");
  }

  public void testNonClassLiteral() {
    expectError("Line 14: Only a class literal may be passed to runAsyncCode");
    testSnippet("RunAsyncCode.runAsyncCode(new SplitPoint1().getClass());");
  }

  public void testNonExistentSplitPoint() {
    expectError("Line 14: No runAsync call is named java.lang.String");
    testSnippet("RunAsyncCode.runAsyncCode(String.class);");
  }

  private void addAsyncLoader(final int sp) {
    sourceOracle.addOrReplace(new MockJavaResource(
        "com.google.gwt.lang.asyncloaders.AsyncLoader" + sp) {
      @Override
      public CharSequence getContent() {
        StringBuffer code = new StringBuffer();
        code.append("package com.google.gwt.lang.asyncloaders;\n");
        code.append("import com.google.gwt.core.client.RunAsyncCallback;");
        code.append("public class AsyncLoader" + sp + " {\n");
        code.append("  public static void onLoad() { }\n");
        code.append("  public static void runAsync(RunAsyncCallback cb) { }\n");
        code.append("  public static void runCallbacks() { }\n");
        code.append("}\n");
        return code;
      }
    });

    addSnippetImport("com.google.gwt.lang.asyncloaders.AsyncLoader" + sp);

    sourceOracle.addOrReplace(new MockJavaResource(
        "com.google.gwt.lang.asyncloaders.AsyncLoader" + sp
            + FragmentLoaderCreator.CALLBACK_LIST_SUFFIX) {
      @Override
      public CharSequence getContent() {
        StringBuffer code = new StringBuffer();
        code.append("package com.google.gwt.lang.asyncloaders;\n");
        code.append("import com.google.gwt.core.client.RunAsyncCallback;");
        code.append("public class AsyncLoader" + sp
            + FragmentLoaderCreator.CALLBACK_LIST_SUFFIX + "{\n");
        code.append("  RunAsyncCallback callback;\n");
        code.append("}\n");
        return code;
      }
    });

    addSnippetImport("com.google.gwt.lang.asyncloaders.AsyncLoader" + sp
        + FragmentLoaderCreator.CALLBACK_LIST_SUFFIX);
  }

  private void addCommonTestCode() {
    addAsyncLoader(1);
    addAsyncLoader(2);
    addAsyncLoader(3);

    sourceOracle.addOrReplace(new MockJavaResource(
        "com.google.gwt.core.client.impl.AsyncFragmentLoader") {
      @Override
      public CharSequence getContent() {
        StringBuffer code = new StringBuffer();
        code.append("package com.google.gwt.core.client.impl;\n");
        code.append("public class AsyncFragmentLoader {\n");
        code.append("  private static AsyncFragmentLoader BROWSER_LOADER =\n");
        code.append("    makeBrowserLoader(1, new int[] {});\n");
        code.append("  private static AsyncFragmentLoader makeBrowserLoader(\n");
        code.append("    int numSp, int[] initial) {\n");
        code.append("    return null;\n");
        code.append("  }\n");
        code.append("}\n");
        return code;
      }
    });
    addSnippetImport("com.google.gwt.core.client.impl.AsyncFragmentLoader");

    sourceOracle.addOrReplace(new MockJavaResource(
        "com.google.gwt.core.client.prefetch.RunAsyncCode") {
      @Override
      public CharSequence getContent() {
        StringBuffer code = new StringBuffer();
        code.append("package com.google.gwt.core.client.prefetch;\n");
        code.append("public class RunAsyncCode {\n");
        code.append("  public static RunAsyncCode runAsyncCode(Class<?> splitPoint) {\n");
        code.append("    return null;\n");
        code.append("  }");
        code.append("}");
        return code;
      }
    });
    addSnippetImport("com.google.gwt.core.client.prefetch.RunAsyncCode");

    sourceOracle.addOrReplace(new MockJavaResource("test.SplitPoint1") {
      @Override
      public CharSequence getContent() {
        StringBuffer code = new StringBuffer();
        code.append("package test;\n");
        code.append("import com.google.gwt.core.client.GWT;\n");
        code.append("public class SplitPoint1 {\n");
        code.append("  void doStuff() {\n");
        code.append("    GWT.runAsync(SplitPoint1.class, null);\n");
        code.append("  }\n");
        code.append("}\n");
        return code;
      }
    });
    addSnippetImport("test.SplitPoint1");

    sourceOracle.addOrReplace(new MockJavaResource("test.SplitPoint2") {
      @Override
      public CharSequence getContent() {
        StringBuffer code = new StringBuffer();
        code.append("package test;\n");
        code.append("import com.google.gwt.core.client.GWT;\n");
        code.append("public class SplitPoint2 {\n");
        code.append("  void doStuff() {\n");
        code.append("    GWT.runAsync(SplitPoint2.class, null);\n");
        code.append("  }\n");
        code.append("}\n");
        return code;
      }
    });
    addSnippetImport("test.SplitPoint2");
  }

  private void expectError(String msg) {
    testLoggerBuilder.expectError(msg, null);
  }

  private void initializeTestLoggerBuilder() {
    testLoggerBuilder = new UnitTestTreeLogger.Builder();
    testLoggerBuilder.setLowestLogLevel(TreeLogger.ERROR);
    expectError("Error in '/mock/test/EntryPoint.java'");
  }

  private void testSnippet(String codeSnippet) {
    UnitTestTreeLogger testLogger = testLoggerBuilder.createLogger();
    logger = testLogger;

    try {
      JProgram program = compileSnippet("void", codeSnippet);
      ReplaceRunAsyncs.exec(logger, program);
      fail("Expected a compile error");
    } catch (UnableToCompleteException e) {
      // expected
    }

    testLogger.assertCorrectLogEntries();
  }
}
