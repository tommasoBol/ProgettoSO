import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Documento {
	
	private String nome;
	private String autore;
	private String autoreUltimaModifica;
	private String testo;
	private LocalDateTime dataCreazione;
	private LocalDateTime dataUltimaModifica;
	private int flagLettura;
	private boolean flagScrittura;
	
		public Documento() {
			this.nome = "";
			this.autore = "";
			this.autoreUltimaModifica = "";
			this.testo = "";
			this.dataCreazione = LocalDateTime.now();
			this.dataUltimaModifica = LocalDateTime.now();
			this.flagLettura = 0;
			this.flagScrittura = false;
		}
		
		public Documento(String n) {
			this.nome = n;
			this.autore = "";
			this.autoreUltimaModifica = "";
			this.testo = "";
			this.dataCreazione = LocalDateTime.now();
			this.dataUltimaModifica = LocalDateTime.now();
			this.flagLettura = 0;
			this.flagScrittura = false;
		}
		
		public Documento(String n, String a) {
			this.nome = n;
			this.autore = a;
			this.autoreUltimaModifica = a;
			this.testo = "";
			this.dataCreazione = LocalDateTime.now();
			this.dataUltimaModifica = LocalDateTime.now();
			this.flagLettura = 0;
			this.flagScrittura = false;
		}
		
		public Documento(String n, String a, String t) {
			this.nome = n;
			this.autore = a;
			this.autoreUltimaModifica = a;
			this.testo = t;
			this.dataCreazione = LocalDateTime.now();
			this.dataUltimaModifica = LocalDateTime.now();
			this.flagLettura = 0;
			this.flagScrittura = false;
		}
		
		
			public synchronized String getNome() {
				return this.nome;
			}
			
			public synchronized String getAutore() {
				return this.autore;
			}
			
			public synchronized String getAutoreUltimaModifica() {
				return this.autoreUltimaModifica;
			}
			
			public synchronized String getTesto() {
				return this.testo;
			}
			
			public synchronized LocalDateTime getDataCreazione() {
				return this.dataCreazione;
			}
			
			public synchronized LocalDateTime getDataUltimaModifica() {
				return this.dataUltimaModifica;
			}
			
			public synchronized int getFlagLettura() {
				return this.flagLettura;
			}
			
			public synchronized boolean getFlagScrittura() {
				return this.flagScrittura;
			} 
			
			public synchronized void setNome(String n) {
				this.nome = n;
			}
			
			public synchronized void setAutore(String a) {
				this.autore = a;
			}
			
			public synchronized void setAutoreUltimaModifica(String a) {
				this.autoreUltimaModifica = a;
			}
			
			public synchronized void setTesto(String t) {
				this.testo = t;
			}
			
			public synchronized void setDataCreazione(LocalDateTime l) {
				this.dataCreazione = l;
			}
			
			public synchronized void setDataUltimaModifica(LocalDateTime l) {
				this.dataUltimaModifica = l;
			}
			
			public synchronized void setFlagLettura(int i) {
				this.flagLettura = i;
			}
			
			public synchronized void setFlagScrittura(boolean b) {
				this.flagScrittura = b;
			}

			public synchronized void caricaDaFile() throws FileNotFoundException{ // permette di caricare dal disco i file 
				Scanner scan = new Scanner(new File(this.nome));
				this.setAutore(scan.nextLine());
				this.setAutoreUltimaModifica(scan.nextLine());
				String dc = scan.nextLine();
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
				this.setDataCreazione(LocalDateTime.parse(dc, dtf));
				String dum = scan.nextLine();
				this.setDataUltimaModifica(LocalDateTime.parse(dum, dtf));
				String t = "";
				while (scan.hasNextLine())
					t += scan.nextLine() + "\n";
				this.setTesto(t);
			}
			
			public synchronized void salvaSuFile() throws IOException{ // permette di salvare su disco i file
				FileWriter fw = new FileWriter(this.nome);
				PrintWriter pw = new PrintWriter(fw);
				pw.println(this.getAutore());
				pw.println(this.getAutoreUltimaModifica());
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
				pw.println(dtf.format(this.getDataCreazione()));
				pw.println(dtf.format(this.getDataUltimaModifica()));
				pw.println(this.getTesto());
				pw.close();
			}
			
			public synchronized void leggi(PrintWriter toClient) throws InterruptedException, FileNotFoundException{ //apre la sessione di lettura
				while (this.flagScrittura) {
					toClient.println("Attendi...");
					wait();
				}
				this.flagLettura++;
			}
			
			public synchronized void chiudLettura() { // chiude la sessione di lettura
				this.flagLettura--;
				notifyAll();
			}
			
			public synchronized void scrivi(PrintWriter toClient) throws InterruptedException{ // apre la sessione di scrittura
				while (this.flagScrittura || this.flagLettura>0) {
					toClient.println("Attendi...");
					wait();
				}
				this.flagScrittura = true;
			}
			
			public synchronized void chiudiScrittura() throws IOException{ // chiude la sessione di scrittura
				this.salvaSuFile();
				this.flagScrittura = false;
				notifyAll();
			}
			
			public void eliminaFile() {
				File f = new File(this.nome);
				f.delete();
			}
			
			public synchronized boolean equals(Documento altro) {
				boolean flag = false;
				if (altro!=null && altro instanceof Documento) {
					Documento tmp = (Documento)altro;
					flag = this.nome.equalsIgnoreCase(tmp.getNome());
				}
				return flag;
			}
			
			public String toString() {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
				String dc = dtf.format(this.dataCreazione);
				String dum = dtf.format(this.dataUltimaModifica);
				String s = "Nome file : " + this.nome + "\n" + "Autore : " + this.autore + "\n" + "Autore ultima modifica : " + this.autoreUltimaModifica +
						"\n" + "Data Creazione : " + dc + "\n" + "Data ultima modifica : " + dum + "\n" + "Testo : " + this.testo;
				return s;
			}

			

}
