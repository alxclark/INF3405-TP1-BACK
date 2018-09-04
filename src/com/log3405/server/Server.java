package com.log3405.server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Server {
	private Selector selector;
	private Map<SocketChannel, List> dataMapper = new HashMap<>();
	private InetSocketAddress serverAddress;

	public Server(String ipAddress, int port) {
		serverAddress = new InetSocketAddress(ipAddress, port);
	}

	public void startServer() throws IOException {
		this.selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		setUpServerSocket(serverChannel);

		System.out.println("Server started...");

		while (true) {
			int readyChannels = selector.select();
			if(readyChannels == 0) continue;

			Iterator keys = selector.keys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = (SelectionKey) keys.next();

				if (!key.isValid()) {
					continue;
				}
				if (key.isAcceptable()) {
					accept(key);
				} else if (key.isReadable()) {
					read(key);
				}

				keys.remove();//prevent a key from being handled twice
			}
		}
	}

	private void accept(SelectionKey key) throws IOException{
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverChannel.accept();
		channel.configureBlocking(false);

		dataMapper.put(channel, new ArrayList());
		channel.register(selector, SelectionKey.OP_READ);
	}

	private void read(SelectionKey key) throws IOException{
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int numRead =  channel.read(buffer);

		if(numRead == -1){ //disconnect
			this.dataMapper.remove(channel);
			channel.close();
			key.cancel();
			return;
		}

		byte[] data = new byte[numRead];
		System.out.println("Received: " + new String(data));
	}

	private void write(SelectionKey key) throws IOException{

	}

	private void setUpServerSocket(ServerSocketChannel serverChannel) throws IOException {
		serverChannel.socket().bind(serverAddress);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
}
