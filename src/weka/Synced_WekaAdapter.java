package weka;

import global.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.core.Instances;

/**
 * Class that is responsible for running the tests and training on the ANN and generating results. 
 * This class acts as the classifier for the project which uses the arrtibutes generated by featureExtraction 
 * and compiled by AnalysisFileWriter.
 * @author mdatla
 *
 */
public class WekaAdapter implements Prediction{

	private Instances trainSet;
	private Instances testSet;
	private String trainSetPath;
	private String testSetPath;

	public WekaAdapter(String trainSetPath){
		this.trainSetPath=trainSetPath;
	}
	public WekaAdapter(String trainSetPath, String TestSetPath){
		this.trainSetPath=trainSetPath;
		this.testSetPath=TestSetPath;

	}

	public void readDataset() throws Exception{

		trainSet = new Instances(new FileReader(this.trainSetPath));
		trainSet.setClassIndex(trainSet.numAttributes()-1);

		testSet = new Instances(new FileReader(this.testSetPath));
		testSet.setClassIndex(testSet.numAttributes()-1);

	}

	public void runDecisionTree() throws Exception{
		Instances test,train;
		trainSet.randomize(new Random(123));

		trainSet.deleteAttributeAt(0);

		for(int i=0;i<10;i++){

			double correct = 0;

			test = trainSet.testCV(10,i);
			train = trainSet.trainCV(10, i);

			J48 tree = new J48();
			tree.setReducedErrorPruning(true);
			tree.setConfidenceFactor((float) 0.15);

			System.out.println("Confidence factor for pessimistic pruning: " + tree.getConfidenceFactor());
			System.out.println("Number of instances per leaf: " + tree.getMinNumObj());
			System.out.println("Use reduced error pruning: " + tree.getReducedErrorPruning());
			System.out.println("Number of folds for post-pruning: " + tree.getNumFolds());
			System.out.println("Use binary splits: " + tree.getBinarySplits());
			System.out.println("Do not use subtree raising: " + tree.getSubtreeRaising());
			System.out.println("Use laplace smoothing: " + tree.getUseLaplace());

			tree.buildClassifier(train);

			System.out.println(tree);

			double total = test.numInstances();

			System.out.println(correct);
			System.out.println(total);


			double testCorrect = 0,trainCorrect =0;
			double testTotal = test.numInstances();
			double trainTotal =  train.numInstances();

			for(int k=0; k<test.numInstances();k++){
				if (test.instance(k).classValue() == tree.classifyInstance(test.instance(k))) {
					correct++;
				}
			}
			System.out.println("Accuracy of the Decision Tree is: " + (correct / total)*100 +"%");
			System.out.println("==================================================================================");
		}
	}

