package it.uniroma2.ing.inf.progetto;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.FileReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.io.FileWriter;

/**
 * Copyright (C) 2020 Simone Mesiano Laureani (a.k.a. Simesi)
 *    
 *    This file is part of the contents developed for the course
 * 	  ISW2 (A.Y. 2019-2020) at Universit√† di Tor Vergata in Rome.
 *
 *    This is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as 
 *    published by the Free Software Foundation, either version 3 of the 
 *    License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with this source.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author simone
 *
 */
public class Main {

	private static String projectName="BOOKKEEPER";//"OPENJPA";
	private static String projectNameGit="apache/bookkeeper.git";//"apache/openjpa.git";



	public static Map<LocalDateTime, String> releaseNames;
	public static Map<LocalDateTime, String> releaseID;
	public static List<LocalDateTime> releases;
	private static Map<String,LocalDateTime> fromReleaseIndexToDate=new HashMap<>();
	private static Map<String,LocalDateTime> fromFileNameToDateOfCreation=new HashMap<>();
	private static List<String> filepathsOfTheCurrentRelease;
	private static List<LineOfDataset> arrayOfEntryOfDataset;
	private static List<TicketTakenFromJIRA> tickets;
	private static List<TicketTakenFromJIRA> ticketsWithoutAV;

	private static boolean discard=false;
	private static boolean calculatingIncrementalMetrics=false;
	private static boolean calculatingNotIncrementalMetrics=false;
	private static boolean calculatingNAuth=false;
	private static boolean gettingLastCommit=false;
	private static boolean ticketWithAV= false;
	private static boolean ticketWithoutAV= false;

