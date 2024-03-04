import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientHandler implements Runnable{
	
	private Socket s;
	private Archivio a;
	private String utente; // nome dell'utente loggato al server
	
		public ClientHandler(Socket so, Archivio arc) {
			this.s = so;
			this.a = arc;
			this.utente = "";
		}
		
		private String list() { // stampa le info relative ai file sul server
			String risposta = "";
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
			if (this.a.getDocs().size()>0) {
				for (Documento d : this.a.getDocs()) {
					String dum = dtf.format(d.getDataUltimaModifica());
					risposta += "\nNome file : " + d.getNome() + "\nUltima modifica : " + dum + "\nLettori : " + d.getFlagLettura() + "\nScrittore : " + d.getFlagScrittura() + "\n\n";
				}
			}
			else {
				risposta = "Nessun file presente sul server";
			}
			return risposta;
		}
		
		private void create(PrintWriter toClient, Scanner fromClient, String richiesta) throws IOException, InterruptedException {
			String risposta;
			String[] reqArray = richiesta.split(" ");
			String nomeNuovoFile = reqArray[1];
			
			if (this.a.getNomiDeiFile().contains(nomeNuovoFile)) {  //controllo che il file non esista
				risposta = "File gia presente sul server\n\n";
				toClient.println(risposta);
			}
			else {
				Documento tmp = new Documento(nomeNuovoFile);
				this.a.getDocs().add(tmp); //aggiungo il file all'archivio
				this.a.getNomiDeiFile().add(nomeNuovoFile);
				this.a.salvaFile();
				tmp.scrivi(toClient); //entro in modalita di scrittura
				tmp.setAutore(this.utente);
				tmp.setAutoreUltimaModifica(this.utente);
				try {
					toClient.println("File creato con successo\nScrivi il tuo documento\nCon il comando :backspace elimin l'ultima riga\nCon il comando :close chiudi la sessione di scrittura\n\n");
					while (true) { // scriviamo il testo del documento
						risposta = fromClient.nextLine();
						if (risposta.equalsIgnoreCase(":close"))
							break;
						else if (risposta.equals(":backspace")) { // elimina l'ultima riga del testo del file
							Scanner scan = new Scanner(tmp.getTesto());
							ArrayList<String> lines = new ArrayList<>();
							while (scan.hasNextLine())
								lines.add(scan.nextLine());
							tmp.setTesto("");
							int i;
							if (lines.size()>0) {
								for (i=0;i<lines.size()-2;i++)
									tmp.setTesto(tmp.getTesto() + lines.get(i) + "\n");
								tmp.setTesto(tmp.getTesto() + lines.get(i));
								toClient.println("Ultima riga eliminata");
							}
							else 
								toClient.println("Il documento e vuoto");
						}
						else {
						tmp.setTesto(tmp.getTesto() + "\n" + risposta);
						toClient.println("Modifiche effettuate");
						}
					}
				}catch(NoSuchElementException e) { // se l'applicazione venisse chiusa senza salvare le modifiche, questa eccezione le salva
					
				}finally {
					tmp.chiudiScrittura();  // creato e scritto il file chiudo la modalita di scrittura
				}
			}
		}
		
		private void read(PrintWriter toClient, Scanner fromClient, String richiesta) throws InterruptedException, FileNotFoundException {
			String risposta;
			String[] reqArray = richiesta.split(" ");
			String nomeFileDaLeggere = reqArray[1];
			Documento tmp = new Documento();
			boolean flag = false;
			for (Documento d : this.a.getDocs()) {
				if (d.equals(new Documento(nomeFileDaLeggere))) { // mi assicuro che il file esista
					flag = true;
					tmp = d;
					break;
				}
			}
			if (flag) {
				tmp.leggi(toClient); //entro in mod lettura
				risposta = tmp.toString();
				risposta += "\n\nUsa il comando :close per chiudere la sessione di lettura\n";
				toClient.println(risposta);
				try {
					while (true) {
						richiesta = fromClient.nextLine();
						if (richiesta.equals(":close")) {
							tmp.chiudLettura();
							toClient.println("Documento chiuso");
							break;
						}
						else {
							toClient.println("Comando non riconosciuto");
						}
					}
				}catch(NoSuchElementException e) {
					tmp.chiudLettura();
				}
			}
			else {
				risposta = "File non presente sul server";
				toClient.println(risposta);
			}
		}
		
		private void edit(PrintWriter toClient, Scanner fromClient, String richiesta) throws InterruptedException, IOException {
			String risposta;
			String[] reqArray = richiesta.split(" ");
			String nomeFileDaEditare = reqArray[1];
			Documento tmp = new Documento();
			boolean flag = false;
			for (Documento d : this.a.getDocs()) {
				if (d.equals(new Documento(nomeFileDaEditare))) {
					flag = true;
					tmp = d;
					break;
				}
			}
			if (flag) {
				tmp.scrivi(toClient);
				risposta = tmp.toString();
				risposta += "\n\nUsa il comando :close per chiudere la sessione di lettura\nIl comando :backspace elimina l'ultima riga";
				risposta += "\nScrivi le tue modifiche";
				toClient.println(risposta);
				boolean flagEdit = false;
				try {
					while (true) {
						richiesta = fromClient.nextLine();
						if (richiesta.equals(":close")) {
							toClient.println("Testo modificato:\n" + tmp.getTesto() + "\n\nVuoi uscire? y/N");
							richiesta = fromClient.nextLine();
							if (richiesta.equalsIgnoreCase("y")) {
								break;
							}
							else
								toClient.println("Continua a modificare il file\n");
								
						}
						else if (richiesta.equals(":backspace")) {
							Scanner scan = new Scanner(tmp.getTesto());
							ArrayList<String> lines = new ArrayList<>();
							while (scan.hasNextLine())
								lines.add(scan.nextLine());
							tmp.setTesto("");
							int i;
							if (lines.size()>0) {
								for (i=0;i<lines.size()-2;i++)
									tmp.setTesto(tmp.getTesto() + lines.get(i) + "\n");
								tmp.setTesto(tmp.getTesto() + lines.get(i));
								flagEdit = true;
								toClient.println("Ultima riga eliminata");
							}
							else
								toClient.println("Il documento e vuoto");
						}
						else {
							tmp.setTesto(tmp.getTesto() + "\n" + richiesta);
							toClient.println("Modifiche effettuate");
							flagEdit = true;
						}
					}
				}catch(NoSuchElementException e) {
					
				}finally {
					if (flagEdit) {
						tmp.setAutoreUltimaModifica(this.utente);
						tmp.setDataUltimaModifica(LocalDateTime.now());
						toClient.println("Documento chiuso");
					}
					tmp.chiudiScrittura();
				}
			}
			else {
				risposta = "Il file " + nomeFileDaEditare + " non esiste\n\n";
				toClient.println(risposta);
			}
		}
		
		private void rename(PrintWriter toClient, Scanner fromClient, String richiesta) throws IOException, InterruptedException {
			String risposta;
			String[] reqArray = richiesta.split(" ");
			String vecchioNome = reqArray[1];
			String nuovoNome = reqArray[2];
			boolean flag = false;
			for (String s : this.a.getNomiDeiFile()) {
				if (s.equalsIgnoreCase(nuovoNome)) {
					risposta = "Esiste gia un file chiamato " + nuovoNome + "\n";
					toClient.println(risposta);
					return;
				}
				if (s.equalsIgnoreCase(vecchioNome))
					flag = true;
			}
			if (flag) {
				for (Documento d : this.a.getDocs()) {
					if (d.equals(new Documento(vecchioNome))) {
						d.scrivi(toClient);
						d.setNome(nuovoNome);
						this.a.getNomiDeiFile().remove(vecchioNome);
						this.a.getNomiDeiFile().add(nuovoNome);
						this.a.salvaFile();
						d.salvaSuFile();
						File oldFile = new File(vecchioNome);
						oldFile.delete();
						d.chiudiScrittura();
						risposta = "File rinominato correttamente\n\n";
						toClient.println(risposta);
						return;
					}
				}
			}
			else {
				risposta = "Il file " + vecchioNome + " non esiste\n\n";
				toClient.println(risposta);
			}
		}
		
		private void delete(PrintWriter toClient, Scanner fromClient, String richiesta) throws InterruptedException, IOException {
			String risposta;
			String[] reqArray = richiesta.split(" ");
			String nomeFileDaEliminare = reqArray[1];
			boolean flag = false;
			Documento tmp = new Documento();
			for (Documento d : this.a.getDocs()) {
				if (d.equals(new Documento(nomeFileDaEliminare))) {
					flag = true;
					tmp = d;
					break;
				}
			}
			if (flag) {
				tmp.scrivi(toClient);
				this.a.getDocs().remove(tmp); 
				this.a.getNomiDeiFile().remove(nomeFileDaEliminare);
				this.a.salvaFile(); // rimuovo il file dalla lista dei documenti e dalla lista dei nomi dei file
				tmp.eliminaFile();
				risposta = "File eliminato correttamente\n\n";
				toClient.println(risposta);
			}
			else {
				risposta = "Il file " + nomeFileDaEliminare + " non esiste\n\n";
				toClient.println(risposta);
			}
		}
		
		public void run() {
			try {
				PrintWriter toClient = new PrintWriter(this.s.getOutputStream(), true);
				Scanner fromClient = new Scanner(this.s.getInputStream());
				toClient.println("Inserisci il tuo nome"); 
				String nome = fromClient.nextLine();
				this.utente = nome;
				while (true) {
					toClient.println("Lista Comandi\nlist - per vedere i file presenti sul server\ncreate <nomefile> - per creare un nuvo file"
							+ "\nread <nomeFile> - per leggere un file\nedit <nomefile> - per modificare un file"
							+ "\nrename <nomeFileDaModificare> <nuovoNome> - per rinominare un file"
							+ "\ndelete <nomeFile> - per eliminare un file"
							+ "\nquit - per arrestare l'applicazione");
					String richiesta = fromClient.nextLine();
					String[] richiestaArray = richiesta.split(" ");
					String risposta = "";
					if (Thread.interrupted())
						break;
					if (richiesta.equalsIgnoreCase("list")) {
						toClient.println(this.list());
					}
					else if (richiesta.startsWith("create") && richiestaArray.length==2) { // mi assicuro che venga scritto create e il nome del file che voglio creare
						this.create(toClient, fromClient, richiesta);
					}
					else if (richiesta.startsWith("read") && richiestaArray.length==2) {
						this.read(toClient, fromClient, richiesta);
					}
					else if (richiesta.startsWith("edit") && richiestaArray.length==2) {
						this.edit(toClient, fromClient, richiesta);
					}
					else if(richiesta.startsWith("rename") && richiestaArray.length==3) { // mi assicuro che venga scritto rename, il nome del file che voglio rinominare e il nome con cui voglio rinominare il file
						this.rename(toClient, fromClient, richiesta);
					}
					else if(richiesta.startsWith("delete") && richiestaArray.length==2) {
						this.delete(toClient, fromClient, richiesta);
					}
					else if (richiesta.equalsIgnoreCase("quit")) {
						toClient.println("Arrivederci");
						break;
					}
					else {
						risposta = "Comando non riconosciuto";
						toClient.println(risposta);
					}
				}
			} catch (Exception e){
				try {
					PrintWriter toClient = new PrintWriter(this.s.getOutputStream(), true);
					toClient.println("Si e verificato un errore");
				} catch(IOException ee2) {
					System.err.println("Errore");
				}
			}
		}

}
