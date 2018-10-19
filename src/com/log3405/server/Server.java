package com.log3405.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Server extends Thread {
	private Socket socket;
	DataInputStream in;
	DataOutputStream out;

	private final static int MAX_BUFFER_SIZE = 1024;

	public Server(Socket socket) throws IOException {
		this.socket = socket;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		System.out.println("New client: " + socket.getRemoteSocketAddress().toString());
	}

	public void run() {
		try {
			while (true) {
				System.out.println("Begin run loop");
				out.write("What do you want".getBytes());

				byte[] received = readBytes(in);
				byte[] response;

				//decryptPacket
				String packet = new String(received);
				System.out.println(packet);

				//process command sent
				if ("bye".equals(packet)) {
					socket.close();
					response = null;
				} else {
					response = "Allo".getBytes();
				}

				//write response if there is one
				if (response == null) {
					break;
				} else {
					out.write(response);
				}
			}

		} catch (SocketException s) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(in, out);
		}
	}

	private byte[] readBytes(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[MAX_BUFFER_SIZE];

		while ((nRead = in.read(data)) > 0) {
			buffer.write(data, 0, nRead);
		}

		return buffer.toByteArray();
	}

	private void close(InputStream in, OutputStream out) {
		try {
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

