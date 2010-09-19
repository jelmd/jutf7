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
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import org.junit.Test;

import com.zimbra.cs.mime.charset.UTF7;

/**
 * @author 	Firstname Lastname
 * @version	$Revision$
 */
public class UTF7Test
	extends CharsetTest
{
	private Charset utf7;

	/**
	 * 
	 */
	public UTF7Test() {
		super(new UTF7Charset("UTF-7", new String[] { }, false));
		utf7 = new UTF7("utf-7", new String[] { });
	}

	private String utf7decode(String s) throws UnsupportedEncodingException {
		return utf7.decode(CharsetTestUtil.wrap(s)).toString();
	}

	private String utf7encode(String decoded) throws UnsupportedEncodingException {
		return CharsetTestUtil.asString(utf7.encode(decoded));
	}

	/**
	 * 
	 */
	@Test
	public void testContains() {
		assertTrue(tested.contains(Charset.forName("US-ASCII")));
		assertTrue(tested.contains(Charset.forName("ISO-8859-1")));
		assertTrue(tested.contains(Charset.forName("UTF-8")));
		assertTrue(tested.contains(Charset.forName("UTF-16")));
		assertTrue(tested.contains(Charset.forName("UTF-16LE")));
		assertTrue(tested.contains(Charset.forName("UTF-16BE")));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEmpty() throws Exception {
		String string = "";
		assertEquals(string, decode(string));
		assertEquals(string, utf7decode(string));
		assertEquals(string, encode(string));
		assertEquals(string, utf7encode(string));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeSimple() throws Exception {
		assertEquals("abcdefghijklmnopqrstuvwxyz",
			decode("abcdefghijklmnopqrstuvwxyz"));
		assertEquals("abcdefghijklmnopqrstuvwxyz",
			utf7decode("abcdefghijklmnopqrstuvwxyz"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeShiftSequence() throws Exception {
		assertEquals("+", decode("+-"));
		assertEquals("+", utf7decode("+-"));
		assertEquals("+-", decode("+--"));
		assertEquals("+-", utf7decode("+--"));
		assertEquals("++", decode("+-+-"));
		assertEquals("++", utf7decode("+-+-"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeBase64() throws Exception {
		assertEquals("Hi Mom \u263A!", decode("Hi Mom +Jjo-!"));
		assertEquals("Hi Mom \u263A!", utf7decode("Hi Mom +Jjo-!"));
		assertEquals("Hi Mom -\u263A-!", decode("Hi Mom -+Jjo--!"));
		assertEquals("Hi Mom -\u263A-!", utf7decode("Hi Mom -+Jjo--!"));
		assertEquals("\u65E5\u672C\u8A9E", decode("+ZeVnLIqe-"));
		assertEquals("\u65E5\u672C\u8A9E", utf7decode("+ZeVnLIqe-"));
		assertEquals("Item 3 is " + (char) 0xA3 + "1.", decode("Item 3 is +AKM-1."));
		assertEquals("Item 3 is " + (char) 0xA3 + "1.", utf7decode("Item 3 is +AKM-1."));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeLong() throws Exception {
		String c = new String(new char[] { 'x', 'x', 'x', 0xFF, 'x', 0xFF, 'x', 
			0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF });
		assertEquals(c, decode("xxx+AP8-x+AP8-x+AP8A/wD/AP8A/wD/AP8-"));
		assertEquals(c, utf7decode("xxx+AP8-x+AP8-x+AP8A/wD/AP8A/wD/AP8-"));
		assertEquals("\u2262\u0391123\u2262\u0391",
			decode("+ImIDkQ-123+ImIDkQ"));
		assertEquals("\u2262\u0391123\u2262\u0391",
			utf7decode("+ImIDkQ-123+ImIDkQ"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeUnclosed() throws Exception {
		String c = new String(new char[] { 0xFF, 0xFF, 0xFF, '#' });
		assertEquals(c, decode("+AP8A/wD/#"));
		assertEquals(c, utf7decode("+AP8A/wD/#"));
		c = new String(new char[] { 0xFF, 0xFF, '#' });
		assertEquals(c, decode("+AP8A/w#"));
		assertEquals(c, utf7decode("+AP8A/w#"));
		c = new String(new char[] { '#', 0xE1, 0xE1, '#', 0xE1, 0xE1, 0xE1, '#' });
		assertEquals(c, decode("#+AOEA4Q#+AOEA4QDh#"));
		assertEquals(c, utf7decode("#+AOEA4Q#+AOEA4QDh#"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeNoUnshiftAtEnd() throws Exception {
		String c = new String(new char[] { 0x20ac, 0xE1, 0xE9 });
		assertEquals(c, decode("+IKwA4QDp"));
		assertEquals(c, utf7decode("+IKwA4QDp"));
		c = new String(new char[] { '#', 0xE1, 0xE1, 0xE1 });
		assertEquals(c, decode("#+AOEA4QDh"));
		assertEquals(c, utf7decode("#+AOEA4QDh"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeMalformed() throws Exception {
		verifyMalformed("+IKx#");
		verifyMalformed("+IKwA#");
		verifyMalformed("+IKwA4#");
		String c = new String(new char[] { 0x20ac, 0xE1 });
		assertEquals(c, decode("+IKwA4Q"));
		assertEquals(c, utf7decode("+IKwA4Q"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeOptionalCharsUTF7() throws Exception {
		assertEquals("~!@", decode("+AH4AIQBA-"));
		assertEquals("~!@", utf7decode("+AH4AIQBA-"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeOptionalCharsPlain() throws Exception {
		assertEquals("!\"#$%*;<=>@[]^_'{|}", decode("!\"#$%*;<=>@[]^_'{|}"));
		assertEquals("!\"#$%*;<=>@[]^_'{|}", utf7decode("!\"#$%*;<=>@[]^_'{|}"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeLimitedOutput() throws Exception {
		CharsetDecoder decoder = tested.newDecoder();
		ByteBuffer in = CharsetTestUtil.wrap("+IKwA4QDp-");
		CharBuffer out = CharBuffer.allocate(3);
		assertEquals(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
		assertEquals(CoderResult.UNDERFLOW, decoder.flush(out));
		out.flip();
		assertEquals(new String(new char[] { 0x20ac, 0xE1, 0xE9 }), out.toString());
		decoder.reset();
		in = CharsetTestUtil.wrap("A+ImIDkQ.");
		out = CharBuffer.allocate(4);
		assertEquals(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
		out.flip();
		assertEquals("A\u2262\u0391.", out.toString());
	}

	private void verifyMalformed(final String encoded)
		throws UnsupportedEncodingException
	{
		CharsetDecoder[] decoder = new CharsetDecoder[] {
			tested.newDecoder(), utf7.newDecoder()
		};
		for (int i=0; i < decoder.length; i++) {
			ByteBuffer in = CharsetTestUtil.wrap(encoded);
			CharBuffer out = CharBuffer.allocate(1024);
			CoderResult result = decoder[i].decode(in, out, false); // "€#"
			if (i == 0) {
				assertTrue(result.isMalformed());
			} else if (!result.isMalformed()) {
				System.err.println(decoder[i].getClass().getName() 
					+ ": result not maleformed for " + encoded);
			}
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeSimple() throws Exception {
		assertEquals("abcdefghijklmnopqrstuvwxyz",
			CharsetTestUtil.asString(tested.encode("abcdefghijklmnopqrstuvwxyz")));
		assertEquals("abcdefghijklmnopqrstuvwxyz",
			utf7encode("abcdefghijklmnopqrstuvwxyz"));
		assertEquals(" \r\t\n", CharsetTestUtil.asString(tested.encode(" \r\t\n")));
		assertEquals(" \r\t\n", utf7encode(" \r\t\n"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeBase64() throws Exception {
		String encoded = "A+ImIDkQ."; // implict unshift
		String encoded2 = "A+ImIDkQ-."; // explicit unshift
		String a = encode("A\u2262\u0391.");
		assertEquals(encoded, a);
		a = utf7encode("A\u2262\u0391.");
		assertTrue(encoded.equals(a) || encoded2.equals(a));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeBase64NoImplicitUnshift() throws Exception {
		assertEquals("A+ImIDkQ-A", encode("A\u2262\u0391A"));
		assertEquals("A+ImIDkQ-A", utf7encode("A\u2262\u0391A"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeLong() throws Exception {
		String c = new String(new char[] { 0x20ac, 0xE1, 0xE9, 0xFA, 0xED, 0xF3,
			0xFD, 0xE4, 0xEB, 0xEF, 0xF6, 0xFC, 0xFF });
		assertEquals("+IKwA4QDpAPoA7QDzAP0A5ADrAO8A9gD8AP8-", encode(c));
		assertEquals("+IKwA4QDpAPoA7QDzAP0A5ADrAO8A9gD8AP8-", utf7encode(c));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeShiftUnshift() throws Exception {
		assertEquals("+--", encode("+-"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeAddUnshiftOnUnshift() throws Exception {
		String encoded = new String(new char[] { 0xED, '+', '-'});
		assertEquals("+AO0AKw--", encode(encoded));
		assertEquals("+AO0AKw--", utf7encode(encoded));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeNormalAndDifferent() throws Exception {
		String decoded = new String(new char[] {
			'x', 'x', 'x', 0xFF, 'x', 0xFF, 'x', 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
			0xFF, 0xFF
		});
		assertEquals("xxx+AP8-x+AP8-x+AP8A/wD/AP8A/wD/AP8-", encode(decoded));
		assertEquals("xxx+AP8-x+AP8-x+AP8A/wD/AP8A/wD/AP8-", utf7encode(decoded));
		String decoded2 = new String(new char[] {
			0xFF, 0xFF, 0xFF, 0xFF, 'x', 0xFF, 0xFF, 0xFF, 0xFF
		});
		assertEquals("+AP8A/wD/AP8-x+AP8A/wD/AP8-", encode(decoded2));
		assertEquals("+AP8A/wD/AP8-x+AP8A/wD/AP8-", utf7encode(decoded2));
		String decoded3 = new String(new char[] {
			'a', 'b', 'c', 0xE1, 0xE9, 0xED, 'd', 'e', 'f', 0xF3, 0xFA, 0xE4,
			'g', 'h'
		});
		assertEquals("abc+AOEA6QDt-def+APMA+gDk-gh", encode(decoded3));
		assertEquals("abc+AOEA6QDt-def+APMA+gDk-gh", utf7encode(decoded3));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeOptionalCharsUTF7() throws Exception {
		assertEquals("+ACEAIgAjACQAJQAqADsAPAA9AD4AQABbAF0AXgBfAGAAewB8AH0-",
			encode("!\"#$%*;<=>@[]^_`{|}"));
		assertEquals("+ACEAIgAjACQAJQAqADsAPAA9AD4AQABbAF0AXgBfAGAAewB8AH0-",
			utf7encode("!\"#$%*;<=>@[]^_`{|}"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeAlphabet() throws Exception {
		String decoded = new String(new char[] { 0xBE, 0xBE, 0xBE });
		assertEquals("+AL4AvgC+-", encode(decoded));
		assertEquals("+AL4AvgC+-", utf7encode(decoded));
		String decoded2 = new String(new char[] { 0xBF, 0xBF, 0xBF });
		assertEquals("+AL8AvwC/-", encode(decoded2));
		assertEquals("+AL8AvwC/-", utf7encode(decoded2));
	}
}
