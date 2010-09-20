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
	private long direct0;
	private long direct1;
	private boolean debug = false;

	UTF7Charset(String name, String[] aliases, boolean includeOptional) {
		super(name, aliases, BASE64_ALPHABET, false);
		char[] all = (includeOptional ? SET_D + SET_O + RULE_3 : SET_D + RULE_3)
			.toCharArray();
		direct0 = 0;
		direct1 = 0;
		for (int i=all.length-1; i >= 0; i--) {
			if (all[i] < 64) {
				direct0 |= 1L << all[i];
			} else {
				direct1 |= 1L << (all[i] - 64);
			}
		}
		if (debug) {
			if (includeOptional) {
				System.out.println("Optional");
			} else {
				System.out.println("None-Optional");
			}
			System.out.print("0x" + Integer.toHexString(all[0]));
			for (int i=1; i < all.length; i++) {
				if (all[i-1] != all[i]-1) {
					System.out.print("-0x" + Integer.toHexString(all[i-1]));
					System.out.print("  0x" + Integer.toHexString(all[i]));
				}
			}
			System.out.println("-0x" + Integer.toHexString(all[all.length-1]));
		}
	}

	@Override
	boolean canEncodeDirectly(char ch) {
		return ch < 64
			? (direct0 & (1L << ch)) != 0
			: (ch < 128 ? (direct1 & (1L << (ch - 64))) != 0 : false);		
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
