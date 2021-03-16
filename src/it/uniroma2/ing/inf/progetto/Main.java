package it.uniroma2.ing.inf.progetto;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.File;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.io.FileWriter;

/**
 * Copyright (C) 2020 Simone Mesiano Laureani (a.k.a. Simesi)
 *    
 *    This file is part of the contents developed for the course
 * 	  ISW2 (A.Y. 2019-2020) at UniversitÃ  di Tor Vergata in Rome.
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

	private static String projectName="OPENJPA";
	private static String projectNameGit="apache/openjpa.git";//"apache/bookkeeper.git";

	private static final String PATH_TO_FINER_GIT_JAR="E:\\FinerGit\\FinerGit\\build\\libs";// "C:\\users\\simone\\Desktop";

	private static final String HARD_DRIVE_NAME="E:";

	private static boolean studyMethodMetrics=false; //calcola le metriche di metodo
	private static boolean studyClassMetrics=true; //calcola le metriche di classe
	private static boolean studyCommitMetrics=false; //calcola le metriche di commit

	private static final boolean doResearchQuest1 =true;
	private static final boolean doResearchQuest2=false;
	
	//cancella questa variabile
	static int counterMethods=0;////

	public static Map<LocalDateTime, String> releaseNames;
	public static Map<LocalDateTime, String> releaseID;
	public static List<LocalDateTime> releases;
	private static Map<String,LocalDateTime> fromReleaseIndexToDate=new HashMap<>();
	private static List<TicketTakenFromJIRA> tickets;
	private static List<Integer> chgSetSizeList; //questa variabile è modificata dai thread per tenere traccia
	//del numero di files committati insieme
	private static List<String> modifiedFilesOfCommit; //lista statica dei file toccati dal commit in esame "lineofcommit"
	private static List<String> modifiedSubOfCommit;  //lista statica dei subsystem toccati dal commit in esame "lineofcommit"
	private static String authorOfCommit; //serve per EXP
	private static List<Integer> listOfDaysPassedBetweenCommits; //serve per l'AGE

	private static List<LineOfClassDataset> arrayOfEntryOfClassDataset;
	private static List<LineOfMethodDataset> arrayOfEntryOfMethodDataset;
	private static List<LineOfCommitDataset> arrayOfEntryOfCommitDataset;

	private static List<String> filepathsOfTheCurrentRelease;
	private static List<String> fileMethodsOfTheCurrentRelease;
	private static List<String> commitOfCurrentRelease;

	private static boolean doingCheckout=false;
	private static boolean calculatingIncrementalMetrics=false;
	private static boolean calculatingNotIncrementalMetrics=false;
	private static boolean calculatingAuthClassLevel=false;
	private static boolean calculatingChgSetSizePhaseOne=false;
	private static boolean calculatingChgSetSizePhaseTwo=false;
	private static boolean getNFixAtClassLevelMetric=false;
	private static boolean calculatingAge=false;



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

	private static final String FINER_GIT="_FinerGit_";



	private static boolean calculatingCommitInRelease=false;
	private static boolean calculatingFirstHalfCommitMetrics=false;
	private static boolean calculatingNumDevAndNucMetricCommitLevel=false;
	private static boolean calculatingSexp=false;
	private static boolean calculatingAuthorOfCommit=false;
	private static boolean calculatingTypeOfCommit=false;
	private static boolean calculatingExp=false;
	private static boolean calculatingFileAgeCommitLevel=false;
	private static boolean calculatingRecExp=false;
	private static boolean calculatingLOCBeforeCommit=false;

	private static final int numOfThreads= 10; //set this variable to set the number of threads


	///////////////////////////////    used to set boolean conditions for every thread
	private static Boolean[][] calculatingMethodMetrics = new Boolean[numOfThreads][5];////////////////////////////////
	/////////////////////////////////

	//this array has much dimensions as threads used, every field is the method upon a thread is working 
	private static LineOfMethodDataset[] threadsLineOfMethod= new LineOfMethodDataset[numOfThreads];

	private static LineOfClassDataset lineOfClassDataset;
	private static LineOfCommitDataset lineOfCommit;
	private static TicketTakenFromJIRA ticket;

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

		private static void gitCheckoutToHead() throws IOException, InterruptedException {

			Path directory;
			
			directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
			String command = "git checkout master";	

			runCommandOnShell(directory, command);
			
		}
	
	
	//questo metodo fa il 'git clone' della repository (necessario per poter ricavare successivamente il log dei commit)   
	private static void gitClone() throws IOException, InterruptedException {

		Path directory;
		String originUrl = "https://github.com/"+projectNameGit;


		directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);

		runCommand(directory.getParent(), "git", "clone", originUrl, directory.getFileName().toString());

	}


	public static void runCommand(Path directory, String... command) throws IOException, InterruptedException {

		Objects.requireNonNull(directory, "directory è NULL");

		if (!Files.exists(directory)) {

			throw new SecurityException("can't run command in non-existing directory '" + directory + "'");

		}

		ProcessBuilder pb = new ProcessBuilder()

				.command(command)

				.directory(directory.toFile());


		runProcAndWait(pb);

	}

	private static void runProcAndWait(ProcessBuilder pb) throws IOException, InterruptedException {
		//lancio un nuovo processo che invocherà il comando 'command',
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

	//routine specifica per il calcolo delle metriche di metodo per il thread tid
	private static void runProcWithTidAndWait(ProcessBuilder pb, Integer tid) throws IOException, InterruptedException {
		//lancio un nuovo processo che invocherà il comando 'command',
		//nella working directory fornita. 
		Process p = pb.start();

		//StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),tid);

		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(),tid);

		outputGobbler.start();

		//errorGobbler.start();

		int exit = p.waitFor();

		//errorGobbler.join();

		outputGobbler.join();

		if (exit != 0) {

			throw new AssertionError(String.format("runCommand with tid %d returned %d",tid, exit));

		}
	}
	public static void runCommandOnShell(Path directory, String command) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c",HARD_DRIVE_NAME+" && cd "+directory.toString()+" && "+command);	

		runProcAndWait(pb);

	}

	public static void runCommandOnShellWithTid(Path directory, String command,Integer tid) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c",HARD_DRIVE_NAME+" && cd "+directory.toString()+" && "+command);	

		runProcWithTidAndWait(pb,tid);

	}

	//questo metodo esegue FinerGit incaricato di produrre una directory con i metodi della repository
	public static void runFinerGitCloneForVersion(Path project,int version) {


		//java -jar FinerGit-all.jar create --src /path/to/repoA --des /path/to/repoB
		ProcessBuilder pb = new ProcessBuilder("cmd.exe","/c", "cd "+PATH_TO_FINER_GIT_JAR+ " && "
				+ "java -jar FinerGit-all.jar create --src "+project.toString()+" --des "+project.toString()+"_FinerGit_"+version);	

		try {
			runProcAndWait(pb);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	private static class SimpleMethodMetricsRunner implements Runnable { 

		private int start; 
		private int stride;
		private int rel;

		public SimpleMethodMetricsRunner(int stride, int start, int rel) {
			this.stride = stride;
			this.start = start;
			this.rel = rel;}

		public void run() {

			calculateMethodsMetrics(rel,stride,start);

		}
	}

	private static class StreamGobbler extends Thread {

		private final InputStream is;
		private int tid=100; //don't care value

		private StreamGobbler(InputStream is, int tid) {

			this.is = is;
			this.tid = tid;

		}

		private StreamGobbler(InputStream is) {

			this.is = is;

		}

		@Override

		public void run() {

			try (BufferedReader br = new BufferedReader(new InputStreamReader(is));) {

				String line;



				while ((line = br.readLine()) != null) {


					if (doingCheckout) {
						doingCheckout=false;
						gitCheckoutAtGivenCommit(line,br);
					}
					else if (calculatingIncrementalMetrics) {
						calculateLOC(line,br);
					}
					else if (calculatingNotIncrementalMetrics) {
						calculatingNotIncrementalMetrics(line,br);
					}
					else if (calculatingAuthClassLevel) {
						calculatingNauthClassLevel(line,br);
					}
					else if (calculatingAge) {
						getAgeMetricsClassLevel(line,br);
					}
					else if (calculatingChgSetSizePhaseOne) {
						getFileCommitsOnGivenRelease(line,br);
					}
					else if (calculatingChgSetSizePhaseTwo) {
						getFileCommittedTogheter(line,br);
					}
					// qui è per il set della metrica Nfix
					else if (getNFixAtClassLevelMetric) {
						gettingLastCommit(line,br);
					}
					else if(calculatingCommitInRelease) {
						getCommitIdForCommitLevel(line,br);
					}
					else if(calculatingFirstHalfCommitMetrics) {
						getFirstHalfCommitMetrics(line,br);
					}
					else if(calculatingNumDevAndNucMetricCommitLevel) {
						getNumDevCommitLevel(line,br);
					}
					else if(calculatingAuthorOfCommit){
						getAuthor(line,br);
					}
					else if(calculatingSexp) {
						getSexp(line,br);
					}
					else if(calculatingExp) {
						getExp(line,br);
					}
					else if(calculatingFileAgeCommitLevel) {
						getFileAgeCommitLevel(line,br);
					}
					else if(calculatingRecExp) {
						getRecentExpCommitLevel(line,br);
					}
					else if(calculatingLOCBeforeCommit) {
						getLOCBeforeCommitOfAFile(line,br);
					}
					else if(calculatingTypeOfCommit) {
						getFixMetricAtCommitLevel(line,br);
					}

					/////////////////////////////////////
					else if(tid!=100) {
						if (calculatingMethodMetrics[tid][0]) {
							getFirstHalfMethodsMetrics(line,br,tid);
						}
						else if (calculatingMethodMetrics[tid][1]) {
							getElseMetricsAtMethodLevel(line,br,tid);
						}
						else if(calculatingMethodMetrics[tid][2]) {
							getCondMetricAtMethodLevel(line,br,tid);
						}
						else if(calculatingMethodMetrics[tid][3]) {
							getAuthMetricAtMethodLevel(line,br,tid);
						}
						else if(calculatingMethodMetrics[tid][4]) {
							getLocMetricAtMethodLevel(line,br,tid);
						}
					}
					/////////////////////////////////////////////
					else {
						System.out.println(line);
					}
				}

			} catch (IOException ioe) {

				ioe.printStackTrace();
				System.exit(-1);
			}

		}


		private void getFixMetricAtCommitLevel(String line, BufferedReader br)  throws IOException{

			String nextLine;
			line=line.trim();
			String[] tokens = line.split("\\s+");
			String bug= tokens[0];
			List<String> commitsList= new ArrayList<>();


			nextLine =br.readLine();

			//non c'è un commit con questo id quindi non scrivo nulla
			if(nextLine==null) {
				return;
			}
			nextLine=nextLine.trim();

			//popolo lista con i commit id dei fix commit sul bug
			commitsList.add(nextLine);

			//ora si continua in caso di altri fix commit 
			nextLine =br.readLine();

			while(nextLine != null) {
				nextLine=nextLine.trim();
				commitsList.add(nextLine);
				nextLine =br.readLine();
			}
			for (int i = 0; i < commitsList.size(); i++) {
				ticket.getFixCommitList().add(commitsList.get(i));	
			}
			commitsList.clear();


		}

		private void getLOCBeforeCommitOfAFile(String line, BufferedReader br) throws IOException{
			String nextLine;
			int addedLines=0;
			int deletedLines=0;
			String[] tokens;

			//il primo output lo scartiamo perchè sono le righe modificate dal commit attuale..

			//lettura prox riga					      					      
			nextLine =br.readLine();

			while (nextLine != null) {
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");
				//si prende il primo valore (che sarà il numero di linee di codice aggiunte in un commit)
				addedLines=addedLines+Integer.parseInt(tokens[0]);
				//si prende il secondo valore (che sarà il numero di linee di codice rimosse in un commit)
				deletedLines=deletedLines+Integer.parseInt(tokens[1]);

				nextLine =br.readLine();
			}
			//si modifica solo se si ha un valore maggiore del precedente
			lineOfCommit.setLineBeforeChange(Math.max(lineOfCommit.getLineBeforeChange(),addedLines-deletedLines ));		

		}

		private void getRecentExpCommitLevel(String line, BufferedReader br) throws IOException{

			String nextLine;
			int years=0;
			double recExp=0;
			LocalDate DateCommit = null;
			LocalDate actualDateCommit = null;

			line=line.trim();
			//"one or more whitespaces = \\s+"
			String[] tokens = line.split("\\s+");

			DateTimeFormatter format = DateTimeFormatter.ofPattern(FORMAT_DATE);

			//il primo output è la data del commit attuale ------------------------------
			actualDateCommit = LocalDate.parse(tokens[0],format);

			//lettura prox riga					      					      
			nextLine =br.readLine();

			//i prossimi output sono la data dei commit precedenti (potrebbero non esserci)
			while (nextLine != null) {

				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");

				DateCommit = LocalDate.parse(tokens[0],format);
				years= Math.toIntExact(ChronoUnit.YEARS.between(DateCommit,actualDateCommit));
				recExp+= (double)1/(double)(years+1);

				nextLine =br.readLine();
			}

			lineOfCommit.setRecentExp(recExp);

		}

		private void getFileAgeCommitLevel(String line, BufferedReader br) throws IOException{

			String nextLine;
			int age=0;
			int count=0;
			LocalDate lastDateCommit = null;
			LocalDate DateCommit = null;

			line=line.trim();
			//"one or more whitespaces = \\s+"
			String[] tokens = line.split("\\s+");

			DateTimeFormatter format = DateTimeFormatter.ofPattern(FORMAT_DATE);

			//il primo output è la data del commit attuale ------------------------------
			DateCommit = LocalDate.parse(tokens[0],format);

			//lettura prox riga					      					      
			nextLine =br.readLine();

			//il secondo output è la data del commit precedente (potrebbe non esistere)
			while (nextLine != null) {
				count++;
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");

				lastDateCommit = LocalDate.parse(tokens[0],format);
				nextLine =br.readLine();
			}
			//primo commit nella storia del file
			if (count==0) {
				listOfDaysPassedBetweenCommits.add(count);
			}
			else {

				age= Math.toIntExact(ChronoUnit.DAYS.between(lastDateCommit,DateCommit));

				listOfDaysPassedBetweenCommits.add(age);
			}
		}

		private void getExp(String line, BufferedReader br) throws IOException {

			String nextLine;
			int numCommit=0;

			line=line.trim();
			String[] tokens = line.split("\\s+");

			numCommit+=Integer.parseInt(tokens[0]);

			nextLine =br.readLine();


			while(nextLine != null) {
				nextLine=nextLine.trim();
				tokens = line.split("\\s+");
				numCommit+=Integer.parseInt(tokens[0]);

				nextLine =br.readLine();
			}

			lineOfCommit.setExp(numCommit);
		}

		private void getSexp(String line, BufferedReader br) throws IOException {

			String nextLine;
			int numCommit=0;

			line=line.trim();
			String[] tokens = line.split("\\s+");

			numCommit+=Integer.parseInt(tokens[0]);

			nextLine =br.readLine();


			while(nextLine != null) {
				nextLine=nextLine.trim();
				tokens = line.split("\\s+");
				numCommit+=Integer.parseInt(tokens[0]);

				nextLine =br.readLine();
			}

			lineOfCommit.setSubExp(numCommit);

		}

		private void getAuthor(String line, BufferedReader br) throws IOException{
			String nextLine;
			String myAuthor = "";

			myAuthor=line;

			nextLine =br.readLine();

			while(nextLine != null) {
			}

			authorOfCommit=myAuthor;
		}


		private void getNumDevCommitLevel(String line, BufferedReader br) throws IOException {
			String nextLine;
			int nDev=1;
			int numCommit=0;

			line=line.trim();
			String[] tokens = line.split("\\s+");

			numCommit+=Integer.parseInt(tokens[0]);
			//method=tokens[1].replace("\"","");

			nextLine =br.readLine();


			while(nextLine != null) {
				nextLine=nextLine.trim();
				tokens = line.split("\\s+");
				numCommit+=Integer.parseInt(tokens[0]);
				nDev++;

				nextLine =br.readLine();
			}

			lineOfCommit.setNumDev(nDev);
			lineOfCommit.setNuc(numCommit);


		}

		private void getFirstHalfCommitMetrics(String line, BufferedReader br)  throws IOException {

			String version;
			String nextLine;
			String commit;
			int numFile=0;
			int realAddedLines=0;
			int realDeletedLOC=0;
			String fullFilePath;
			String subSystem;
			String directory;
			int sumModifiedLines=0;
			int i;
			double entropy = 0.0;
			List<Integer> arrModifiedLines= new ArrayList<>();
			List<String> arrDirList = new ArrayList<>();

			line=line.trim();
			String[] tokens = line.split("\\s+");

			//il primo input è la versione
			version=tokens[0];
			//con il commit id
			commit=tokens[1];

			nextLine =br.readLine();

			while (nextLine != null) {
				numFile++;
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");

				//discard of modified lines
				realAddedLines +=Math.max((Integer.parseInt(tokens[0])-Integer.parseInt(tokens[1])),0);
				realDeletedLOC+=Math.max((Integer.parseInt(tokens[1])-Integer.parseInt(tokens[0])),0);

				//for entropy
				if (Math.min(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]))>0) {
					arrModifiedLines.add(Math.min(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1])));
				}

				fullFilePath=tokens[2];

				modifiedFilesOfCommit.add(fullFilePath.concat(" "));//ci servirà dopo per il calcolo di NDEV

				//split del pathname (la divisione avviene al primo src trovato nel path)
				String[] parts = fullFilePath.split("/src/",2);
				subSystem = parts[0];


				if (!modifiedSubOfCommit.contains(subSystem)){
					modifiedSubOfCommit.add(subSystem);
				}


				if(parts[0].length()!=fullFilePath.length()){
					//ora levo il nome del file per avere la directory
					i = parts[1].lastIndexOf("/");
					directory =  parts[1].substring(0, i);
					if (!arrDirList.contains(directory)){
						arrDirList.add(directory);
					}
				}
				nextLine =br.readLine();
			}

			lineOfCommit = new LineOfCommitDataset(Integer.parseInt(version), commit);
			lineOfCommit.setLineAdded(realAddedLines);
			lineOfCommit.setLineDeleted(realDeletedLOC);
			lineOfCommit.setNumModFiles(numFile);
			lineOfCommit.setNumModDir(arrDirList.size());
			lineOfCommit.setNumModSub(modifiedSubOfCommit.size());

			//per l'entropia
			for (int j = 0; j < arrModifiedLines.size(); j++) {
				sumModifiedLines += arrModifiedLines.get(j); 	
			} 

			for (int j = 0; j < arrModifiedLines.size(); j++) {
				entropy+=(double)((((double)arrModifiedLines.get(j)*(-1))/(double)sumModifiedLines)*
						(Math.log(((double)arrModifiedLines.get(j))/((double)sumModifiedLines))
								/((double) Math.log(2)))); 
			} 


			lineOfCommit.setEntropy(entropy);

			arrModifiedLines.clear();
			arrDirList.clear();
		}

		private void getCommitIdForCommitLevel(String line, BufferedReader br) throws IOException {
			String version;
			String nextLine;

			line=line.trim();
			String[] tokens = line.split("\\s+");

			//il primo input è la versione
			version=tokens[0];

			nextLine =br.readLine();

			//da qui si otterrà una lista di hash commits
			while(nextLine != null) {
				nextLine=nextLine.trim();
				//si popola la lista della release corrente
				commitOfCurrentRelease.add(nextLine);
				nextLine =br.readLine();
			}
		}

		private void getAuthMetricAtMethodLevel(String line, BufferedReader br, int tid) throws IOException {
			String nextLine;
			String version;
			String method = "";
			int nAuth=0;

			line=line.trim();
			String[] tokens = line.split("\\s+");

			version=tokens[0];
			method=tokens[1].replace("\"","");

			nextLine =br.readLine();


			while(nextLine != null) {
				nextLine=nextLine.trim();
				nAuth++;
				nextLine =br.readLine();
			}

			if (nAuth!=0) {
				threadsLineOfMethod[tid].setAuthors(nAuth);

			}

		}



		private void getCondMetricAtMethodLevel(String line, BufferedReader br, int tid) throws IOException {
			String nextLine;
			String version;
			String filename;
			String ifString="if";
			int countIfAdded=0;
			int countIfDeleted=0;
			int sumOfModifiedCondition=0;


			line=line.trim();
			String[] tokens = line.split("\\s+");
			//il primo input è la versione	
			version= tokens[0];



			//la seconda riga ci ritorna la segnatura del metodo
			nextLine =br.readLine();

			nextLine=nextLine.trim();
			tokens = nextLine.split("\\s+");
			filename= tokens[0].replace("\"","");

			//dalla terza riga in poi otteniamo le modifiche apportate (da filtrare) 
			nextLine =br.readLine();


			while(nextLine != null) {

				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");
				if (tokens[0].equals("+"+ifString)){
					countIfAdded++;
				}
				else if(tokens[0].equals("-"+ifString)) {
					countIfDeleted++;
				}
				nextLine =br.readLine();
			}

			sumOfModifiedCondition+=threadsLineOfMethod[tid].getElseAdded();
			sumOfModifiedCondition+=threadsLineOfMethod[tid].getElseDeleted();
			sumOfModifiedCondition+=countIfAdded;
			sumOfModifiedCondition+=countIfDeleted;
			threadsLineOfMethod[tid].setCond(sumOfModifiedCondition);

		}


		private void getElseMetricsAtMethodLevel(String line, BufferedReader br, int tid) throws IOException {
			String nextLine;
			String version;
			String filename;
			String elseString="else";
			int countElseAdded=0;
			int countElseDeleted=0;


			line=line.trim();
			String[] tokens = line.split("\\s+");
			//il primo input è la versione	
			version= tokens[0];



			//la seconda riga ci ritorna la segnatura del metodo
			nextLine =br.readLine();

			nextLine=nextLine.trim();
			tokens = nextLine.split("\\s+");
			filename= tokens[0].replace("\"","");

			//dalla terza riga in poi otteniamo le modifiche apportate (da filtrare) 
			nextLine =br.readLine();


			while(nextLine != null) {

				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");
				if (tokens[0].equals("+"+elseString)){
					countElseAdded++;
				}
				else if(tokens[0].equals("-"+elseString)) {
					countElseDeleted++;
				}
				nextLine =br.readLine();
			}

			//la scrittura avviene solo se il risultato è maggiore di 0 
			if (countElseAdded>0||countElseDeleted>0) {
				threadsLineOfMethod[tid].setElseAdded(countElseAdded);
				threadsLineOfMethod[tid].setElseDeleted(countElseDeleted);
			}

		}

		private void getFirstHalfMethodsMetrics(String line, BufferedReader br, int tid) throws IOException {
			String version;
			int numOfCommits=0;
			String nextLine;
			int addedLines=0;
			int deletedLines=0;
			String methodName="";
			int realAddedLinesOfCommit=0;
			int sumOfRealDeletedLOC=0;
			int realDeletedLOC=0;
			int maxAddedlines=0;
			int maxChurn=0;
			int avgChurn=0;
			int maxDeletedLines=0;
			int totalAdded=0;
			int average=0;
			int numOfCommitsWithDel=0;
			ArrayList<Integer> realAddedLinesOverCommits=new ArrayList<>();


			line=line.trim();
			//"one or more whitespaces = \\s+"
			String[] tokens = line.split("\\s+");

			//operazioni per il primo output che è il numero di versione------------------------------
			version=tokens[0];
			methodName=tokens[1];
			//---------------------------------------------------------------------- 

			//lettura prox riga					      					      
			nextLine =br.readLine();

			while (nextLine != null) {
				numOfCommits++;
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");

				//discard of modified lines
				realAddedLinesOfCommit = Integer.parseInt(tokens[0])-Integer.parseInt(tokens[1]);

				//per il Max_LOC_Added
				maxAddedlines=Math.max(realAddedLinesOfCommit, maxAddedlines);


				//solo se le righe inserite sono maggiori di quelle cancellate
				if(realAddedLinesOfCommit>0) {
					//per AVG_LOC_Added
					realAddedLinesOverCommits.add(realAddedLinesOfCommit);
				}


				//si prende il primo valore (che sarà il numero di linee di codice aggiunte in un commit)
				addedLines=addedLines+Integer.parseInt(tokens[0]);
				//si prende il secondo valore (che sarà il numero di linee di codice rimosse in un commit)
				deletedLines=deletedLines+Integer.parseInt(tokens[1]);


				//per CHURN (togliamo i commit che hanno solo modificato il codice e quindi risultano +1 sia in linee aggiunte che in quelle eliminate)
				if((Integer.parseInt(tokens[0])-Integer.parseInt(tokens[1]))<0){
					realDeletedLOC=Integer.parseInt(tokens[1])-Integer.parseInt(tokens[0]);
					numOfCommitsWithDel++;
					sumOfRealDeletedLOC= sumOfRealDeletedLOC + realDeletedLOC;
					maxDeletedLines=Math.max(realDeletedLOC, maxDeletedLines);
				}

				//per MAX_CHURN
				maxChurn=Math.max(realAddedLinesOfCommit, maxChurn);

				realDeletedLOC=0;
				realAddedLinesOfCommit=0;

				nextLine =br.readLine();
			}

			if(numOfCommits!=0) {
				avgChurn=Math.floorDiv(Math.max((addedLines-deletedLines),0),numOfCommits);
			}

			threadsLineOfMethod[tid]= new LineOfMethodDataset(Integer.parseInt(version), methodName);

			threadsLineOfMethod[tid].setMethodHistories(numOfCommits);
			threadsLineOfMethod[tid].setMaxStmtAdded(maxAddedlines);

			threadsLineOfMethod[tid].setChurn(Math.max(addedLines-deletedLines, 0));
			threadsLineOfMethod[tid].setMaxChurn(maxChurn);
			threadsLineOfMethod[tid].setAvgChurn(avgChurn);

			//calcolo AVG_LOC_Added (è fatto solo sulle linee inserite)-----------------------
			for(int n=0; n<realAddedLinesOverCommits.size(); n++){

				totalAdded = totalAdded + realAddedLinesOverCommits.get(n);

			}
			if (totalAdded>0) {
				average = Math.floorDiv(totalAdded,realAddedLinesOverCommits.size());
			}



			//--------------------------------------------------
			threadsLineOfMethod[tid].setAvgStmtAdded(average);  
			threadsLineOfMethod[tid].setStmtAdded(totalAdded);
			threadsLineOfMethod[tid].setStmtDeleted(sumOfRealDeletedLOC);
			threadsLineOfMethod[tid].setMaxStmtDeleted(maxDeletedLines);

			if (numOfCommitsWithDel>0) {
				threadsLineOfMethod[tid].setAvgStmtDeleted(Math.floorDiv(sumOfRealDeletedLOC, numOfCommitsWithDel));
			}

		}

		private void getLocMetricAtMethodLevel(String line, BufferedReader br, int tid) throws IOException {
			String version;
			String nextLine;
			int addedLines=0;
			int deletedLines=0;


			line=line.trim();
			//"one or more whitespaces = \\s+"
			String[] tokens = line.split("\\s+");

			//operazioni per il primo output che è il numero di versione------------------------------
			version=tokens[0];
			String methodName=tokens[1];
			//---------------------------------------------------------------------- 

			//lettura prox riga					      					      
			nextLine =br.readLine();

			while (nextLine != null) {
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");

				//si prende il primo valore (che sarà il numero di linee di codice aggiunte in un commit)
				addedLines=addedLines+Integer.parseInt(tokens[0]);
				//si prende il secondo valore (che sarà il numero di linee di codice rimosse in un commit)
				deletedLines=deletedLines+Integer.parseInt(tokens[1]);

				nextLine =br.readLine();
			}

			threadsLineOfMethod[tid].setLOC(Math.max(addedLines-deletedLines, 0));
			arrayOfEntryOfMethodDataset.add(threadsLineOfMethod[tid]);
		}

		//per ogni commit si chiamerà questo metodo che ritorna i pathname dei file committati
		private void getFileCommittedTogheter(String input, BufferedReader br) {
			String version="";
			String nextLine="";
			int count=0;

			input=input.trim();
			String[] tokens = input.split("\\s+");
			String filename= tokens[0];
			try {
				//la seconda riga ci ritorna il numero di versione
				nextLine =br.readLine();

				nextLine=nextLine.trim();
				tokens = nextLine.split("\\s+");
				version= tokens[0];

				//dalla terza riga in poi otteniamo tutti i filepath modificati dal commit 
				nextLine =br.readLine();

				//per ogni file
				while(nextLine != null) {
					count++;

					nextLine =br.readLine();

				}
				calculatingChgSetSizePhaseTwo=false;
				//calcoliamo la metrica solo per i commit con più di un file committato
				if (count>1) {
					chgSetSizeList.add(count);	
				}	

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			} 



		}

		private void getFileCommitsOnGivenRelease(String input, BufferedReader br) {

			//directory da cui far partire il comando git    
			Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
			String command;
			String nextLine = "";
			String myCommit="";
			String version="";
			int myChgSetSize=0;
			int maxChgSet=0;
			int count=0;


			input=input.trim();
			String[] tokens = input.split("\\s+");
			String filename= tokens[0];
			try {
				//la seconda riga ci ritorna il numero di versione
				nextLine =br.readLine();

				nextLine=nextLine.trim();
				tokens = nextLine.split("\\s+");
				version= tokens[0];

				//dalla terza riga in poi otteniamo tutti i commit che hanno "lavorato" sul file filename 
				nextLine =br.readLine();

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			} 


			calculatingChgSetSizePhaseOne=false;
			//ora prendo la data dell'ultimo commit
			while(nextLine != null) {
				nextLine=nextLine.trim();
				tokens = nextLine.split("\\s+");
				myCommit= tokens[0];



				//per stampare i filepath dei file java cambiati al commit passato
				command = ECHO+filename+" && "+ECHO+version+
						" && git diff-tree --no-commit-id --name-only -r "+myCommit+""
						+ " --grep= *.java"; 



				try {

					calculatingChgSetSizePhaseTwo=true;
					runCommandOnShell(directory, command);
					nextLine =br.readLine();

				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}


			}

			if (chgSetSizeList.size()>0) {
				//adesso si sono esaminati tutti i commit e quindi si calcola la metrica ChgSetSize
				for (int index = 0; index < chgSetSizeList.size(); index++) {
					count++;
					maxChgSet=Math.max(chgSetSizeList.get(index), maxChgSet);
					myChgSetSize=myChgSetSize+chgSetSizeList.get(index);
				}			
			}

			lineOfClassDataset.setChgSetSize(myChgSetSize);
			lineOfClassDataset.setMaxChgSet(maxChgSet);
			if(count>0) {
				lineOfClassDataset.setAvgChgSet(Math.floorDiv(myChgSetSize, count));
			}
			else {
				lineOfClassDataset.setAvgChgSet(count);
			}
			//clear dellal lista che verrà ripopolata per analizzare la metrica su un altro file
			chgSetSizeList.clear();

			arrayOfEntryOfClassDataset.add(lineOfClassDataset);
		}

		private void gitCheckoutAtGivenCommit(String commit, BufferedReader br) {

			//directory da cui far partire il comando git    
			Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
			String command;
			String nextLine = "";


			commit=commit.trim();
			String[] tokens = commit.split("\\s+");
			String myCommit= tokens[0];

			try {
				nextLine =br.readLine();

				//ora prendo la data dell'ultimo commit
				while(nextLine != null) {
					myCommit=nextLine;

					nextLine =br.readLine();
				}
				//si prende solo l'ultimo commit dello stream (il più vecchio) e si fa il checkout
				command = "git checkout "+myCommit;	

				runCommandOnShell(directory, command);

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
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

			//non c'è un commit con questo id quindi non scrivo nulla
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

			setFixedVersionAndSetNFixMetric(bug,date,filesAffected);

		}



		private void setFixedVersionAndSetNFixMetric(String bug,LocalDate date,ArrayList<String> filesAffected) {
			String fixedVers;
			int count=0;

			//se è la prima versione
			if (date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(1)))
					||date.atStartOfDay().isBefore(fromReleaseIndexToDate.get(String.valueOf(1)))){
				fixedVers= String.valueOf(1);
			}
			else {
				fixedVers=iterateForFixVersion(date);
			}

			//set della metrica NFix --------------------------------------
			for (int n = 0; n < arrayOfEntryOfClassDataset.size(); n++) {  
				//si aggiunge un'unità al numero di bug fixed in base alla versione associata
				if(filesAffected.contains(arrayOfEntryOfClassDataset.get(n).getFileName())) {

					//aumento contatore in modo da fermare il ciclo una volta osservate tutte le entry
					// di quel file per tutte le versioni 
					count++;

					if (arrayOfEntryOfClassDataset.get(n).getVersion()>=Integer.parseInt(fixedVers)) {
						arrayOfEntryOfClassDataset.get(n).setnFix(arrayOfEntryOfClassDataset.get(n).getnFix()+1);
					}

					//abbiamo controllato tutte le versioni del file
					if(count==Integer.max(Math.floorDiv(fromReleaseIndexToDate.size(),10),3)) {
						break;
					}
				}
			}

			ticket.setFixedVersion(fixedVers);
			return;

		}

		private static String iterateForFixVersion(LocalDate date) {

			for(int a=1;a<=fromReleaseIndexToDate.size();a++) {

				if(a==fromReleaseIndexToDate.size()) {
					return String.valueOf(a);

				}

				if ((date.atStartOfDay().isAfter(fromReleaseIndexToDate.get(String.valueOf(a)))
						&&(date.atStartOfDay().isBefore(fromReleaseIndexToDate.get(String.valueOf(a+1)))||
								(date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(a+1))))))) {
					return String.valueOf(a+1);

				}
			}
			return String.valueOf(fromReleaseIndexToDate.size());
		}

		private void calculatingNauthClassLevel(String line, BufferedReader br) throws IOException {
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
			lineOfClassDataset.setNauth(nAuth);


		}

		private void getAgeMetricsClassLevel(String line, BufferedReader br) throws IOException{

			String filename;
			String version;
			String nextLine;
			int age=0;
			LocalDate firstDateCommit = null;
			LocalDate lastDateCommit = null;
			LocalDate DateCommit = null;
			int count=0;
			line=line.trim();
			//"one or more whitespaces = \\s+"
			String[] tokens = line.split("\\s+");

			//il primo output è il filename ------------------------------
			filename=tokens[0];

			//lettura prox riga	che ha la version				      					      
			nextLine =br.readLine();
			tokens = nextLine.split("\\s+");
			version=tokens[0];

			DateTimeFormatter format = DateTimeFormatter.ofPattern(FORMAT_DATE);


			//lettura prox riga					      					      
			nextLine =br.readLine();

			while (nextLine != null) {
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");
				count++;

				DateCommit = LocalDate.parse(tokens[0],format);
				lastDateCommit=DateCommit;


				//primo commit nella storia del file
				if (count==1) {
					firstDateCommit =DateCommit;
				}
				nextLine =br.readLine();
			}

			//fine dello stream (ultimo output è l'ultima data di commit)

			age= Math.toIntExact(ChronoUnit.WEEKS.between(firstDateCommit,lastDateCommit));

			lineOfClassDataset.setWeightedAge(0);

			if(lineOfClassDataset.getLOCTouched()>0) {
				lineOfClassDataset.setWeightedAge((double)age/(double)lineOfClassDataset.getLOCTouched());
			}
			lineOfClassDataset.setAge(age);

		}



		private void calculateLOC(String line, BufferedReader br) throws IOException {
			String nextLine;
			String filename= "";
			int addedLines=0;
			int deletedLines=0;
			String version;
			line=line.trim();
			//"one or more whitespaces = \\s+"
			String[] tokens = line.split("\\s+");

			//operazioni per il primo output che è il numero di versione------------------------------
			version=tokens[0];
			//--------------------------------------------------------- 

			//lettura prox riga					      					      
			nextLine =br.readLine();


			while (nextLine != null) {
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");
				//si prende il primo valore (che sarà il numero di linee di codice aggiunte in un commit)
				addedLines=addedLines+Integer.parseInt(tokens[0]);
				//si prende il secondo valore (che sarà il numero di linee di codice rimosse in un commit)
				deletedLines=deletedLines+Integer.parseInt(tokens[1]);
				filename= tokens[2];

				nextLine =br.readLine();
			}
			//abbiamo raggiunto la fine (la prima riga ha il numero di versione)
			lineOfClassDataset=new LineOfClassDataset(Integer.parseInt(version),filename); //id versione, filename
			lineOfClassDataset.setSize(addedLines-deletedLines);//set del valore di LOC



		}

		private void calculatingNotIncrementalMetrics(String line,BufferedReader br) throws IOException {
			String version;
			ArrayList<Integer> realAddedLinesOverCommit=new ArrayList<>();
			String nextLine;
			int modified=0;
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

			//operazione per il primo output che è il numero di versione------------------------------
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
				//si prende il primo valore (che sarà il numero di linee di codice aggiunte in un commit)
				addedLines=addedLines+Integer.parseInt(tokens[0]);
				//si prende il secondo valore (che sarà il numero di linee di codice rimosse in un commit)
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

			if(numberOfCommit!=0) {
				avgChurn=Math.floorDiv(Math.max((addedLines-deletedLines),0),numberOfCommit);
			}
			//provato empiricamente...
			modified= deletedLines-sumOfRealDeletedLOC;

			//abbiamo raggiunto la fine

			calculateNotIncrementalMetricsPart2(version,filename,sumOfRealDeletedLOC,
					maxAddedlines,realAddedLinesOverCommit,modified,average,numberOfCommit,
					addedLines-deletedLines,maxChurn,avgChurn);
		}

		private static void calculateNotIncrementalMetricsPart2(String version,String filename,
				int sumOfRealDeletedLOC,int maxAddedlines,ArrayList<Integer> addedLinesForEveryRevision
				,int modified,int average, int numOfCommit, int churn,int maxChurn,int avgChurn) {
			int totalAdded=0;

			lineOfClassDataset.setMAXLOCAdded(maxAddedlines);
			lineOfClassDataset.setNR(numOfCommit);
			lineOfClassDataset.setChurn(Math.max(churn, 0));
			lineOfClassDataset.setMaxChurn(maxChurn);
			lineOfClassDataset.setAVGChurn(avgChurn);

			//per il AVG_LOC_Added (è fatto solo sulle linee inserite)-----------------------
			for(int n=0; n<addedLinesForEveryRevision.size(); n++){

				totalAdded = totalAdded + addedLinesForEveryRevision.get(n);

			}
			if (totalAdded>0) {
				average = Math.floorDiv(totalAdded,addedLinesForEveryRevision.size());
			}
			//--------------------------------------------------
			lineOfClassDataset.setAVGLOCAdded(average);
			lineOfClassDataset.setLOCAdded(totalAdded);
			lineOfClassDataset.setLOCTouched(totalAdded+sumOfRealDeletedLOC+modified);

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


				//doingCheckout of the local prefix to the file name (that depends to this program)

				fileRenamed=f.getAbsolutePath().replace((Paths.get(new File("").getAbsolutePath()+SLASH+projectName)+SLASH).toString(), "");

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

	private static void getNumberOfAuthorsClassLevel(String filename, Integer i) {

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

	private static void getAllCommitOfBugFix(String id) throws IOException, InterruptedException{

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		try {    //ritorna id bug, id commit con quel bug nel commento
			command= ECHO+"\""+id+"\" && git log --grep=\""+id+" \" --grep="+id+": --pretty=format:%H -- *.java ";

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private static void getNumberOfAuthorsOfMetod(String method, Integer version, int start) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName+FINER_GIT+version);
		String command;

		try {

			command = ECHO+version+" \""+method+"\" && git shortlog -sn --all --until="
					+fromReleaseIndexToDate.get(String.valueOf(version))+" \""+method+"\"";	

			runCommandOnShellWithTid(directory, command,start);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private static void getLOC_Of_Method(String method, Integer version, int start) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName+FINER_GIT+version);
		String command;

		try {    

			//ritorna release e metodo (con il nome che aveva nella release version), poi righe aggiunte, eliminate e nome del metodo

			command = ECHO+version+" "+method+" && git log --follow "+ 
					"--until="+fromReleaseIndexToDate.get(String.valueOf(version))+FORMATNUMSTAT+"\""+method+"\"";	


			runCommandOnShellWithTid(directory, command,start);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private static void getCondMetric(String method, Integer version, int start) {
		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName+FINER_GIT+version);
		String command;

		//per vedere tutti i cambiamenti avvenuti nella storia che hanno aggiunto/eliminato/modificato righe contenenti una data parola nel codice
		//git log --since="..." -p -G "word" -- file.java

		try {    

			if(version>1) {

				//ritorna release,nome del metodo e storico cambiamenti
				command = ECHO+version+" && "+ECHO+"\""+method+"\" && git log --follow -p "
						+"--since="+fromReleaseIndexToDate.get(String.valueOf(version-1))+ 
						" --until="+fromReleaseIndexToDate.get(String.valueOf(version))+
						" -G"+" if -- \""+method+"\"";	
			}
			else{
				command = ECHO+version+" && "+ECHO+"\""+method+"\" && git log --follow -p "+ 
						"--until="+fromReleaseIndexToDate.get(String.valueOf(version))+
						" -G"+" if -- \""+method+"\"";	

			}

			runCommandOnShellWithTid(directory, command,start);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}



	private static void getElseMetrics(String method, Integer version, int start) {
		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName+FINER_GIT+version);
		String command;

		//per vedere solo i cambiamenti avvenuti nella storia che hanno aggiunto o eliminato righe con una data parola (qui else) nel codice
		//git log --since="..." -p -S "word" -- file.java

		try {    

			if(version>1) {

				//ritorna release,nome del metodo e storico cambiamenti
				command = ECHO+version+" && "+ECHO+"\""+method+"\" && git log --follow -p "
						+"--since="+fromReleaseIndexToDate.get(String.valueOf(version-1))+ 
						" --until="+fromReleaseIndexToDate.get(String.valueOf(version))+
						" -S"+" else -- \""+method+"\"";
			}
			else{
				command = ECHO+version+" && "+ECHO+"\""+method+"\" && git log --follow -p "+ 
						" --until="+fromReleaseIndexToDate.get(String.valueOf(version))+
						" -S"+" else -- \""+method+"\"";	

			}

			runCommandOnShellWithTid(directory, command,start);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	//start==tid
	private static void getFirstHalfMethodMetrics(String filename, Integer version, Integer start) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName+FINER_GIT+version);
		String command;

		try {    

			if(version>1) {

				//ritorna release e metodo (con il nome di allora), poi righe aggiunte, eliminate e nome del metodo
				command = ECHO+version+" "+filename+" && git log --follow "
						+"--since="+fromReleaseIndexToDate.get(String.valueOf(version-1))+ 
						" --until="+fromReleaseIndexToDate.get(String.valueOf(version))+FORMATNUMSTAT+"\""+filename+"\"";	
			}
			else{
				command = ECHO+version+" "+filename+" && git log --follow "+ 
						"--until="+fromReleaseIndexToDate.get(String.valueOf(version))+FORMATNUMSTAT+"\""+filename+"\"";	

			}

			runCommandOnShellWithTid(directory, command,start);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

	}


	//--------------------------------------





	//chiamato per Weka
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

	private static void writeClassMetricsResult() {
		String outname = projectName + "_Class.csv";
		//Name of CSV for output

		try (FileWriter fileWriter = new FileWriter(outname)){



			fileWriter.append("Project;Release;Class;Size(LOC);LOC_Touched;"
					+ "NR;NFix;NAuth;LOC_Added;MAX_LOC_Added;AVG_LOC_Added;"
					+ "Churn;MAX_Churn;AVG_Churn;ChgSetSize;MAX_ChgSet;AVG_ChgSet;Age;Weighted_Age;Actual_Defective");
			fileWriter.append("\n");
			for ( LineOfClassDataset line : arrayOfEntryOfClassDataset) {

				fileWriter.append(projectName);
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getVersion()));
				fileWriter.append(";");
				fileWriter.append(line.getFileName());
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getSize()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getLOCTouched()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getNR()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getnFix()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getNauth()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getLocadded()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getMAXLOCAdded()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getAVGLOCAdded()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getChurn()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getMaxChurn()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getAVGChurn()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getChgSetSize()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getMaxChgSet()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getAvgChgSet()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getAge()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getWeightedAge()));
				fileWriter.append(";");
				fileWriter.append(line.getBuggy());
				fileWriter.append("\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}



	private static void startToGetBugFixCommitFromJira() throws JSONException, IOException {

		//inizio operazioni per calcolo bugginess
		tickets=new ArrayList<>();
		Integer j=0;
		Integer total=1;
		JSONObject json ;
		JSONArray issues;
		Integer i=0;
		//Get JSON API for ticket with Type == “Bug” AND (status == “Closed” OR status == “Resolved”) AND Resolution == “Fixed” in the project
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + 1000;

			//%20 = spazio                      %22=virgolette
			//Si ricavano tutti i ticket di tipo bug nello stato di risolto o chiuso, con risoluzione "fixed" e con affected version non nulla.
			String url = URLJIRA+ projectName + PIECE_OF_URL_JIRA+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22"
					+ "%20&fields=key,created&startAt="
					+ i.toString() + MAX_RESULT + j.toString();

			// Il field created indica la data di creazione del ticket 

			json = readJsonFromUrl(url);
			issues = json.getJSONArray(ISSUES);
			//ci si prende il numero totale di ticket recuperati
			total = json.getInt(TOTAL);

			DateTimeFormatter format = DateTimeFormatter.ofPattern(FORMAT_DATE);

			LocalDate date;

			// si itera sul numero di ticket
			for (; i < total && i < j; i++) {

				String key = issues.getJSONObject(i%1000).get("key").toString();
				String createdDate= issues.getJSONObject(i%1000).getJSONObject(FIELDS).get("created").toString().substring(0,10);

				date = LocalDate.parse(createdDate,format);

				//si popola la list 'tickets'di TicketTakenFromJIRA 
				getCreatedVersionAndAddToList(date,key);
			}  
		} while (i < total);

	}

	private static void getCreatedVersionAndAddToList(LocalDate createdDate,String key) {
		String createdVers=null;
		TicketTakenFromJIRA tick;

		//se è la prima versione
		if (createdDate.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(1)))){
			createdVers= String.valueOf(1);
			tick= new TicketTakenFromJIRA(key, createdVers);
			tickets.add(tick);
		}
		else {
			for(int a=1;a<=fromReleaseIndexToDate.size();a++) {

				//abbiamo raggiunto nel for l'ultima release
				if(a==fromReleaseIndexToDate.size()) {
					createdVers= String.valueOf(a);
					tick= new TicketTakenFromJIRA(key, createdVers);
					tickets.add(tick);
					return;

				}// fine if ultima release

				else if ((createdDate.atStartOfDay().isAfter(fromReleaseIndexToDate.get(String.valueOf(a)))
						&&(createdDate.atStartOfDay().isBefore(fromReleaseIndexToDate.get(String.valueOf(a+1)))||
								(createdDate.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(a+1))))))) {
					createdVers= String.valueOf(a+1);
					tick= new TicketTakenFromJIRA(key, createdVers);
					tickets.add(tick);
					break;//per uscire dal for una volta trovata la created version
				}
			}
		}
	}



	//questo metodo lancia un processo che calcola le metriche ChgSet-based 
	private static void getChgSetMetrics(String filename, int version) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		//il comando sottostante ritorna la lista dei commit che hanno modificato il file 
		try {
			if(version>1) {
				//git log --follow --pretty=format:"%H"  [filename]
				command = ECHO+filename+
						" && "+ECHO+version+" && git log --follow --pretty=format:%H "
						+"--since="+fromReleaseIndexToDate.get(String.valueOf(version-1))+ 
						"--until="+fromReleaseIndexToDate.get(String.valueOf(version))+" "+filename;	
			}
			else{
				command = ECHO+filename+
						" && "+ECHO+version+" && git log --follow --pretty=format:%H "+ 
						"--until="+fromReleaseIndexToDate.get(String.valueOf(version))+" "+filename;	

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

	//questo metodo lancia un processo che calcola le metriche age-based 
	private static void getAgeMetrics(String filename, int version) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		try {
			//git log --date=short --format="format:%ad" --reverse [filename]
			command = ECHO+filename+
					" && "+ECHO+version+" && git log --date=short --format=%ad --reverse "+filename	;	

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

	}

	private static void startToGetNFixAtClassLevelMetric() throws IOException {


		getNFixAtClassLevelMetric=true;
		for (TicketTakenFromJIRA myTicket : tickets) {
			ticket= myTicket;
			//ora si prendono i commit su GIT associati a quei bug per ottenere la fixed version
			try {

				getLastCommitOfBug(myTicket.getKey());

			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}

		}

		getNFixAtClassLevelMetric=false;

	}
	//per la metrica di classe NFix
	private static void getLastCommitOfBug(String id) throws IOException, InterruptedException{

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		try {    //ritorna id bug, data dell'ultimo commit con quel bug nel commento e una lista di tutti i file java modificati
			command= ECHO+"\""+id+"\" && git log --grep=\""+id+" \" --grep="+id+": -1 --date=short --pretty=format:%ad &&"
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
			//ritorna gli id dei commit e sul più vecchio si farà il checkout
			String command = "git rev-list "
					+ "--after="+fromReleaseIndexToDate.get(String.valueOf(version))+" master";//" master ";

			doingCheckout =true;
			runCommandOnShell(directory, command);
			doingCheckout=false;


		}
		catch (InterruptedException | IOException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			System.exit(-1);
		}		
	}





	public static void main(String[] args) throws IOException, JSONException {

		findNumberOfReleases();

		if (doResearchQuest1){


			try {
				//si fa il clone della versione odierna del progetto
				gitClone();	
			}
			catch (InterruptedException | IOException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
				System.exit(-1);
			}	

			//----------------------------------------------------------------------------

			if(studyClassMetrics||studyCommitMetrics) {
				//per la metrica di classe NFix e/o per la metrica di commit Fix 
				//si popola l'array di TicketTakenFormJira per ogni bug
				startToGetBugFixCommitFromJira();
			}


			//CLASS CLASSIFICATION
			if(studyClassMetrics) {

				arrayOfEntryOfClassDataset= new ArrayList<>();
				//per ogni versione nella primà metà delle release
				for(int i=1;i<=Integer.max(Math.floorDiv(fromReleaseIndexToDate.size(),10),3);i++) {


					gitCheckoutAtGivenVersion(i);


					File folder = new File(projectName);
					filepathsOfTheCurrentRelease = new ArrayList<>();
					chgSetSizeList=new ArrayList<>();

					//search for java files in the cloned repository
					searchFileJava(folder, filepathsOfTheCurrentRelease);
					System.out.println("Founded "+filepathsOfTheCurrentRelease.size()+" files");

					calculateClassMetrics(i);
					filepathsOfTheCurrentRelease.clear();
				}

				//facciamo  un altro clone per poter calcolare la bugginess slla versione aggiornata del progetto
				try {
					//si ritorna alla versione odierna del progetto
					gitCheckoutToHead();	
				}
				catch (InterruptedException | IOException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
					System.exit(-1);
				}

				// per calcolare metrica di classe Nfix
				startToGetNFixAtClassLevelMetric();

				writeClassMetricsResult();
			}

			//----------------------------------------------------------------------------

			//METHOD CLASSIFICATION
			if(studyMethodMetrics) {

				fileMethodsOfTheCurrentRelease = new ArrayList<>();
				arrayOfEntryOfMethodDataset = new ArrayList<LineOfMethodDataset>();

				//per ogni versione nella primà metà delle release
				for(int rel=1;rel<=Integer.max(Math.floorDiv(fromReleaseIndexToDate.size(),10),3);rel++) {
					//int rel=1;//cancella questa riga

					gitCheckoutAtGivenVersion(rel);

					Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
					//create repository with FinerGit------------------------------
					runFinerGitCloneForVersion(directory,rel); // commenta per non produrre i file metodo Finergit
					//-------------------------------------------------------------



					//search for java methods in the cloned repository         
					File folder = new File(projectName+"_FinerGit_"+rel);
					searchMethods(folder, fileMethodsOfTheCurrentRelease,rel);
					System.out.println("Founded "+fileMethodsOfTheCurrentRelease.size()+" methods");

					//////////////////////////////////////////////
					//initialize of thread boolean parameters 
					for ( int i = 0; i <numOfThreads; i++)
					{
						for (int j=0;j<5; j++) {
							calculatingMethodMetrics[i][j] = false;
						}
					}

					ArrayList<Thread> threads= new ArrayList<Thread>();

					for(int i = 0; i < numOfThreads; i++) {

						Thread t = new Thread(new SimpleMethodMetricsRunner(numOfThreads,i,rel));
						t.start();
						threads.add(t);
					}

					try { 

						for(int i = 0; i < numOfThreads; i++) {
							threads.get(i).join();
						}

					} 
					catch (InterruptedException exc) {
						exc.printStackTrace();
						Thread.currentThread().interrupt();
						System.exit(-1);
					}
					////////////////////////////////////////////////

					System.out.println("Calculated metrics version "+rel);
					fileMethodsOfTheCurrentRelease.clear();
				} 

				writeMethodMetricsResult();

			}

			//----------------------------------------------------------------------------

			//COMMIT CLASSIFICATION
			if(studyCommitMetrics) {

				commitOfCurrentRelease = new ArrayList<>();
				arrayOfEntryOfCommitDataset = new ArrayList<>();
				modifiedFilesOfCommit = new ArrayList<>();
				modifiedSubOfCommit= new ArrayList<>();
				listOfDaysPassedBetweenCommits=new ArrayList<>();



				//per ogni versione nella primà metà delle release
				for(int rel=1;rel<=Integer.max(Math.floorDiv(fromReleaseIndexToDate.size(),10),3);rel++) {

					gitCheckoutAtGivenVersion(rel);

					calculatingCommitInRelease=true;
					//search for commits in the release in the cloned repository         
					SearchForCommitsOfGivenRelease(rel);
					calculatingCommitInRelease=false;

					System.out.println("Founded "+commitOfCurrentRelease.size()+" commits on release "+rel);

					calculateCommitMetrics(rel);
					System.out.println("Calculated metrics version "+rel);

					commitOfCurrentRelease.clear();
				}

				getBugFixCommitsFromGitAndSetFixMetric();

				writeCommitMetricsResult();
			}

			//cancellazione directory clonata locale del progetto   
			recursiveDelete(new File(new File("").getAbsolutePath()+SLASH+projectName));
		}

		//----------------------------------------------------------------------------
		if (doResearchQuest2) {


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



		//se Deliverable 2 Milestone 1 non è stato eseguito allora scrivi a mano la release.size

		for(i=2;i<=(Math.floorDiv(releases.size(),2));i++) {//modifica questa linea se Milestone 1 non è stata eseguita


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

	private static void calculateClassMetrics(int version) {



		//per ogni file nella release (version)
		for (String s : filepathsOfTheCurrentRelease) {

			calculatingIncrementalMetrics = true;
			//il metodo getLOCMetric creerà anche l'arrayList di entry LineOfDataSet
			getLOCMetric(s,version);
			calculatingIncrementalMetrics = false;
			calculatingNotIncrementalMetrics = true;
			//i metodi successivi modificano semplicemente le entry in quell'array
			getNotIncrementalMetrics(s,version);
			calculatingNotIncrementalMetrics = false;
			calculatingAuthClassLevel= true;
			getNumberOfAuthorsClassLevel(s,version);
			calculatingAuthClassLevel= false;

			calculatingAge=true;
			getAgeMetrics(s,version);
			calculatingAge=false;

			calculatingChgSetSizePhaseOne=true;
			getChgSetMetrics(s,version);
			calculatingChgSetSizePhaseOne=false;

		}
		System.out.println("########## Evaluated metrics for version "+version+"############");


	}



	private static void calculateMethodsMetrics(int version, int stride, int start) {

		//per ogni metodo nella release (version)
		for (int j = start; j < fileMethodsOfTheCurrentRelease.size(); j=j+stride) {
			//for (int j = start; j < 100; j=j+stride) {
			String method = fileMethodsOfTheCurrentRelease.get(j);

			calculatingMethodMetrics[start][0]=true;
			getFirstHalfMethodMetrics(method,version,start);
			calculatingMethodMetrics[start][0] = false;

			if (threadsLineOfMethod[start].getMethodHistories()!=0) {
				calculatingMethodMetrics[start][1]=true;
				getElseMetrics(method,version,start);
				calculatingMethodMetrics[start][1]=false;

				calculatingMethodMetrics[start][2]=true;
				getCondMetric(method,version,start);
				calculatingMethodMetrics[start][2]=false;

			}

			calculatingMethodMetrics[start][3]=true;
			getNumberOfAuthorsOfMetod( method,version,start);
			calculatingMethodMetrics[start][3]=false;

			calculatingMethodMetrics[start][4]=true;
			getLOC_Of_Method( method,version,start);
			calculatingMethodMetrics[start][4]=false;

			//commenta queste due righe per non avere printf
			counterMethods++;
			System.out.println("metodo: "+counterMethods+"°, versione "+version);

		}

	}

	private static void calculateCommitMetrics(int version) {

		int counter =0;
		//per ogni commit nella release (version)
		for (String commit : commitOfCurrentRelease) {

			counter++;
			if(Math.floorMod(counter, 10)==0) {
				System.out.println("Commit "+counter+" release "+version);
			}


			calculatingFirstHalfCommitMetrics = true;
			//il metodo getFirstHalfCommitMetrics() creerà anche l'arrayList di entry LineOfCommitDataset
			searchFirstHalfCommitMetrics(version,commit);
			calculatingFirstHalfCommitMetrics = false;

			calculatingNumDevAndNucMetricCommitLevel=true;
			getNumberOfDevAndNucOfCommitLevel(commit,version);
			calculatingNumDevAndNucMetricCommitLevel=false;

			calculatingAuthorOfCommit=true;
			getAuthorOfCommit(commit);
			calculatingAuthorOfCommit=false;

			calculatingSexp=true;
			getSexpCommitLevel(commit);
			calculatingSexp=false;

			calculatingExp=true;
			getExpCommitLevel(commit);
			calculatingExp=false;


			//ora per le metriche AGE e REXP  -----------------
			//per ogni file occorre calcolare la data dell'ultimo commit avvenuto
			for (int i = 0; i < modifiedFilesOfCommit.size(); i++) {
				calculatingFileAgeCommitLevel=true;
				getAgeCommitLevelOfFile(commit,modifiedFilesOfCommit.get(i));
				calculatingFileAgeCommitLevel=false;

				calculatingLOCBeforeCommit=true;
				getLinesOfCodeOfFileBeforeCommit(commit,modifiedFilesOfCommit.get(i));
				calculatingLOCBeforeCommit=false;
			}

			//questo metodo utilizzerà dati condivisi per settare l'age di lineOfCommit 
			calculateAgeCommitLevel();

			calculatingRecExp=true;
			getRecExpCommitLevel(commit);
			calculatingRecExp=false;

			arrayOfEntryOfCommitDataset.add(lineOfCommit);

			//--------------------------------
			listOfDaysPassedBetweenCommits.clear();
			modifiedFilesOfCommit.clear();
			modifiedSubOfCommit.clear();
		}
	}

	//Search and list of all methods of java files in the repository at the given release
	public static void searchMethods( final File folder, List<String> result, int version) {
		String fileRenamed;
		for (final File f : folder.listFiles()) {

			if (f.isDirectory()) {
				searchMethods(f, result,version);
			}

			//si prendono solo i file java
			if (f.isFile()&&f.getName().matches(".*\\.mjava")) {


				//doingCheckout of the local prefix to the file name (that depends to this program)

				fileRenamed=f.getAbsolutePath();
				//System.out.println(fileRenamed);
				//System.out.println((Paths.get(new File("").getAbsolutePath()+SLASH+projectName)+SLASH).toString());

				fileRenamed=fileRenamed.replace((Paths.get(new File("").getAbsolutePath()+SLASH+projectName+FINER_GIT+version)+SLASH).toString(), "");

				//ci si costruisce una lista 

				//il comando git log prende percorsi con la '/'
				fileRenamed= fileRenamed.replace("\\", "/");
				result.add(fileRenamed);
				//System.out.println(fileRenamed);

			}
		}
	}

	private static void writeMethodMetricsResult() {
		String outname = projectName + "_Method.csv";
		//Name of CSV for output

		try (FileWriter fileWriter = new FileWriter(outname)){



			fileWriter.append("Project;Release;Method;methodHistories;LOC;authors;"
					+ "stmtAdded;maxStmtAdded;avgStmtAdded;stmtDeleted;maxStmtDeleted;"
					+ "avgStmtDeleted;Churn;MaxChurn;AvgChurn;cond;elseAdded;elseDeleted;Actual_Defective");
			fileWriter.append("\n");
			for ( LineOfMethodDataset line : arrayOfEntryOfMethodDataset) {

				fileWriter.append(projectName);
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getVersion()));
				fileWriter.append(";");

				int ind=line.getMethod().indexOf("."); 
				if (ind!=-1){
					String myMethodName = line.getMethod().substring(0,ind);//discard of ".mjava" from method name
					myMethodName = myMethodName.replace("#",".java#");//adding ".java" on class name

					fileWriter.append(myMethodName);

				}
				else 
					fileWriter.append(line.getMethod());

				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getMethodHistories()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getLOC()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getAuthors()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getStmtAdded()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getMaxStmtAdded()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getAvgStmtAdded()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getStmtDeleted()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getMaxStmtDeleted()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getAvgStmtDeleted()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getChurn()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getMaxChurn()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getAvgChurn()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getCond()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getElseAdded()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getElseDeleted()));
				fileWriter.append(";");
				fileWriter.append(line.getDefective());
				fileWriter.append("\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	//questo metodo lancia un comando che ritornerà tutti i commit hash dei copmmit avvenuti nella release passata
	public static void SearchForCommitsOfGivenRelease(int release) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		try {
			if(release>1) {
				command = ECHO+release+" && git log --pretty=format:\"%H\""
						+ " --since="+fromReleaseIndexToDate.get(String.valueOf(release-1))+
						" --until="+fromReleaseIndexToDate.get(String.valueOf(release))+" -- *.java" ;	
			}
			else {  //prima release
				command = ECHO+release+" && git log --pretty=format:\"%H\" "
						+ "--until="+fromReleaseIndexToDate.get(String.valueOf(release))+" -- *.java";	
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


	private static void searchFirstHalfCommitMetrics(Integer version, String commit) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		try {    
			//ritorna release e commit id, poi righe aggiunte, eliminate e nome del file java modificato
			command = ECHO+version+" "+commit+" && git show  --format= --numstat "
					+commit+" --no-renames -- *.java";	

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

	}

	//per ottenere il numero di commit per ogni autore che ha modificato i "modifiedFiles" fino al commit passato 
	private static void getNumberOfDevAndNucOfCommitLevel(String commit, Integer version) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		try {
			command = "git shortlog -sn "+commit+" -- ";
			//aggiungo i nome dei file toccati dal commit in esame
			for (int i = 0; i < modifiedFilesOfCommit.size(); i++) {
				command.concat(modifiedFilesOfCommit.get(i));				
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

	private static void getAuthorOfCommit(String commit) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		try {    
			//ritorna l'autore del commit 
			command = "git show -s --format=\"%an\" "+commit;	
			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}


	//per ottenere il numero di commit dell'author riguardanti i "subsystem" fino al commit passato 
	private static void getSexpCommitLevel(String commit) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		try {
			command = "git shortlog -sn "+commit+" --author=\""+authorOfCommit+"\" -- ";
			//aggiungo i nomi dei file toccati dal commit in esame
			for (int i = 0; i < modifiedSubOfCommit.size(); i++) {
				command.concat(modifiedSubOfCommit.get(i));				
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

	//per ottenere il numero di commit dell'author nel progetto fino al commit passato 
	private static void getExpCommitLevel(String commit) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		try {
			command = "git shortlog -sn "+commit+" --author=\""+authorOfCommit+"\"";

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	//occorre calcolare la data dell'ultimo commit avvenuto sul file 
	private static void getAgeCommitLevelOfFile(String commit,String file) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		//si printa la data del commit passato e anche del commit precedente
		try {
			command = "git log --date=short -2 "+commit+" --format=%ad -- "+file+"";

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	//qui si fa la media dei giorni passati dall'ultimo commit tra tutti i file e si setta il valore della metrica
	private static void calculateAgeCommitLevel(){
		int age=0;
		for (int i = 0; i < listOfDaysPassedBetweenCommits.size(); i++) {
			age+=listOfDaysPassedBetweenCommits.get(i);	
		}
		age=Math.floorDiv(age, listOfDaysPassedBetweenCommits.size());
		lineOfCommit.setAge(age);
	}

	//per ottenere le date dei commit dell'author nel progetto fino al commit passato 
	private static void getRecExpCommitLevel(String commit) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		try {
			command = "git log --date=short "+commit+" --author=\""+authorOfCommit+"\" --format=%ad";

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	//per ottenere le linee di codice fino a quel commit (incluso) del file passato
	private static void getLinesOfCodeOfFileBeforeCommit(String commit, String filename) {
		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
		String command;

		// git log [commit] --format= --numstat -- [filename]

		try {

			command ="git log "+commit+FORMATNUMSTAT+filename;	

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

	}

	//qui si cercano tutti i fix commit di ogni bug ticket di Jira
	private static void getBugFixCommitsFromGitAndSetFixMetric() throws IOException {
		calculatingTypeOfCommit=true;
		for (TicketTakenFromJIRA myTicket : tickets) {
			try {
				ticket=myTicket;
				getAllCommitOfBugFix(myTicket.getKey());

			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}

		}
		calculatingTypeOfCommit=false;

		//ora si settano i fix commit trovati nel dataset
		//per ogni ticket
		for (TicketTakenFromJIRA myTicket : tickets) {
			//per ogni fix commit del ticket
			for (int i = 0; i < myTicket.getFixCommitList().size(); i++) {
				//si cerca la linea del dataset di commit corrispondente
				for (LineOfCommitDataset commitLine : arrayOfEntryOfCommitDataset) {
					if(commitLine.getCommit().equals(myTicket.getFixCommitList().get(i))) {
						commitLine.setDefectFix("YES");
						break;
					}

				}
			}
		}


	}

	private static void writeCommitMetricsResult() {
		String outname = projectName + "_Commit.csv";
		//Name of CSV for output

		try (FileWriter fileWriter = new FileWriter(outname)){



			fileWriter.append("Project;Release;Commit;NS;ND;"
					+ "NF;Entropy;LA;LD;LT;"
					+ "FIX;NDEV;AGE;NUC;EXP;REXP;SEXP;Actual_Defective");
			fileWriter.append("\n");
			for ( LineOfCommitDataset line : arrayOfEntryOfCommitDataset) {

				fileWriter.append(projectName);
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getVersion()));
				fileWriter.append(";");
				fileWriter.append(line.getCommit());
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getNumModSub()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getNumModDir()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getNumModFiles()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getEntropy()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getLineAdded()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getLineDeleted()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getLineBeforeChange()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getDefectFix()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getNumDev()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getAge()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getNuc()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getExp()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getRecentExp()));
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getSubExp()));
				fileWriter.append(";");
				fileWriter.append(line.getBugIntroducing());
				fileWriter.append("\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}


}
