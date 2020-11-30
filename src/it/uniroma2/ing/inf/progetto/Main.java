package it.uniroma2.ing.inf.progetto;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.channels.NonReadableChannelException;
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

	private static String projectName="BOOKKEEPER";//"OPENJPA";
	private static String projectNameGit="apache/bookkeeper.git";//"apache/openjpa.git";



	public static Map<LocalDateTime, String> releaseNames;
	public static Map<LocalDateTime, String> releaseID;
	public static List<LocalDateTime> releases;
	private static Map<String,LocalDateTime> fromReleaseIndexToDate=new HashMap<>();
	private static List<TicketTakenFromJIRA> tickets;
	private static List<TicketTakenFromJIRA> ticketsWithoutAV;
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
	private static boolean gettingLastCommit=false;
	private static boolean calculatingAge=false;
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
	private static final String PATH_TO_FINER_GIT_JAR="E:\\FinerGit\\FinerGit\\build\\libs";
	private static final String FINER_GIT="_FinerGit_";

	private static boolean studyMethodMetrics=false; //calcola le metriche di metodo
	private static boolean studyClassMetrics=false; //calcola le metriche di classe
	private static boolean studyCommitMetrics=true; //calcola le metriche di commit

	private static boolean calculatingStmtMetricsMethodLevel=false;
	private static boolean calculatingElseMetricsMethodLevel=false;
	private static boolean calculatingCondMetricMethodLevel=false;
	private static boolean calculatingAuthMetricMethodLevel=false;

	private static boolean calculatingCommitInRelease=false;
	private static boolean calculatingFirstHalfCommitMetrics=false;
	private static boolean calculatingNumDevAndNucMetricCommitLevel=false;
	private static boolean calculatingSexp=false;
	private static boolean calculatingAuthorOfCommit=false;
	private static boolean calculatingTypeOfCommit=false;
	private static boolean calculatingExp=false;
	private static boolean calculatingFileAgeCommitLevel=false;

	private static LineOfMethodDataset lineOfMethod;
	private static LineOfClassDataset lineOfClassDataset;
	private static LineOfCommitDataset lineOfCommit;

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

	public static void runCommandOnShell(Path directory, String command) throws IOException, InterruptedException {

		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c","E: && cd "+directory.toString()+" && "+command);	

		runProcAndWait(pb);

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
					// qui è per il set di "buggy"
					else if (gettingLastCommit) {
						gettingLastCommit(line,br);
					}
					else if (calculatingStmtMetricsMethodLevel) {
						getFirstHalfMethodsMetrics(line,br);
					}
					else if (calculatingElseMetricsMethodLevel) {
						getElseMetricsAtMethodLevel(line,br);
					}
					else if(calculatingCondMetricMethodLevel) {
						getCondMetricAtMethodLevel(line,br);
					}
					else if(calculatingAuthMetricMethodLevel) {
						getAuthMetricAtMethodLevel(line,br);
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
					else {

						System.out.println(line);
					}
				}

			} catch (IOException ioe) {

				ioe.printStackTrace();
				System.exit(-1);
			}

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

			line=line.trim();

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
			int sumOfDelLines=0;
			String fullFilePath;
			String subSystem;
			String directory;
			int sumModifiedLines=0;
			int i;
			double entropy = 0.0;
			int diff=0;
			List<Integer> arrModifiedLines= new ArrayList<>();
			List<String> arrDirList = new ArrayList<>();

			line=line.trim();
			String[] tokens = line.split("\\s+");

			//il primo input è la versione
			version=tokens[0];
			//con il commit id
			commit=tokens[1];

			nextLine =br.readLine();

			//da qui si otterrà una lista di hash commits

			while (nextLine != null) {
				numFile++;
				nextLine=nextLine.trim();
				tokens=nextLine.split("\\s+");

				//discard of modified lines
				realAddedLines +=Integer.parseInt(tokens[0])-Integer.parseInt(tokens[1]);
				realDeletedLOC+=Integer.parseInt(tokens[1])-Integer.parseInt(tokens[0]);

				//for entropy
				sumOfDelLines=Integer.parseInt(tokens[1]);
				if((Integer.parseInt(tokens[0])-Integer.parseInt(tokens[1]))<0){
					diff=Integer.parseInt(tokens[1])-Integer.parseInt(tokens[0]);
				}
				arrModifiedLines.add(sumOfDelLines-diff);
				diff=0;

				fullFilePath=tokens[2];

				modifiedFilesOfCommit.add(fullFilePath.concat(" "));//ci servirà dopo per il calcolo di NDEV
                
				//split del pathname (la divisione avviene al primo src trovato nel path)
				String[] parts = fullFilePath.split("/src/",2);
				subSystem = parts[0];


				if (!modifiedSubOfCommit.contains(subSystem)){
					modifiedSubOfCommit.add(subSystem);
				}

				//ora levo il nome del file per avere la directory
				i = parts[1].lastIndexOf("/");
				directory =  parts[1].substring(0, i);
				if (!arrDirList.contains(directory)){
					arrDirList.add(directory);
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
				entropy+=(double)((((double)arrModifiedLines.get(j)*(-1))/(double)sumModifiedLines))*
						(Math.log((double)arrModifiedLines.get(j)/(double)sumModifiedLines)
								/(double) Math.log(2)); 	
			} 

			lineOfCommit.setEntropy(entropy);

			arrModifiedLines.clear();
			arrDirList.clear();
		}

		private void getCommitIdForCommitLevel(String line, BufferedReader br) throws IOException {
			String version;
			String nextLine;
			String commit;

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

		private void getAuthMetricAtMethodLevel(String line, BufferedReader br) throws IOException {
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
				lineOfMethod.setAuthors(nAuth);
				arrayOfEntryOfMethodDataset.add(lineOfMethod);
			}

		}



		private void getCondMetricAtMethodLevel(String line, BufferedReader br) throws IOException {
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

			sumOfModifiedCondition+=lineOfMethod.getElseAdded();
			sumOfModifiedCondition+=lineOfMethod.getElseDeleted();
			sumOfModifiedCondition+=countIfAdded;
			sumOfModifiedCondition+=countIfDeleted;
			lineOfMethod.setCond(sumOfModifiedCondition);

		}


		private void getElseMetricsAtMethodLevel(String line, BufferedReader br) throws IOException {
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
				lineOfMethod.setElseAdded(countElseAdded);
				lineOfMethod.setElseDeleted(countElseDeleted);
			}

		}

		private void getFirstHalfMethodsMetrics(String line, BufferedReader br) throws IOException {
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

			if(numOfCommits==0) {
				avgChurn=Math.floorDiv(Math.max((addedLines-deletedLines),0),numOfCommits);
			}

			lineOfMethod = new LineOfMethodDataset(Integer.parseInt(version), methodName);

			lineOfMethod.setMethodHistories(numOfCommits);
			lineOfMethod.setMaxStmtAdded(maxAddedlines);

			lineOfMethod.setChurn(Math.max(addedLines-deletedLines, 0));
			lineOfMethod.setMaxChurn(maxChurn);
			lineOfMethod.setAvgChurn(avgChurn);

			//calcolo AVG_LOC_Added (è fatto solo sulle linee inserite)-----------------------
			for(int n=0; n<realAddedLinesOverCommits.size(); n++){

				totalAdded = totalAdded + realAddedLinesOverCommits.get(n);

			}
			if (totalAdded>0) {
				average = Math.floorDiv(totalAdded,realAddedLinesOverCommits.size());
			}



			//--------------------------------------------------
			lineOfMethod.setAvgStmtAdded(average);  
			lineOfMethod.setStmtAdded(totalAdded);
			lineOfMethod.setStmtDeleted(sumOfRealDeletedLOC);
			lineOfMethod.setMaxStmtDeleted(maxDeletedLines);

			if (numOfCommitsWithDel>0) {
				lineOfMethod.setAvgStmtDeleted(Math.floorDiv(sumOfRealDeletedLOC, numOfCommitsWithDel));
			}

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

			if(ticketWithAV) {
				setFixedVersionAndSetNFixMetric(bug,date,filesAffected);
			}
			else if(ticketWithoutAV) {
				setFixVersionWithoutAv(bug,date,filesAffected);
			}


		}

		private void setFixVersionWithoutAv(String bug, LocalDate date, ArrayList<String> filesAffected) {
			String fixedVers;
			for (int i = 0; i < tickets.size(); i++) {
				if(ticketsWithoutAV.get(i).getKey().equals(bug)) {
					//se è la prima versione
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

		private void setFixedVersionAndSetNFixMetric(String bug,LocalDate date,ArrayList<String> filesAffected) {
			String fixedVers;
			int count=0;
			for (int i = 0; i < tickets.size(); i++) {
				if(tickets.get(i).getKey().equals(bug)) {
					//se è la prima versione
					if (date.atStartOfDay().isEqual(fromReleaseIndexToDate.get(String.valueOf(1)))){
						fixedVers= String.valueOf(2);
					}
					else {
						fixedVers=iterateForFixVersion(date);
					}

					//set della metrica NFix
					for (int n = 0; n < arrayOfEntryOfClassDataset.size(); n++) {  
						//si aggiunge un'unità al numero di bug fixed in base alla versione associata
						if(filesAffected.contains(arrayOfEntryOfClassDataset.get(i).getFileName())) {

							//aumento contatore in modo da fermare il ciclo una volta osservate tutte le entry
							// di quel file per tutte le versioni 
							count++;

							if (arrayOfEntryOfClassDataset.get(n).getVersion()>=Integer.parseInt(fixedVers)) {
								arrayOfEntryOfClassDataset.get(n).setnFix(arrayOfEntryOfClassDataset.get(n).getnFix()+1);
							}

							//abbiamo controllato tutte le versioni del file
							if(count==Math.floorDiv(fromReleaseIndexToDate.size(),2)) {
								break;
							}
						}
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
				lineOfClassDataset.setWeightedAge(Math.floorDiv(age,
						lineOfClassDataset.getLOCTouched()));
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


				//doingCheckout of the local prefix to the file name (that depends to this program)

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

	private static void getNumberOfAuthorsOfMetod(String method, Integer version) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName+FINER_GIT+version);
		String command;

		try {

			command = ECHO+version+" \""+method+"\" && git shortlog -sn --all --until="
					+fromReleaseIndexToDate.get(String.valueOf(version))+" \""+method+"\"";	

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private static void getCondMetric(String method, Integer version) {
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

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}



	private static void getElseMetrics(String method, Integer version) {
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

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}


	private static void getFirstHalfMethodMetrics(String filename, Integer version) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName+FINER_GIT+version);
		String command;

		try {    

			if(version>1) {

				//ritorna release e metodo (con il nome di allora), poi righe aggiunte, elimiante e nome del metodo (ad oggi)
				command = ECHO+version+" "+filename+" && git log --follow "
						+"--since="+fromReleaseIndexToDate.get(String.valueOf(version-1))+ 
						" --until="+fromReleaseIndexToDate.get(String.valueOf(version))+FORMATNUMSTAT+"\""+filename+"\"";	
			}
			else{
				command = ECHO+version+" "+filename+" && git log --follow "+ 
						"--until="+fromReleaseIndexToDate.get(String.valueOf(version))+FORMATNUMSTAT+"\""+filename+"\"";	

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

	private static void writeClassMetricsResult() {
		String outname = projectName + "_Class.csv";
		//Name of CSV for output

		try (FileWriter fileWriter = new FileWriter(outname)){



			fileWriter.append("Project,Release,Class,Size(LOC), LOC_Touched,"
					+ "NR,NFix,NAuth,LOC_Added,MAX_LOC_Added,AVG_LOC_Added,"
					+ "Churn,MAX_Churn,AVG_Churn,ChgSetSize,MAX_ChgSet,AVG_ChgSet,Age,Weighted_Age,Actual_Defective");
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
				for (i=0;i< arrayOfEntryOfClassDataset.size();i++) {
					if (arrayOfEntryOfClassDataset.get(i).getFileName().equals(file)
							&&(arrayOfEntryOfClassDataset.get(i).getVersion()<Integer.parseInt(tick.getFixedVersion()))
							&&arrayOfEntryOfClassDataset.get(i).getVersion()>= predictedInjectedVersion) {

						arrayOfEntryOfClassDataset.get(i).setBuggy("YES");

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

		//Get JSON API for ticket with Type == “Bug” AND (status == “Closed” OR status == “Resolved”) AND Resolution == “Fixed” AND affected version = null in the project
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

				//se è la prima versione
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
				for (i=0;i< arrayOfEntryOfClassDataset.size();i++) {
					if (arrayOfEntryOfClassDataset.get(i).getFileName().equals(file)
							&&(arrayOfEntryOfClassDataset.get(i).getVersion()<Integer.parseInt(tick.getFixedVersion()))
							&&arrayOfEntryOfClassDataset.get(i).getVersion()>= Integer.parseInt(tick.getAffectedVersion())) {

						arrayOfEntryOfClassDataset.get(i).setBuggy("YES");

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

	private static void startToCalculateBugginessWithKnownAV() throws JSONException, IOException {

		//inizio operazioni per calcolo bugginess
		tickets=new ArrayList<>();
		Integer j=0;
		Integer total=1;
		JSONObject json ;
		JSONArray issues;
		Integer i=0;
		//Get JSON API for ticket with Type == “Bug” AND (status == “Closed” OR status == “Resolved”) AND Resolution == “Fixed” AND affectedVersion != null in the project
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

				//le righe seguenti sono necessarie perchè Jira potrebbe non fornire le releaseDate delle versioni affette

				for(int h=0;h<issues.getJSONObject(i%1000).getJSONObject(FIELDS).getJSONArray(VERSIONS).length();h++) {
					if(issues.getJSONObject(i%1000).getJSONObject(FIELDS).getJSONArray(VERSIONS).getJSONObject(h).has(RELEASE_DATE)) {
						//affVers è per es. 4.1.0
						affVersReleaseDate= issues.getJSONObject(i%1000).getJSONObject(FIELDS).getJSONArray(VERSIONS).getJSONObject(h).get(RELEASE_DATE).toString();
						break;
					}
				}
				//se la data dell'affected release non è stata presa allora si utilizerrà quella del bug più vicino temporalmente e se 
				// non è consistente con la created version allora si ignorerà il bug con le righe successive di check


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

		//se è la prima versione
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
			//ritorna gli id del commit e sul più vecchio si farà il checkout
			String command = "git rev-list "
					+ "--after="+fromReleaseIndexToDate.get(String.valueOf(version))+" master ";

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

		//CLASS CLASSIFICATION
		if(studyClassMetrics) {

			arrayOfEntryOfClassDataset= new ArrayList<>();
			//per ogni versione nella primà metà delle release
			for(int i=1;i<=Math.floorDiv(fromReleaseIndexToDate.size(),2);i++) {


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
				//cancellazione della directory clonata del progetto (che non è aggiornata)   
				recursiveDelete(new File(new File("").getAbsolutePath()+SLASH+projectName));

				//si fa il clone della versione odierna del progetto
				gitClone();	
			}
			catch (InterruptedException | IOException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
				System.exit(-1);
			}
			startToCalculateBugginessWithKnownAV();

			startToGetFixedVersWithAV();
			setBuggy();

			//startToGetCreatedVersWithoutAV();
			//checkFixedVersWithoutAV();
			//setBuggyWithoutAV();		 


			writeClassMetricsResult();
		}

		//----------------------------------------------------------------------------

		//METHOD CLASSIFICATION
		if(studyMethodMetrics) {

			fileMethodsOfTheCurrentRelease = new ArrayList<>();
			arrayOfEntryOfMethodDataset = new ArrayList<LineOfMethodDataset>();

			//per ogni versione nella primà metà delle release
			for(int rel=1;rel<=Math.floorDiv(fromReleaseIndexToDate.size(),2);rel++) {

				gitCheckoutAtGivenVersion(rel);

				Path directory = Paths.get(new File("").getAbsolutePath()+SLASH+projectName);
				//create repository with FinerGit------------------------------
				runFinerGitCloneForVersion(directory,rel); // commenta per non produrre i file metodo Finergit
				//-------------------------------------------------------------


				//search for java methods in the cloned repository         
				File folder = new File(projectName+"_FinerGit_"+rel);
				searchMethods(folder, fileMethodsOfTheCurrentRelease,rel);
				System.out.println("Founded "+fileMethodsOfTheCurrentRelease.size()+" methods");

				calculateMethodsMetrics(rel);
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
			for(int rel=1;rel<=Math.floorDiv(fromReleaseIndexToDate.size(),2);rel++) {

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
			//writeCommitMetricsResult();
		}

		//cancellazione directory clonata locale del progetto   
		recursiveDelete(new File(new File("").getAbsolutePath()+SLASH+projectName));
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



	private static void calculateMethodsMetrics(int version) {

		int count=0;
		//per ogni metodo nella release (version)
		for (String method : fileMethodsOfTheCurrentRelease) {
			count++;
			System.out.println("Inizio a calcolare metriche per metodo: "+count+"°");

			calculatingStmtMetricsMethodLevel = true;
			//il metodo getFirstHalfMethodMetrics() creerà anche l'arrayList di entry LineOfMethodDataset
			getFirstHalfMethodMetrics(method,version);
			calculatingStmtMetricsMethodLevel = false;

			calculatingElseMetricsMethodLevel=true;
			getElseMetrics(method,version);
			calculatingElseMetricsMethodLevel=false;

			calculatingCondMetricMethodLevel=true;
			getCondMetric(method,version);
			calculatingCondMetricMethodLevel=false;

			calculatingAuthMetricMethodLevel=true;
			getNumberOfAuthorsOfMetod( method,version);
			calculatingAuthMetricMethodLevel=false;
		}
	}

	private static void calculateCommitMetrics(int version) {

		int count=0;
		//per ogni commit nella release (version)
		for (String commit : commitOfCurrentRelease) {
			count++;

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

			/*calculatingTypeOfCommit=true;
             getTypeOfCommit(commit); da fare dopo
			calculatingTypeOfCommit=false;*/

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
			}

			//questo metodo utilizzerà dati condivisi per settare l'age di lineOfCommit 
			calculateAgeCommitLevel();

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



			fileWriter.append("Project,Release,Method,methodHistories,authors,"
					+ "stmtAdded,maxStmtAdded,avgStmtAdded,stmtDeleted,maxStmtDeleted,"
					+ "avgStmtDeleted,Churn,MaxChurn,AvgChurn,cond,elseAdded,elseDeleted,Actual_Defective");
			fileWriter.append("\n");
			for ( LineOfMethodDataset line : arrayOfEntryOfMethodDataset) {

				fileWriter.append(projectName);
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getVersion()));
				fileWriter.append(";");
				fileWriter.append(line.getMethod());
				fileWriter.append(";");
				fileWriter.append(String.valueOf(line.getMethodHistories()));
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

	private static void getTypeOfCommit(String commit) {
/*
		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		try {    
			//ritorna l'autore del commit 
			command = "git shortlog -s "+commit+"^! --grep=\""+\\\" ";	

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}*/
	}

	private static void getAuthorOfCommit(String commit) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		try {    
			//ritorna l'autore del commit 
			command = "git show -s --format=\"%an\"" +commit;	

			runCommandOnShell(directory, command);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}


	//per ottenere il numero di commit dell'author rigurdanti i "subsystem" fino al commit passato 
	private static void getSexpCommitLevel(String commit) {

		//directory da cui far partire il comando git    
		Path directory = Paths.get(new File("").getAbsolutePath()+
				SLASH+projectName);
		String command;

		try {
			command = "git shortlog -sn "+commit+" --author=\""+authorOfCommit+"\" -- ";
			//aggiungo i nome dei file toccati dal commit in esame
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
			command = "git log --date=short -2 "+commit+" --format=%cd -- "+file+"";

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
}
