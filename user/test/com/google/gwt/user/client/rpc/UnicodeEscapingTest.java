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
package com.google.gwt.user.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.UnicodeEscapingService.InvalidCharacterException;

/**
 * Test that any valid string can be sent via RPC in both directions.
 * 
 * TODO(jat): make unpaired surrogates work properly if it is possible to do
 * so on all browsers, then add them to this test.
 */
public class UnicodeEscapingTest extends GWTTestCase {

  /** the size of a block of characters to test */
  private static final int CHARACTER_BLOCK_SIZE = 64;

  /**
   * When doing the non-BMP test, we don't test every block of characters
   * because it takes too long - this is the increment to use.  It is not a
   * power of two so we alter the alignment of the block of characters we skip.
   */
  private static final int NON_BMP_TEST_INCREMENT = 8192 + 64;

  /** the time to wait for the test of a block of characters */
  private static final int TEST_FINISH_DELAY_MS = 500000;

  /**
   * Generates a string containing a sequence of code points.
   * 
   * @param start first code point to include in the string
   * @param end one past the last code point to include in the string
   * @return a string containing all the requested code points
   */
  public static String getStringContainingCharacterRange(int start, int end) {
    StringBuffer buf = new StringBuffer();
    for (int codePoint = start; codePoint < end; ++codePoint) {
      if (Character.isSupplementaryCodePoint(codePoint)) {
        buf.append(Character.toChars(codePoint));
      } else {
        buf.append((char) codePoint);
      }
    }

    return buf.toString();
  }

  /*
   * Copied from HistoryTest.
   */
  private static native boolean isSafari2() /*-{
    var exp = / AppleWebKit\/([\d]+)/;
    var result = exp.exec(navigator.userAgent);
    if (result) {
      // The standard history implementation works fine on WebKit >= 522
      // (Safari 3 beta).
      if (parseInt(result[1]) >= 522) {
        return false;
      }
    }
  
    // The standard history implementation works just fine on the iPhone, which
    // unfortunately reports itself as WebKit/420+.
    if (navigator.userAgent.indexOf('iPhone') != -1) {
      return false;
    }
  
    return true;
  }-*/;
  /**
   * Verifies that the supplied string includes the requested code points.
   * 
   * @param start first code point to include in the string
   * @param end one past the last code point to include in the string
   * @param str the string to test
   * @throws InvalidCharacterException if a character doesn't match
   * @throws RuntimeException if the string is too long
   */
  public static void verifyStringContainingCharacterRange(int start, int end,
      String str) throws InvalidCharacterException {
    if (str == null) {
      throw new NullPointerException("String is null");
    }
    int expectedLen = end - start;
    int strLen = str.codePointCount(0, str.length());
    for (int i = 0, codePoint = start; i < strLen;
        i = Character.offsetByCodePoints(str, i, 1)) {
      int strCodePoint = str.codePointAt(i);
      if (strCodePoint != codePoint) {
        throw new InvalidCharacterException(i, codePoint, strCodePoint);
      }
      ++codePoint;
    }
    if (strLen < expectedLen) {
      throw new InvalidCharacterException(strLen, start + strLen, -1);
    } else if (expectedLen != strLen) {
      throw new RuntimeException("Too many characters returned on block from U+"
          + Integer.toHexString(start) + " to U+" + Integer.toHexString(end)
          + ": expected=" + expectedLen + ", actual=" + strLen);
    }
  }
  private static UnicodeEscapingServiceAsync getService() {
    UnicodeEscapingServiceAsync service = GWT.create(
        UnicodeEscapingService.class);
    ServiceDefTarget target = (ServiceDefTarget) service;
    target.setServiceEntryPoint(GWT.getModuleBaseURL() + "unicodeEscape");
    return service;
  }

  /** start of current block being tested */
  private int current;

  @Override
  public String getModuleName() {
    return "com.google.gwt.user.RPCSuite";
  }

