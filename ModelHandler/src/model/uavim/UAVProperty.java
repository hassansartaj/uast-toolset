/**
 * 
 */
package model.uavim;

/**
 * A class that represents the UAV domain classes property/attribute.
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class UAVProperty {
	private String name;
	private String type;
	private Object defaultValue;
	/**
	 * @param name
	 * @param type
	 * @param defaultValue
	 */
	public UAVProperty(String name, String type, Object defaultValue) {
		super();
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the defaultValue
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}
	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	@Override
	protected UAVProperty clone() throws CloneNotSupportedException {
		UAVProperty cloneProp = new UAVProperty(this.name, this.type, this.defaultValue);
		return cloneProp;
	}
	
	@Override
	public boolean equals(Object prop) {
		if(prop instanceof UAVProperty) {
			UAVProperty pName = (UAVProperty) prop;
			if(this.name.equals(pName.getName()))
				return true;
		}
		return false;
	}
}
