package com.log3405.server;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.IO;

public class ApplicationStartup {

	public static void main(String[] args) throws IOException {
		Scanner consoleInputReader = new Scanner(System.in);

		String ipAddress = getIPAddress(consoleInputReader);
		System.out.println("IPAddress is: " + ipAddress);
		int port = getPort(consoleInputReader);
		System.out.println("Port is: " + port);

		ServerSocket listener = new ServerSocket();
		InetAddress serverAddress = InetAddress.getByName(ipAddress);
		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(serverAddress, port));

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
		String ipAddressPattern = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
		Pattern pattern = Pattern.compile(ipAddressPattern);
		String ipAddress = consoleInputReader.nextLine();
		Matcher matcher = pattern.matcher(ipAddress);
		if (!matcher.matches())
		{
			System.out.println("Invalid IPAddress. Valid example: 192.168.1.1");
			ipAddress = getIPAddress(consoleInputReader);
		}
		return ipAddress;
	}

	private static int getPort(Scanner consoleInputReader) {
		System.out.println("Please Enter the Port of the server");
		int port = consoleInputReader.nextInt();
		if(port != 5000 && port != 5050){
			System.out.println("Invalid port. Only 5000 and 5050 are supported");
			port = getPort(consoleInputReader);
		}
		return port;
	}
}