  /**
   * Generate strings containing ranges of characters and sends them to the
   * server for verification. This ensures that client->server string escaping
   * properly handles all BMP characters.
   * 
   * Unpaired or improperly paired surrogates are not tested here, as some
   * browsers refuse to accept them.  Properly paired surrogates are tested
   * in the non-BMP test.
   *  
   * Note that this does not test all possible combinations, which might be an
   * issue, particularly with combining marks, though they should be logically
   * equivalent in that case.
   * 
   * @throws InvalidCharacterException
   */
  public void testClientToServerBMP() throws InvalidCharacterException {
    delayTestFinish(TEST_FINISH_DELAY_MS);
    if (isSafari2()) {
      // Safari2 can't be fixed for many characters, including null
      // We only guarantee that basic ISO-Latin characters are unmolested.
      clientToServerVerifyRange(0x0001, 0x0300, CHARACTER_BLOCK_SIZE,
          CHARACTER_BLOCK_SIZE);
    } else {
      clientToServerVerifyRange(Character.MIN_CODE_POINT,
          Character.MIN_SURROGATE, CHARACTER_BLOCK_SIZE,
          CHARACTER_BLOCK_SIZE);
      clientToServerVerifyRange(Character.MAX_SURROGATE + 1,
          Character.MIN_SUPPLEMENTARY_CODE_POINT, CHARACTER_BLOCK_SIZE,
          CHARACTER_BLOCK_SIZE);
    }
  }

  /**
   * Generate strings containing ranges of characters and sends them to the
   * server for verification. This ensures that client->server string escaping
   * properly handles all non-BMP characters.
   * 
   * Note that this does not test all possible combinations, which might be an
   * issue, particularly with combining marks, though they should be logically
   * equivalent in that case.
   * 
   * @throws InvalidCharacterException
   */
  public void testClientToServerNonBMP() throws InvalidCharacterException {
    delayTestFinish(TEST_FINISH_DELAY_MS);
    clientToServerVerifyRange(Character.MIN_SUPPLEMENTARY_CODE_POINT,
        Character.MAX_CODE_POINT + 1, CHARACTER_BLOCK_SIZE,
        NON_BMP_TEST_INCREMENT);
  }

  /**
   * Requests strings of CHARACTER_RANGE_SIZE from the server and validates
   * that the returned string length matches CHARACTER_RANGE_SIZE and that all
   * of the characters remain intact.
   * 
   * Note that this does not test all possible combinations, which might be an
   * issue, particularly with combining marks, though they should be logically
   * equivalent in that case.
   */
  public void testServerToClientBMP() {
    delayTestFinish(TEST_FINISH_DELAY_MS);
    serverToClientVerify(Character.MIN_CODE_POINT,
        Character.MIN_SUPPLEMENTARY_CODE_POINT, CHARACTER_BLOCK_SIZE,
        CHARACTER_BLOCK_SIZE);
  }

  /**
   * Requests strings of CHARACTER_RANGE_SIZE from the server and validates that
   * the returned string length matches CHARACTER_RANGE_SIZE and that all of the
   * characters remain intact.  Note that this test verifies non-BMP characters
   * (ie, those which are represented as pairs of surrogates).
   * 
   * Note that this does not test all possible combinations, which might be an
   * issue, particularly with combining marks, though they should be logically
   * equivalent in that case.
   */
  public void testServerToClientNonBMP() {
    delayTestFinish(TEST_FINISH_DELAY_MS);
    serverToClientVerify(Character.MIN_SUPPLEMENTARY_CODE_POINT,
        Character.MAX_CODE_POINT + 1, CHARACTER_BLOCK_SIZE,
        NON_BMP_TEST_INCREMENT);
  }

  private void clientToServerVerifyRange(final int start, final int end,
      final int size, final int step) throws InvalidCharacterException {
    current = start;
    int blockEnd = Math.min(end, current + size);
    getService().verifyStringContainingCharacterRange(current, blockEnd,
        getStringContainingCharacterRange(start, blockEnd),
        new AsyncCallback<Boolean>() {
      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(Boolean ignored) {
        current += step;
        if (current < end) {
          delayTestFinish(TEST_FINISH_DELAY_MS);
          int blockEnd = Math.min(end, current + size);
          try {
            getService().verifyStringContainingCharacterRange(current, blockEnd,
                getStringContainingCharacterRange(current, blockEnd), this);
          } catch (InvalidCharacterException e) {
            TestSetValidator.rethrowException(e);
          }
        } else {
          finishTest();
        }
      }
    });
  }

  private void serverToClientVerify(final int start, final int end,
      final int size, final int step) {
    current = start;
    getService().getStringContainingCharacterRange(start, Math.min(end,
        current + size), new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        TestSetValidator.rethrowException(caught);
      }

      public void onSuccess(String str) {
        try {
          verifyStringContainingCharacterRange(current, Math.min(end,
              current + size), str);
        } catch (InvalidCharacterException e) {
          TestSetValidator.rethrowException(e);
        }
        current += step;
        if (current < end) {
          delayTestFinish(TEST_FINISH_DELAY_MS);
          getService().getStringContainingCharacterRange(current,
              Math.min(end, current + size), this);
        } else {
          finishTest();
        }
      }
    });
  }
}
