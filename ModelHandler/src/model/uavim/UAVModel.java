/**
 * 
 */
package model.uavim;

/**
 * A class that represents a UAV model including domain model and behavioral model.
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class UAVModel {
	private UAVDomainModel domainModel;
	private UAVBehavioralModel behavioralModel;
	/**
	 * @param domainModel
	 * @param behavioralModel
	 */
	public UAVModel(UAVDomainModel domainModel, UAVBehavioralModel behavioralModel) {
		super();
		this.domainModel = domainModel;
		this.behavioralModel = behavioralModel;
	}
	/**
	 * @return the domainModel
	 */
	public UAVDomainModel getDomainModel() {
		return domainModel;
	}
	/**
	 * @param domainModel the domainModel to set
	 */
	public void setDomainModel(UAVDomainModel domainModel) {
		this.domainModel = domainModel;
	}
	/**
	 * @return the behavioralModel
	 */
	public UAVBehavioralModel getBehavioralModel() {
		return behavioralModel;
	}
	/**
	 * @param behavioralModel the behavioralModel to set
	 */
	public void setBehavioralModel(UAVBehavioralModel behavioralModel) {
		this.behavioralModel = behavioralModel;
	}
	
	public void printUAVModel() {
		domainModel.printUAVDomainModel();
		behavioralModel.printUAVFlightModel();
	}
}
