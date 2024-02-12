package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStreamReader;


public class Client {
	private static Socket socket;
    public static void main(String[] args) throws Exception{
    	
    	String adress = "";
    	try {
    		Scanner reader = new Scanner(System.in);
        	System.out.println("Entrez une adresse IP valide sur laquelle s'exécutera le serveur: ");
        	adress = reader.nextLine();
            if(!Server.isValidIPAddress(adress)) {
            	throw new IOException("Adresse IP invalide");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    	
    	int enteredPort = 0;
    	try {
    		Scanner reader = new Scanner(System.in);
        	System.out.println("Entrez un port entre 5000 et 5050:");
        	enteredPort = reader.nextInt();
            if(enteredPort < 5000 || enteredPort > 5050) {
            	throw new IOException("Le port n'est pas entre 5000 et 5050");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    	String serverAddress = adress;
    	int port = enteredPort;
    	
    	socket = new Socket(serverAddress,port);
    	
        // System.out.format("Serveur lancé sur [%s:%d]", serverAddress,port);
        // String helloMessageFromServer = in.readUTF();
        // System.out.println(helloMessageFromServer);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        
        String username = askForUsername();
        String password = askForPassword();
        out.writeUTF(username);
        out.writeUTF(password);
        
        DataInputStream in= new DataInputStream(socket.getInputStream());
        String isAuthenticated = in.readUTF();
        
        if (isAuthenticated.equals("connected")) {
            System.out.println("Connexion à votre compte réussie.\n");
            sendAndReceive(out);
        } else if(isAuthenticated.equals("created")) {
        	System.out.println("Un compte a été automatiquement créé pour vous.");
        	sendAndReceive(out);
        } else if(isAuthenticated.equals("wrong")) {
        	System.out.println("Erreur dans la saisie du mot de passe.");
        } 
        
    }
    
    private static void sendAndReceive(DataOutputStream out) throws Exception {
    	BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Thread thread = new Thread(() -> {
            try {
                String inputLine;
                while ((inputLine = input.readLine()) != null) {
                    System.out.println(inputLine);
                }
            } catch (IOException e) {
                System.out.println("Connection au serveur perdue.");
            }
        });
        thread.start();
        Scanner reader = new Scanner(System.in);
        try {
            while (true) {
                String message = reader.nextLine();
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }
                if (message.length()< 200) {
                out.writeUTF(message);
                }else {
                	System.out.println("Erreur d'envoi. Votre message dépasse la taille maximale de 200 caractères.");
                }
                
            }
        } finally {
            thread.interrupt();
            reader.close();
            socket.close();
        }
    }
    
    
    
    public static String askForUsername() {
    	Scanner reader = new Scanner(System.in);
    	System.out.println("Entrez un nom d'utilisateur: ");
    	String username = reader.nextLine();
    	return username;
    }
    
    public static String askForPassword() {
    	Scanner reader = new Scanner(System.in);
    	System.out.println("Entrez votre mot de passe: ");
    	String password = reader.nextLine();
    	return password;
    }
}
