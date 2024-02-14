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
    	Scanner scanner = new Scanner(System.in);
    	String adress = "";
    	try {
	    	try {
		        System.out.println("Entrez une adresse IP valide sur laquelle s'exécutera le serveur: ");
		        adress = scanner.nextLine();
		        if (!Server.isValidIPAddress(adress)) {
		            throw new IOException("Adresse IP invalide");
		        }
		    } catch (IOException e) {
		        System.err.println("Erreur: " + e.getMessage());
		    }
	    	
	    	int enteredPort = 0;
	    	try {
	        	System.out.println("Entrez un port entre 5000 et 5050:");
	        	while (!scanner.hasNextInt()) {
	                System.out.println("Veuillez entrer un format de port valide:");
	                scanner.next();
	            }
	        	enteredPort = scanner.nextInt();
	            if(enteredPort < 5000 || enteredPort > 5050) {
	            	throw new IOException("Le port n'est pas entre 5000 et 5050");
	            }
	        } catch (IOException e) {
	            System.err.println("Erreur: " + e.getMessage());
	        }
	
	    	String serverAddress = adress;
	    	int port = enteredPort;
	    	
	    	socket = new Socket(serverAddress,port);
	    	
	        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	        
	        Scanner usernameScanner = new Scanner(System.in);
	        Scanner passwordScanner = new Scanner(System.in);
	        String username = askForUsername(usernameScanner);
	        String password = askForPassword(passwordScanner);
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
	        usernameScanner.close();
	        passwordScanner.close();
    	} catch(Exception e) {
    		System.out.println("Erreur de connection au serveur: " + e.getMessage());
    	} finally {
    		scanner.close();
    	}
    }
    
    private static void sendAndReceive(DataOutputStream out) throws Exception {
    	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Thread thread = new Thread(() -> {
            try {
                String input;
                while ((input = in.readLine()) != null) {
                    System.out.println(input);
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
                } else {
                	System.out.println("Erreur d'envoi. Votre message dépasse la taille maximale de 200 caractères.");
                }
            }
        } finally {
            thread.interrupt();
            reader.close();
            socket.close();
        }
    }
    
    public static String askForUsername(Scanner usernameScanner) {
    	System.out.println("Entrez un nom d'utilisateur: ");
    	String username = usernameScanner.nextLine();
    	return username;
    }
    
    public static String askForPassword(Scanner passwordScanner) {
    	System.out.println("Entrez votre mot de passe: ");
    	String password = passwordScanner.nextLine();
    	return password;
    }
}

