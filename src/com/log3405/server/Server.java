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
			exec:
			while (true) {
				byte[] received = readBytes(in, MAX_BUFFER_SIZE);

				// decryptPacket
				byte[] packetType = Arrays.copyOfRange(received, 0, 4);
				byte[] packetPayload = Arrays.copyOfRange(received, 4, received.length - 1);

				int packetTypeAsInt = ByteBuffer.wrap(packetType).getInt();

				switch (packetTypeAsInt) {
					case 0: // CD
						String newDirCD = new String(packetPayload).trim();
						String messageCD;

						if ("..".equals(newDirCD)) {
							currentDirectory = currentDirectory.getParentFile();
							messageCD = "Vous êtes dans le dossier " + currentDirectory.getName() + ".";
						} else {
							String newPath = currentDirectory.getPath() + '/' + newDirCD;
							File newDirectory = new File(newPath);

							if (newDirectory.isDirectory()) {
								currentDirectory = newDirectory;
								messageCD = "Vous êtes dans le dossier " + currentDirectory.getName() + ".";
							} else {
								messageCD = "Ce dossier n'existe pas.";
							}
						}
						writeBytes(messageCD.getBytes());

						break;
					case 1: // LS
						listCurrentDirectory();
						break;
					case 2: // mkdir
						// TODO
						String newDirMK = new String(packetPayload).trim();
						String newPath = currentDirectory.getPath() + '/' + newDirMK;
						String messageMK;
						if (new File(newPath).mkdir()) {
							messageMK = "Le dossier " + newDirMK + " a été créé.";
						} else {
							messageMK = "Il y a eu une erreur lors de la creation de ce dossier";
						}
						writeBytes(messageMK.getBytes());
						break;
					case 3: // UPLOAD
						// TODO
						byte[] fileLengthPayload = Arrays.copyOfRange(packetPayload, 0, 4);
						byte[] fileNamePayload = Arrays.copyOfRange(packetPayload, 4, packetPayload.length);
						String newFileName = new String(fileNamePayload).trim();
						int fileLengthAsInt = ByteBuffer.wrap(fileLengthPayload).getInt();
						String messageMidUpload = "Ready to read:" + fileLengthAsInt + " bytes";
						writeBytes(messageMidUpload.getBytes());

						byte[] file = readBytes(in, fileLengthAsInt);
						String messageUPLOAD;

						File newFile = new File(currentDirectory.getPath() + '/' + newFileName);

						try (FileOutputStream fos = new FileOutputStream(newFile.getPath())) {
							if (!newFile.exists()) {
								newFile.createNewFile();
							}
							fos.write(file);
							messageUPLOAD = "Le fichier " + newFileName + " a bien été téléversé.";
						} catch (IOException e) {
							messageUPLOAD = "Une erreur est arrivee lors du televersement du fichier";
						}

						writeBytes(messageUPLOAD.getBytes());
						break;
					case 4: // DOWNLOAD
						// TODO
						String downloadedFileName = new String(packetPayload).trim();
						String messageDOWNLOAD = "Le fichier " + downloadedFileName + " a bien été téléchargé";
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

	private byte[] readBytes(InputStream in, int bufferSize) throws IOException {
		byte[] data = new byte[bufferSize];

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
				if (child.isDirectory()) {
					response += "[ FOLDER ] ";
				} else {
					response += "[ FILE ] ";
				}
				response += child.getName() + "\n";
			}
			if ("".equals(response)) {
				writeBytes("[ EMPTY ]".getBytes());
			} else {
				writeBytes(response.getBytes());
			}
		} else {
			writeBytes("There was an error with this directory".getBytes());
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

