package server;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
	
	private static ServerSocket Listener;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static List<String> messageHistory = new ArrayList<>();

    public static void main(String[] args)throws Exception {
        int clientNumber=0;
        
        Scanner addressScanner = new Scanner(System.in);
        Scanner portScanner = new Scanner(System.in);
        
        String serverAddress = askForIPAddress(addressScanner);
        int serverPort = askForPort(portScanner);
        
        Listener = new ServerSocket();
        Listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);
        Listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
        
        try {
            while(true) {
                Socket client = Listener.accept();
                ClientHandler clientHandler = new ClientHandler(client, clientNumber++, clients, messageHistory);
                clients.add(clientHandler);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (Exception e) {
            System.out.println("Erreur démarrage du serveur: " + e.getMessage());
        } finally {
            Listener.close();
            addressScanner.close();
            portScanner.close();
        }
    }

    public static String askForIPAddress(Scanner addressScanner) {
        System.out.println("Entrez une adresse IP valide sur laquelle s'exécutera le serveur: ");
        String address = addressScanner.nextLine();
        
        while (!isValidIPAddress(address)) {
            System.out.println("L'adresse IP entrée est invalide, veuillez entrez une adresse valide: ");
            address = addressScanner.nextLine();
        }
        return address;
    }

    public static int askForPort(Scanner portScanner) {
        System.out.println("Entrez un port entre 5000 et 5050: ");
        while (!portScanner.hasNextInt()) {
            System.out.println("Veuillez enter un format de port valide:");
            portScanner.next();
        }
        
        int port = portScanner.nextInt();
        int minValidPort = 5000;
        int maxValidPort = 5050;
        
        while (port < minValidPort || port > maxValidPort) {
            System.out.println("Le port entré n'est pas entre 5000 et 5050, veuillez entrez un autre port: ");
            port = portScanner.nextInt();
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
 