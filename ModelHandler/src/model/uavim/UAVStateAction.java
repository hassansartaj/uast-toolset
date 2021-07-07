/**
 * 
 */
package model.uavim;

import java.util.ArrayList;

/**
 * A class that represents an action that can be performed during a particular state of the UAV flight.
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class UAVStateAction {
	private String name;
	private String nextState;
	private ArrayList<UAVProperty> parameters;
	/**
	 * @param name
	 */
	public UAVStateAction(String name, String nextState) {
		super();
		this.name = name;
		this.nextState = nextState;
		this.parameters = new ArrayList<>(1);
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
	 * @return the nextState
	 */
	public String getNextState() {
		return nextState;
	}
	/**
	 * @param nextState the nextState to set
	 */
	public void setNextState(String nextState) {
		this.nextState = nextState;
	}
	/**
	 * @return the parameter
	 */
	public ArrayList<UAVProperty> getParameters() {
		return parameters;
	}
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(ArrayList<UAVProperty> parameter) {
		this.parameters = parameter;
	}
	
	/**
	 * @param parameter
	 */
	public void addParameter(UAVProperty parameter) {
		if(this.parameters == null)
			this.parameters = new ArrayList<>(1);
		this.parameters.add(parameter);
	}
	
	@Override
	public boolean equals(Object action) {
		if(action instanceof UAVStateAction) {
			UAVStateAction aName = (UAVStateAction) action;
			if(this.name.equals(aName.getName()))
				return true;
		}
		return false;
	}
}
