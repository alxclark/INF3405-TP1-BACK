package com.log3405.server;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BytesUtils {
	public static byte[] readBytes(InputStream in, int bufferSize) throws IOException {
		byte[] data = new byte[bufferSize];

		in.read(data, 0, data.length);

		return data;
	}

	public static void writeBytes(OutputStream out, byte[] data) throws IOException {
		out.write(data, 0, data.length);
	}

	public static byte[] intToBytes(int i) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(i);
		return bb.array();
	}

	public static int bytesToInt(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getInt();
	}

	public static byte[] extractSubByteArray(byte[] bytes, int begin, int end) {
		return Arrays.copyOfRange(bytes, begin, end);
	}

	public static String bytesToString(byte[] bytes) {
		return new String(bytes).trim();
	}

	public static byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
}
