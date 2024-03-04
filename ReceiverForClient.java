import java.io.IOException;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ReceiverForClient implements Runnable{

	private Socket soc;
	
		public ReceiverForClient(Socket s) {
			this.soc = s;
		}
		
			public void run() {
				try {
					Scanner scan = new Scanner(soc.getInputStream());
					while (true) {
						String risposta = scan.nextLine(); // il receiver riceve continuamente e stampa a video
						System.out.println(risposta);
						if (Thread.interrupted()) 
							break;
					}
				}catch(IOException e) {
					System.out.println("Il server non e accessibile. Usa il comando quit per chiudere l'applicazione");
				} catch(NoSuchElementException e1) {
					System.out.println("Il server non e accessibile. Usa il comando quit per chiudere l'applicazione");
				}
				
			}
}
