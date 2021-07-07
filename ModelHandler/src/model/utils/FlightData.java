/**
 * 
 */
package model.utils;

/**
 * A class that represents UAV flight data
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class FlightData {
	private String property;
	private Object value;
	/**
	 * @param property
	 * @param value
	 */
	public FlightData(String property, Object value) {
		super();
		this.property = property;
		this.value = value;
	}
	/**
	 * @return the property
	 */
	public String getProperty() {
		return property;
	}
	/**
	 * @param property the property to set
	 */
	public void setProperty(String property) {
		this.property = property;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}
