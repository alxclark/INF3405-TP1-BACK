package com.log3405.server;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BytesUtils {
	/**
	 * Read bytes from the input stream. Also blocks the thread if there is nothing to read
	 * @param in InputStream of the socket
	 * @param bufferSize Size of the buffer allocated to read from the stream
	 * @param fullRead Decides if it need to be read until the byte array is full
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBytes(InputStream in, int bufferSize, boolean fullRead) throws IOException {
		byte[] data = new byte[bufferSize];

		int read = in.read(data, 0, data.length);
		while (fullRead && read < bufferSize) {
			read += in.read(data, read, data.length);//sometimes its partial try again
		}

		return data;
	}

	/**
	 * Write a byte array into the stream
	 * @param out OutputStream of the socket
	 * @param data Data to write
	 * @throws IOException
	 */
	public static void writeBytes(OutputStream out, byte[] data) throws IOException {
		out.write(data, 0, data.length);
	}

	/**
	 * Converts a int into a byte array
	 * @param i Integer to convert
	 * @return converted value
	 */
	public static byte[] intToBytes(int i) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(i);
		return bb.array();
	}

	/**
	 * Converts a byte array into a integer
	 * @param bytes Byte array to convert
	 * @return the integer
	 */
	public static int bytesToInt(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getInt();
	}

	/**
	 * Extract a sub array of bytes into another array
	 * @param bytes Original byte array
	 * @param begin Beginning index
	 * @param end Ending index (excluded)
	 * @return Subarray of bytes
	 */
	public static byte[] extractSubByteArray(byte[] bytes, int begin, int end) {
		return Arrays.copyOfRange(bytes, begin, end);
	}

	/**
	 * Converts a byte array into a String
	 * @param bytes Byte array to convert
	 * @return the converted string
	 */
	public static String bytesToString(byte[] bytes) {
		return new String(bytes).trim();
	}

	/**
	 * Concat tow byte arrays together
	 * @param a First array
	 * @param b Second array
	 * @return An array containing the two arrays
	 */
	public static byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
}
