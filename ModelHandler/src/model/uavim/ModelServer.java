/**
 * 
 */
package model.uavim;

import model.evaluator.ModelEvaluator;
import py4j.GatewayServer;

/**
 * A class that acts as a server to receive request for model processing and generate response accordingly. 
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class ModelServer {
	private static String umlFilePath = "models/ArduCopter-DM.uml";
	private static UAVModel uavModel = null;
	
	public UAVBehavioralModel getUAVBehModel() {
		if(uavModel == null) uavModel = UAVModelLoader.loadModel(umlFilePath);
		return uavModel.getBehavioralModel();
	}
	
	public UAVDomainModel getUAVDomainModel() {
		if(uavModel == null) uavModel = UAVModelLoader.loadModel(umlFilePath);
		return uavModel.getDomainModel();
	}
	
	public ModelEvaluator getModelEvaluator() {
		return new ModelEvaluator();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		uavModel = UAVModelLoader.loadModel(umlFilePath);
//		uavModel.printUAVModel();
		
		System.out.println("Model loaded.\n");
		GatewayServer gatewayServer = new GatewayServer(new ModelServer());
		gatewayServer.start();
		System.out.println("Model server is running.");
	}

}
