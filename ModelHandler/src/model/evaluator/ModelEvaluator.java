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


	public int evaluateModel(String state, HashMap<String, Integer> data) {
		int failedCount = 0;
		
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
			}
			else{	
				System.out.println("[Passed] "+constraint);
			}
		}
		OCLEvaluator.resetEOCL();
		return failedCount;
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


	//TODO: need to relocate - for testing
	public static void main(String[] args) {
		//		GatewayServer gatewayServer = new GatewayServer(new ModelEvaluator());
		//		gatewayServer.start();
		//		System.out.println("Gateway Server Started");

		//gatewayServer.shutdown()
		OCLEvaluator.setupOCLEvalutor(modelPath);
		String constraint = "context ArduCopter inv: self.airspeed>0 and self.airspeed<100";
		//		String constraint = "ArduCopter.allInstances()->exists(a| a.airspeed>0 and a.airspeed<100)";	
		ArrayList<ClassifierTuple> tuples = OCLEvaluator.getInstanceModel(modelPath, constraint);

		//temp data- for testing
		ArrayList<FlightData> flightData = new ArrayList<>(1);
		FlightData fd1 = new FlightData("airspeed", 50);
		flightData.add(fd1);
		
		OCLEvaluator.updateInstanceModel(tuples, fd1);
		String result = OCLEvaluator.evaluateConstraint(tuples, constraint);
		if(!result.equals("true") && !result.equals("false"))
			result = "false";

		if(!result.equals("true")){
			System.out.println("Failed constraint");
		}
		else{	
			System.out.println("Passed constraint");
		}
	}

}
