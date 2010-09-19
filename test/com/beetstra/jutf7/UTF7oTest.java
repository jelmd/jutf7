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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import net.freeutils.charset.UTF7OptionalCharset;

import org.junit.Test;

import com.zimbra.cs.mime.charset.UTF7;

/**
 * @author 	Firstname Lastname
 * @version	$Revision$
 */
public class UTF7oTest
	extends CharsetTest
{
	private Charset zimbraUTF7;
	private Charset freeutilsUTF7o;

	private String zimbraUTF7decode(String s) throws UnsupportedEncodingException {
		return zimbraUTF7.decode(CharsetTestUtil.wrap(s)).toString();
	}

	private String zimbraUTF7encode(String decoded) throws UnsupportedEncodingException {
		return CharsetTestUtil.asString(zimbraUTF7.encode(decoded));
	}

	private String freeutilsUTF7odecode(String s) throws UnsupportedEncodingException {
		return freeutilsUTF7o.decode(CharsetTestUtil.wrap(s)).toString();
	}

	private String freeutilsUTF7oencode(String decoded) throws UnsupportedEncodingException {
		return CharsetTestUtil.asString(freeutilsUTF7o.encode(decoded));
	}

	/**
	 * 
	 */
	public UTF7oTest() {
		super(new UTF7Charset("X-UTF-7-Optional", new String[] { }, true));
		zimbraUTF7 = new UTF7("utf-7", new String[] { });
		freeutilsUTF7o = new UTF7OptionalCharset();
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeOptionalCharsUTF7() throws Exception {
		assertEquals("~!@", decode("+AH4AIQBA-"));
		assertEquals("~!@", zimbraUTF7decode("+AH4AIQBA-"));
		assertEquals("~!@", freeutilsUTF7odecode("+AH4AIQBA-"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeOptionalCharsPlain() throws Exception {
		assertEquals("!\"#$%*;<=>@[]^_'{|}", decode("!\"#$%*;<=>@[]^_'{|}"));
		assertEquals("!\"#$%*;<=>@[]^_'{|}", zimbraUTF7decode("!\"#$%*;<=>@[]^_'{|}"));
		assertEquals("!\"#$%*;<=>@[]^_'{|}", freeutilsUTF7odecode("!\"#$%*;<=>@[]^_'{|}"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeOptionalCharsUTF7() throws Exception {
		assertEquals("!\"#$%*;<=>@[]^_`{|}", encode("!\"#$%*;<=>@[]^_`{|}"));
		// zimbra UTF does not use optional chars
		assertFalse("!\"#$%*;<=>@[]^_`{|}".equals(zimbraUTF7encode("!\"#$%*;<=>@[]^_`{|}")));
		assertEquals("!\"#$%*;<=>@[]^_`{|}", freeutilsUTF7oencode("!\"#$%*;<=>@[]^_`{|}"));
	}
}
