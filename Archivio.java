import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Archivio {
	
	private ArrayList<Documento> docs;  // lista dei file presenti sul server
	private ArrayList<String> nomiDeiFile; // lista dei nomi dei file
										   // utile per sapere quali file devo caricare da disco
	
		public Archivio() {
			this.docs = new ArrayList<>();
			this.nomiDeiFile = new ArrayList<>();
		}
		
			public ArrayList<Documento> getDocs() {
				return this.docs;
			}
			
			public ArrayList<String> getNomiDeiFile() {
				return this.nomiDeiFile;
			}
			
			public void setDocs(ArrayList<Documento> a) {
				this.docs = a;
			}
			
			public void setNomiDeiFile(ArrayList<String> a) {
				this.nomiDeiFile = a;
			}
			
				public void caricaFile() throws FileNotFoundException{  // legge da nomideifile i file da caricare e li carica sulla lista dei documenti
					Scanner scan = new Scanner(new File("nomideifile.txt"));
					while (scan.hasNextLine()) {
						String tmp = scan.nextLine();
						Documento docTmp = new Documento(tmp);
						docTmp.caricaDaFile();
						this.docs.add(docTmp);
						this.nomiDeiFile.add(tmp);
					}
					scan.close();
				}
				
				public void salvaFile() throws FileNotFoundException{ // aggiorna la lista dei nomi dei file
					PrintWriter pw = new PrintWriter("nomideifile.txt");
					for (Documento d : this.docs) {
						pw.println(d.getNome());
					}
					pw.close();
				}

}
