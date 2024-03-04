import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Server {
	
	public static void main(String[] args) {
		if (args.length<1) {
			System.err.println("Inserire il numero di porta");
			return;
		}
		int port = Integer.parseInt(args[0]);
		try {
			Archivio arch = new Archivio();
			File f = new File("nomideifile.txt");
			if (f.exists())
				arch.caricaFile();
			else
				f.createNewFile();
			AccettaConnessioni ac = new AccettaConnessioni(port, arch); 
			Thread acThread = new Thread(ac); // crea un thread che accetta connessioni dai client
			acThread.start();
			
			Scanner fromUser = new Scanner(System.in);
			while (true) { // il server puo digitare i comandi info e quit
				System.out.println("Lista Comandi\ninfo - per mostrare il numero di file gestiti dal server, il numero di client attualmente connessi in lettura e scrittura"
						+ "\nquit per uscire\n\n");
				String richiesta = fromUser.nextLine();
				if (richiesta.equalsIgnoreCase("info")) { // con info puo sapere quanti file sono presenti sul server e conoscere quanti utenti sono collegati in lettura o scrittura
					System.out.println("Il numero dei file presenti sul Server e: " + arch.getDocs().size());
					int contalettore = 0;
					int contascrittore = 0;
					for (Documento d : arch.getDocs()) {
						contalettore += d.getFlagLettura();
						contascrittore = d.getFlagScrittura() ? contascrittore+1 : contascrittore;
					}
					System.out.println("Il numero dei Client attualmente connessi in scrittura " + contascrittore);
					System.out.println("Il numero dei Client attualmente connessi in lettura " + contalettore + "\n\n");
				}
				else if (richiesta.equalsIgnoreCase("quit")) { // con il comando quit il server interrompe tutte le connessioni client attive
					ac.close();
					System.out.println("Server chiuso");
					break;
				}
				else {
					System.out.println("Comando non riconosciuto");
				}
			}
		} catch(IOException e) {
			System.out.println("Impossibile creare il file");
		} 
	}

}
