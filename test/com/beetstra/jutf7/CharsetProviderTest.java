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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Test;
/**
 * @author Firstname Lastname
 * @version $Revision$
 */
public class CharsetProviderTest {
	private CharsetProvider tested = new CharsetProvider();

	/**
	 * 
	 */
	@Test
	public void testModifiedUTF7() {
		Charset charset = tested.charsetForName("x-modified-UTF-7");
		assertNotNull("charset not found", charset);
		assertEquals(ModifiedUTF7Charset.class, charset.getClass());
		assertEquals(charset, tested.charsetForName("x-imap-modified-utf-7"));
		assertEquals(charset, tested.charsetForName("x-imap4-modified-utf7"));
		assertEquals(charset, tested.charsetForName("x-imap4-modified-utf-7"));
		assertEquals(charset, tested.charsetForName("x-RFC3501"));
		assertEquals(charset, tested.charsetForName("x-RFC-3501"));
		assertEquals(charset, tested.charsetForName("imap-utf-7"));
		assertEquals(charset, tested.charsetForName("imap-utf7"));
	}

	/**
	 * 
	 */
	@Test
	public void testUTF7() {
		Charset charset = tested.charsetForName("UTF-7");
		assertNotNull("charset not found", charset);
		assertEquals(UTF7Charset.class, charset.getClass());
		assertEquals(charset, tested.charsetForName("utf-7"));
		assertEquals(charset, tested.charsetForName("utf7"));
		assertEquals(charset, tested.charsetForName("UNICODE-1-1-UTF-7"));
		assertEquals(charset, tested.charsetForName("csUnicode11UTF7"));
		assertEquals(charset, tested.charsetForName("x-RFC2152"));
		assertEquals(charset, tested.charsetForName("x-RFC-2152"));
	}

	/**
	 * 
	 */
	@Test
	public void testUTF7optional() {
		Charset charset = tested.charsetForName("X-UTF-7-OPTIONAL");
		assertNotNull("charset not found", charset);
		assertEquals(UTF7Charset.class, charset.getClass());
		assertEquals(charset, tested.charsetForName("x-utf-7-optional"));
		assertEquals(charset, tested.charsetForName("x-RFC2152-optional"));
		assertEquals(charset, tested.charsetForName("x-RFC-2152-optional"));
	}

	/**
	 * 
	 */
	@Test
	public void testNotHere() {
		assertNull(tested.charsetForName("X-DOES-NOT-EXIST"));
	}

	/**
	 * 
	 */
	@Test
	public void testIterator() {
		Iterator<Charset> iterator = tested.charsets();
		HashSet<Charset> found = new HashSet<Charset>();
		while (iterator.hasNext())
			found.add(iterator.next());
		assertEquals(3, found.size());
		Charset charset1 = tested.charsetForName("x-IMAP4-modified-UTF7");
		Charset charset2 = tested.charsetForName("UTF-7");
		Charset charset3 = tested.charsetForName("X-UTF-7-OPTIONAL");
		assertTrue(found.contains(charset1));
		assertTrue(found.contains(charset2));
		assertTrue(found.contains(charset3));
	}

	/**
	 * 
	 */
	@Test
	public void testTurkish() {
		Locale.setDefault(new Locale("tr", "TR"));
		assertEquals(tested.charsetForName("UTF-7"),
			tested.charsetForName("unicode-1-1-utf-7"));
	}
}
