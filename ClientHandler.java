package server;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.util.*;


import java.io.FileWriter;

public class ClientHandler implements Runnable {
	 private Socket socket;
	 private int clientNumber;
	 private PrintWriter output;
     //private BufferedReader input;
     private List<ClientHandler> clients;
     private String receivedUsername;
     private List<String> messageHistory; 
     private static final int MESSAGE_HISTORY_SIZE = 15;
     
     
	 public ClientHandler(Socket socket,int clientNumber, List<ClientHandler> clients, List<String> messageHistory ) throws IOException{
		 this.socket=socket;
	     this.clientNumber=clientNumber;
	     this.output = new PrintWriter(socket.getOutputStream(), true);
         //this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         this.clients = clients;
         this.messageHistory = messageHistory;
	     System.out.println("New connection with client#" + clientNumber + " at " + socket);
	    }
	 @Override
	 public void run() {
	        try {
	        	DataInputStream in = new DataInputStream(socket.getInputStream());
	            // out.writeUTF("Hello from server - you are client#" + clientNumber);
	            
	            String receivedUsername = in.readUTF();
	            this.receivedUsername = receivedUsername;
	            String receivedPassword = in.readUTF();
	            String isAuthenticated = checkUserPassword(receivedUsername, receivedPassword);
	            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	            out.writeUTF(isAuthenticated);
	            sendMessageHistory();
	            sendOutMessage("SERVER: " + receivedUsername + " a rejoint la salle de clavardage!", "SERVER", this);
	            String message;
	            
                while ((message = in.readUTF()) != null && !message.equalsIgnoreCase("exit")) {
                	sendOutMessage(message, receivedUsername, this);
                }
	        }catch(IOException e) {
	            //System.out.println("Error Handling Client#" + clientNumber + ": " + e);
	        }finally {
	            try {
	            	sendOutMessage(String.format("SERVER: %s a quitté la salle de clavardage.", receivedUsername),"SERVER", this);
	            	clients.remove(this);
	                socket.close();
	            }catch(IOException e) {
	                System.out.println("Couldn't close a socket, what's going on?");
	            }
	        }
	        System.out.println("Connection with client#" + clientNumber + " closed");
	    }
	 private void sendMessageHistory() {
	        for (String message : messageHistory) {
	            this.output.println(message);
	        }
	    }
	 private void sendOutMessage(String message, String receivedUsername, ClientHandler clientHandler) {
		 String date = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss").format(new Date());
         String formattedMessage = String.format("[%s - %s:%d - %s]: %s",
                 receivedUsername,
                 socket.getInetAddress().getHostAddress(),
                 socket.getPort(),
                 date,
                 message);

         archiveMessage(formattedMessage);
         addToMessageHistory(formattedMessage);
         
         synchronized (clients) {
             for (ClientHandler client : clients) {
                 if (client != clientHandler) {
                     client.output.println(formattedMessage);
                 }
             }
         }
         System.out.println(formattedMessage);
     }
	 
	 private synchronized void addToMessageHistory(String message) {
	        if (messageHistory.size() >= MESSAGE_HISTORY_SIZE) {
	            messageHistory.remove(0);
	        }
	        messageHistory.add(message);
	    }
	 
	 private static void archiveMessage(String formattedMessage) {
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
	        String filename = LocalDateTime.now().format(formatter) + ".txt";

	        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)))) {
	            out.println(formattedMessage); 
	        } catch (IOException e) {
	            System.out.println("Impossible d'écrire dans le fichier: " + e.getMessage());
	        }
	    }
	 
	 public String checkUserPassword(String username, String password) {
		 ArrayList<String> credentials = readFileToArray("Credentials.txt");
		 String validCredentials = "";
		 ArrayList<String> usernames = new ArrayList<>();
		 
		 for (String credential : credentials) {
			 String[] arrOfStr = credential.split(", ");
			 usernames.add(arrOfStr[0]);
			 }
         if(credentials.contains("Username: " + username + ", Password: " + password)) {
	        validCredentials = "connected";
	    }
         else if(usernames.contains("Username: " + username)) {
        	 validCredentials = "wrong";
         }
         else {
        	 try {
            	 FileWriter myWriter = new FileWriter("Credentials.txt", true);
                 myWriter.write("Username: " + username + ", Password: " + password + "\n");
                 myWriter.flush();
                 myWriter.close();
                 
                 credentials.clear();
                 Scanner reloadReader = new Scanner(new File("Credentials.txt"));
                 while (reloadReader.hasNextLine()) {
                     String data = reloadReader.nextLine();
                     credentials.add(data);
                 }
                 validCredentials = "created";
        	 } catch (IOException e) {
        	      System.out.println("Il y a eu une erreur dans la création du compte");
        	   }
         }
         return validCredentials;
	}
	 
	 public ArrayList<String> readFileToArray(String fileName){
		 ArrayList<String> fileLines = new ArrayList<>();
		 try {
		      File myObj = new File(fileName);
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        String data = myReader.nextLine();
		        fileLines.add(data);
		      }
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("Il y a eu un problème dans la lecture du fichier de base de données");
		    }
		 return fileLines;
	 }
}
