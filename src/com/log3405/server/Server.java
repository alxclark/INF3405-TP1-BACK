package com.log3405.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.time.LocalDateTime;

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
		System.out.println("[" + socket.getRemoteSocketAddress().toString() + " - " + LocalDateTime.now() + "]: Connected");
		currentDirectory = new File(ROOT_DIRECTORY);

		byte[] starter = "Connected".getBytes();
		BytesUtils.writeBytes(out, starter);
	}

	public void run() {
		try {
			exec:
			while (true) {
				byte[] received = BytesUtils.readBytes(in, MAX_BUFFER_SIZE);

				// decryptPacket
				byte[] packetType = BytesUtils.extractSubByteArray(received, 0, 4);
				byte[] packetPayload = BytesUtils.extractSubByteArray(received, 4, received.length - 1);

				int packetTypeAsInt = BytesUtils.bytesToInt(packetType);
				System.out.print("[" + socket.getRemoteSocketAddress().toString() + " - " + LocalDateTime.now() + "]: ");

				switch (packetTypeAsInt) {
					case 0: {// CD
						System.out.println("cd " + BytesUtils.bytesToString(packetPayload));
						String newDirCD = BytesUtils.bytesToString(packetPayload);
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
						BytesUtils.writeBytes(out, messageCD.getBytes());

						break;
					}
					case 1: {// LS
						System.out.println("ls");
						listCurrentDirectory();
						break;
					}
					case 2: { // mkdir
						System.out.println("mkdir " + BytesUtils.bytesToString(packetPayload));
						String newDirMK = BytesUtils.bytesToString(packetPayload);
						String newPath = currentDirectory.getPath() + '/' + newDirMK;
						String messageMK;
						if (new File(newPath).mkdir()) {
							messageMK = "Le dossier " + newDirMK + " a été créé.";
						} else {
							messageMK = "Il y a eu une erreur lors de la creation de ce dossier";
						}
						BytesUtils.writeBytes(out, messageMK.getBytes());
						break;
					}
					case 3: {// UPLOAD
						byte[] fileLengthPayload = BytesUtils.extractSubByteArray(packetPayload, 0, 4);
						byte[] fileNamePayload = BytesUtils.extractSubByteArray(packetPayload, 4, packetPayload.length - 1);
						System.out.println("upload " + BytesUtils.bytesToString(fileNamePayload));

						String newFileName = BytesUtils.bytesToString(fileNamePayload);
						int fileLengthAsInt = BytesUtils.bytesToInt(fileLengthPayload);
						String messageMidUpload = "Ready to read:" + fileLengthAsInt + " bytes";
						BytesUtils.writeBytes(out, messageMidUpload.getBytes());

						byte[] file = BytesUtils.readBytes(in, fileLengthAsInt);
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

						BytesUtils.writeBytes(out, messageUPLOAD.getBytes());
						break;
					}
					case 4: {// DOWNLOAD
						String targetFileName = BytesUtils.bytesToString(packetPayload);
						System.out.println("download " + targetFileName);
						File targetFile = new File(currentDirectory.getPath() + '/' + targetFileName);
						if (targetFile.exists()) {
							byte[] fileStatus = BytesUtils.intToBytes(0);
							byte[] filePayload = Files.readAllBytes(targetFile.toPath());
							byte[] dFileLengthPayload = BytesUtils.intToBytes(filePayload.length);
							byte[] dFileNamePayload = targetFile.getName().getBytes();
							byte[] tempPacket = BytesUtils.concat(fileStatus, dFileLengthPayload);
							byte[] finalPacket = BytesUtils.concat(tempPacket, dFileNamePayload);

							BytesUtils.writeBytes(out, finalPacket);
							System.out.println("[" + socket.getRemoteSocketAddress().toString() + " - " + LocalDateTime.now() + "]:");
							System.out.println(
									BytesUtils.bytesToString(BytesUtils.readBytes(in, MAX_BUFFER_SIZE)));//read until client is ready to handle the file
							BytesUtils.writeBytes(out, filePayload);
						} else {
							byte[] fileStatus = BytesUtils.intToBytes(1);
							byte[] responsePayload = ("File " + targetFile.getName() + " does not exists").getBytes();
							byte[] finalPacket = BytesUtils.concat(fileStatus, responsePayload);

							BytesUtils.writeBytes(out, finalPacket);
						}

						break;
					}
					case 5: {// EXIT
						System.out.println("exit");
						BytesUtils.writeBytes(out, "Vous avez été déconnecté avec succès.".getBytes());
						// disconnect now ?
						break exec;
					}
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
				BytesUtils.writeBytes(out, "[ EMPTY ]".getBytes());
			} else {
				BytesUtils.writeBytes(out, response.getBytes());
			}
		} else {
			BytesUtils.writeBytes(out, "There was an error with this directory".getBytes());
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

