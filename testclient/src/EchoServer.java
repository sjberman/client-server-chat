import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import ocsf.server.*;

import common.ChatIF;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends ObservableServer 
{
	//Class variables *************************************************

	/**
	 * The default port to listen on.
	 */
	final public static int DEFAULT_PORT = 5555;

	//Instance variables *************************************************

	private Observer serverUI;	
	ArrayList<String> users;
	ArrayList<String> accounts;
	ArrayList<String> passwords;
	ArrayList<String> serverMuteUsers;
	ArrayList<String> blockedClients;

	TimerTask StatusTask = new TimerTask(){
		@Override
		public void run(){
			Date now = new Date();
			Thread[] t = getClientConnections();
			for(int i = 0; i < t.length; i ++){
				ConnectionToClient tempClient = (ConnectionToClient) t[i];
				Date lastActive = (Date) tempClient.getInfo("lastAct");
				long temp = now.getTime() - lastActive.getTime();
				long secstemp = temp/1000;				
				if(now.getTime() - lastActive.getTime() >= 300000.0) {// 300000ms == 5min
					//Idle for more than 5 min
					if(tempClient.getInfo("status").equals("online"))
						tempClient.setInfo("status", "idle");
				}
			}
		}
	};

	//Constructor ****************************************************

	public EchoServer(int port, Observer serverConsole) 
	{
		super(port);
		serverUI = serverConsole;
		users = new ArrayList<String>();
		accounts = new ArrayList<String>();
		passwords = new ArrayList<String>();
		serverMuteUsers = new ArrayList<String>();
		blockedClients = new ArrayList<String>();
		Timer UpdateTimer = new Timer();
		//Run "idol status updater" every 10 sec
		UpdateTimer.scheduleAtFixedRate(StatusTask, 0, 10000);
		this.buildUserList(port);

		try {
			this.listen();
		} catch (IOException e) {
			NotifyObservers("Unable to start listening!");
		} 
	}

	//Instance methods ************************************************

	private void NotifyObservers(String message) {
		setChanged();
		notifyObservers(message);		
	}

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client)
	{
		//Update client last active time
		client.setInfo("lastAct", new Date());

		if(client.getInfo("status").equals("idle"))
			client.setInfo("status", "online");

		String message = msg.toString();
		if(blockedClients.contains(client.getInfo("loginId")) && !message.startsWith("#")) {
			return;
		} else if(message.startsWith("#linedraw")){
			sendToAllClients(message);
			return;
		} else{
			NotifyObservers("Message received: " + message + " from " + client.getInfo("loginId"));
		}

		if(!message.startsWith("#")){ 
			//message
			SelectiveSendToClients(client.getInfo("loginId") + "> " + msg, client);
		} else { 
			//command
			String cmd = GetCommand(message);

			if(cmd.equals("login")){
				String[] login = message.split(" ");
				String id = login[1];
				String pw = login[2];
				boolean validLogin = LoginRecived(client, id, pw);					
			} else if (cmd.equals("block")){
				String blockee = message.substring(message.indexOf(' ')+1, message.length());
				NewBlock(client,blockee);
			} else if (cmd.equals("unblock")){
				UnblockCmd(client, message);
			} else if (cmd.equals("whoiblock")){
				WhoIBlockCmd(client);
			} else if (cmd.equals("whoblocksme")){
				WhoBlocksMeCmd(client);
			} else if (cmd.equals("setchannel")){
				SetChannelCmd(client, message);
			} else if (cmd.equals("private")){
				SendPvtMsg(client, message);
			} else if (cmd.equals("meeting")){
				NewMeeting(client, message);
			} else if (cmd.equals("forward")){
				ForwardMessage(client, message);
			} else if (cmd.equals("status")){
				GetStatus(client, message);				
			} else if (cmd.equals("available") || cmd.equals("notavailable")){
				SetClientStatus(client, cmd);
			} else if (cmd.equals("endforward")){
				endForward(client);
			}else if (cmd.equals("forward_message")) {
				sendForward(client, message);
			}else if (cmd.equals("endmeeting")) {
				endForward(client);
			}
		}	
	}

	/**
	 * This method handles incoming messages from the server UI
	 */
	public void handleMessageFromServerUI(String message) {
		if(!message.startsWith("#")) {//Server Msg			
			NotifyObservers(message);
			SendToServerFriendlyClients("SERVER MSG> " + message);
		} else {
			int cmdEnd = message.indexOf(' ');
			if (cmdEnd < 1) 
				cmdEnd = message.length();
			String cmd = message.substring(1, cmdEnd);

			//Switch based on user command
			switch (cmd) {
			case "quit" :
				System.exit(0);
				break;
			case "stop" :
				if(!isListening())
					NotifyObservers("Server is already stopped.");
				else {
					stopListening();
				}
				break;
			case "close" :
				try{
					sendToAllClients("SERVER SHUTTING DOWN! DISCONNECTING!");
					sendToAllClients("Abnormal termination of connection");
					close();
				} catch (IOException e){
					NotifyObservers("Unable to close.");
				}
				/*}*/
				break;
			case "setport" :
				try{
					int port = Integer.parseInt(message.substring(cmdEnd +1, message.length()));
					setPort(port);
					NotifyObservers("Port set to: " + port);
				}catch (NumberFormatException e){
					NotifyObservers("Port could not be set");
				}
				break;
			case "start" :
				if(isListening())
					NotifyObservers("Server is already listening.");
				else {
					try {							
						listen();
					} catch (IOException e) {
						NotifyObservers("Unable to start listening.");
					}
				}
				break;
			case "getport" :
				NotifyObservers("Current Port: " + getPort());				
				break;
			case "block" :
				String blockee = message.substring(cmdEnd+1, message.length());				

				if(!users.contains(blockee)){
					NotifyObservers("User " + blockee + " does not exist.");
				} else if(blockedClients.contains(blockee)){
					NotifyObservers("Messages from " + blockee + " were already blocked.");					
				} else {
					NotifyObservers("Messages from " + blockee + " will be blocked.");
					blockedClients.add(blockee);	
				}
				break;
			case "unblock" :
				if (message.trim().equals("#"+cmd)) { //#unblock all command
					if (blockedClients.isEmpty()) {
						NotifyObservers("No blocking is in effect.");
					}else { //
						for (int i = 0; i < blockedClients.size(); i++) {
							NotifyObservers("Messages from " + blockedClients.get(i) + " will now be displayed.");
						}
						blockedClients.clear();
					}
				} else {
					String unBlockee = message.substring(cmdEnd+1, message.length());	
					if(blockedClients.contains(unBlockee)){
						blockedClients.remove(unBlockee);
						NotifyObservers("Messages from " + unBlockee + " will now be displayed" );
					}
					else{
						NotifyObservers("Messages from " + unBlockee + " were not blocked");
					}
				}

				break;
			case "whoiblock" :
				if (blockedClients.isEmpty()) {
					NotifyObservers("No blocking is in effect.");
				}else {
					for (int i = 0; i < blockedClients.size(); i++) {
						NotifyObservers("Messages from " + blockedClients.get(i) + " are blocked.");
					}
				}
				break;
			default: 
				NotifyObservers("Command not recognized.");
			}		
		}
	}

	/**
	 * This method overrides the one in the superclass.  Called
	 * when the server starts listening for connections.
	 */
	protected void serverStarted()
	{
		NotifyObservers("Server listening for connections on port " + getPort());
	}

	/**
	 * This method overrides the one in the superclass.  Called
	 * when the server stops listening for connections.
	 */
	protected void serverStopped()
	{
		NotifyObservers("Server has stopped listening for connections.");
		sendToAllClients("WARNING - The server has stopped listening for connections");
	}

	/**
	 * This method overrides the one in the superclass.  Called
	 * when a client connects.
	 */
	protected void clientConnected(ConnectionToClient client){
		client.setInfo("Blocked", new ArrayList<ArrayList<String>>());
		client.setInfo("status", "online");
		String msg = "A new client is attempting to connect to the server.";
		NotifyObservers(msg);		
	}

	/**
	 * This method overrides the one in the superclass.  Called
	 * when a client disconnects.
	 */
	protected void clientDisconnected(ConnectionToClient client){
		String msg = client.getInfo("loginId") + " has disconnected!";
		//removeUser((String) client.getInfo("loginId"));
		if(client.getInfo("loginId") != null){
			NotifyObservers(msg);
			sendToChannel((String) client.getInfo("channel"),msg);		
		}
	}

	//Class methods ***************************************************

	/**
	 * Handles login requests from clients
	 */
	private boolean LoginRecived(ConnectionToClient client, String id, String pw) {
		String clientOrigLogin = (String) client.getInfo("loginId");
		if(clientOrigLogin != null){
			try {
				client.sendToClient("ERROR- You have already logged in with user id: " + clientOrigLogin + ".");				
			} catch (IOException e1) {
				NotifyObservers("ERROR- Unable to send login error message to client: " + clientOrigLogin);
			}
			return false;
		}

		try{
			Thread[] clients = this.getClientConnections();
			for(int i = 0; i < clients.length; i++){
				if(((ConnectionToClient)clients[i]).getInfo("loginId").equals(id)){
					//User already existed
					try {
						client.sendToClient("Login Error: The client " + id + " is already logged in.");
						NotifyObservers("A client, " + id + " tried to log in but " + id + " is already logged in.");
						client.close();
					} catch (IOException e) {
						NotifyObservers("ERROR- Unable to send login error message to client: " + id);
					}
					return false;
				}
			}
		} catch (RuntimeException e){} //Catches when there are no clients to getClientConnections

		if(users.contains(id)) {
			//User already existed
			int index = users.indexOf(id);
			if (!pw.equals(passwords.get(index))) {
				try {
					client.sendToClient("Login Error: The password entered was incorrect. Please try again.");
					NotifyObservers("A client, " + id + " tried to log in with the wrong password.");
					client.close();
					return false;
				} catch (IOException e) {
					NotifyObservers("ERROR- Unable to send login error message to client: " + id);
				}

			}else {

				client.setInfo("loginId", id);
				client.setInfo("pw", pw);

				//Initially put all users into public chat
				client.setInfo("channel", "public");

				sendToChannel("public", id + " has logged on.");
				NotifyObservers(id + " has logged on.");

				return  true;
			}
		}
		//first unique login for client
		client.setInfo("loginId", id);
		client.setInfo("pw", pw);

		addUser(id, pw);

		//Initially put all users into public chat
		client.setInfo("channel", "public");

		sendToChannel("public", id + " has logged on.");
		NotifyObservers(id + " has logged on.");

		return  true;
	}

	private void addUser (String id, String pw) {
		users.add(id);
		passwords.add(pw);

		try (BufferedWriter buffer = new BufferedWriter(new FileWriter("server" + this.getPort() + ".txt", true))) {
			buffer.write(id + " " + pw + System.getProperty("line.separator"));
		}catch (IOException e){
		}
	}

	private void sendToChannel(String channel, String msg) {
		Thread[] clientThreadList = getClientConnections();

		for (int i=0; i<clientThreadList.length; i++)
		{
			ConnectionToClient conn= (ConnectionToClient) clientThreadList[i];
			if (conn.getInfo("channel").equals(channel)) {
				try {
					conn.sendToClient(msg);
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
			}
		}
	}

	/**
	 * This method adds the blockee to the clients block list
	 */
	private void NewBlock(ConnectionToClient client, String blockee) {
		String blocker = client.getInfo("loginId").toString();
		ArrayList<String> blocked = (ArrayList<String>) client.getInfo("Blocked");	
		if(blockee.equals(blocker)){
			try {
				client.sendToClient("You cannot block the sending of messages to yourself.");
			} catch (IOException e) {
				NotifyObservers("ERROR - Failed to send message to client " + blocker);
			}
		} else if (!UserExists(blockee)){
			try {
				client.sendToClient("User " + blockee + " does not exist");
			} catch (IOException e) {
				NotifyObservers("ERROR - Failed to send message to client " + blocker);
			}
		} else if(blocked.contains(blockee)){
			try {
				client.sendToClient("Messages from " + blockee + " were already blocked.");
			} catch (IOException e) {
				NotifyObservers("ERROR - Failed to send message to client " + blocker);
			}
		} else if (blocker.length() > 0){
			try {
				if(blockee.equals("server")){
					serverMuteUsers.add((String) client.getInfo("loginId"));
					ArrayList<String> clientBlocked = (ArrayList<String>)client.getInfo("Blocked");
					clientBlocked.add("server");
					client.setInfo("Blocked", clientBlocked);
					client.sendToClient("Messages from " + blockee + " will be blocked.");
				}else {
					if (users.contains(blockee)) {
						ConnectionToClient blockee_connection = GetClientConnection(blockee);
						String blockeeMonitor = (String) blockee_connection.getInfo("Monitor");
						if (blockeeMonitor != null && blockeeMonitor.equals(client.getInfo("loginId"))) {
							client.sendToClient("Forwarding of messages form " + blockee + " to you has been terminated.");
							blockee_connection.sendToClient("#forwardBlocked");
						}else {
							client.sendToClient("Messages from " + blockee + " will be blocked.");
						}
					}else {
						client.sendToClient("Messages from " + blockee + " will be blocked.");
					}
					blocked.add(blockee);
					client.setInfo("Blocked", blocked);
				}
			} catch (IOException e) {
				NotifyObservers("ERROR - Failed to send message to client " + blocker);
			}
		}
	}

	private void NewMeeting(ConnectionToClient client, String message) {

		String monitor = message.substring(message.indexOf(' ') +1);
		if(client.getInfo("loginId").equals(monitor)){
			try {
				client.sendToClient("ERROR - You cannot monitor your own chat.");
			} catch (IOException e) {
				NotifyObservers("ERROR - Failed to send message to client");
			}
		} else if(!UserExists(monitor)){
			try {
				client.sendToClient("ERROR - User to monitor chat must exist.");
			} catch (IOException e) {
				NotifyObservers("ERROR - Failed to send message to client");
			}
		} else {
			try {
				client.sendToClient("#meeting " + monitor);
			} catch (IOException e) {
				NotifyObservers("ERROR - Failed to send message to client");
			}
			SendMessageToClient(client, GetClientConnection(monitor), client.getInfo("loginId") + " is in a meeting and has selected you to monitor their chat. You will now receive all of " + client.getInfo("loginId") + "'s messages"); 
		}		
	}

	/**
	 * Handles unblock commands from clients
	 */
	private void UnblockCmd(ConnectionToClient client, String message) {
		String cmd = GetCommand(message);
		int cmdEnd = message.indexOf(' ');

		if (message.trim().equals("#"+cmd)) { //#unblock all command
			if (GetBlocks(client).isEmpty()) {
				try {
					client.sendToClient("No blocking is in effect.");
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
			}else { //
				ArrayList<String> blocks = GetBlocks(client);
				for (int i = 0; i < blocks.size(); i++) {
					try {
						client.sendToClient("Messages from " + blocks.get(i) + " will now be displayed.");
						if( blocks.get(i).equals("server"))
							serverMuteUsers.remove(client.getInfo("loginId"));
					} catch (IOException e) {
						NotifyObservers("Message could not be sent to the client.");
					}
				}
				client.setInfo("Blocked", new ArrayList<String>());
			}
		}else { //#unblock user command
			serverMuteUsers.remove(client.getInfo("loginId"));
			String unBlockee = message.substring(cmdEnd+1, message.length());
			if(Unblock(client,unBlockee)){
				try {
					client.sendToClient("Messages from " + unBlockee + " will now be displayed.");
					if( unBlockee.equals("server"))
						serverMuteUsers.remove("server");
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
			}else {
				try {
					client.sendToClient("Messages from " + unBlockee + " were not blocked");
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
			}
		}		
	}

	/**
	 * displays to client list of users the client blocks
	 */
	private void WhoIBlockCmd(ConnectionToClient client) {
		ArrayList<String> iBlocked = GetBlocks(client);
		if (iBlocked.isEmpty()) {
			try {
				client.sendToClient("No blocking is in effect.");
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to the client.");
			}
		}else {
			for (int i = 0; i < iBlocked.size(); i++) {
				try {
					client.sendToClient("Messages from " + iBlocked.get(i) + " are blocked.");
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
			}
		}
	}

	/**
	 * displays to client list of users that block the client
	 */
	private void WhoBlocksMeCmd(ConnectionToClient client) {
		ArrayList<Thread> blockedMe = GetBlockedMe(client);
		for (int i = 0; i < blockedMe.size(); i++) {
			try {
				client.sendToClient("Messages to " + ((ConnectionToClient) (blockedMe.get(i))).getInfo("loginId") + " are being blocked.");
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to the client.");
			}
		}		
	}

	/**
	 * This method unblocks the unBlockee from the clients block list
	 */
	private boolean Unblock(ConnectionToClient client, String unBlockee) {
		ArrayList<String> blocks = GetBlocks(client);
		if (blocks.contains(unBlockee)) {
			blocks.remove(unBlockee);
			return true;
		} 
		return false;
	}

	/**
	 * This method gets the clients) blocked users
	 */
	private ArrayList<String> GetBlocks(ConnectionToClient client) {
		ArrayList<String> blocks = (ArrayList<String>) client.getInfo("Blocked");
		return blocks;
	}

	/**
	 * This method gets the users that have blocked the client
	 */
	private ArrayList<Thread> GetBlockedMe(ConnectionToClient client) {
		Thread[] clientThreadList = getClientConnections();
		ArrayList<Thread> blockedMe = new ArrayList<>();
		for (int i=0; i<clientThreadList.length; i++)
		{
			ConnectionToClient conn= (ConnectionToClient) clientThreadList[i];
			ArrayList<String> blocked = (ArrayList<String>) conn.getInfo("Blocked");
			if (blocked.contains(client.getInfo("loginId").toString())) {
				blockedMe.add(conn);
			}
		}
		return blockedMe;
	}

	/**
	 * Sends to client the status of user specified in message after the command
	 * @param message including command ("status") and the name of the user to get status of.
	 */
	private void GetStatus(ConnectionToClient client, String message) {
		int cmdEnd = message.indexOf(' ');
		String statuseeName = message.substring(cmdEnd+1, message.length());
		if(!users.contains(statuseeName)){
			//not a user, check if a channel
			boolean isChannel = false;
			Thread[] clients = this.getClientConnections();
			for(int i = 0; i < clients.length; i++){
				String channel = (String) ((ConnectionToClient) clients[i]).getInfo("channel");
				if(channel.equals(statuseeName)){
					String clientChannel = (String) client.getInfo("channel");
					if(!clientChannel.equals(statuseeName)){
						//client is in different channel that info is requested for, display error
						try {
							client.sendToClient("You are not authorized to get information about channel " + statuseeName);
						} catch (IOException e) {
							NotifyObservers("Unable to send status messasge to user.");
						}
						return;
					}
					isChannel = true;
					ConnectionToClient tempClient = (ConnectionToClient) clients[i];
					try {
						client.sendToClient("User " + tempClient.getInfo("loginId") + " is " + tempClient.getInfo("status") +".");
					} catch (IOException e) {
						NotifyObservers("Unable to send status messasge to user.");
					}
				}
			}
			if(!isChannel){
				try {
					client.sendToClient("Channel " + statuseeName + " does not exist.");
				} catch (IOException e) {
					NotifyObservers("Unable to send status messasge to user.");
				}
			}

		} else {
			//show user status
			ConnectionToClient statusee = GetClientConnection(statuseeName);
			if(statusee != null){
				try {
					client.sendToClient("User " + statuseeName + " is " + (String) statusee.getInfo("status")  + ".");
				} catch (IOException e) {
					NotifyObservers("Unable to send status messasge to user.");
				}
			} else {
				try {
					client.sendToClient("User " + statuseeName + " is offline.");
				} catch (IOException e) {
					NotifyObservers("Unable to send status messasge to user.");
				}
			}
		}
	}

	private void SetClientStatus(ConnectionToClient client, String status) {
		try {
			if(status.equals("available")){
				client.setInfo("status", "online");
				client.sendToClient("Your status has been set to online");
			} else if (status.equals("notavailable")){
				client.setInfo("status", "unavailable");
				client.sendToClient("Your status has been set to unavailable");
			}	
		} catch (IOException e) {
			NotifyObservers("Unable to send message to client.");
		}		
	}

	/**
	 * Sets the clients chat channel
	 */
	private void SetChannelCmd(ConnectionToClient client, String message) {
		if(client.getInfo("status").equals("unavailable")){
			try {
				client.sendToClient("You cannot be added to new channels while you are unavailable");
			} catch (IOException e) {
				NotifyObservers("ERROR- Unable to send message to client: " + client.getInfo("loginId"));
			}
		} else {
			String newChannel = message.substring(message.indexOf(' ')+1, message.length());
			client.setInfo("channel", newChannel);
			try {
				client.sendToClient("Channel has been set to: " + newChannel);
			} catch (IOException e) {
				NotifyObservers("ERROR- Unable to send message to client: " + client.getInfo("loginId"));
			}
		}
	}

	/**
	 * Parses out and returns the command from a string message
	 */
	private String GetCommand(String message) {
		int cmdEnd = message.indexOf(' ');
		if (cmdEnd < 1) 
			cmdEnd = message.length();
		return message.substring(1, cmdEnd).toLowerCase();
	}

	/**
	 * Gets the ConnectionToClient that has the same name as clientName
	 * @param clientName
	 * @return Connection to client with loginId==clientName, null if client not found
	 */
	private ConnectionToClient GetClientConnection(String clientName) {
		Thread[] clients = this.getClientConnections();
		for(int i = 0; i < clients.length; i++){
			if(((ConnectionToClient) clients[i]).getInfo("loginId").equals(clientName))
				return (ConnectionToClient) clients[i];		
		}
		//client with clientName not found
		return null;
	}

	/**
	 * Determines if the given userId exists
	 */
	private boolean UserExists(String userId) {
		Thread[] clientList = getClientConnections();
		for (int i=0; i<clientList.length; i++)
		{
			if(((ConnectionToClient) clientList[i]).getInfo("loginId").equals(userId))
				return true;
		}
		return false;
	}

	private void ForwardMessage(ConnectionToClient sender, String message) {	
		String recipient = message.substring(message.indexOf(' ')+1, message.length());
		//String msg = message.substring(endIndex +1);

		//Check if recipient is sender
		if(sender.getInfo("loginId").equals(recipient)){
			try {
				sender.sendToClient("ERROR - You cannot forward your own chat to yourself.");
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to client.");
			}
			return;
		}
		if(!UserExists(recipient)){
			try {
				sender.sendToClient("Cannot forward to " + recipient + " because " + recipient + " does not exist.");
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to client.");
			}
			return;
		}
		if(isBlocking(recipient, (String) sender.getInfo("loginId"))) {
			try {
				sender.sendToClient("Cannot forward to " + recipient + " because " + recipient + " is blocking messages from you.");
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to client.");
			}
			return;
		}
		ConnectionToClient recip = GetClientConnection(recipient);
		if(recip == null || recip.getInfo("status").equals("unavailable") ){
			try {
				sender.sendToClient("Cannot forward to " + recipient + " because " + recipient + " is unavailable.");
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to client.");
			}
			return;
		}
		try {
			sender.sendToClient("#forward " + recipient);
		} catch (IOException e) {
			NotifyObservers("ERROR - Failed to send message to client");
		}
		sender.setInfo("Monitor", recipient);
		SendMessageToClient(sender, recip, sender.getInfo("loginId") + " is forwarding their messages to you."); 
	}

	private void sendForward(ConnectionToClient client, String message) {

		int startIndex = message.indexOf(' ');
		int recipIndex =  message.indexOf(' ', startIndex +1);		
		String recipient = message.substring(startIndex +1, recipIndex);
		String msg = message.substring(recipIndex +1);
		recipIndex++;
		int sendIndex = message.indexOf('>', recipIndex);
		String origSender = message.substring(recipIndex, sendIndex);
		if (isBlocking(recipient, origSender)) {
			SendMessageToClient(GetClientConnection(origSender), GetClientConnection(recipient), msg);
		}else {
			try {
				msg = msg.substring(msg.indexOf('>') + 2, msg.length());
				GetClientConnection(recipient).sendToClient((String) client.getInfo("loginId") + "> " + origSender + "> " + msg);
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to client.");
			}
		}
	}

	private void endForward(ConnectionToClient client) {
		try {
			client.sendToClient("#endforward");
			client.setInfo("Monitor", null);
		} catch (IOException e) {
			NotifyObservers("Message could not be sent to the client.");
		}
	}

	/**
	 * This method sends a message to all unblocked, available clients in same channel
	 */
	private void SelectiveSendToClients(Object msg, ConnectionToClient client){
		String channel = (String) client.getInfo("channel");
		ArrayList<Thread> blockedMe = GetBlockedMe(client);
		Thread[] clientThreadList = getClientConnections();

		for (int i=0; i<clientThreadList.length; i++)
		{
			ConnectionToClient recipClient= (ConnectionToClient) clientThreadList[i];
			String recipChannel = (String) recipClient.getInfo("channel");
			if (!blockedMe.contains(recipClient) && recipChannel.equals(channel) && !recipClient.getInfo("status").equals("unavailable")) {
				try {
					//available, not blocked, and in same channel
					recipClient.sendToClient(msg);
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
			}
		}
	}

	/**
	 * parses out command message and sends private message
	 */	
	private void SendPvtMsg(ConnectionToClient sender, String message) {
		if(sender.getInfo("status") != "unavailable"){

			int startIndex = message.indexOf(' ');
			int endIndex =  message.indexOf(' ', startIndex +1);
			String recipient = message.substring(startIndex +1, endIndex);

			if(recipient.equals(sender.getInfo("loginId"))){
				try {
					sender.sendToClient("You cannot send a private message to yourself.");
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
				return;
			}
			if(!UserExists(recipient)){
				try {
					sender.sendToClient("You cannot send a private message to a user that does not exist.");
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
				return;
			}
			if(GetClientConnection(recipient).getInfo("status").equals("notavailable")) {
				try {
					sender.sendToClient("Cannot send message because " + recipient + " is not available.");
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
				return;
			}
			String msg = message.substring(endIndex +1);
			msg = "(Private) " + msg;
			if (isBlocking (recipient, (String) sender.getInfo("loginId"))) {
				try {
					sender.sendToClient("Cannot send message because " + recipient + " is blocking messages from you.");
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
			}else {
				SendMessageToClient(sender, GetClientConnection(recipient), msg);
				try {
					sender.sendToClient(sender.getInfo("loginId") + "> " + msg);
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
			}
		} else {
			try {
				sender.sendToClient("You can not send a private message while your status is unavailable");
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to the client.");
			}
		}

	}

	/**
	 * This method sends server messages to clients that have not blocked the server.
	 */
	private void SendToServerFriendlyClients(Object msg){

		Thread[] clientThreadList = getClientConnections();

		for (int i=0; i<clientThreadList.length; i++)
		{
			ConnectionToClient conn= (ConnectionToClient) clientThreadList[i];
			if (!serverMuteUsers.contains(conn.getInfo("loginId"))) {
				try {
					conn.sendToClient(msg);
				} catch (IOException e) {
					NotifyObservers("Message could not be sent to the client.");
				}
			}
		}
	}

	private void SendMessageToClient(ConnectionToClient sender, ConnectionToClient recipient, String msg) {

		//Check if recipient exists
		if(recipient == null){
			try {
				sender.sendToClient("ERROR- User " + recipient + " does not exist.");
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to client.");
			}
			return;
		}
		ArrayList<Thread> blockedSender = GetBlockedMe(sender);
		if (!blockedSender.contains((String) recipient.getInfo("loginId")) && (String) recipient.getInfo("status") != "unavailable") {
			try {
				//Not blocked and specified recipient, send msg
				recipient.sendToClient(sender.getInfo("loginId") + "> " + msg);
				//sender.sendToClient(sender.getInfo("loginId") + "> " + msg);
			} catch (IOException e) {
				NotifyObservers("Message could not be sent to client.");
			}
		}
	}

	/**
	 * This method removes a user from the user list
	 */
	private void RemoveUser(String id) {
		for(int i= 0; i < users.size(); i++){
			if(users.get(i).equals(id)){
				users.remove(i);
			}
		}
	}

	private boolean isBlocking(String recip, String sender) {
		ConnectionToClient recipClient = GetClientConnection(recip);
		ArrayList<String> blocked = (ArrayList<String>) recipClient.getInfo("Blocked");
		if (blocked.contains(sender)) {
			return true;
		}else {
			return false;
		}
	}
	private void buildUserList(int port) {
		File file = new File("server" + this.getPort() + ".txt");
		try {
			if(!file.createNewFile()) {
				try (BufferedReader buffer = new BufferedReader(new FileReader("server" + port + ".txt"))) {
					String current;
					while ((current = buffer.readLine()) != null) {
						String[] user = current.split(" ");
						users.add(user[0]);
						passwords.add(user[1]);
					}
				}catch (IOException e){
				}
			}
		} catch (IOException e) {
			NotifyObservers("Error - Could not creat account file.");
		}
	}

	public static void main(String[] args) 
	{
		//Get port
		int port = 0;
		try {
			port = Integer.parseInt(args[0]); 
		} catch(Throwable t) {
			port = DEFAULT_PORT; //Set port to 5555
		}

		ServerConsole server = new ServerConsole(port);
		server.accept();  //Wait for console data
	}
}
//End of EchoServer class