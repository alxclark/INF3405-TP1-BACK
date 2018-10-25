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

	/**
	 * Setup streams and inital directory, sends a message when ready
	 * @param socket Socket assigned to this client
	 * @throws IOException
	 */
	public Server(Socket socket) throws IOException {
		this.socket = socket;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		System.out.println("[" + socket.getRemoteSocketAddress().toString() + " - " + LocalDateTime.now() + "]: Connected");
		currentDirectory = new File(ROOT_DIRECTORY);

		//send a connected confirmation to the client
		byte[] starter = "Connected".getBytes();
		BytesUtils.writeBytes(out, starter);
	}

	/**
	 * Main loop of the thread
	 */
	public void run() {
		try {
			exec:
			while (true) {
				//read client command
				byte[] received = BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false);

				//Transform packet into byte Array
				//0 to 4: operation type code
				//4 to end: data for the operation (named payload)
				byte[] packetType = BytesUtils.extractSubByteArray(received, 0, 4);
				byte[] packetPayload = BytesUtils.extractSubByteArray(received, 4, received.length);
				int packetTypeAsInt = BytesUtils.bytesToInt(packetType);

				//Log the IPAddress and time prefix
				System.out.print("[" + socket.getRemoteSocketAddress().toString() + " - " + LocalDateTime.now() + "]: ");

				switch (packetTypeAsInt) {
					case 0: {// CD
						//for the CD operation, the payload is a string with the target directory
						String newDirCD = BytesUtils.bytesToString(packetPayload);
						System.out.println("cd " + newDirCD);

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

						//send a message to the client with the result of the operation
						BytesUtils.writeBytes(out, messageCD.getBytes());
						break;
					}
					case 1: {// LS
						//for the LS operation, the payload doesnt matter
						System.out.println("ls");
						listCurrentDirectory();
						break;
					}
					case 2: { // mkdir
						//for the mkdir operation, the payload is a string with the new directory name
						String newDirMK = BytesUtils.bytesToString(packetPayload);
						System.out.println("mkdir " + newDirMK);

						String newPath = currentDirectory.getPath() + '/' + newDirMK;
						String messageMK;
						if (new File(newPath).mkdir()) {
							messageMK = "Le dossier " + newDirMK + " a été créé.";
						} else {
							messageMK = "Il y a eu une erreur lors de la creation de ce dossier";
						}

						//send a message to the client with the result of the operation
						BytesUtils.writeBytes(out, messageMK.getBytes());
						break;
					}
					case 3: {// UPLOAD
						//for the upload operation, the payload is a byte array that need to be divided further
						//0 to 4: Number of bytes of the uploaded file
						//4 to end: Name of the uploaded file
						byte[] fileLengthPayload = BytesUtils.extractSubByteArray(packetPayload, 0, 4);
						byte[] fileNamePayload = BytesUtils.extractSubByteArray(packetPayload, 4, packetPayload.length);
						String newFileName = BytesUtils.bytesToString(fileNamePayload);
						int fileLengthAsInt = BytesUtils.bytesToInt(fileLengthPayload);

						System.out.println("upload " + newFileName);

						//send a message to the client, to warn that the server is ready to receive the file
						String messageMidUpload = "Ready to read:" + fileLengthAsInt + " bytes";
						BytesUtils.writeBytes(out, messageMidUpload.getBytes());

						//read file as bytes
						byte[] file = BytesUtils.readBytes(in, fileLengthAsInt, true);

						//Location of the new file
						File newFile = new File(currentDirectory.getPath() + '/' + newFileName);
						String messageUPLOAD;

						//write the bytes to the file
						try (FileOutputStream fos = new FileOutputStream(newFile.getPath())) {
							if (!newFile.exists()) {
								newFile.createNewFile();
							}
							fos.write(file);
							messageUPLOAD = "Le fichier " + newFileName + " a bien été téléversé.";
						} catch (IOException e) {
							messageUPLOAD = "Une erreur est arrivee lors du televersement du fichier";
						}

						//send a message to the client with the result of the operation
						BytesUtils.writeBytes(out, messageUPLOAD.getBytes());
						break;
					}
					case 4: {// DOWNLOAD
						//for the download operation, the payload is a string with the target file
						String targetFileName = BytesUtils.bytesToString(packetPayload);
						System.out.println("download " + targetFileName);
						File targetFile = new File(currentDirectory.getPath() + '/' + targetFileName);

						//if the file exists, continue the operation, else send an error to the client
						if (targetFile.exists()) {
							//Send a packet with the file structure
							//0 to 4: Status code for the file (0 = normal), (1 = error)
							//4 to 8: Length in bytes of the file
							//8 to end: Length of the file name
							byte[] fileStatus = BytesUtils.intToBytes(0);
							byte[] filePayload = Files.readAllBytes(targetFile.toPath());
							byte[] fileLengthPayload = BytesUtils.intToBytes(filePayload.length);
							byte[] fileNamePayload = targetFile.getName().getBytes();
							byte[] tempPacket = BytesUtils.concat(fileStatus, fileLengthPayload);
							byte[] finalPacket = BytesUtils.concat(tempPacket, fileNamePayload);

							BytesUtils.writeBytes(out, finalPacket);

							//read until client is ready to handle the file
							System.out.println("[" + socket.getRemoteSocketAddress().toString() + " - " + LocalDateTime.now() + "]:");
							System.out.println(BytesUtils.bytesToString(BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false)));

							//send a packet containing only the file's bytes
							BytesUtils.writeBytes(out, filePayload);
						} else {
							//Send a packet describing the error
							//0 to 4: Status code for the file (0 = normal), (1 = error)
							//4 to end: error message
							byte[] fileStatus = BytesUtils.intToBytes(1);
							byte[] responsePayload = ("File " + targetFile.getName() + " does not exists").getBytes();
							byte[] finalPacket = BytesUtils.concat(fileStatus, responsePayload);

							//send packet
							BytesUtils.writeBytes(out, finalPacket);
						}

						break;
					}
					case 5: {// EXIT
						//for the exit operation, the payload doesnt matter
						System.out.println("exit");
						//send a message to the client with the result of the operation
						BytesUtils.writeBytes(out, "Vous avez été déconnecté avec succès.".getBytes());

						// break the loop to excute the closing functions
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

	/**
	 * Builds a String for the directory structure and send it to the client
	 * @throws IOException thrown in case of an error with the sockets
	 */
	private void listCurrentDirectory() throws IOException {
		File[] directoryListing = currentDirectory.listFiles();
		StringBuilder builder = new StringBuilder();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.isDirectory()) {
					builder.append("[ FOLDER ] ");
				} else {
					builder.append("[ FILE ] ");
				}
				builder.append(child.getName()).append("\n");
			}
			if ("".equals(builder.toString())) {
				BytesUtils.writeBytes(out, "[ EMPTY ]".getBytes());
			} else {
				BytesUtils.writeBytes(out, builder.toString().getBytes());
			}
		} else {
			BytesUtils.writeBytes(out, "There was an error with this directory".getBytes());
		}
	}

	/**
	 * Close the streams and sockets
	 * @param in InputStream of the socket
	 * @param out OutputStream of the socket
	 */
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

