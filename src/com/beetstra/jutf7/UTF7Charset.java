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

import java.util.Arrays;

/**
 * The character set specified in RFC 2152. Two variants are supported using the
 * encodeOptional constructor flag.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc2152">RFC 2152</a>
 * @author Jaap Beetstra
 */
class UTF7Charset
	extends UTF7StyleCharset
{
	// SET_B , i.e. base64 without '=' (pad)
	private static final String BASE64_ALPHABET = 
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String SET_D = 
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'(),-./:?";
	private static final String SET_O = 
		"!\"#$%&*;<=>@[]^_`{|}";
	private static final String RULE_3 = " \t\r\n";
	final String directlyEncoded;
	private boolean debug = false;

	UTF7Charset(String name, String[] aliases, boolean includeOptional) {
		super(name, aliases, BASE64_ALPHABET, false);
		this.directlyEncoded = includeOptional 
			? SET_D + SET_O + RULE_3
			: SET_D + RULE_3;
		if (debug) {
			if (includeOptional) {
				System.out.println("Optional");
			} else {
				System.out.println("None-Optional");
			}
			byte[] bytes = directlyEncoded.getBytes();
			Arrays.sort(bytes);
			System.out.print("0x" + Integer.toHexString(bytes[0]));
			for (int i=1; i < bytes.length; i++) {
				if (bytes[i-1] != bytes[i]-1) {
					System.out.print("-0x" + Integer.toHexString(bytes[i-1]));
					System.out.print("  0x" + Integer.toHexString(bytes[i]));
				}
			}
			System.out.println("-0x" + Integer.toHexString(bytes[bytes.length-1]));
		}
	}

	@Override
	boolean canEncodeDirectly(char ch) {
		return directlyEncoded.indexOf(ch) >= 0;
	}

	@Override
	byte shift() {
		return '+';
	}

	@Override
	byte unshift() {
		return '-';
	}
}
