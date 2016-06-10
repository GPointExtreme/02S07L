package org.campus02.chatserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient implements Runnable{
	
	private BufferedReader reader;
	private PrintWriter printwirter;
	private ArrayList<ChatClient> clients;
	private Socket client;
	private String name;
	
	private Object lock = new Object();
	
	public ChatClient(ArrayList<ChatClient> clients, Socket client) {
		super();
		try {
			this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			this.printwirter = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.clients = clients;
		this.client = client;
	}

	@Override
	public void run() {
		String line;
		try {
			while((line = reader.readLine()) != null) {
				String[] array = line.split(":");
				if(array[0].equals("<name>")) {
					this.name = array[1];
				}
				else if(array[0].equals("<msg>")) {
					for (ChatClient c : clients) {
						c.sendMessage(array[1]);
						log(array[0] + " " + array[1]);
					}	
				}
				else if(array[0].equals("<msgto>")) {
					if (array.length == 3) {
						for (ChatClient c : clients) {
							if(c.name.equals(array[1])) {
								c.sendMessage(array[2]);
								log(array[0] + " " + array[1] + " " + array[2]);
							}
						}
					}
					else {
						sendMessage("wrong format");
					}
				}
				else if(array[0].equals("<bye>")) {
					close();
				}
				else {
					sendMessage("wrong format");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message) {
		printwirter.println(message);
		printwirter.flush();
	}
	
	public void close() {
		clients.remove(this.client);
		try {
			printwirter.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void log(String logEintrag) {
		synchronized (lock) {
			File file = new File("/temp/log.txt");
			try (
				FileOutputStream fos = new FileOutputStream(file);
				PrintWriter pw = new PrintWriter(fos, true);
				) {
					pw.println(logEintrag);
					pw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
	}

}
