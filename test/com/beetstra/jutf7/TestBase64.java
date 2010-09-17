/* ====================================================================
 * Copyright (c) 2006 J.T. Beetstra
 *
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to 
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * ====================================================================
 */
package com.beetstra.jutf7;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author 	Firstname Lastname
 * @version	$Revision$
 */
public class TestBase64 {
	private static final String BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
		+ "abcdefghijklmnopqrstuvwxyz" + "0123456789+/";
	private static Base64Util tested;

	/**
	 * 
	 */
	@BeforeClass
	public static void setUp() {
		tested = new Base64Util(BASE64_ALPHABET);
	}

	/**
	 */
	@Test
	public void testRejectShort() {
		try {
			Base64Util util = new Base64Util("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+");
			assertNotNull(util);
			fail("short alphabet error accepted");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/**
	 * 
	 */
	@Test
	public void testRejectShortLong() {
		try {
			Base64Util util =  new Base64Util("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/,");
			assertNotNull(util);
			fail("long alphabet error accepted");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/**
	 * 
	 */
	@Test
	public void testContains() {
		assertTrue(tested.contains('A'));
		assertTrue(tested.contains('/'));
		assertFalse(tested.contains(','));
	}

	/**
	 * 
	 */
	@Test
	public void testGetSextet() {
		assertEquals(0, tested.getSextet((byte) 'A'));
		assertEquals(63, tested.getSextet((byte) '/'));
		assertEquals( -1, tested.getSextet((byte) ','));
	}

	/**
	 * 
	 */
	@Test
	public void testGetChar() {
		assertEquals('A', tested.getChar(0));
		assertEquals('/', tested.getChar(63));
		assertEquals('a', tested.getChar(26));
		assertEquals('0', tested.getChar(52));
	}
}
