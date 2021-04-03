package it.uniroma2.ing.inf.progetto;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.lazy.IBk;


public class Weka {

	private static final String TRAINING_FOR_RELEASE =" Training for Release "; 
	private static final String TESTING_FOR_RELEASE =" Testing for Release ";
	private static final String ARFF=".arff";
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
	public void doPrediction(String type,String projectName) {
		//System.out.println(type+" "+projectName); // Class DIRSERVER
		
		String name = projectName+" Result RQ2.csv"; 
		// ------------------------- 

		try (   	//True = Append to file, false = Overwrite
				FileWriter fileWriter = new FileWriter(name,true);
				)
		{
			fileWriter.append("ID,type,size,ML_Model,Predicted,Bugginess");
			fileWriter.append("\n");


			String arffNameFileTrain = "";
			String arffNameFileTest = "";

			//prima ci si crea un file arff da quello csv

			// load CSV
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(projectName+TRAINING_FOR_RELEASE+".csv"));
			Instances data = loader.getDataSet();

			// save ARFF
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);


			arffNameFileTrain = projectName+TRAINING_FOR_RELEASE+ARFF;


			saver.setFile(new File(arffNameFileTrain));
			saver.writeBatch();




			//adesso ci si crea l'arff per il my_test

			// load CSV
			loader = new CSVLoader();
			loader.setSource(new File(projectName+TESTING_FOR_RELEASE+".csv"));
			data = loader.getDataSet();

			// save ARFF
			saver = new ArffSaver();
			saver.setInstances(data);


			arffNameFileTest = projectName +TESTING_FOR_RELEASE+ARFF;


			saver.setFile(new File(arffNameFileTest));
			saver.writeBatch();



			//load datasets
			DataSource source1 = new DataSource(arffNameFileTrain);
			Instances training = source1.getDataSet();

			DataSource source2 = new DataSource(arffNameFileTest);
			Instances myTest = source2.getDataSet();

			int numAttr = training.numAttributes();
			training.setClassIndex(numAttr - 1); //leviamo 1 perchè l'ultima colonna la vogliamo stimare 
			myTest.setClassIndex(numAttr - 1);

			//per ogni classificatore
			for(int n=1;n<=3;n++) {
				if(n==1) {

					//NaiveBayes---------------
					NaiveBayes classifier = new NaiveBayes(); //scelgo come classificatore il naive bayes
					myClassificator ="NaiveBayes";
					classifier.buildClassifier(training); //qui si fa il training

					eval = new Evaluation(myTest);	

					eval.evaluateModel(classifier, myTest); 
				}

				else if (n==2) {
					//RandomForest---------------
					RandomForest classifier = new RandomForest(); //scelgo come classificatore RandomForest
					myClassificator ="RandomForest";
					classifier.buildClassifier(training); //qui si fa il training

					eval = new Evaluation(myTest);	

					eval.evaluateModel(classifier, myTest); 
				}
				else if (n==3) {
					//Ibk---------------
					IBk classifier = new IBk(); //scelgo come classificatore Ibk
					myClassificator ="IBk";
					classifier.buildClassifier(training); //qui si fa il training

					eval = new Evaluation(myTest);	

					eval.evaluateModel(classifier, myTest); 
				}

				//ora si scrive file csv coi risultati


				fileWriter.append(projectName);
				fileWriter.append(",");
				fileWriter.append(String.valueOf(1)); //version
				fileWriter.append(",");
				fileWriter.append(myClassificator);
				fileWriter.append(",");
				fileWriter.append(String.valueOf(numberFormat.format(eval.precision(1))).replace(',', '.'));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(numberFormat.format(eval.recall(1))).replace(',', '.'));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(numberFormat.format(eval.areaUnderROC(1))).replace(',', '.'));
				fileWriter.append(",");
				fileWriter.append(String.valueOf(numberFormat.format(eval.kappa())).replace(',', '.'));
				fileWriter.append("\n");

			}


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


				DataSource source = new DataSource(projectName +TRAINING_FOR_RELEASE+version+ARFF);

				DataSource source2 = new DataSource(projectName +TESTING_FOR_RELEASE+version+ARFF);

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

}
