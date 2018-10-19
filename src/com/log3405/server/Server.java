package com.log3405.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Server extends Thread {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

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
				byte[] starter = "What do you want".getBytes();
				writeBytes(starter);

				byte[] received = readBytes(in);
				byte[] response;

				//decryptPacket
				String packet = new String(received);
				System.out.println(packet);

				//process command sent
				if ("bye".equals(packet)) {
					//socket.close();
					response = null;
				} else {
					response = "Allo".getBytes();
				}

				//write response if there is one
				if (response == null) {
					break;
				} else {
					writeBytes(response);
				}
			}

		} catch (SocketException s) {
			try {
				System.out.println("Closing thread and socket");
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
		byte[] data = new byte[MAX_BUFFER_SIZE];

		in.read(data, 0, data.length);

		return data;
	}

	private void writeBytes(byte[] data) throws IOException {
		out.write(data, 0, data.length);
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

