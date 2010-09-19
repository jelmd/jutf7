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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

abstract class CharsetTestUtil {
	static void outToSB(ByteBuffer out, StringBuffer sb)
		throws UnsupportedEncodingException
	{
		out.flip();
		sb.append(CharsetTestUtil.asString(out));
		out.clear();
	}

	/**
	 * Wrap the given ByteBuffer into a String using US-ASCII
	 * @param buffer
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	static String asString(ByteBuffer buffer)
		throws UnsupportedEncodingException
	{
		byte[] bytes = new byte[buffer.limit()];
		buffer.get(bytes);
		return new String(bytes, "US-ASCII");
	}

	/**
	 * Wrap the given string into a byte buffer.
	 * 
	 * @param string
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	static ByteBuffer wrap(String string) throws UnsupportedEncodingException {
		byte[] bytes = string.getBytes("US-ASCII");
		return ByteBuffer.wrap(bytes);
	}
	
	public static final String EOL = System.getProperty("line.separator");
	public static final String toBytes(String s, boolean linefeed) {
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

	public static final String toHex(String... a) {
		StringBuilder buf = new StringBuilder(EOL);
		for (String s : a) {
			buf.append(toBytes(s, false)).append("  ").append(EOL); 
		}
		return buf.toString();
	}

}
