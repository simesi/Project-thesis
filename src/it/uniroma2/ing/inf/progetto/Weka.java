package it.uniroma2.ing.inf.progetto;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.misc.HyperPipes;
import weka.classifiers.lazy.IB1;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.NumericTransform;
import weka.filters.unsupervised.attribute.ReplaceMissingWithUserConstant;
import weka.filters.unsupervised.attribute.SwapValues;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.estimators.KernelEstimator;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
//import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.trees.ADTree;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesSimple;
import weka.classifiers.rules.OneR;


public class Weka {


	private static final String ARFF=".arff";
	private static final String dirRQ2= "Results RQ2";
	private int numDefectiveTrain=0;
	private int numDefectiveTest=0;
	private Instances filteredTraining = null;
	private Instances testingFiltered = null;
	private int numAttrNoFilter=0;
	int percentInstOfMajorityClass=0;
	String projectName;
	Instances noFilterTraining;
	Instances testing;
	Evaluation eval;
	Resample resample;
	DecimalFormat numberFormat = new DecimalFormat("0.00");
	String myClassificator;
	int writeHeader=1;



	//questo metodo compara i risultati dei tre classificatori utilizzando la tecnica WalkForward
	public void doPrediction(String type,String projectName, ArrayList<String> idList,ArrayList<String> sizeList) {
		//System.out.println(type+" "+projectName); // Class DIRSERVER

		String name = projectName+"_"+type; 
		// ------------------------- 

		try {


			String arffNameFileTrain = "";
			String arffNameFileTest = "";

			//prima ci si crea un file arff da quello csv

			// load CSV
			CSVLoader loader = new CSVLoader();
			loader.setFieldSeparator(";");
			loader.setSource(new File(projectName+"_"+type+"_Train.csv"));
			Instances data = loader.getDataSet();


			//  re-order of Nominal Values in ClassIndex 
			if (data.attribute(data.numAttributes()-1).value(0).equals("YES")){
				SwapValues swp = new SwapValues();
				swp.setAttributeIndex("last");
				swp.setInputFormat(data);
				data = Filter.useFilter(data, swp);
			}

			// re-order of Nominal Values for Fix commit metric 
			if (type.equals("Commit")&& data.attribute(9).value(0).equals("YES")){
				SwapValues swp = new SwapValues();
				swp.setAttributeIndex("9");
				swp.setInputFormat(data);
				data = Filter.useFilter(data, swp);
			}


			//Normalization 
			NumericTransform normFilter = new NumericTransform();
			normFilter.setMethodName("log10");
			normFilter.setAttributeIndices("first-last");
			normFilter.setInputFormat(data);
			data = Filter.useFilter(data, normFilter);

			//replace missing values generated by the normalization
			ReplaceMissingWithUserConstant repl= new ReplaceMissingWithUserConstant();
			repl.setNumericReplacementValue("0");
			repl.setNominalStringReplacementValue("NO");
			repl.setInputFormat(data);
			data =Filter.useFilter(data,repl);

			// save ARFF
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);

			arffNameFileTrain = projectName+"_"+type+"_Train"+ARFF;


			saver.setFile(new File(arffNameFileTrain));
			saver.writeBatch();


			//adesso ci si crea l'arff per il test
			// load CSV
			loader = new CSVLoader();
			loader.setFieldSeparator(";");
			loader.setSource(new File(projectName+"_"+type+"_Test.csv"));
			data = loader.getDataSet();

			//  re-order of Nominal Values in ClassIndex
			if (data.attribute(data.numAttributes()-1).value(0).equals("YES")){
				SwapValues swp = new SwapValues();
				swp.setAttributeIndex("last");
				swp.setInputFormat(data);
				data = Filter.useFilter(data, swp);
			}

			// re-order of Nominal Values for Fix commit metric 
			if (type.equals("Commit")&& data.attribute(9).value(0).equals("YES")){
				SwapValues swp = new SwapValues();
				swp.setAttributeIndex("9");
				swp.setInputFormat(data);
				data = Filter.useFilter(data, swp);
			}

			//Normalization 
			normFilter = new NumericTransform();
			normFilter.setMethodName("log10");
			normFilter.setAttributeIndices("first-last");
			normFilter.setInputFormat(data);
			data = Filter.useFilter(data, normFilter);

			//replace missing values generated by the normalization
			repl= new ReplaceMissingWithUserConstant();
			repl.setNumericReplacementValue("0");
			repl.setNominalStringReplacementValue("NO");
			repl.setInputFormat(data);
			data =Filter.useFilter(data,repl);

			// save ARFF
			saver = new ArffSaver();
			saver.setInstances(data);


			arffNameFileTest = projectName+"_"+type+"_Test"+ARFF;


			saver.setFile(new File(arffNameFileTest));
			saver.writeBatch();

			//load train dataset
			DataSource source1 = new DataSource(arffNameFileTrain);
			Instances training = source1.getDataSet();

			training.attribute(arffNameFileTest);
			//---------------------------
			//Feature Selection 
			//create AttributeSelection object
			AttributeSelection filter = new AttributeSelection();
			//create evaluator and search algorithm objects
			CfsSubsetEval subEval = new CfsSubsetEval();
			GreedyStepwise search = new GreedyStepwise();
			//set the algorithm to search backward
			search.setSearchBackwards(true);
			//set the filter to use the evaluator and search algorithm
			filter.setEvaluator(subEval);
			filter.setSearch(search);



			//specify the dataset
			filter.setInputFormat(training);

			//qui si crea il training filtrato
			training = Filter.useFilter(training, filter);

			//---------------------------------	


			//load test dataset
			DataSource source2 = new DataSource(arffNameFileTest);
			Instances myTest = source2.getDataSet();

			//stima numero attributi con i filtri
			int numAttr = training.numAttributes();
			training.setClassIndex(numAttr - 1); //leviamo 1 perch� l'ultima colonna la vogliamo stimare 


			myTest= Filter.useFilter(myTest, filter);
			myTest.setClassIndex(numAttr - 1);


			int numtesting = myTest.numInstances();

			// make sure they're compatible
			String msg = training.equalHeadersMsg(myTest);
			if (msg != null)
				throw new Exception(msg);


			//-----------------
			//SMOTE

			SMOTE smote = new SMOTE();
			smote.setInputFormat(training);

			training = Filter.useFilter(training, smote); //Apply SMOTE on Dataset


			//-----------------			

			Classifier classifier = null;
			FileWriter fileWriter = null;
			int numOfClassifiers = 19;
			//per ogni classificatore
			//for(int n=1;n<numOfClassifiers+1;n++) {
			for(int n=1;n<5;n++) {
				if(n==1) {


					//Bayes Network---------------
					classifier = new BayesNet();
					myClassificator ="BN";
					classifier.buildClassifier(training); //qui si fa il training



					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");


				}
				
				else if (n==2) {
					//RandomForest---------------
					 classifier = new RandomForest(); //scelgo come classificatore RandomForest
					myClassificator ="RF";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					 fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

					

				}
				else if (n==3) {
					//SVM---------------
					 classifier = new SMO(); //scelgo come classificatore SVM
					myClassificator ="SVM";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					 fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}

				else if (n==4) {
					//J48---------------
					 classifier = new J48(); //scelgo come classificatore random forest
					myClassificator ="J48";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}/*
				
				else if (n==5) {
					//Decision Stump---------------
					 classifier = new DecisionStump(); 
					myClassificator ="DS";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==6) {
					//Decision Table---------------
					 classifier = new DecisionStump(); 
					myClassificator ="DT";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				
				else if (n==7) {
					//Hyper Pipes---------------
					 classifier = new HyperPipes(); 
					myClassificator ="HP";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				
				else if (n==8) {
					//IB1---------------
					 classifier = new IB1(); 
					myClassificator ="IB1";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==9) {
					//IBk---------------
					 classifier = new IBk(); 
					myClassificator ="IBk";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==10) {
					//J48.PART---------------
					 classifier = new PART(); 
					myClassificator ="PART";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==11) {
					//LOGIT BOOST---------------
					 classifier = new LogitBoost(); 
					myClassificator ="LB";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==12) {
					//KSTAR---------------
					 classifier = new KStar(); 
					myClassificator ="KS";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==13) {
					//LOGISTIC---------------
					 classifier = new Logistic(); 
					myClassificator ="LOG";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==14) {
					//NAIVE BAYES SIMPLE---------------
					 classifier = new NaiveBayesSimple(); 
					myClassificator ="NBS";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==15) {
					//NAIVE BAYES ---------------
					 classifier = new NaiveBayes(); 
					myClassificator ="NB";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				
				else if (n==16) {
					//Neural Network ---------------
					 classifier = new MultilayerPerceptron(); 
					myClassificator ="NN";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				
				else if (n==17) {
					//OneR ---------------
					 classifier = new OneR(); 
					myClassificator ="O";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==18) {
					//VotedPerceptron ---------------
					 classifier = new VotedPerceptron(); 
					myClassificator ="VP";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}
				else if (n==19) {
					//ADTree ---------------
					 classifier = new ADTree(); 
					myClassificator ="AT";
					classifier.buildClassifier(training); //qui si fa il training


					//ora si scrive l'header del file csv coi risultati
					fileWriter = new FileWriter(dirRQ2+"\\"+name+"_"+myClassificator+".csv");

				}*/
				
				
				//---------------------------------------//
				fileWriter.append("ID;Size;Predicted;Actual");
				fileWriter.append("\n");


				// Loop over each test instance.
				for (int i = 0; i < numtesting; i++)
				{
					// Get the true class label from the instance's own classIndex.
					String trueClassLabel = 
							myTest.instance(i).toString(myTest.classIndex());

					// Get the prediction probability distribution.
					double[] predictionDistribution = 
							classifier.distributionForInstance(myTest.instance(i)); 

					// Get the probability.
					double predictionProbability = 
							predictionDistribution[1]; //1==prob YES

					fileWriter.append(idList.get(i));
					fileWriter.append(";");
					fileWriter.append(sizeList.get(i));
					fileWriter.append(";");
					fileWriter.append(String.format("%6.3f",predictionProbability).replace(',', '.'));
					fileWriter.append(";");
					fileWriter.append(trueClassLabel);
					fileWriter.append("\n");


				}

				fileWriter.close();

			}

			saver.resetWriter();
			myTest.clear();
			data.clear();

			//recursiveDelete(new File(projectName+"_"+type+"_Train"+ARFF));
			//recursiveDelete(new File(projectName+"_"+type+"_Test"+ARFF));
			//recursiveDelete(new File(projectName+"_"+type+"_Train.csv"));			
			//recursiveDelete(new File(projectName+"_"+type+"_Test.csv"));



		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} 



	}


	public void doClassificationMilestone3(int maxversion, String projectName) {



		this.projectName=projectName;

		try 
		{



			for(int version=2;version<=maxversion;version++) {


				DataSource source = new DataSource(projectName +version+ARFF);

				DataSource source2 = new DataSource(projectName +version+ARFF);

				this.noFilterTraining = source.getDataSet();
				this.testing = source2.getDataSet();


				//stima senza filtri
				numAttrNoFilter = noFilterTraining.numAttributes();
				noFilterTraining.setClassIndex(numAttrNoFilter - 1);
				this.testing.setClassIndex(numAttrNoFilter - 1);


				//senza e con feature selection
				for (int fs=0;fs<=1;fs++) {

					doOrNotFeatureSelection(fs,noFilterTraining,testing);


					//senza balancing o con i tre tipi di balancing			
					for(int balancing=1;balancing<=4;balancing++) {


						//per ogni classificatore
						for(int n=1;n<=3;n++) {


							classifyAndWrite(n,balancing,fs,version);



						}//per ogni classificatore

					}//per ogni sampling
				}//per ogni fs
			}//per ogni versione
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); 
		}



	}


	private void classifyAndWrite(int n,int balancing,int fs,int version) {


		startClassificator(fs,balancing,n);

		//--------------------------------------------------------------
		//ora si scrive file csv coi risultati
		String name = projectName+" Deliverable 2 Milestone 3.csv";
		try (
				//True = Append to file, false = Overwrite
				FileWriter fileWriter = new FileWriter(name,true);
				)
		{
			if(writeHeader==1) {
				fileWriter.append("Dataset,#Training Release,%Training,%Defective in training,"
						+ "%Defective in testing,classifier,balancing,Feature Selection,TP,FP,TN,FN,"
						+ "Precision,Recall,ROC Area, Kappa");

				fileWriter.append("\n");
				writeHeader--;

			}


			fileWriter.append(projectName);
			fileWriter.append(",");
			fileWriter.append(String.valueOf(version-1));
			fileWriter.append(",");
			fileWriter.append(String.valueOf((String.format("%.3f", (double) (noFilterTraining.size()/(double)(testing.size()+noFilterTraining.size()))))).replace(',', '.'));//modifica con sampling
			fileWriter.append(",");
			fileWriter.append(String.valueOf((String.format("%.3f",(double)numDefectiveTrain/(double)noFilterTraining.size()))).replace(',', '.'));
			fileWriter.append(",");
			fileWriter.append(String.valueOf((String.format("%.3f",(double)numDefectiveTest/(double)testing.size()))).replace(',', '.'));
			fileWriter.append(",");
			fileWriter.append(myClassificator);
			fileWriter.append(",");
			fileWriter.append(String.valueOf(String.valueOf(balancing)));
			fileWriter.append(",");
			fileWriter.append(String.valueOf(fs));
			fileWriter.append(",");
			fileWriter.append(String.valueOf((int)eval.numTruePositives(1)));
			fileWriter.append(",");
			fileWriter.append(String.valueOf((int)eval.numFalsePositives(1)));
			fileWriter.append(",");
			fileWriter.append(String.valueOf((int)eval.numTrueNegatives(1)));
			fileWriter.append(",");
			fileWriter.append(String.valueOf((int)eval.numFalseNegatives(1)));
			fileWriter.append(",");
			fileWriter.append(String.valueOf(numberFormat.format(eval.precision(1)).replace(',', '.')));
			fileWriter.append(",");
			fileWriter.append(String.valueOf(numberFormat.format(eval.recall(1)).replace(',', '.')));
			fileWriter.append(",");
			fileWriter.append(String.valueOf(numberFormat.format(eval.areaUnderROC(1)).replace(',', '.')));
			fileWriter.append(",");
			fileWriter.append(String.valueOf(numberFormat.format(eval.kappa()).replace(',', '.')));
			fileWriter.append("\n");
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(-1); 
		}


	}


	private void startClassificator(int fs,int balancing,int n) {
		/*



		if(n==1) {
			//NaiveBayes---------------
			NaiveBayes classifier = new NaiveBayes(); //scelgo come classificatore il naive bayes
			myClassificator ="NaiveBayes";
			classify(classifier, fs, balancing);
		}

		else if (n==2) {
			//RandomForest---------------
			RandomForest classifier = new RandomForest(); //scelgo come classificatore RandomForest
			myClassificator ="RandomForest";
			classify(classifier, fs, balancing);
		}

		else {
			//Ibk---------------
			myClassificator ="IBk";
			IBk classifier = new IBk(); //scelgo come classificatore Ibk

			classify(classifier, fs, balancing );
		}



		 */


	}


	private void classify(Classifier classifier,int fs,int balancing) {

		try {

			if(fs==0) {
				classifier.buildClassifier(noFilterTraining); //qui si fa il training non filtrato
				//no resample
				if(balancing==1) {										
					eval =new Evaluation(testing);	
					eval.evaluateModel(classifier, testing);
				}
				//Oversampling
				else if(balancing==2) {

					this.resample = new Resample();
					resample.setInputFormat(noFilterTraining);
					resample.setNoReplacement(false);
					FilteredClassifier fc = new FilteredClassifier();
					fc.setClassifier(classifier);
					String[] opts = new String[]{ "-B", "1.0", "-Z", ""+percentInstOfMajorityClass+""};
					resample.setOptions(opts);
					fc.setFilter(resample);
					fc.buildClassifier(noFilterTraining);
					eval = new Evaluation(testing);	
					eval.evaluateModel(fc, testing); //sampled
				}

				//undersampling
				else if(balancing==3) {

					FilteredClassifier fc = new FilteredClassifier();
					resample = new Resample();

					setUndersampling(fc,resample,classifier,fs);					               
				}

				else if(balancing==4) {

					resample = new Resample();
					resample.setInputFormat(noFilterTraining);
					FilteredClassifier fc = new FilteredClassifier();
					SMOTE smote = new SMOTE();
					smote.setInputFormat(noFilterTraining);
					fc.setFilter(smote);
					fc.buildClassifier(noFilterTraining);
					eval =new Evaluation(testing);	
					eval.evaluateModel(fc, testing);	

				}


			}
			else if(fs==1) {
				classifier.buildClassifier(filteredTraining); //qui si fa il training filtrato

				if(balancing==1) {
					eval =new Evaluation(testing);
					eval.evaluateModel(classifier, testingFiltered);

				}

				//Oversampling
				else if(balancing==2) {

					resample = new Resample();
					resample.setInputFormat(filteredTraining);
					resample.setNoReplacement(false);
					FilteredClassifier fc = new FilteredClassifier();
					fc.setClassifier(classifier);
					String[] opts = new String[]{ "-B", "1.0", "-Z", ""+percentInstOfMajorityClass+""};
					resample.setOptions(opts);
					fc.setFilter(resample);
					fc.buildClassifier(filteredTraining);
					eval = new Evaluation(testing);	
					eval.evaluateModel(fc, testingFiltered); //sampled
				}
				//undersampling
				else if(balancing==3) {

					resample = new Resample();
					FilteredClassifier fc = new FilteredClassifier();
					setUndersampling(fc,resample,classifier,fs);			               
				}

				else if(balancing==4) {

					resample = new Resample();
					resample.setInputFormat(noFilterTraining);
					FilteredClassifier fc = new FilteredClassifier();
					SMOTE smote = new SMOTE();
					smote.setInputFormat(noFilterTraining);
					fc.setFilter(smote);
					fc.buildClassifier(filteredTraining);
					eval =new Evaluation(testing);	
					eval.evaluateModel(fc, testingFiltered);	

				}								



			}//fine fs
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void setUndersampling(FilteredClassifier fc, Resample resample,Classifier classifier,int fs) {

		try {
			if(fs==0) {
				resample.setInputFormat(noFilterTraining);
				fc.setClassifier(classifier);
				SpreadSubsample  spreadSubsample = new SpreadSubsample();
				String[] opts = new String[]{ "-M", "1.0"};
				spreadSubsample.setOptions(opts);
				fc.setFilter(spreadSubsample);
				fc.buildClassifier(noFilterTraining);
				eval =new Evaluation(testing);	
				eval.evaluateModel(fc, testing);

			}
			else {
				resample.setInputFormat(noFilterTraining);

				fc.setClassifier(classifier);
				SpreadSubsample  spreadSubsample = new SpreadSubsample();
				String[] opts = new String[]{ "-M", "1.0"};
				spreadSubsample.setOptions(opts);
				fc.setFilter(spreadSubsample);
				fc.buildClassifier(filteredTraining);
				eval =new Evaluation(testing);	
				eval.evaluateModel(fc, testingFiltered);	
			}

		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void doOrNotFeatureSelection(int fs, Instances train, Instances testing)  {
		//fs=1 allora con feature selection
		if(fs==1) {

			//create AttributeSelection object
			AttributeSelection filter = new AttributeSelection();
			//create evaluator and search algorithm objects
			CfsSubsetEval subEval = new CfsSubsetEval();
			GreedyStepwise search = new GreedyStepwise();
			//set the algorithm to search backward
			search.setSearchBackwards(true);
			//set the filter to use the evaluator and search algorithm
			filter.setEvaluator(subEval);
			filter.setSearch(search);


			try {

				//specify the dataset
				filter.setInputFormat(train);

				//qui si crea il training filtrato
				this.filteredTraining = Filter.useFilter(train, filter);

				//stima numero attributi con i filtri
				int numAttrFiltered = filteredTraining.numAttributes();

				//evaluation with filtered
				filteredTraining.setClassIndex(numAttrFiltered - 1);


				testingFiltered = Filter.useFilter(testing, filter);


				testingFiltered.setClassIndex(numAttrFiltered - 1);


				//qui si contano le istanze positive...
				this.percentInstOfMajorityClass=calculateDefectiveInInstances(filteredTraining,testingFiltered,numAttrFiltered);


			} catch (Exception e) {
				e.printStackTrace();
			}



		}//fine if


		if(fs==0) {

			//qui si contano le istanze positive...
			this.percentInstOfMajorityClass= calculateDefectiveInInstances(train,testing,numAttrNoFilter);

		}



	}


	private int calculateDefectiveInInstances(Instances train, Instances test, int numAttrFiltered) {
		this.numDefectiveTrain=0;
		this.numDefectiveTest=0;

		//ora si contano il numero di buggy nelle Instances
		for(Instance instance: train){
			if(instance.stringValue(numAttrFiltered-1).equals("YES")) {
				this.numDefectiveTrain++;
			}
		}
		for(Instance instance: test){
			if(instance.stringValue(numAttrFiltered-1).equals("YES")) {
				this.numDefectiveTest++;
			}
		}

		return 2*Math.max(this.numDefectiveTrain/train.size(),1-this.numDefectiveTrain/train.size())*100;


	}

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


}
