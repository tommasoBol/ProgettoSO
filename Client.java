import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	
	public static void main(String[] args) {
		if (args.length<2) {
			System.err.println("Specificare indirizzo e porta a cui connettersi");
			return;
		}
		String ind = args[0];
		int port = Integer.parseInt(args[1]);
		try {
			Socket s = new Socket(ind, port); // connessione al server
			System.out.println("Connesso");
			PrintWriter toServer = new PrintWriter(s.getOutputStream(), true);
			Scanner userInput = new Scanner(System.in);
			Thread rfcThread = new Thread(new ReceiverForClient(s)); //il client puo ricevere piu stringhe alla volta. viene quindi create un receiver che permette di ricevere stringhe dal server senza dover prima invarne una
			rfcThread.start();
			while (true) {
				String richiesta = userInput.nextLine();
				toServer.println(richiesta);
				if (richiesta.equalsIgnoreCase("quit")) {
					rfcThread.interrupt();
					break;
				}
			}
			userInput.close();
		} catch(UnknownHostException e) {
			System.out.println("L'host specificato non e raggiungibile");
		} catch(IOException e1) {
			System.out.println("Non e stato possibile creare la socket");
		}
	}

}
