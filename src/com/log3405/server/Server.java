package com.log3405.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Server extends Thread {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private File currentDirectory;

	private final static String ROOT_DIRECTORY = "Server/Storage";
	private final static int MAX_BUFFER_SIZE = 1024;

	public Server(Socket socket) throws IOException {
		this.socket = socket;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		System.out.println("New client: " + socket.getRemoteSocketAddress().toString());
		currentDirectory = new File(ROOT_DIRECTORY);

		byte[] starter = "Connected".getBytes();
		writeBytes(starter);
	}

	public void run() {
		try {
			exec: while (true) {
				byte[] received = readBytes(in);

				// decryptPacket
				byte[] packetType = Arrays.copyOfRange(received, 0, 4);
				byte[] packetPayload = Arrays.copyOfRange(received, 4, received.length - 1);

				int packetTypeAsInt = ByteBuffer.wrap(packetType).getInt();

				switch(packetTypeAsInt) {
					case 0: // CD
						String newDirCD = new String(packetPayload).trim();
						String messageCD = "Vous êtes dans le dossier " + newDirCD + ".";
						writeBytes(messageCD.getBytes());
						break;
					case 1: // LS
						listCurrentDirectory();
						break;
					case 2: // mkdir
						// TODO
						String newDirMK = new String(packetPayload).trim();
						String messageMK = "Le dossier " + newDirMK + " a été créé.";
						writeBytes(messageMK.getBytes());
						break;
					case 3: // UPLOAD
						// TODO
						String newFileName = new String(packetPayload).trim();
						String messageUPLOAD = "Le fichier " + newFileName +" a bien été téléversé.";
						writeBytes(messageUPLOAD.getBytes());
						break;
					case 4: // DOWNLOAD
						// TODO
						String downloadedFileName = new String(packetPayload).trim();
						String messageDOWNLOAD = "Le fichier " + downloadedFileName +" a bien été téléchargé";
						writeBytes(messageDOWNLOAD.getBytes());
						break;
					case 5: // EXIT
						writeBytes("Vous avez été déconnecté avec succès.".getBytes());
						// disconnect now ?
						break exec;
					default:
						writeBytes("Unknown command. Possible commands are : ls, cd, mkdir, upload, download, exit".getBytes());
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

	private void listCurrentDirectory() throws IOException {
		File[] directoryListing = currentDirectory.listFiles();
		if (directoryListing != null) {
			String response = "";
			for (File child : directoryListing) {
				if(child.isDirectory()) {
					response += "[ FOLDER ] ";
				} else {
					response += "[ FILE ] ";
				}
				response += child.getName() + "\n";
			}
			writeBytes(response.getBytes());
		} else {
			// TODO: Handle the case where dir is not really a directory.
		}
	}

	private void close(InputStream in, OutputStream out) {
		try {
			System.out.println("Closing socket and I/O streams");
			socket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

