package com.log3405.server;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationStartup {

	/**
	 * Setup the server on the specified IpAddress and port
	 * @param args (not used)
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Scanner consoleInputReader = new Scanner(System.in);

		//Read from console
		String ipAddress = getIPAddress(consoleInputReader);
		System.out.println("IPAddress is: " + ipAddress);
		int port = getPort(consoleInputReader);
		System.out.println("Port is: " + port);

		//Setup server on specified IPaddress and port
		ServerSocket listener = new ServerSocket();
		InetAddress serverAddress = InetAddress.getByName(ipAddress);
		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(serverAddress, port));

		while (true) {
			try {
				System.out.println("Waiting for a new connection ...");

				//Starts a new thread for every connection request
				Thread t = new Server(listener.accept());
				t.start();
			} catch (IOException e) {
				listener.close();
				e.printStackTrace();
			}
		}
	}

	/**
	 * Read the IpAddress written into the console. Also compares it to a RegEx to validate the IpAddress Format
	 * @param consoleInputReader Console Input Scanner
	 * @return a String representing the IpAddress
	 */
	private static String getIPAddress(Scanner consoleInputReader) {
		System.out.println("Hello, Please Enter the IPAddress of the server");
		String ipAddressPattern = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
		Pattern pattern = Pattern.compile(ipAddressPattern);
		String ipAddress = consoleInputReader.nextLine();
		Matcher matcher = pattern.matcher(ipAddress);
		if (!matcher.matches()) {
			System.out.println("Invalid IPAddress. Valid example: 192.168.1.1");
			ipAddress = getIPAddress(consoleInputReader);
		}
		return ipAddress;
	}

	/**
	 * Read the port written into the console. Also verifies it's between 5000 and 5050
	 * @param consoleInputReader Console Input Scanner
	 * @return a Integer representing the port
	 */
	private static int getPort(Scanner consoleInputReader) {
		System.out.println("Please Enter the Port of the server");
		int port = consoleInputReader.nextInt();
		if (port < 5000 && port > 5050) {
			System.out.println("Invalid port. Only ports between 5000 and 5050 are supported");
			port = getPort(consoleInputReader);
		}
		return port;
	}
}