	/**
	 * This methods uses the *.arff files as a dataset and trains the ANN and then uses the same ANN to test the images in the testing dataset.
	 * Training time is set to 1000, and the results are compiled into a *.csv file. 
	 * @throws Exception
	 */
	public void runNeuralNetwork() throws Exception{
		Instances test,train,testCopy,trainCopy;
		Instances test2 = null;

		int iterations =1000;


		trainSet.randomize(new Random(123));

		Instances trainSetCopy = new Instances(trainSet, 0, trainSet.numInstances());
		//trainSet.deleteAttributeAt(0);
		Instances testSetCopy = new Instances(testSet, 0, testSet.numInstances());
		testSet.deleteAttributeAt(0);


		File trueSnippetOut = new File(Constants.data,"TrueSnippets.txt");
		File falseSnippetOut = new File(Constants.data,"FalseSnippets.txt");
		File truePageOut = new File(Constants.data,"TruePages.txt");
		File falsePageOut = new File(Constants.data,"FalsePages.txt");
		BufferedWriter trueSnippetStream = new BufferedWriter(new FileWriter(trueSnippetOut, false));
		BufferedWriter falseSnippetStream = new BufferedWriter(new FileWriter(falseSnippetOut, false));
		BufferedWriter truePageStream = new BufferedWriter(new FileWriter(truePageOut, false));
		BufferedWriter falsePageStream = new BufferedWriter(new FileWriter(falsePageOut, false));
		StringBuilder sbSnippetTrue = new StringBuilder();
		StringBuilder sbSnippetFalse = new StringBuilder();
		StringBuilder sbPageTrue = new StringBuilder();
		StringBuilder sbPageFalse = new StringBuilder();

		File inFile = new File(Constants.data,"SnippetList_Sorted.txt");

		
		System.out.println("Creating txt file...");

		MultilayerPerceptron network = new MultilayerPerceptron();
		network.setTrainingTime(iterations);

		train = trainSet;
		test = testSet;
		trainCopy = trainSetCopy;
		testCopy = testSetCopy;


		System.out.println("Training and Testing Complete...");
		System.out.println("Building Multilayer Perception...");

		network.buildClassifier(train);

		//	System.out.println("Calculating Test accuracy...");

		ArrayList<String> trueParentList = new ArrayList<String>();
		ArrayList<String> falseParentList= new ArrayList<String>();
		ArrayList<String> fullList = new ArrayList<String>();
		int pos_num=0, neg_num=0;
		for (int k = 0; k < test.numInstances(); k++) {
				String[] snippetName1= testCopy.instance(k).toString(0).split("_");
				String Name = snippetName1[0]+"_"+snippetName1[1]+"_"+snippetName1[2]+"_"+snippetName1[3];
				if(!fullList.contains(Name)){
					fullList.add(Name);
				}
				
				if(network.classifyInstance(test.instance(k))==1.0){
				pos_num++;
				String[] snippetName= testCopy.instance(k).toString(0).split("_");
				String parentName = snippetName[0]+"_"+snippetName[1]+"_"+snippetName[2]+"_"+snippetName[3];
				//System.out.print("True, "+pos_num+", "+testCopy.instance(k).toString(0)+", "+parentName+", ");
				if(!trueParentList.contains(parentName)){
					trueParentList.add(parentName);
					System.out.println("Added");
				} else{
					System.out.println("Not Added");
				}
				sbSnippetTrue.append(testCopy.instance(k).toString(0)+"\n");
			} else /*if(network.classifyInstance(test.instance(k))==0.0)*/{
				neg_num++;
				String[] snippetName= testCopy.instance(k).toString(0).split("_");
				String parentName = snippetName[0]+"_"+snippetName[1]+"_"+snippetName[2]+"_"+snippetName[3];
				//System.out.print("False, "+neg_num+", "+testCopy.instance(k).toString(0)+", "+parentName+", ");
				if(!falseParentList.contains(parentName) && !trueParentList.contains(parentName)){
					falseParentList.add(parentName);
					System.out.println("Added");
				}else{
                                        System.out.println("Not Added");
                                }

				sbSnippetFalse.append(testCopy.instance(k).toString(0)+"\n");
			}
		}

		System.out.println("Full List ----------------------");
		for(String s: fullList){
			System.out.println(s);
		}
		System.out.println(fullList.size());
		
		//System.out.println("Before:\n TrueParentList:\n----------");
		//for(String s : trueParentList){
		//	System.out.println(s);
		//}
		//System.out.println("Size:"+trueParentList.size());
		//System.out.println("FalseParentList:\n -----------");
		//for(String s : falseParentList){
                //        System.out.println(s);
                //}
		//System.out.println("Size:"+falseParentList.size());
		
		ArrayList<String> falseParentTemp = new ArrayList<String>();
		for (String s : falseParentList){
			falseParentTemp.add(s);
		}
		for(String falseName : falseParentTemp){
			if (trueParentList.contains(falseName)){
		//		falseParentList.remove(falseName);
			}
		}

		//System.out.println("After:\n TrueParentList:\n---------");
                //for(String s : trueParentList){
                //        System.out.println(s);
                //}
		//System.out.println("Size:"+trueParentList.size());
                //System.out.println("FalseParentList:\n--------");
                //for(String s : falseParentList){
                //       System.out.println(s);
                //}
		//System.out.println("Size:"+falseParentList.size());



		trueSnippetStream.append(sbSnippetTrue.toString());
		trueSnippetStream.flush();
		trueSnippetStream.close();
			
		falseSnippetStream.append(sbSnippetFalse.toString());
		falseSnippetStream.flush();
		falseSnippetStream.close();

		for(String s: trueParentList){
			sbPageTrue.append(s+"\n");
		}

		truePageStream.append(sbPageTrue.toString());
		truePageStream.flush();
		truePageStream.close();
			
		for(String s: falseParentList){
			sbPageFalse.append(s+"\n");
		}

		falsePageStream.append(sbPageFalse.toString());
		falsePageStream.flush();
		falsePageStream.close();
		
		
		System.out.println("Classification Successful. \nEvaluating:");

		//Evaluation classes is used to calculate accuracies, and other useful machine learning statistics.

		//		Evaluation eval = new Evaluation(train);
		//		eval.evaluateModel(network, test);
		//		System.out.println("Accuracy: "+((1-eval.errorRate())*100));
		//		System.out.println("Precision: "+eval.precision(1)*100);
		//		System.out.println("Recall: "+eval.recall(1)*100);
		//		System.out.println("True Negative Rate: "+eval.trueNegativeRate(1)*100);
		//		System.out.println("False Positive Rate: "+eval.falsePositiveRate(1)*100);
		//		System.out.println("False Negative Rate: "+eval.falseNegativeRate(1)*100);
		//		System.out.println("True Positives: "+eval.numTruePositives(1));
		//		System.out.println("True Negatives: "+eval.numTrueNegatives(1));
		//		System.out.println("False Positives: "+eval.numFalsePositives(1));
		//		System.out.println("False Negatives: "+eval.numFalseNegatives(1));

		System.out.println("Evaluation Complete. \nOutputting Results");

		// FinalResultGenerator is imported to generate results for the parent newspapers and its statistics.

		FinalResultGenerator rg = new FinalResultGenerator();
		rg.generateResults();




	}
	@Override
	public double actual() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public double predicted() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public double weight() {
		// TODO Auto-generated method stub
		return 0;
	}

}
