/**
 * 
 */
package model.uavim;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class that represents an abstract flight state of the UAV behavioral model.
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class UAVFlightState {
	private String name;
	private String instanceName;
	private boolean isInitial;
	private boolean isFinal;
	private boolean isComposite;
	private ArrayList<UAVStateAction> actions;
	private ArrayList<String> nextStates;
	
	/**
	 * @param name
	 * @param actualName
	 * @param isInitial
	 * @param isFinal
	 * @param actions
	 * @param nextStates
	 */
	public UAVFlightState(String name, String instanceName, boolean isInitial, boolean isFinal, boolean isComposite, 
			ArrayList<UAVStateAction> actions, ArrayList<String> nextStates) {
		super();
		this.name = name;
		this.instanceName = instanceName;
		this.isInitial = isInitial;
		this.isFinal = isFinal;
		this.isComposite = isComposite;
		this.actions = actions;
		this.nextStates = nextStates;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the instanceName
	 */
	public String getInstanceName() {
		return instanceName;
	}
	/**
	 * @param instanceName the instanceName to set
	 */
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	/**
	 * @return the isInitial
	 */
	public boolean isInitial() {
		return isInitial;
	}
	/**
	 * @param isInitial the isInitial to set
	 */
	public void setInitial(boolean isInitial) {
		this.isInitial = isInitial;
	}
	/**
	 * @return the isFinal
	 */
	public boolean isFinal() {
		return isFinal;
	}
	/**
	 * @param isFinal the isFinal to set
	 */
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}
	
	/**
	 * @return the isComposite
	 */
	public boolean isComposite() {
		return isComposite;
	}
	/**
	 * @param isComposite the isComposite to set
	 */
	public void setComposite(boolean isComposite) {
		this.isComposite = isComposite;
	}
	/**
	 * @return the actions
	 */
	public ArrayList<UAVStateAction> getActions() {
		return actions;
	}
	
	/**
	 * @return the actions names
	 */
	public ArrayList<String> getActionsNames() {
		ArrayList<String> actionNames = new ArrayList<>(1);
		for(UAVStateAction sa : this.actions) {
			actionNames.add(sa.getName());
		}
		return actionNames;
	}
	
	/**
	 * @param actions the actions to set
	 */
	public void setActions(ArrayList<UAVStateAction> actions) {
		this.actions = actions;
	}
	/**
	 * @return the nextStates
	 */
	public ArrayList<String> getNextStates() {
		return nextStates;
	}
	/**
	 * @param nextStates the nextStates to set
	 */
	public void setNextStates(ArrayList<String> nextStates) {
		this.nextStates = nextStates;
	}
	
	/**
	 * @param domainClasses
	 * @param constraints
	 */
	public void populateActionParams(ArrayList<UAVDomainClass> domainClasses, HashMap<String, String> constraints) {
		for(String ckey:constraints.keySet()) {
			if(this.getInstanceName().equals(ckey)) {
				String stateConstraints = constraints.get(ckey);
				for(UAVDomainClass dClass: domainClasses) {
					for(UAVProperty dcProp: dClass.getAttributes()) {
						if(stateConstraints.contains(dcProp.getName())) {
							try {
								UAVProperty newProp = dcProp.clone();
								for(UAVStateAction action:this.actions) {
									action.addParameter(newProp);
								}
							} catch (CloneNotSupportedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean equals(Object state) {
		if(state instanceof UAVFlightState) {
			UAVFlightState sName = (UAVFlightState) state;
			if(this.name.equals(sName.getName()))
				return true;
		}
		return false;
	}
}
