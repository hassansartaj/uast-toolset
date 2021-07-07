/**
 * 
 */
package model.uavim;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class that represents the UAV behavioral model including the abstract flight states.
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class UAVBehavioralModel {
	private ArrayList<UAVFlightState> uavFlightStates;

	/**
	 * @return the uavFlightStates
	 */
	public ArrayList<UAVFlightState> getUAVFlightStates() {
		return uavFlightStates;
	}
	
	/**
	 * @return all flight state names
	 */
	public ArrayList<String> getUAVStateNames() {
		ArrayList<String> states = new ArrayList<>(1);
		for(UAVFlightState fs:this.uavFlightStates) {
			if(!states.contains(fs.getName()))
				states.add(fs.getName());
		}
		return states;
	}
	
	public HashMap<String, ArrayList<String>> getUAVState2NextStatesMap() {
		HashMap<String, ArrayList<String>> s2nsMap=new HashMap<>(1); 
		for(UAVFlightState fs:this.uavFlightStates) {
			s2nsMap.put(fs.getName(), fs.getNextStates());
		}
		return s2nsMap;
	}
	
	public ArrayList<String> getUAVStateActionNextState() {
		ArrayList<String> stActNsList = new ArrayList<>(1);
		for(UAVFlightState fs:this.uavFlightStates) {
			String stName = fs.getName();
			for(UAVStateAction act:fs.getActions()) {
				String actName = act.getName();
				String nsName = act.getNextState();
				if(!fs.getNextStates().contains(nsName)) {
					for(String ns:fs.getNextStates()) {
						if(ns.contains(nsName)) {
							stActNsList.add(stName+"-"+actName+"-"+ns);
						}
					}
				}
				else
					stActNsList.add(stName+"-"+actName+"-"+nsName);
			}
		}
		return stActNsList;
	}
	
	public HashMap<String, ArrayList<String>> getUAVState2ActionsMap() {
		HashMap<String, ArrayList<String>> s2aMap=new HashMap<>(1); 
		for(UAVFlightState fs:this.uavFlightStates) {
			s2aMap.put(fs.getName(), fs.getActionsNames());
		}
		return s2aMap;
	}
	
	public ArrayList<String> getUAVActionNames() {
		ArrayList<String> actions = new ArrayList<>(1);
		for(UAVFlightState fs:this.uavFlightStates) {
			for(UAVStateAction act:fs.getActions()) {
				if(!actions.contains(act.getName()))
					actions.add(act.getName());
			}
		}
		return actions;
	}

	/**
	 * @param uavFlightStates the uavFlightStates to set
	 */
	public void setUAVFlightStates(ArrayList<UAVFlightState> uavFlightStates) {
		this.uavFlightStates = uavFlightStates;
	}
	
	/**
	 * @param uavFlightState
	 */
	public void addUAVFlightState(UAVFlightState uavFlightState) {
		if(this.uavFlightStates == null)
			this.uavFlightStates = new ArrayList<UAVFlightState>(1);
		this.uavFlightStates.add(uavFlightState);
	}
	
	/**
	 * @return initial state
	 */
	public UAVFlightState getInitialState() {
		UAVFlightState initialState = null;
		for(UAVFlightState fs:this.uavFlightStates) {
			if(fs.isInitial()) {
				initialState = fs;
				break;
			}
		}
		return initialState;
	}
	
	/**
	 * @return initial state name
	 */
	public String getInitialStateName() {
		String initialState = null;
		for(UAVFlightState fs:this.uavFlightStates) {
			if(fs.isInitial()) {
				initialState = fs.getName();
				break;
			}
		}
		return initialState;
	}
	
	/**
	 * @return final state
	 */
	public UAVFlightState getFinalState() {
		UAVFlightState finalState = null;
		for(UAVFlightState fs:this.uavFlightStates) {
			if(fs.isInitial()) {
				finalState = fs;
				break;
			}
		}
		return finalState;
	}
	
	/**
	 * @return final state name
	 */
	public String getFinalStateName() {
		String finalState = null;
		for(UAVFlightState fs:this.uavFlightStates) {
			if(fs.isInitial()) {
				finalState = fs.getName();
				break;
			}
		}
		return finalState;
	}
	
	/**
	 * @param uav domain model
	 * @param constraints
	 */
	public void updateStateActions(UAVDomainModel uavDM, HashMap<String, String> constraints) {
		for(UAVFlightState fs:this.uavFlightStates) {
			fs.populateActionParams(uavDM.getDomainClasses(), constraints);
		}
	}
	
	public void printUAVFlightModel() {
		System.out.println("\n---------------------------------------------------"
				+ "\n------------UAV Flight Behavioral Model------------"
				+ "\n---------------------------------------------------");
		for(UAVFlightState fs:this.uavFlightStates) {
			System.out.println("[stState] "+fs.getName()+"\n[iState] "+fs.getInstanceName()+"\n[isInitial] "+fs.isInitial()+"\n[isFinal] "+fs.isFinal()+"\n[isComopsite] "+fs.isComposite());
			System.out.print("[Actions] {");
			for(UAVStateAction action:fs.getActions()) {
				System.out.print(action.getName()+"(");
				for(UAVProperty param:action.getParameters()) {
					System.out.print(param.getName()+" : "+param.getType());
					if(action.getParameters().indexOf(param)<action.getParameters().size()-1)
						System.out.print(", ");
				}
				System.out.print(")");
				System.out.print("::NS: "+action.getNextState());
				if(fs.getActions().indexOf(action)<fs.getActions().size()-1)
					System.out.print(", ");
			}
			System.out.print("}\n[Next States] {");
			for(String ns:fs.getNextStates()) {
				System.out.print(ns);
				if(fs.getNextStates().indexOf(ns)<fs.getNextStates().size()-1)
					System.out.print(", ");
			}
			System.out.println("}\n");
		}
	}
}
