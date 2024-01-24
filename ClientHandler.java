package server;
import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.Scanner;
import java.io.FileWriter;

public class ClientHandler extends Thread {
	 private Socket socket;
	 private int clientNumber;
	 public ClientHandler(Socket socket,int clientNumber) {
		 this.socket=socket;
	     this.clientNumber=clientNumber;
	     System.out.println("New connection with client#" + clientNumber + " at " + socket);
	    }
	 public void run() {
	        try {
	        	DataInputStream in = new DataInputStream(socket.getInputStream());
	            // out.writeUTF("Hello from server - you are client#" + clientNumber);
	            
	            String receivedUsername = in.readUTF();
	            String receivedPassword = in.readUTF();
	            String isAuthenticated = checkUserPassword(receivedUsername, receivedPassword);
	            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	            out.writeUTF(isAuthenticated);
	            
	        }catch(IOException e) {
	            System.out.println("Error Handling Client#" + clientNumber + ": " + e);
	        }finally {
	            try {
	                socket.close();
	            }catch(IOException e) {
	                System.out.println("Couldn't close a socket, what's going on?");
	            }
	        }
	        System.out.println("Connection with client#" + clientNumber + " closed");
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
