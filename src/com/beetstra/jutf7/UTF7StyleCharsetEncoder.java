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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * The CharsetEncoder used to encode both variants of the UTF-7 charset and the
 * modified-UTF-7 charset.
 * 
 * @author Jaap Beetstra
 */
class UTF7StyleCharsetEncoder
	extends CharsetEncoder
{
	private static final float AVG_BYTES_PER_CHAR = 1.5f;
	private static final float MAX_BYTES_PER_CHAR = 5.0f;
	private final UTF7StyleCharset cs;
	private final Base64Util base64;
	private final byte shift;
	private final byte unshift;
	private final boolean strict;
	private boolean base64mode;
	private int bitsToOutput;
	private int sextet;

	UTF7StyleCharsetEncoder(UTF7StyleCharset cs, Base64Util base64,
		boolean strict)
	{
		super(cs, AVG_BYTES_PER_CHAR, MAX_BYTES_PER_CHAR);
		this.cs = cs;
		this.base64 = base64;
		this.strict = strict;
		this.shift = cs.shift();
		this.unshift = cs.unshift();
	}

	@Override
	protected void implReset() {
		base64mode = false;
		sextet = 0;
		bitsToOutput = 0;
	}

	@Override
	protected CoderResult implFlush(ByteBuffer out) {
		if (base64mode) {
			if (out.remaining() < 2) {
				return CoderResult.OVERFLOW;
			}
			if (bitsToOutput != 0) {
				out.put(base64.getChar(sextet));
			}
			out.put(unshift);
		}
		return CoderResult.UNDERFLOW;
	}

	@Override
	protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
		while (in.hasRemaining()) {
			if (out.remaining() < 4) {
				return CoderResult.OVERFLOW;
			}
			char ch = in.get();
			if (cs.canEncodeDirectly(ch)) {
				if (base64mode) {
					// write remaining base64 char
					if (bitsToOutput != 0) {
						out.put(base64.getChar(sextet));
					}
					// unshift, if required
					if (base64.contains(ch) || ch == unshift || strict) {
						out.put(unshift);
					}
					base64mode = false;
					sextet = 0;
					bitsToOutput = 0;
				}
				out.put((byte) ch);
			} else if (ch == shift && !base64mode) {
				out.put(shift);
				out.put(unshift);
			} else {
				encodeBase64(ch, out);
			}
		}
		return CoderResult.UNDERFLOW;
	}

	/**
	 * Writes the bytes necessary to encode a character in <i>base 64 mode</i>.
	 * All bytes which are fully determined will be written. The fields
	 * <code>bitsToOutput</code> and <code>sextet</code> are used to remember
	 * the bytes not yet fully determined.
	 * 
	 * @param out
	 * @param ch
	 */
	private void encodeBase64(char ch, ByteBuffer out) {
		if ( !base64mode) {
			out.put(shift);
		}
		base64mode = true;
		bitsToOutput += 16;
		while (bitsToOutput >= 6) {
			bitsToOutput -= 6;
			sextet += (ch >> bitsToOutput);
			sextet &= 0x3F;
			out.put(base64.getChar(sextet));
			sextet = 0;
		}
		sextet = (ch << (6 - bitsToOutput)) & 0x3F;
	}
}