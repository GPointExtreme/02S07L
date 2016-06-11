package org.campus02.chatserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatClient implements Runnable{
	
	private BufferedReader reader;
	private PrintWriter printwirter;
	private ArrayList<ChatClient> clients;
	private Socket client;
	private String name;
	private HashMap<String, ChatClient> map;
	
	private Object lock = new Object();
	
	public ChatClient(ArrayList<ChatClient> clients, HashMap<String, ChatClient> map,Socket client) {
		super();
		try {
			this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			this.printwirter = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.clients = clients;
		this.map = map;
		this.client = client;
	}

	@Override
	public void run() {
		String line;
		try {
			while((line = reader.readLine()) != null) {
				String[] array = line.split(":");
				if(this.name == null && !(array[0].equals("<name>"))) {
					sendMessage("No Username! <name>: [your nick]");
				}
				else {
					if(array[0].equals("<name>")) {
						if(map.containsKey(array[1])) {
							sendMessage("Nick not unique!");
						}
						else {
							this.name = array[1];
							map.put(this.name, this);
							for (ChatClient c : clients) {
								if(c.name.equals(this.name)) {
									continue;
								}
								else {
									c.sendMessage(this.name + " connected!");
								}
							}
							log(this.name + " connected!");
						}
					}
					else if(array[0].equals("<msg>")) {
						if(array.length == 2) {
							for (ChatClient c : clients) {
								if(c.name.equals(this.name)) {
									continue;
								}
								else {
									c.sendMessage(this.name + ": " + array[1]);
								}
							}
							log(this.name + " " + array[0] + " " + array[1]);
						}
						else {
							sendMessage("wrong format");
						}
					}
					else if(array[0].equals("<msgto>")) {
						if (array.length == 3) {
							if(map.containsKey(array[1])) {
								map.get(array[1]).sendMessage("private msg from " + this.name + ": " + array[2]);
								log(this.name + " " + array[0] + " " + array[1] + " " + array[2]);
							}
							else {
								sendMessage("User doesnt exist!");
							}
						}
						else {
							sendMessage("wrong format");
						}
					}
					else if(array[0].equals("<bye>")) {
						for (ChatClient c : clients) {
							if(c.name.equals(this.name)) {
								continue;
							}
							else {
								c.sendMessage(this.name + ": bye -User disconnected-");
							}
						}
						log(this.name + ": bye -User disconnected-");
						close();
					}
					else {
						sendMessage("wrong format");
					}
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.close(); //falls while verlassen wird soll der Socket geschlossen werden!
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
				FileOutputStream fos = new FileOutputStream(file, true);
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
