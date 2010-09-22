/**
 * $Id$ 
 * 
 * Copyright (c) 2005-2010 Jens Elkner.
 * All Rights Reserved.
 *
 * This software is the proprietary information of Jens Elkner.
 * Use is subject to license terms.
 */
package de.ovgu.cs.utf7;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import net.freeutils.charset.UTF7Charset;
import net.freeutils.charset.UTF7OptionalCharset;

import com.zimbra.cs.mime.charset.ImapUTF7;
import com.zimbra.cs.mime.charset.UTF7;

/**
 * Microbenchmark wrt. UTF-7 charset implementations.
 * 
 * @author 	Jens Elkner
 * @version	$Revision$
 */
public class Benchmark {
	private static final String EOL = System.getProperty("line.separator");
	
	private static void usage() {
		System.out.println("Usage: java -jar utf7all.jar infile [statfile]" + EOL + EOL +
"  infile ..   UTF-8 file to read. Its content will be used for UTF-7 " + EOL +
"              encoding/decoding. If its size is less than the number of" + EOL +
"              characters to encode in one pass, the file gets read from the" + EOL +
"              beginning as many times as necessary." + EOL +
"  statfile .. if given, write stats to the given file. Gets overwritten" + EOL +
"              unconditionally, if it already exists."
			);
	}
	
