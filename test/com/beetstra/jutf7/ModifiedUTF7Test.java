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
	private Charset zimbraImapUTF7;
	
	/**
	 * 
	 */
	public ModifiedUTF7Test() {
		super(new ModifiedUTF7Charset("X-MODIFIED-UTF-7", new String[] { }));
		zimbraImapUTF7 = new ImapUTF7("imap-utf-7", new String[] { });
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
		assertEquals(encoded, zimbraImapUTF7decode(encoded));
		String directly = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'(),-./:?\r\n";
		assertEquals(directly, decode(directly));
		assertEquals(directly, zimbraImapUTF7decode(directly));
		String optional = "!\"#$%*;<=>@[]^_'{|}";
		assertEquals(optional, decode(optional));
		assertEquals(optional, zimbraImapUTF7decode(optional));
		String nonUTF7 = "+\\~";
		assertEquals(nonUTF7, decode(nonUTF7));
		assertEquals(nonUTF7, zimbraImapUTF7decode(nonUTF7));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeComplex() throws Exception {
		String encoded = "A&ImIDkQ-.";
		String decoded = "A\u2262\u0391.";
		assertEquals(decoded, decode(encoded));
		assertEquals(decoded, zimbraImapUTF7decode(encoded));
	}

	private String zimbraImapUTF7decode(String s) throws UnsupportedEncodingException {
		return zimbraImapUTF7.decode(CharsetTestUtil.wrap(s)).toString();
	}

	private String zimbraImapUTF7encode(String decoded) throws UnsupportedEncodingException {
		return CharsetTestUtil.asString(zimbraImapUTF7.encode(decoded));
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
		String c = zimbraImapUTF7decode(encoded);
		String msg = CharsetTestUtil.toHex(decoded, b, c);
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
		String b = zimbraImapUTF7decode(encoded);
		String msg = CharsetTestUtil.toHex(decoded, a, b);
		assertEquals(msg, decoded, a);
		assertEquals(msg, decoded, b);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDecodeShiftSequence() throws Exception {
		assertEquals("&", decode("&-"));
		assertEquals("&", zimbraImapUTF7decode("&-"));
		assertEquals("&-", decode("&--"));
		assertEquals("&-", zimbraImapUTF7decode("&--"));
		assertEquals("&&", decode("&-&-"));
		assertEquals("&&", zimbraImapUTF7decode("&-&-"));
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
		assertEquals("\u2262\u0391a\u2262\u0391", zimbraImapUTF7decode("&ImIDkQ-a&ImIDkQ-"));
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
		assertEquals(directly, zimbraImapUTF7encode(directly));
		String optional = "!\"#$%*;<=>@[]^_'{|}";
		assertEquals(optional, encode(optional));
		assertEquals(optional, zimbraImapUTF7encode(optional));
		String nonUTF7 = "+\\~";
		assertEquals(nonUTF7, encode(nonUTF7));
		assertEquals(nonUTF7, zimbraImapUTF7encode(nonUTF7));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeComplex() throws Exception {
		assertEquals("A&ImIDkQ-.", encode("A\u2262\u0391."));
		assertEquals("A&ImIDkQ-.", zimbraImapUTF7encode("A\u2262\u0391."));
		String decoded = new String(new char[] { 0xed, 0xe1 });
		assertEquals("&AO0A4Q-", encode(decoded));
		assertEquals("&AO0A4Q-", zimbraImapUTF7encode(decoded));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeLong() throws Exception {
		String decoded = new String(new char[] { 0x20ac, 0xe1, 0xe9, 0xfa, 0xed,
			0xf3, 0xfd, 0xe4, 0xeb, 0xef, 0xf6, 0xfc, 0xff });
		assertEquals("&IKwA4QDpAPoA7QDzAP0A5ADrAO8A9gD8AP8-", encode(decoded));
		assertEquals("&IKwA4QDpAPoA7QDzAP0A5ADrAO8A9gD8AP8-", zimbraImapUTF7encode(decoded));
	}

	private static final String EOL = System.getProperty("line.separator");

	/**
	 * @throws Exception
	 */
	@Test
	public void testEncodeAlphabet() throws Exception {
		String decoded = new String(new char[] { 0xbe, 0xbe, 0xbe });
		String encoded = encode(decoded);
		String msg = EOL + decoded + EOL + encoded + EOL;
		assertEquals(msg, "&AL4AvgC+-", encoded);
		assertEquals(msg, "&AL4AvgC+-", zimbraImapUTF7encode(decoded));
		decoded = new String(new char[] { 0xbf, 0xbf, 0xbf });
		encoded = encode(decoded);
		msg = EOL + decoded + EOL + encoded + EOL;
		assertEquals(msg, "&AL8AvwC,-", encoded);
		assertEquals(msg, "&AL8AvwC,-", zimbraImapUTF7encode(decoded));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testGetBytes() throws Exception {
		// simulate what is done in String.getBytes
		// (cannot be used directly since Charset is not installed while
		// testing)
		String string = new String(new char[] { 'c', 'a', 'f', 0xe9 });
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
		assertEquals("caf&AOk-", zimbraImapUTF7encode(string));
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
			tested.newDecoder(), zimbraImapUTF7.newDecoder()  
		};
		for (int i=0; i < decoder.length; i++) {
			ByteBuffer in = CharsetTestUtil.wrap(encoded);
			CharBuffer out = CharBuffer.allocate(1024);
			CoderResult result = decoder[i].decode(in, out, true);
			if (result.isUnderflow()) {
				result = decoder[i].flush(out);
			}
			out.flip();
			String a = out.toString();
			String msg = decoder[i].getClass().getName() + ": "
				+ CharsetTestUtil.toHex(decoded, a);
			if (i == 0) {
				assertEquals(msg, decoded, a);
				assertTrue(decoder[i].getClass().getName() 
					+ ": result not maleformed for " + encoded, 
					result.isMalformed());
			} else if (!decoded.equals(a)) {
				System.err.println("decoded value for " + encoded 
					+ " unexpected (expected/got) by " + msg);
			} else if (!result.isMalformed()) {
				System.err.println(decoder[i].getClass().getName() 
					+ ": result not maleformed for " + encoded);
			}
		}
	}
}
