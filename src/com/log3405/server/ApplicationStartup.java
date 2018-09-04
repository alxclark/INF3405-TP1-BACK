package com.log3405.server;

import java.io.IOException;
import java.util.Scanner;

public class ApplicationStartup {

	public static void main(String[] args) {
		Scanner consoleInputReader = new Scanner(System.in);

		String ipAddress = getIPAddress(consoleInputReader);
		int port = getPort(consoleInputReader);

		try {
			new Server(ipAddress, port).startServer();
		} catch (IOException e) {
			e.printStackTrace();
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