	/**
	 * Since we do not want to test file I/O performance, we cache the content.
	 * @param utf8	file to read.
	 * @return a possible empty String.
	 */
	private static char[] readFile(File utf8) {
		BufferedReader in = null;
		StringBuilder buf = new StringBuilder((int)utf8.length());
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(utf8), "UTF-8"));
			char[] rb = new char[1<<10];
			int read;
			while ((read = in.read(rb, 0, rb.length)) != -1) {
				buf.append(rb, 0, read);
			}
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		} finally {
			if (in!= null) {
				try { in.close(); } catch (Exception e) { /** ignore */ }
			}
		}
		char[] dst = new char[buf.length()];
		buf.getChars(0, buf.length(), dst, 0);
		return dst;
	}
	
	/**
	 * Allocate src buffers, so that we do not need to measure allocation times. 
	 * @param txt	text to use to create buffers
	 * @return a bunch of char buffers
	 */
	private static CharBuffer[] createCharBuffers(char[] txt) {
		int max = 1 << 20;
		int num = 20 - 6 + 1; // 1 M .. 64 chars
		CharBuffer[] buffers = new CharBuffer[num];
		buffers[num-1] = CharBuffer.allocate(max);
		int todo = max;
		while (todo > 0) {
			if (todo < txt.length) {
				buffers[num-1].put(txt, 0, todo);
				todo = 0;
			} else {
				buffers[num-1].put(txt, 0, txt.length);
				todo -= txt.length;
			}				
		}
		todo = max >> 1;
		for (int i=num-2; i >= 0; i--) {
			buffers[i] = buffers[num-1].duplicate();
			buffers[i].limit(todo);
			todo >>= 1;
		}
		return buffers;
	}

	/**
	 * Encode the given <var>src</var> buffer for each of the given charsets
	 * and return the largest result buffer.
	 * 
	 * @param src	text to encode
	 * @param charsets	harsets in question
	 * @return the buffer, which is able to take the encoding results of all
	 * 	given charsets
	 */
	private static ByteBuffer createResultBuffer(CharBuffer src, 
		Charset[] charsets) 
	{
		int max = 0;
		for (Charset cs : charsets) {
			try {
				src.rewind();
				ByteBuffer buf = cs.newEncoder().encode(src);
				if (buf.limit() > max) {
					max = buf.limit();
				}
			} catch (CharacterCodingException e) {
				System.err.println(e.getLocalizedMessage());
			}
		}
		System.out.println("Max. limit for " + src.limit() + " chars: " + max
			+ " bytes");
		return ByteBuffer.allocateDirect(max);
	}
	
	@SuppressWarnings("boxing")
	private static void printSummary(String[] units, String[][] rowID, 
		double[][] stats, double[][] stats2, File file) 
	{
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw, false);
		out.printf("%58s", "Class - UTF7 type/ Chunk size");
		for (int i=0; i < units.length; i++) {
			out.printf(" %6s", units[i]);
		}
		out.printf("%n");
		for (int x = 0; x < 2; x++) {
			double[][] cstats = x == 0 ? stats : stats2;
			for (int i=0; i < rowID[x].length; i++) {
				out.printf("%c %58s", x==0 ? 'e' : 'c', rowID[x][i]);
				for (int k=0; k < units.length; k++) {
					out.printf(" %6.2f", cstats[i][k]);
				}
				out.printf("%n");
			}
		}
		out.flush();
		System.out.println(sw.toString());
		FileOutputStream fos = null;
		if (file != null) {
			try {
				fos = new FileOutputStream(file);
				fos.write(sw.toString().getBytes("US-ASCII"));
			} catch (Exception e) {
				System.err.println(e.getLocalizedMessage());
			} finally {
				if (fos!= null) {
					try { fos.close(); } catch (Exception e) { /** ignore */ }
				}
			}
		}
		out.close();
	}
	
	private static Charset[] getCharsets() {
		ArrayList<Charset> csList = new ArrayList<Charset>();
		CharsetProvider cp = new com.beetstra.jutf7.CharsetProvider();
		Iterator<Charset> csi = cp.charsets();
		while (csi.hasNext()) {
			csList.add(csi.next());
		}
		csList.add(new ImapUTF7("imap-utf-7", new String[] { }));
		csList.add(new UTF7("utf-7", new String[] { }));
		csList.add(new UTF7Charset());
		csList.add(new UTF7OptionalCharset());
		Collections.reverse(csList);
		return csList.toArray(new Charset[csList.size()]);
	}
	
	/**
	 * Reads an UTF-8 file and uses its content to populate the content to use
	 * for UTF-7 benchmarking wrt. encoding and decoding.
	 * 
	 * @param args	0 .. UTF-8 file to read, 1 .. optional: stat file to write
	 * @throws CharacterCodingException 
	 */
	@SuppressWarnings("boxing")
	public static void main(String[] args) throws CharacterCodingException {
		if (args.length < 1) {
			usage();
			return;
		}
		char[] content = readFile(new File(args[0]));
		if (content.length == 0) {
			System.err.println("have nothing to encode - exiting");
			return;
		}
		Charset[] charsets = getCharsets();
		
		CharBuffer[] txtBuffers = createCharBuffers(content);
		ByteBuffer resultBuffer = 
			createResultBuffer(txtBuffers[txtBuffers.length-1], charsets);
		String[] units = new String[txtBuffers.length];
		for (int i=0; i < txtBuffers.length; i++) {
			int idx = 0;
			int len = txtBuffers[i].limit();
			while (len >= 1024) {
				len >>= 10;
				idx++;
			}
			units[i] = idx == 0
				? txtBuffers[i].limit() + "B"
				: idx == 1 
					? (txtBuffers[i].limit() >> 10) + "KiB"
					: (txtBuffers[i].limit() >> 20) + "MiB";
		}
		
		double[][] stats = new double[charsets.length][txtBuffers.length];
		String[][] rowID = new String[2][charsets.length];
		int cycles = 1 << 10;
		double factor = 1000.0 / (1 << 10); // 1000 ms * cycles / Mchars
		for (int id=0; id < rowID[0].length; id++) {
			for (int i=0; i < txtBuffers.length; i++) {
				// warmup
				testEncoder(charsets[id], txtBuffers[i], resultBuffer, cycles);
			}
			for (int i=0; i < txtBuffers.length; i++) {
				// test
				double t = testEncoder(charsets[id], txtBuffers[i], resultBuffer, cycles);
				double rate = txtBuffers[i].limit() * factor / t;
				if (Double.isInfinite(rate)) {
					rate = 0;
				}
				stats[id][i] = rate;
				rowID[0][id] = charsets[id].getClass().getName() + " - " 
					+ charsets[id].displayName();
				System.out.printf("e %58s: %6s %.2f MiB/s%n", 
					rowID[0][id], units[i], rate);
			}
		}

		double[][] stats2 = new double[charsets.length][txtBuffers.length];
		ByteBuffer[] bb = new ByteBuffer[txtBuffers.length];
		CharBuffer result2buffer = CharBuffer.allocate(txtBuffers[txtBuffers.length-1].limit() + 3);
		for (int id=0; id < rowID[1].length; id++) {
			// create the buffers
			CharsetEncoder encoder = charsets[id].newEncoder();
			for (int k=0; k < txtBuffers.length; k++) {
				txtBuffers[k].rewind();
				bb[k] = encoder.encode(txtBuffers[k]);
				encoder.reset();
			}
			for (int i=0; i < txtBuffers.length; i++) {
				// warmup
				testDecoder(charsets[id], bb[i], result2buffer, cycles);
			}
			for (int i=0; i < txtBuffers.length; i++) {
				// test
				double t = testDecoder(charsets[id], bb[i], 
					result2buffer, cycles);
				double rate = bb[i].limit() * factor / t;
				if (Double.isInfinite(rate)) {
					rate = 0;
				}
				stats2[id][i] = rate;
				rowID[1][id] = charsets[id].getClass().getName() + " - " 
					+ charsets[id].displayName();
				System.out.printf("d %58s: %6s %.2f MiB/s%n", 
					rowID[1][id], units[i], rate);
			}
		}
		printSummary(units, rowID, stats, stats2, 
			args.length > 1 ? new File(args[1]) : null);
	}
	
	private static long testEncoder(Charset cs, CharBuffer in, ByteBuffer out,
		int cycles)
	{
		CharsetEncoder encoder = cs.newEncoder();
		long start = System.currentTimeMillis();
		for (int i=0; i < cycles; i++) {
			out.clear();
			in.rewind();
			encoder.reset();
			@SuppressWarnings("unused")
			CoderResult result = encoder.encode(in, out, true);
		}
		long stop = System.currentTimeMillis();
		return stop - start;
	}

	private static long testDecoder(Charset cs, ByteBuffer buf, CharBuffer out,
		int cycles) 
	{
		CharsetDecoder decoder = cs.newDecoder();
		long start = System.currentTimeMillis();
		for (int i=0; i < cycles; i++) {
			out.clear();
			buf.rewind();
			decoder.reset();
			@SuppressWarnings("unused")
			CoderResult result = decoder.decode(buf, out, true);
		}
		long stop = System.currentTimeMillis();
		return stop - start;
	}
}
