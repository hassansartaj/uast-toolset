/**
 * 
 */
package model.evaluator;

import java.util.ArrayList;

import model.utils.FlightData;
import snt.oclsolver.IEvaluatorWrapper;
import snt.oclsolver.datatypes.IntegerValueTuple;
import snt.oclsolver.distance.ClassDiagramTestData;
import snt.oclsolver.distance.Problem;
import snt.oclsolver.distance.SimpleProblem;
import snt.oclsolver.experiment.ExperimentUtil;
import snt.oclsolver.search.IndividualFactory;
import snt.oclsolver.search.Search;
import snt.oclsolver.tuples.ClassifierTuple;
import snt.oclsolver.tuples.ClassifierValueTuple;
import snt.oclsolver.tuples.ValueTuple;

/**
 * A class to setup OCL evaluator, manipulate instance model, and evaluate OCL constraints. 
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class OCLEvaluator {
	private static ExperimentUtil expUtil = new ExperimentUtil();
	public OCLEvaluator() {
	}
	/**
	 * A method that loads UML model from the file to setup OCL evaluator 
	 * 
	 * @param filePath
	 */
	public static void setupOCLEvalutor(String filePath) {
		expUtil.loadModel(filePath);
		Problem.useRangeReduction = false;
		Problem.reinitializeEOCL = false;
	}

	public static void resetEOCL() {
		expUtil.resetStaticMembers();
	}

	/**
	 * A method that retrieves instance model from environment and returns in the form of classifier tuples 
	 * 
	 * @param modelPath
	 * @param constraint
	 * @return instanceModel: ArrayList<ClassifierTuple>
	 */
	public static ArrayList<ClassifierTuple> getInstanceModel(String modelPath, String constraint) {
		boolean useInstanceOptimization = false;
		expUtil.initializeEOCL(modelPath, constraint, useInstanceOptimization);
		Problem prob = new SimpleProblem(constraint,modelPath, null);
		//create random individual
		Search searchAlgo = new snt.oclsolver.search.AVM();
		IndividualFactory iFactory = searchAlgo.getiFactory();
		//set problem type to simple - just for evaluation
		iFactory.setType("simple");
		return prob.getQueryVariables();
	}

	/**
	 * A method that updates instance model in environment according to the given classifier tuples and the values
	 * 
	 * @param tuples
	 * @param flightData
	 */
	public static void updateInstanceModel(ArrayList<ClassifierTuple> tuples, FlightData flightData) {
		for(ClassifierTuple ct: tuples) {
			for(ClassifierValueTuple cvt: ct.getObjectTuples()) {
				for(ValueTuple vt: cvt.getAttributeValues()) {
					if(vt.getRelatedProperty().getRoleeName().equals(flightData.getProperty())) {
						if(vt instanceof IntegerValueTuple) {
							int value=0;
							if(flightData.getValue() instanceof Double) {
								Double dval = (Double) flightData.getValue();
								dval.intValue();
							}
							((IntegerValueTuple) vt).setValue(value);
						}
					}
				}
			}
		}
	}

	/**
	 * A method that evaluates OCL constraint on the instance model (in environment)
	 * 
	 * @param tuples
	 * @param constraint
	 * @return result
	 */
	public static String evaluateConstraint(ArrayList<ClassifierTuple> tuples, String constraint) {
		String result = null;
		ClassDiagramTestData data = ClassDiagramTestData.getInstance();
		try {
			data.updateObjectDiagram(tuples, constraint);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		IEvaluatorWrapper ieos = data.getIEOSWrapper();
		try {
			result = ieos.query(constraint);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
