/**
 * 
 */
package model.uavim;

import java.util.ArrayList;

/**
 * A class that represents the UAV domain classes and their attributes.
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class UAVDomainClass {
	private String className;
	private ArrayList<UAVProperty> attributes;
	/**
	 * @param className
	 * @param attributes
	 */
	public UAVDomainClass(String className, ArrayList<UAVProperty> attributes) {
		super();
		this.className = className;
		this.attributes = attributes;
	}
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	/**
	 * @return the attributes
	 */
	public ArrayList<UAVProperty> getAttributes() {
		return attributes;
	}
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(ArrayList<UAVProperty> attributes) {
		this.attributes = attributes;
	}
	
	@Override
	public boolean equals(Object dmClass) {
		if(dmClass instanceof UAVDomainClass) {
			UAVDomainClass cName = (UAVDomainClass) dmClass;
			if(this.className.equals(cName.getClassName()))
				return true;
		}
		return false;
	}
}
