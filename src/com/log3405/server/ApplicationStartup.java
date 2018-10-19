package com.log3405.server;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.IO;

public class ApplicationStartup {

	public static void main(String[] args) throws IOException {
		Scanner consoleInputReader = new Scanner(System.in);

		String ipAddress = getIPAddress(consoleInputReader);
		int port = getPort(consoleInputReader);

		ServerSocket listener = new ServerSocket();
		InetAddress serverAdress = InetAddress.getByName(ipAddress);
		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(serverAdress, port));

		while (true) {//waits for new connections
			try {
				System.out.println("Waiting for a new connection ...");
				Thread t = new Server(listener.accept());
				t.start();
			} catch (IOException e) {
				listener.close();
				e.printStackTrace();
			}
		}
	}

	private static String getIPAddress(Scanner consoleInputReader) {
		System.out.println("Hello, Please Enter the IPAddress of the server");
		String ipAddress = consoleInputReader.nextLine();
		System.out.println("IPAddress is: " + ipAddress);
		return ipAddress;
	}

	private static int getPort(Scanner consoleInputReader) {
		System.out.println("Please Enter the Port of the server");
		int port = consoleInputReader.nextInt();
		System.out.println("Port is: " + port);
		return port;
	}
}