	private static final String ECHO = "echo "; 
	private static final String VERSIONS = "versions";
	private static final String FIELDS ="fields";
	private static final String FORMATNUMSTAT= " --format= --numstat -- ";
	private static final String URLJIRA="https://issues.apache.org/jira/rest/api/2/search?jql=project=%22";
	private static final String PIECE_OF_URL_JIRA="%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR";
	private static final String SLASH="\\";
	private static final String MAX_RESULT="&maxResults=";
	private static final String ISSUES= "issues";
	private static final String TOTAL= "total"; 
	private static final String FORMAT_DATE= "yyyy-MM-dd";
	private static final String RELEASE_DATE="releaseDate";
	private static final int YEARS_INTERVAL=14;
	//--------------------------



	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		JSONObject json;
		InputStream is = new URL(url).openStream();
		try(BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String jsonText = readAll(rd);
			json = new JSONObject(jsonText);

		} finally {
			is.close();
		}
		return json;
	}

	//questo metodo fa il 'git clone' della repository (necessario per poter ricavare successivamente il log dei commit)   
	private static void gitClone() throws IOException, InterruptedException {

		Path directory;
		String originUrl = "https://github.com/"+projectNameGit;


		directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);

		runCommand(directory.getParent(), "git", "clone", originUrl, directory.getFileName().toString());

	}


	public static void runCommand(Path directory, String... command) throws IOException, InterruptedException {

		Objects.requireNonNull(directory, "directory Ë NULL");

		if (!Files.exists(directory)) {

			throw new SecurityException("can't run command in non-existing directory '" + directory + "'");

		}

		ProcessBuilder pb = new ProcessBuilder()

				.command(command)

				.directory(directory.toFile());

		runProcAndWait(pb);

	}

	private static void runProcAndWait(ProcessBuilder pb) throws IOException, InterruptedException {
		//lancio un nuovo processo che invocher‡ il comando 'command',
		//nella working directory fornita. 
		Process p = pb.start();

		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());

		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());

		outputGobbler.start();

		errorGobbler.start();

		int exit = p.waitFor();

		errorGobbler.join();

		outputGobbler.join();

		if (exit != 0) {

			throw new AssertionError(String.format("runCommand returned %d", exit));

		}
	}

	public static void runCommandOnShell(Path directory, String command) throws IOException, InterruptedException {

		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c","E: && cd "+directory.toString()+" && "+command);	

		runProcAndWait(pb);

	}




	private static class StreamGobbler extends Thread {

		private final InputStream is;

		private StreamGobbler(InputStream is) {

			this.is = is;

		}

		@Override

		public void run() {

			try (BufferedReader br = new BufferedReader(new InputStreamReader(is));) {

				String line;



				while ((line = br.readLine()) != null) {

					if (discard) {
						System.out.print("linea del checkout: "+line);
					}

					else if (calculatingIncrementalMetrics) {

						calculateLOC(line,br);
					}

					else if (calculatingNotIncrementalMetrics) {
						calculatingNotIncrementalMetrics(line,br);
					}

					else if (calculatingNAuth) {

						calculatingNauth(line,br);

					}
					else if (gettingLastCommit) {

						gettingLastCommit(line,br);

					}


				}

			} catch (IOException ioe) {

				ioe.printStackTrace();

			}

		}


		private void gettingLastCommit(String line, BufferedReader br) throws IOException {

			String nextLine;
			String filename="";
			ArrayList<String> filesAffected = new ArrayList<>();
			line=line.trim();
			String[] tokens = line.split("\\s+");
			String bug= tokens[0];

			//ora prendo la data dell'ultimo commit
			nextLine =br.readLine();

			//non c'Ë un commit con questo id quindi non scrivo nulla
			if(nextLine==null) {
				return;
			}
			nextLine=nextLine.trim();

			//prendo anno mese e giorno dell'ultimo commit
			LocalDate date =LocalDate.parse(nextLine.substring(0, 10));
			//ora prendo i file modificati aventi quel bug nel commento del commit 
			nextLine =br.readLine();

			while(nextLine != null) {
				nextLine=nextLine.trim();
				filename=nextLine;
				//potrebbero venire introdotti delle righe vuote o con solo '*'
				if(!filename.contains("*")&&!filename.contains(" ")) {
					filesAffected.add(filename);
				}							
				nextLine =br.readLine();
			}

			if(ticketWithAV) {
				setFixedVersion(bug,date,filesAffected);
			}
			else if(ticketWithoutAV) {
				setFixVersionWithoutAv(bug,date,filesAffected);
			}


		}

		private void setFixVersionWithoutAv(String bug, LocalDate date, ArrayList<String> filesAffected) {
			String fixedVers;
			for (int i = 0; i < tickets.size(); i++) {
				if(ticketsWithoutAV.get(i).getKey().equals(bug)) {
					//se Ë la prima versione
					if (date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(1)))){
						fixedVers= String.valueOf(2);
					}
					else {
						fixedVers=iterateForFixVersion(date);

					}
					ticketsWithoutAV.get(i).setFixedVersion(fixedVers);
					ticketsWithoutAV.get(i).setFilenames(filesAffected);
					break;

				}
			}

		}

		private void setFixedVersion(String bug,LocalDate date,ArrayList<String> filesAffected) {
			String fixedVers;
			for (int i = 0; i < tickets.size(); i++) {
				if(tickets.get(i).getKey().equals(bug)) {
					//se Ë la prima versione
					if (date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(1)))){
						fixedVers= String.valueOf(2);
					}
					else {
						fixedVers=iterateForFixVersion(date);
					}

					tickets.get(i).setFixedVersion(fixedVers);
					tickets.get(i).setFilenames(filesAffected);
					break;
				}
			}

		}

		private String iterateForFixVersion(LocalDate date) {

			for(int a=1;a<=fromReleaseIndexToDate.size();a++) {

				if(a==fromReleaseIndexToDate.size()) {
					return String.valueOf(a);

				}

				if ((date.atStartOfDay().isAfter(fromReleaseIndexToDate.get(String.valueOf(a)))
						&&(date.atStartOfDay().isBefore(fromReleaseIndexToDate.get(String.valueOf(a+1)))||
								(date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(a+1))))))) {
					return String.valueOf(a+2);

				}
			}
			return String.valueOf(fromReleaseIndexToDate.size());
		}

		private void calculatingNauth(String line, BufferedReader br) throws IOException {
			String nextLine;
			int version;
			String filename = "";
			line=line.trim();
			int nAuth=0;
			String[] tokens = line.split("\\s+");

			version=Integer.parseInt(tokens[0]);
			filename=tokens[1];

			nextLine =br.readLine();

			while(nextLine != null) {
				nAuth++;
				nextLine =br.readLine();
			}
			

			//cerchiamo l'oggetto giusto su cui scrivere
			for (int i = 0; i < arrayOfEntryOfDataset.size(); i++) { 
				if((arrayOfEntryOfDataset.get(i).getVersion()==version) && (arrayOfEntryOfDataset.get(i).getFileName().equals(filename))) {
					arrayOfEntryOfDataset.get(i).setNauth(nAuth);

					break;
				}

			}

		}

		private void calculateLOC(String line, BufferedReader br) throws IOException {
			String nextLine;
			String filename= "";
			int addedLines=0;
			int deletedLines=0;
			String version;
			int sumOfRealDeletedLOC=0;
			int realDeletedLOC=0;
			int maxChurn=0;
			line=line.trim();
			//"one or more whitespaces = \\s+"
			String[] tokens = line.split("\\s+");

			//operazioni per il primo output che Ë il numero di versione------------------------------
			version=tokens[0];
			//--------------------------------------------------------- 

			//lettura prox riga					      					      
			nextLine =br.readLine();


			while (nextLine != null) {
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");
				//si prende il primo valore (che sar‡ il numero di linee di codice aggiunte in un commit)
				addedLines=addedLines+Integer.parseInt(tokens[0]);
				//si prende il secondo valore (che sar‡ il numero di linee di codice rimosse in un commit)
				deletedLines=deletedLines+Integer.parseInt(tokens[1]);
				
				nextLine =br.readLine();
			}
			
			//abbiamo raggiunto la fine (la prima riga ha il numero di versione)
			LineOfDataset l=new LineOfDataset(Integer.parseInt(version),filename); //id versione, filename
			l.setSize(addedLines-deletedLines);//set del valore di LOC

			arrayOfEntryOfDataset.add(l);

		}

		private void calculatingNotIncrementalMetrics(String line,BufferedReader br) throws IOException {
			String version;
			ArrayList<Integer> realAddedLinesOverCommit=new ArrayList<>();
			String nextLine;
			int total=0;
			int addedLines=0;
			int deletedLines=0;
			int maxAddedlines=0;
			int average=0;
			String filename="";
			int numberOfCommit=0;
			int realDeletedLOC=0;
			int maxChurn=0;
			int avgChurn=0;
			int sumOfRealDeletedLOC=0;
			int realAddedLinesOfCommit=0;

			line=line.trim();
			String[] tokens = line.split("\\s+");

			//operazione per il primo output che Ë il numero di versione------------------------------
			version=tokens[0];
			//--------------------------------------------------------- 

			nextLine =br.readLine();

			while(nextLine != null) {
				//per NR
				numberOfCommit++;
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");
				
				//set of a local variable
				realAddedLinesOfCommit=Integer.parseInt(tokens[0])-Integer.parseInt(tokens[1]);
				
				//per il Max_LOC_Added
				maxAddedlines=Math.max(realAddedLinesOfCommit, maxAddedlines);
				
				
				//solo se le righe inserite sono maggiori di quelle cancellate
				if(realAddedLinesOfCommit>0) {
				//per il AVG_LOC_Added
				realAddedLinesOverCommit.add(realAddedLinesOfCommit);
				}
				//si prende il primo valore (che sar‡ il numero di linee di codice aggiunte in un commit)
				addedLines=addedLines+Integer.parseInt(tokens[0]);
				//si prende il secondo valore (che sar‡ il numero di linee di codice rimosse in un commit)
				deletedLines=deletedLines+Integer.parseInt(tokens[1]);
				filename=tokens[2];
				
				//per CHURN (togliamo i commit che hanno solo modificato il codice e quindi risultano +1 sia in linee aggiunte che in quelle eliminate)
				if((Integer.parseInt(tokens[0])-Integer.parseInt(tokens[1]))<0){
					realDeletedLOC=Integer.parseInt(tokens[1])-Integer.parseInt(tokens[0]);
					sumOfRealDeletedLOC= sumOfRealDeletedLOC + realDeletedLOC;
				}
				else {
					realDeletedLOC=0;
				}
				//per MAX_CHURN
				maxChurn=Math.max(realAddedLinesOfCommit, maxChurn);
				
				realAddedLinesOfCommit=0;
				
				nextLine =br.readLine();
			}
			
			avgChurn=Math.floorDiv(Math.max((addedLines-deletedLines),0),numberOfCommit);
			
			
			//abbiamo raggiunto la fine
			
			calculateNotIncrementalMetricsPart2(version,filename,addedLines+deletedLines,
					maxAddedlines,realAddedLinesOverCommit,total,average,numberOfCommit,
					addedLines-deletedLines,maxChurn,avgChurn);

		}

		private void calculateNotIncrementalMetricsPart2(String version,String filename,
				int lines,int maxAddedlines,ArrayList<Integer> addedLinesForEveryRevision
				,int total,int average, int numOfCommit, int churn,int maxChurn,int avgChurn) {
			//si itera nell'arraylist per cercare l'oggetto giusto da scrivere 
			for (int i = 0; i < arrayOfEntryOfDataset.size(); i++) {  
				if((arrayOfEntryOfDataset.get(i).getVersion()==Integer.parseInt(version))&& 
						arrayOfEntryOfDataset.get(i).getFileName().equals(filename)) {
					arrayOfEntryOfDataset.get(i).setLOCTouched(lines);
					arrayOfEntryOfDataset.get(i).setMAXLOCAdded(maxAddedlines);
					arrayOfEntryOfDataset.get(i).setNR(numOfCommit);
					arrayOfEntryOfDataset.get(i).setChurn(Math.max(churn, 0));
					arrayOfEntryOfDataset.get(i).setChurn(maxChurn);
					arrayOfEntryOfDataset.get(i).setAVGChurn(avgChurn);

					//per il AVG_LOC_Added (Ë fatto solo sulle linee inserite)-----------------------
					for(int n=0; n<addedLinesForEveryRevision.size(); n++){
						if(addedLinesForEveryRevision.get(n) >= 0) {
							total = total + addedLinesForEveryRevision.get(n);
						}
					}
					if (total>=0) {
						average = Math.floorDiv(addedLinesForEveryRevision.size(),total);
					}
					//--------------------------------------------------
					arrayOfEntryOfDataset.get(i).setAVGLOCAdded(average);
					arrayOfEntryOfDataset.get(i).setLOCAdded(total);

					break;
				}
			}
		}


	}

	/*
	  Java isn't able to delete folders with data in it. We have to delete
	     all files before deleting the directory.This utility class is used to delete 
	  folders recursively in java.*/

	public static void recursiveDelete(File file) {
		//to end the recursive loop
		if (!file.exists())
			return;

		//if directory exists, go inside and call recursively
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				//call recursively
				recursiveDelete(f);
			}
		}
		//call delete to delete files and empty directory
		try {
			//disabling Read Only property of the file to be deleted resolves the issue triggered by Files.delete
			Files.setAttribute(file.toPath(), "dos:readonly", false);
			Files.deleteIfExists(file.toPath());
		} catch (IOException| InvalidPathException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}


	//------------------------------------------
	//Metodi per Deliverable 2 Milestone 1

	public static void addRelease(String strDate, String name, String id) {
		LocalDate date = LocalDate.parse(strDate);
		LocalDateTime dateTime = date.atStartOfDay();
		if (!releases.contains(dateTime))
			releases.add(dateTime);
		releaseNames.put(dateTime, name);
		releaseID.put(dateTime, id);
	}




	//Search and list of all file java in the repository at the given release
	public static void searchFileJava( final File folder, List<String> result) {
		String fileRenamed;
		for (final File f : folder.listFiles()) {

			if (f.isDirectory()) {
				searchFileJava(f, result);
			}

			//si prendono solo i file java
			if (f.isFile()&&f.getName().matches(".*\\.java")) {


				//discard of the local prefix to the file name (that depends to this program)

				fileRenamed=f.getAbsolutePath().replace((Paths.get(new File("").getAbsolutePath()+SLASH+projectName)+SLASH).toString(), "");

				//ci si costruisce una HashMap con la data di creazione dei file java

				//il comando git log prende percorsi con la '/'
				fileRenamed= fileRenamed.replace("\\", "/");
				result.add(fileRenamed);

			}
		}
	}


	//data una versione/release e un filename si ricava il LOC/size del file
	private static void getLOCMetric(String filename, Integer i) {



		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		try {

			command = ECHO+i+" && git log --until="+fromReleaseIndexToDate.get(String.valueOf(i))+FORMATNUMSTAT+filename;	

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

	}


	private static void getNotIncrementalMetrics(String filename, Integer i) {
		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		try {
			if(i>1) {
				command = ECHO+i+" && git log --since="+fromReleaseIndexToDate.get(String.valueOf(i-1))+" --until="+fromReleaseIndexToDate.get(String.valueOf(i))	+FORMATNUMSTAT+filename;	
			}
			else {  //prima release
				command = ECHO+i+" && git log --until="+fromReleaseIndexToDate.get(String.valueOf(i))+FORMATNUMSTAT+filename;	
			}
			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}


	}

	private static void getNumberOfAuthors(String filename, Integer i) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		try {

			command = ECHO+i+" "+filename+" && git shortlog -sn --all --until="+fromReleaseIndexToDate.get(String.valueOf(i))	+" "+filename;	

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private static void getLastCommitOfBug(String id) throws IOException, InterruptedException{

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		try {    //ritorna id bug, data dell'ultimo commit con quel bug nel commento e una lista di tutti i file java modificati
			command= ECHO+id+" && git log --grep="+id+": -1 --date=short --pretty=format:%cd &&"
					+ " git log --graph --pretty=format:%d --name-only --grep="+id+": -- *.java";

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	//metodo che computa P per la versione passata in ingresso con il metodo incrementale come "average among the defects fixed in previous versions"
	private static int computeP(int i) {
		int validBugsFixed=0;
		int p=0;

		//caso limite della prima versione
		if(i==1) {
			return 1;
		}

		// //ora si calcola P con il metodo proportion
		for (TicketTakenFromJIRA ticket : tickets) {


			//prendiamo solo i difetti fixed delle versioni passate
			if(Integer.parseInt(ticket.getFixedVersion())>=i) {
				continue;
			}
			validBugsFixed++;
			p+=(Integer.parseInt(ticket.getFixedVersion())-Integer.parseInt(ticket.getAffectedVersion()))
					/(Integer.parseInt(ticket.getFixedVersion())-Integer.parseInt(ticket.getCreatedVersion()));
		}


		if(validBugsFixed!=0) {
			p=p/validBugsFixed;
		}
		else {
			p=1;
		}

		if(p==0) {
			return 1;
		}
		return p;
	}

	//--------------------------------------






	private static void writePieceOfCsv(FileWriter fileWriter,String[] entry) throws IOException {

		for(int n=0;n<=12;n++) {

			if(n==1) {
				continue;
			}

			fileWriter.append(entry[n]);
			if(n==12) {
				fileWriter.append("\n");
				return;
			}

			fileWriter.append(",");

		}

	}

	private static void writeResult() {
		String outname = projectName + " Deliverable 2 Milestone 1.csv";
		//Name of CSV for output

		try (FileWriter fileWriter = new FileWriter(outname)){



			fileWriter.append("Version,File Name,Size(LOC), LOC_Touched,NR,NAuth,LOC_Added,MAX_LOC_Added,AVG_LOC_Added,Churn,MAX_Churn,AVG_Churn,Buggy");
			fileWriter.append("\n");
			for ( LineOfDataset line : arrayOfEntryOfDataset) {

				fileWriter.append(String.valueOf(line.getVersion()));
				fileWriter.append(",");
				fileWriter.append(line.getFileName());
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getSize()));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getLOCTouched()));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getNR()));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getNauth()));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getLocadded()));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getMAXLOCAdded()));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getAVGLOCAdded()));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getChurn()));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getMaxChurn()));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(line.getAVGChurn()));
				fileWriter.append(",");
				fileWriter.append(line.getBuggy());
				fileWriter.append("\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private static void setBuggyWithoutAV() {

		int i;

		int predictedInjectedVersion;
		int p;
		//set della bugginess per i file dei ticket presi da JIRA
		for (TicketTakenFromJIRA tick : ticketsWithoutAV) {

			p=computeP(Integer.parseInt(tick.getFixedVersion()));
			predictedInjectedVersion=(Integer.parseInt(tick.getFixedVersion())-(Integer.parseInt(tick.getFixedVersion())
					-Integer.parseInt(tick.getCreatedVersion()))*p);

			//per ogni file ritenuto buggy da quel ticket
			for (String file : tick.getFilenames()) {
				//cerca la inea giusta da scrivere
				for (i=0;i< arrayOfEntryOfDataset.size();i++) {
					if (arrayOfEntryOfDataset.get(i).getFileName().equals(file)
							&&(arrayOfEntryOfDataset.get(i).getVersion()<Integer.parseInt(tick.getFixedVersion()))
							&&arrayOfEntryOfDataset.get(i).getVersion()>= predictedInjectedVersion) {

						arrayOfEntryOfDataset.get(i).setBuggy("YES");

					}

				}
			}

		}

		ticketsWithoutAV.clear();
		tickets.clear();


	}

	private static void checkFixedVersWithoutAV() throws IOException {

		//ora si calcola la fixed version
		gettingLastCommit=true;
		ticketWithoutAV=true;
		for (TicketTakenFromJIRA ticket : ticketsWithoutAV) {
			//ora si prendono i commit su GIT associati a quei bug per ottenere la fixed version
			try {

				getLastCommitOfBug(ticket.getKey());

			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}

		}

		gettingLastCommit=false;
		ticketWithoutAV=false;

		//rimuovo ticket senza file java o IV e OV inconsistenti
		ArrayList<TicketTakenFromJIRA> ticketsWithoutAVToDelete = new ArrayList<>();	

		for (TicketTakenFromJIRA ticket : ticketsWithoutAV) {
			if((ticket.getCreatedVersion()==null)
					||(ticket.getFixedVersion()==null)||ticket.getFilenames().size()==0){

				ticketsWithoutAVToDelete.add(ticket);
			}
		}


		//si eliminano i ticket selezionati prima
		for (TicketTakenFromJIRA ticket : ticketsWithoutAVToDelete) {
			ticketsWithoutAV.remove(ticket);
		}
		ticketsWithoutAVToDelete.clear();


	}

	private static void startToGetCreatedVersWithoutAV() throws JSONException, IOException {
		Integer j=0;
		Integer total=1;
		JSONObject json ;
		JSONArray issues;
		Integer i=0;

		//ora prendiamo da jira tutti i ticket di bug chiusi SENZA affected version

		//inizio operazioni per calcolo bugginess
		ticketsWithoutAV=new ArrayList<>();

		//Get JSON API for ticket with Type == ìBugî AND (status == ìClosedî OR status == ìResolvedî) AND Resolution == ìFixedî AND affected version = null in the project
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + 1000;

			//%20 = spazio                      %22=virgolette
			//Si ricavano tutti i ticket di tipo bug nello stato di risolto o chiuso, con risoluzione "fixed" e SENZA affected version.
			String url = URLJIRA+ projectName + PIECE_OF_URL_JIRA+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22AND%22affectedVersion%22is%20EMPTY"
					+ "%20AND%20updated%20%20%3E%20endOfYear(-"+YEARS_INTERVAL+")"
					+ "&fields=key,created&startAt="
					+ i.toString() + MAX_RESULT + j.toString();


			json = readJsonFromUrl(url);
			issues = json.getJSONArray(ISSUES);
			//ci si prende il numero totale di ticket recuperati
			total = json.getInt(TOTAL);

			DateTimeFormatter format = DateTimeFormatter.ofPattern(FORMAT_DATE);

			String createdVers=null;
			LocalDate date;
			TicketTakenFromJIRA tick;

			// si itera sul numero di ticket
			for (; i < total && i < j; i++) {

				String key = issues.getJSONObject(i%1000).get("key").toString();
				String createdDate= issues.getJSONObject(i%1000).getJSONObject(FIELDS).get("created").toString().substring(0,10);



				date = LocalDate.parse(createdDate,format);

				//se Ë la prima versione
				if (date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(1)))){
					createdVers= String.valueOf(1);
				}
				else {
					createdVers= finalizeGetCreatedVersionWithoutAv(date);
				}

				tick= new TicketTakenFromJIRA(key, createdVers, null);
				ticketsWithoutAV.add(tick);

			}  
		} while (i < total);


	}

	private static String finalizeGetCreatedVersionWithoutAv(LocalDate date) {

		for(int a=1;a<=fromReleaseIndexToDate.size();a++) {

			//abbiamo raggiunto nel for l'ultima release
			if(a==fromReleaseIndexToDate.size()) {
				return  String.valueOf(a);

			}

			else if ((date.atStartOfDay().isAfter(fromReleaseIndexToDate.get(String.valueOf(a)))
					&&(date.atStartOfDay().isBefore(fromReleaseIndexToDate.get(String.valueOf(a+1)))||
							(date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(a+1))))))) {
				return  String.valueOf(a+1);


			}
		}
		return  String.valueOf(fromReleaseIndexToDate.size());
	}

	private static void setBuggy() {

		int i;
		gettingLastCommit=false;
		ticketWithAV=false;

		checkTicket();



		//set della bugginess per i file dei ticket presi da JIRA
		for (TicketTakenFromJIRA tick : tickets) {
			//per ogni file ritenuto buggy da quel ticket
			for (String file : tick.getFilenames()) {
				//cerca la inea giusta da scrivere
				for (i=0;i< arrayOfEntryOfDataset.size();i++) {
					if (arrayOfEntryOfDataset.get(i).getFileName().equals(file)
							&&(arrayOfEntryOfDataset.get(i).getVersion()<Integer.parseInt(tick.getFixedVersion()))
							&&arrayOfEntryOfDataset.get(i).getVersion()>= Integer.parseInt(tick.getAffectedVersion())) {

						arrayOfEntryOfDataset.get(i).setBuggy("YES");

					}

				}
			}

		}

	}

	private static void checkTicket() {
		//rimuovo ticket senza file java o AV,IV e OV inconsistenti
		ArrayList<TicketTakenFromJIRA> ticketsToDelete = new ArrayList<>();	

		for (TicketTakenFromJIRA ticket : tickets) {
			if((ticket.getAffectedVersion()==null)||(ticket.getCreatedVersion()==null)
					||(ticket.getFixedVersion()==null)||ticket.getFilenames().size()==0){

				ticketsToDelete.add(ticket);
			}
		}

		//si eliminano i ticket selezionati prima
		for (TicketTakenFromJIRA ticket : ticketsToDelete) {
			tickets.remove(ticket);
		}
		ticketsToDelete.clear();

	}

	private static void startToGetFixedVersWithAV() throws IOException {
		gettingLastCommit=true;
		ticketWithAV=true;

		for (TicketTakenFromJIRA ticket : tickets) {
			//ora si prendono i commit su GIT associati a quei bug per ottenere la fixed version
			try {

				getLastCommitOfBug(ticket.getKey());

			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}

		}

	}

	private static void startToCalculateBugginess() throws JSONException, IOException {

		//inizio operazioni per calcolo bugginess
		tickets=new ArrayList<>();
		Integer j=0;
		Integer total=1;
		JSONObject json ;
		JSONArray issues;
		Integer i=0;
		//Get JSON API for ticket with Type == ìBugî AND (status == ìClosedî OR status == ìResolvedî) AND Resolution == ìFixedî AND affectedVersion != null in the project
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + 1000;

			//%20 = spazio                      %22=virgolette
			//Si ricavano tutti i ticket di tipo bug nello stato di risolto o chiuso, con risoluzione "fixed" e con affected version.
			String url = URLJIRA+ projectName + PIECE_OF_URL_JIRA+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22AND%22affectedVersion%22is%20not%20EMPTY"
					+ "%20AND%20updated%20%20%3E%20endOfYear(-"+YEARS_INTERVAL+")"
					+ "&fields=key,created,versions&startAt="
					+ i.toString() + MAX_RESULT + j.toString();


			json = readJsonFromUrl(url);
			issues = json.getJSONArray(ISSUES);
			//ci si prende il numero totale di ticket recuperati
			total = json.getInt(TOTAL);

			DateTimeFormatter format = DateTimeFormatter.ofPattern(FORMAT_DATE);


			LocalDate date;
			LocalDate affReleaseDate;
			String affVersReleaseDate="";



			// si itera sul numero di ticket
			for (; i < total && i < j; i++) {

				String key = issues.getJSONObject(i%1000).get("key").toString();
				String createdDate= issues.getJSONObject(i%1000).getJSONObject(FIELDS).get("created").toString().substring(0,10);

				//le righe seguenti sono necessarie perchË Jira potrebbe non fornire le releaseDate delle versioni affette

				for(int h=0;h<issues.getJSONObject(i%1000).getJSONObject(FIELDS).getJSONArray(VERSIONS).length();h++) {
					if(issues.getJSONObject(i%1000).getJSONObject(FIELDS).getJSONArray(VERSIONS).getJSONObject(h).has(RELEASE_DATE)) {
						//affVers Ë per es. 4.1.0
						affVersReleaseDate= issues.getJSONObject(i%1000).getJSONObject(FIELDS).getJSONArray(VERSIONS).getJSONObject(h).get(RELEASE_DATE).toString();
						break;
					}
				}
				//se la data dell'affected release non Ë stata presa allora si utilizerr‡ quella del bug pi˘ vicino temporalmente e se 
				// non Ë consistente con la created version allora si ignorer‡ il bug con le righe successive di check


				date = LocalDate.parse(createdDate,format);
				affReleaseDate =LocalDate.parse(affVersReleaseDate,format);

				checkAndGetCreatedVersion(date,affReleaseDate,key);

			}  
		} while (i < total);

	}

	private static void checkAndGetCreatedVersion(LocalDate date,LocalDate affReleaseDate,String key) {
		String createdVers=null;
		String affVers=null;
		TicketTakenFromJIRA tick;

		//se Ë la prima versione
		if (date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(1)))){
			createdVers= String.valueOf(1);
			affVers=String.valueOf(1);
		}
		else {
			for(int a=1;a<=fromReleaseIndexToDate.size();a++) {

				//abbiamo raggiunto nel for l'ultima release
				if(a==fromReleaseIndexToDate.size()) {
					createdVers= String.valueOf(a);
					affVers=getAffVers(affReleaseDate);

					//check su opening version e affected version
					if (Integer.parseInt(createdVers)>=Integer.parseInt(affVers)) {
						tick= new TicketTakenFromJIRA(key, createdVers, affVers);
						tickets.add(tick);
					}
					return;

				}
				else if ((date.atStartOfDay().isAfter(fromReleaseIndexToDate.get(String.valueOf(a)))
						&&(date.atStartOfDay().isBefore(fromReleaseIndexToDate.get(String.valueOf(a+1)))||
								(date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(a+1))))))) {
					createdVers= String.valueOf(a+1);

					affVers= getAffVers(affReleaseDate);
					break;//per uscire dal for
				}
			}
		}

		//check su opening version e affected version
		if (Integer.parseInt(createdVers)>=Integer.parseInt(affVers)) {
			tick= new TicketTakenFromJIRA(key, createdVers, affVers);
			tickets.add(tick);
		}
	}




	private static String getAffVers(LocalDate affReleaseDate) {
		for(int k=0;k<releases.size();k++) {
			if(releases.get(k).isEqual(affReleaseDate.atStartOfDay())) {
				return String.valueOf(k+1);

			}
		}
		return String.valueOf(releases.size());
	}

	private static void calculateMetrics(int version) {
		
		arrayOfEntryOfDataset= new ArrayList<>();
		
			//per ogni file nella release (version)
			for (String s : filepathsOfTheCurrentRelease) {
				calculatingIncrementalMetrics = true;
				//il metodo getChurnMetrics creer‡ l'arrayList di entry LineOfDataSet
				getLOCMetric(s,version);
				calculatingIncrementalMetrics = false;
				calculatingNotIncrementalMetrics = true;
				//i metodi successivi modificano semplicemente le entry in quell'array
				getNotIncrementalMetrics(s,version);
				calculatingNotIncrementalMetrics = false;
				calculatingNAuth= true;
				getNumberOfAuthors(s,version);
				calculatingNAuth= false;

			}

		 

	}

	private static void findNumberOfReleases() throws JSONException, IOException {


		// qui si calcolano le release
		Integer i = 0;
		JSONObject json ;

		//Fills the arraylist with releases dates and orders them
		//Ignores releases with missing dates
		releases = new ArrayList<>();

		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projectName;
		json = readJsonFromUrl(url);
		JSONArray versions = json.getJSONArray(VERSIONS);
		releaseNames = new HashMap<>();
		releaseID = new HashMap<> ();
		for (i = 0; i < versions.length(); i++ ) {
			String name = "";
			String id = "";
			if(versions.getJSONObject(i).has(RELEASE_DATE)) {
				if (versions.getJSONObject(i).has("name"))
					name = versions.getJSONObject(i).get("name").toString();
				if (versions.getJSONObject(i).has("id"))
					id = versions.getJSONObject(i).get("id").toString();
				addRelease(versions.getJSONObject(i).get(RELEASE_DATE).toString(),
						name,id);
			}
		}


		Comparator <LocalDateTime> comp = (o1,o2)->o1.compareTo(o2);
		// order releases by date
		Collections.sort(releases, comp);




		//--------------------------------------------------------

		//popolo un'HasMap con associazione indice di release-data delle release
		for ( i = 1; i <= releases.size(); i++) {
			fromReleaseIndexToDate.put(i.toString(),releases.get(i-1));
		}


		//cancellazione preventiva della directory clonata del progetto (se esiste)   
		recursiveDelete(new File(new File("").getAbsolutePath()+SLASH+projectName));


	}

	//questo metodo fa il checkout della repository per ottenerne lo stato visibile alla versione passatagli
	private static void gitCheckoutAtGivenVersion(int version){

		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		try {

			String command = "git checkout `git rev-list -n 1 --first-parent "
					+ "--after="+fromReleaseIndexToDate.get(String.valueOf(version))+" master'";	
			discard =true;
			runCommandOnShell(directory, command);
			discard=false;


		}
		catch (InterruptedException | IOException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			System.exit(-1);
		}		
	}





	public static void main(String[] args) throws IOException, JSONException {

		Integer i = 0;
		String outname;


		findNumberOfReleases();


		try {
			//si fa il clone della versione odierna del progetto
			gitClone();	
		}
		catch (InterruptedException | IOException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			System.exit(-1);
		}		


		//per ogni versione nella prim‡ met‡ delle release
		for(i=1;i<=Math.floorDiv(fromReleaseIndexToDate.size(),2);i++) {


			gitCheckoutAtGivenVersion(i);


			File folder = new File(projectName);
			filepathsOfTheCurrentRelease = new ArrayList<>();

			//search for java files in the cloned repository
			searchFileJava(folder, filepathsOfTheCurrentRelease);


			calculateMetrics(i);

			startToCalculateBugginess();

			startToGetFixedVersWithAV();
			setBuggy();

			startToGetCreatedVersWithoutAV();
			checkFixedVersWithoutAV();
			setBuggyWithoutAV();		 
			filepathsOfTheCurrentRelease.clear();
			}
		writeResult();
		//cancellazione directory clonata locale del progetto   
		recursiveDelete(new File(new File("").getAbsolutePath()+SLASH+projectName));

		//}

		//----------------------------------------------------------------------------

		////MILESTONE 2 DELIVERABLE 2

		//creo due file CSV (uno per il training con le vecchie release e uno per il testing) per ogni release
/*
		projectName= "OPENJPA";

		outname = projectName + " Deliverable 2 Milestone 1.csv";
		String csvTrain;
		String csvTest;
		String row = "";

		File csvFile = new File(outname);
		if (!csvFile.isFile()) {
			System.exit(-1);
		}



		//se Deliverable 2 Milestone 1 non Ë stato eseguito allora scrivi a mano la release.size

		for(i=2;i<=(Math.floorDiv(releases.size(),2));i++) {//modifica questa linea se Milestone 1 non Ë stata eseguita


			csvTrain = projectName+" Training for "+"Release "+i+".csv";
			csvTest = projectName+" Testing for "+"Release "+i+".csv";


			try (FileWriter fileWriterTrain= new FileWriter(csvTrain);
					FileWriter fileWriterTest=new FileWriter(csvTest);
					BufferedReader csvReader = new BufferedReader(new FileReader(outname));
					){


				fileWriterTrain.append("Version,Size(LOC),LOC_Touched,NR,NAuth,LOC_Added,MAX_LOC_Added,AVG_LOC_Added,Churn,MAX_Churn,AVG_Churn,Buggy");
				fileWriterTrain.append("\n");

				fileWriterTest.append("Version,Size(LOC),LOC_Touched,NR,NAuth,LOC_Added,MAX_LOC_Added,AVG_LOC_Added,Churn,MAX_Churn,AVG_Churn,Buggy");
				fileWriterTest.append("\n");


				//si leva l'header
				row=csvReader.readLine();
				while ((row = csvReader.readLine()) != null) {

					String[] entry = row.split(",");

					//per creare il dataset di training
					if ((Integer.parseInt(entry[0]))<i) {

						writePieceOfCsv(fileWriterTrain,entry);
					} 
					else if (Integer.parseInt(entry[0])==i) {
						writePieceOfCsv(fileWriterTest,entry);
					}

					else {

						break;
					}
				}//fine while

			} catch (Exception e) {
				e.printStackTrace();
			} 
		}



		Weka w = new Weka();

		i=Math.floorDiv(releases.size(),2);
		//i=Math.floorDiv(14,2);//commenta questa linea di codice e lascia quella sopra!

		//a doClassification() gli si passa il max numero di versioni da classificare
		w.doClassificationMilestone2(i, projectName);


		//--------------------------------------------------------------------------------
		//inizio ultima milestone Deliverable 2 

		try {
			w.doClassificationMilestone3(i, projectName);

		} catch (Exception e) {
			e.printStackTrace();
		}
*/
	}
}
