package org.campus02.chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

	public static void main(String[] args) {
		
		ArrayList<ChatClient> list = new ArrayList<>();
		Socket client;
		
		try (
			ServerSocket server = new ServerSocket(1111);
			) {
				while(true) {
					client = server.accept();
					ChatClient cc = new ChatClient(list, client);
					list.add(cc);
					Thread t = new Thread(cc);
					t.start();
				}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
