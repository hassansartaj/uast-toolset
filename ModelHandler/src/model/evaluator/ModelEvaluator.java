package model.evaluator;

import java.util.ArrayList;
import java.util.HashMap;

import model.utils.ConstraintsLoader;
import model.utils.FlightData;
import snt.oclsolver.tuples.ClassifierTuple;

public class ModelEvaluator {
	//TODO: change paths
	private static String modelPath = "models/UAVModel.uml";
	private static HashMap<String, String> constraints = null;
	
	public ModelEvaluator() {
		OCLEvaluator.setupOCLEvalutor(modelPath);
		constraints = ConstraintsLoader.loadConstraints("constraints/copter-constraints.ocl");
	}


	public HashMap<String, String> evaluateModel(String state, HashMap<String, Integer> data) {
		int failedCount = 0;
		HashMap<String,String> evalresult = new HashMap<>();
		evalresult.put("count", "0");
		evalresult.put("failed", "");
		evalresult.put("passed", "");
		
//		OCLEvaluator.setupOCLEvalutor(modelPath);
//		constraints = ConstraintsLoader.loadConstraints("constraints/copter-constraints.ocl");
//		String stateConstraints = constraints.get(state);
//		ArrayList<FlightData> flightData = constructFlightData(data);
		
		String stateConstraints = extractStateInvariants(state);
		String [] allConstraints = null;
				
		if(stateConstraints != null)
			allConstraints = stateConstraints.split(";");
		for(String constraint : allConstraints) {
			FlightData flightData = getFlightData(constraint, data);
			if(flightData==null) continue;
			
			System.out.println("[Evaluating] "+constraint);
			ArrayList<ClassifierTuple> tuples = OCLEvaluator.getInstanceModel(modelPath, constraint);
			//get UAV data and update the following
			OCLEvaluator.updateInstanceModel(tuples, flightData);
			String result = OCLEvaluator.evaluateConstraint(tuples, constraint);
			if(!result.equals("true") && !result.equals("false"))
				result = "false";

			if(!result.equals("true")){
				System.out.println("[Failed] "+constraint);
				failedCount++;
				evalresult.put("failed", evalresult.get("failed")+constraint+"!@!");
				evalresult.put("count", failedCount+"");
			}
			else{	
				System.out.println("[Passed] "+constraint);
				evalresult.put("passed", evalresult.get("passed")+constraint+"!@!");
			}
		}
		OCLEvaluator.resetEOCL();
		return evalresult;
	}

	private String extractStateInvariants(String state) {
		String stateConstraints = constraints.get(state);
		if(stateConstraints == null) {
			if(state.contains("_")) {
				String spStates[] = state.split("_");
				stateConstraints = "";
				for(int i=0; i<spStates.length;i++) {
					stateConstraints += constraints.get(spStates[i])+";";
				}
			}
		}
		return stateConstraints;
	}

	private FlightData getFlightData(String constraint, HashMap<String, Integer> data) {
		FlightData fd = null;
		for(String prop:data.keySet()) {
			if(constraint.contains(prop)) {
				fd = new FlightData(prop, data.get(prop));
			}
		}
		return fd;
	}


	@SuppressWarnings("unused")
	private ArrayList<FlightData> constructFlightData(HashMap<String, Integer> data) {
		ArrayList<FlightData> flightData = new ArrayList<>(1);
		for(String prop:data.keySet()) {
			FlightData fd = new FlightData(prop, data.get(prop));
			flightData.add(fd);
		}
		return flightData;
	}

}
