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
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import org.junit.Test;

import com.zimbra.cs.mime.charset.ImapUTF7;

/**
 * @author 	Firstname Lastname
 * @version	$Revision$
 */
public class ModifiedUTF7Test
	extends CharsetTest
{
	private Charset imapUTF7;
	
	/**
	 * 
	 */
	public ModifiedUTF7Test() {
		super(new ModifiedUTF7Charset("X-MODIFIED-UTF-7", new String[] { }));
		imapUTF7 = new ImapUTF7("imap-utf-7", new String[] { });
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
		assertEquals(string, encode(string));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeSimple() throws Exception {
		String encoded = "abcdefghijklmnopqrstuvwxyz";
		assertEquals(encoded, decode(encoded));
		assertEquals(encoded, imap7decode(encoded));
		String directly = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'(),-./:?\r\n";
		assertEquals(directly, decode(directly));
		assertEquals(directly, imap7decode(directly));
		String optional = "!\"#$%*;<=>@[]^_'{|}";
		assertEquals(optional, decode(optional));
		assertEquals(optional, imap7decode(optional));
		String nonUTF7 = "+\\~";
		assertEquals(nonUTF7, decode(nonUTF7));
		assertEquals(nonUTF7, imap7decode(nonUTF7));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeComplex() throws Exception {
		String encoded = "A&ImIDkQ-.";
		String decoded = "A\u2262\u0391.";
		assertEquals(decoded, decode(encoded));
		assertEquals(decoded, imap7decode(encoded));
	}

	private static String EOL = System.getProperty("line.separator");
	private static String toBytes(String s, boolean linefeed) {
		int count = 0;
		char[] x = s.toCharArray();
		StringBuilder buf = new StringBuilder();
		for (int i=0; i < x.length; i++) {
			count++;
			if (linefeed && count == 8) {
				buf.append(EOL);
				count = 0;
			}
			if (x[i] < 0x1000) {
				buf.append('0');
			}
			if (x[i] < 0x100) {
				buf.append('0');
			}
			if (x[i] < 0x10) {
				buf.append('0');
			}
			buf.append(Integer.toHexString(x[i])).append(' ');
		}
		return buf.toString();
	}

	private static String toHex(String... a) {
		StringBuilder buf = new StringBuilder(EOL);
		for (String s : a) {
			buf.append(toBytes(s, false)).append("  ").append(EOL); 
		}
		return buf.toString();
	}

	private String imap7decode(String s) throws UnsupportedEncodingException {
		return imapUTF7.decode(CharsetTestUtil.wrap(s)).toString();
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeLong() throws Exception {
		String decoded = new String(new char[] { 
			0x20ac, 0xe1, 0xe9, 0xfa, 0xed, 0xf3, 0xfd, 0xe4, 0xeb, 0xef,
			0xf6, 0xfc, 0xff });
		String encoded = "&IKwA4QDpAPoA7QDzAP0A5ADrAO8A9gD8AP8-";
		String b = decode(encoded);
		String c = imap7decode(encoded);
		String msg = toHex(decoded, b, c);
		assertEquals(msg, decoded, b);
		assertEquals(msg, decoded, c);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeLimitedOutput() throws Exception {
		String encoded = "A&ImIDkQ-.";
		String decoded = "A\u2262\u0391.";
		CharsetDecoder decoder = tested.newDecoder();
		ByteBuffer in = CharsetTestUtil.wrap(encoded);
		CharBuffer out = CharBuffer.allocate(4);
		assertEquals(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
		out.flip();
		assertEquals(decoded, out.toString());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodePerfectSized() throws Exception {
		String decoded = new String(new char[] { 0x20ac , 0xe1, 0xe9 });
		String encoded = "&IKwA4QDp-";
		String a = decode(encoded);
		String b = imap7decode(encoded);
		String msg = toHex(decoded, a, b);
		assertEquals(msg, decoded, a);
		assertEquals(msg, decoded, b);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeShiftSequence() throws Exception {
		assertEquals("&", decode("&-"));
		assertEquals("&", imap7decode("&-"));
		assertEquals("&-", decode("&--"));
		assertEquals("&-", imap7decode("&--"));
		assertEquals("&&", decode("&-&-"));
		assertEquals("&&", imap7decode("&-&-"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeNoClosing() throws Exception {
		ByteBuffer in = CharsetTestUtil.wrap("&");
		CharBuffer out = CharBuffer.allocate(1024);
		final CharsetDecoder decoder = tested.newDecoder();
		CoderResult result = decoder.decode(in, out, true);
		assertEquals(CoderResult.UNDERFLOW, result);
		result = decoder.flush(out);
		assertEquals(CoderResult.malformedForLength(1), result);
		assertEquals(1, in.position());
		assertEquals(0, out.position());
		in = CharsetTestUtil.wrap("&AO");
		out = CharBuffer.allocate(1024);
		decoder.reset();
		result = decoder.decode(in, out, true);
		assertEquals(CoderResult.UNDERFLOW, result);
		result = decoder.flush(out);
		assertEquals(CoderResult.malformedForLength(1), result);
		assertEquals(3, in.position());
		assertEquals(0, out.position());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeInvalidLength() throws Exception {
		assertMalformed("&a-", "");
		assertMalformed("ab&IKwD-", new String(new char[] { 0x61, 0x62, 0x20ac}));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeUnshiftShiftSequence() throws Exception {
		assertMalformed("&ImIDkQ-&ImIDkQ-", "\u2262\u0391");
		assertEquals("\u2262\u0391a\u2262\u0391", decode("&ImIDkQ-a&ImIDkQ-"));
		assertEquals("\u2262\u0391a\u2262\u0391", imap7decode("&ImIDkQ-a&ImIDkQ-"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeIllegalBase64char() throws Exception {
		assertMalformed("&[-", "");
		assertMalformed("&&ImIDkQ-", "");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testLongBadDecode() throws Exception {
		String decoded = new String(new char[] { 0x20ac, 0xe1, 0xe9 });
		assertMalformed("&IKwA4QDpA-", decoded);
		assertMalformed("&IKwA4QDpA", decoded);
		assertMalformed("&IKwA4QDp", decoded);
		assertMalformed("&IKwA4QDpAP", decoded);
		assertMalformed("&IKwA4QDpAPoA7QDzAP0A5ADrAO8A9gD8AP-", 
			new String(new char[] { 0x20ac, 0xe1, 0xe9, 0xfa, 0xed, 0xf3, 0xfd, 0xe4, 0xeb, 0xef, 0xf6, 0xfc}));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeSimple() throws Exception {
		String directly = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'(),-./:?";
		assertEquals(directly, encode(directly));
		assertEquals(directly, encode(directly));
		String optional = "!\"#$%*;<=>@[]^_'{|}";
		assertEquals(optional, encode(optional));
		String nonUTF7 = "+\\~";
		assertEquals(nonUTF7, encode(nonUTF7));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeComplex() throws Exception {
		assertEquals("A&ImIDkQ-.", encode("A\u2262\u0391."));
		assertEquals("&AO0A4Q-", encode("íá"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeLong() throws Exception {
		assertEquals("&IKwA4QDpAPoA7QDzAP0A5ADrAO8A9gD8AP8-",
			encode("€áéúíóýäëïöüÿ"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeAlphabet() throws Exception {
		assertEquals("&AL4AvgC+-", encode("¾¾¾"));
		assertEquals("&AL8AvwC,-", encode("¿¿¿"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testGetBytes() throws Exception {
		// simulate what is done in String.getBytes
		// (cannot be used directly since Charset is not installed while
		// testing)
		String string = "café";
		CharsetEncoder encoder = tested.newEncoder();
		ByteBuffer bb = ByteBuffer
			.allocate((int) (encoder.maxBytesPerChar() * string.length()));
		CharBuffer cb = CharBuffer.wrap(string);
		encoder.reset();
		CoderResult cr = encoder.encode(cb, bb, true);
		if ( !cr.isUnderflow())
			cr.throwException();
		cr = encoder.flush(bb);
		if ( !cr.isUnderflow())
			cr.throwException();
		bb.flip();
		assertEquals("caf&AOk-", CharsetTestUtil.asString(bb));
	}

	/**
	 * @param encoded
	 * @param decoded
	 * @throws UnsupportedEncodingException
	 */
	protected void assertMalformed(String encoded, String decoded)
		throws UnsupportedEncodingException
	{
		CharsetDecoder[] decoder = new CharsetDecoder[] {
			imapUTF7.newDecoder(), tested.newDecoder() 
		};
		for (int i=decoder.length-1; i >= 0; i--) {
			ByteBuffer in = CharsetTestUtil.wrap(encoded);
			CharBuffer out = CharBuffer.allocate(1024);
			CoderResult result = decoder[i].decode(in, out, true);
			if (result.isUnderflow()) {
				result = decoder[i].flush(out);
			}
			out.flip();
			String a = out.toString();
			String msg = decoder[i].getClass().getName() + ": "
				+ toHex(decoded, a);
			assertEquals(msg, decoded, a);
			assertTrue(decoder[i].getClass().getName() 
				+ ": result not maleformed for " + encoded, 
				result.isMalformed());
		}
	}
}
