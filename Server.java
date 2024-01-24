package server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Scanner;

public class Server {
	
	private static ServerSocket Listener;
    public static void main(String[] args)throws Exception{
        int clientNumber=0;
        String serverAddress = askForIPAddress();
        int serverPort = askForPort();
        
        Listener = new ServerSocket();
        Listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);
        Listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
        try {
            while(true) {
                new ClientHandler(Listener.accept(),clientNumber++).start();
            }
        }finally {
            Listener.close();
        }
    }
    
    public static String askForIPAddress() {
    	Scanner reader = new Scanner(System.in);
    	System.out.println("Entrez une adresse IP valide sur laquelle s'exécutera le serveur: ");
    	String adress = reader.nextLine();
        Scanner newReader = new Scanner(System.in);
        while(!isValidIPAddress(adress)) {
        	System.out.println("L'adresse IP entrée est invalide, veuillez entrez une adresse valide: ");
        	adress = newReader.nextLine();
        }
        return adress;
    }
    
    public static int askForPort() {
    	Scanner portReader = new Scanner(System.in);
        System.out.println("Entrez un port entre 5000 et 5050: ");
        int port = portReader.nextInt();
        Scanner newPortReader = new Scanner(System.in);
        int minValidPort = 5000;
        int maxValidPort = 5050;
        while(port < minValidPort || port > maxValidPort) {
        	System.out.println("Le port entré n'est pas entre 5000 et 5050, veuillez entrez un autre port: ");
        	port = portReader.nextInt();
        }
        return port;
    }
    
    public static boolean isValidIPAddress(String address) {
    	try {
    		InetAddress inetAddress = InetAddress.getByName(address);
    		return inetAddress.getHostAddress().equals(address);
    	} catch (Exception e) {
    		return false;
    	}
    }
}
