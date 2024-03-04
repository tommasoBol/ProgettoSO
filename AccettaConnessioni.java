import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class AccettaConnessioni implements Runnable {

	private ServerSocket listener;
	private Archivio a;
	private ArrayList<Socket> sock; //liste che servono per tenere traccia dei client connessi e disconnetterli quando si chiude il server
	private ArrayList<Thread> Th;
	
		public AccettaConnessioni(int port, Archivio pa) {
			try {
				this.listener = new ServerSocket(port);
				this.a = pa;
				this.sock = new ArrayList<>();
				this.Th = new ArrayList<>();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			try {
				while (true) {
					Socket s = listener.accept();
					Thread clientHandlerThread = new Thread(new ClientHandler(s, a));
					clientHandlerThread.start(); // creazione di un'istanza di clientHandler che permettera al client di dare comandi
					sock.add(s);
					Th.add(clientHandlerThread);
				}
			}catch(IOException e) {
			}
		}
		
		public void close() {
			int c=0;
			try {
				listener.close();
				for(Socket s : sock) {
						Th.get(c).interrupt(); // interruzione dei vari clientHandler
						s.close();  //chiusura della socket
						c++;
				}
			}catch(IOException e) {
			}
		}
}
