/**
 * 
 */
package model.uavim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import model.utils.ConstraintsLoader;

/**
 * A class that represents the UAV domain model including the domain classes.
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class UAVDomainModel {
	private ArrayList<UAVDomainClass> domainClasses;
	public UAVDomainModel() {
		this.domainClasses = new ArrayList<>(1);
	}
	/**
	 * @param domainClasses
	 */
	public UAVDomainModel(ArrayList<UAVDomainClass> domainClasses) {
		super();
		this.domainClasses = domainClasses;
	}

	/**
	 * @return the domainClasses
	 */
	public ArrayList<UAVDomainClass> getDomainClasses() {
		return domainClasses;
	}

	/**
	 * @param domainClasses the domainClasses to set
	 */
	public void setDomainClasses(ArrayList<UAVDomainClass> domainClasses) {
		this.domainClasses = domainClasses;
	}

	public ArrayList<String> getAllProperties() {
		ArrayList<String> properties = new ArrayList<>(1);
		for(UAVDomainClass dmClass:domainClasses) {
			for(UAVProperty attr:dmClass.getAttributes()) {
				if(!properties.contains(attr.getName()))
					properties.add(attr.getName());
			}
		}
		return properties;
	}

	public HashMap<String, Integer[]> getPropertiesWithDomain() {
		HashMap<String, Integer[]> propsDomainMap = new HashMap<>(1);
		HashMap<String, String> constraints = ConstraintsLoader.loadConstraints("constraints/copter-constraints.ocl");
		for(UAVDomainClass dmClass:domainClasses) {
			for(UAVProperty attr:dmClass.getAttributes()) {
				ArrayList<String> propConstraints = getConstraintsWithProperty(attr.getName(), constraints);
				if (!propConstraints.isEmpty()) {
					ArrayList<Integer> constants = getConstants(propConstraints);
					Integer[] domain = new Integer[2];
					domain[0] = Collections.min(constants);
					domain[1] = Collections.max(constants);
					propsDomainMap.put(attr.getName(), domain);
				}
			}
		}
		return propsDomainMap;
	}

	private ArrayList<Integer> getConstants(ArrayList<String> propConstraints) {
		ArrayList<Integer> constants = new ArrayList<>(1);
		for(String pc: propConstraints) {
			String predicate = pc.substring(pc.indexOf(":")+1).trim();
			ArrayList<String> allClauses = new ArrayList<>(1);
			String[] andClauses = predicate.split(" and ");
			for(String ac:andClauses) {
				String[] orClauses = null;
				if(ac.contains(" or ")) {
					orClauses = ac.split(" or ");
					for(String oc:orClauses) {
						if(!allClauses.contains(oc))
							allClauses.add(oc);
					}
				}
				else if(!allClauses.contains(ac)) {
						allClauses.add(ac);
				}
			}


			String value = null;
			for(String ac: allClauses) {
				if(ac.contains(">=")) {
					if(ac.indexOf(" ")>-1)
						value = ac.substring(ac.indexOf(">=")+2, ac.indexOf(" ")).trim();
					else if(ac.indexOf(")")>-1)
						value = ac.substring(ac.indexOf(">=")+2, ac.indexOf(")")).trim();
					else
						value = ac.substring(ac.indexOf(">=")+2).trim();
				}
				else if(ac.contains("<=")) {
					if(ac.indexOf(" ")>-1)
						value = ac.substring(ac.indexOf("<=")+2, ac.indexOf(" ")).trim();
					else if(ac.indexOf(")")>-1)
						value = ac.substring(ac.indexOf("<=")+2, ac.indexOf(")")).trim();
					else
						value = ac.substring(ac.indexOf("<=")+2).trim();
				}
				else if(ac.contains(">")) {
					if(ac.indexOf(" ")>-1)
						value = ac.substring(ac.indexOf(">")+1, ac.indexOf(" ")).trim();
					else if(ac.indexOf(")")>-1)
						value = ac.substring(ac.indexOf(">")+1, ac.indexOf(")")).trim();
					else
						value = ac.substring(ac.indexOf(">")+1).trim();
				}
				else if(ac.contains("<")) {
					if(ac.indexOf(" ")>-1)
						value = ac.substring(ac.indexOf("<")+1, ac.indexOf(" ")).trim();
					else if(ac.indexOf(")")>-1)
						value = ac.substring(ac.indexOf("<")+1, ac.indexOf(")")).trim();
					else
						value = ac.substring(ac.indexOf("<")+1).trim();
				}
				constants.add(Integer.parseInt(value));
			}
		}
		return constants;
	}
	private ArrayList<String> getConstraintsWithProperty(String property, HashMap<String, String> constraints) {
		ArrayList<String> propConstraints = new ArrayList<>(1);
		for(String key: constraints.keySet()) {
			String allConstraints = constraints.get(key);
			String[] indConstraints = allConstraints.split(";");
			for(String ic: indConstraints) {
				if(ic.contains(property) && !propConstraints.contains(ic))
					propConstraints.add(ic);
			}
		}
		return propConstraints;
	}
	public void printUAVDomainModel() {
		System.out.println("\n----------------------------------------"
				+ "\n------------UAV Domain Model------------"
				+ "\n----------------------------------------");
		for(UAVDomainClass dmClass:domainClasses) {
			System.out.println("[Class] "+dmClass.getClassName());
			System.out.print("[Attributes] - {");
			for(UAVProperty attr:dmClass.getAttributes()) {
				System.out.print(attr.getName()+":"+attr.getType());
				if(dmClass.getAttributes().indexOf(attr)<dmClass.getAttributes().size()-1)
					System.out.print(", ");
			}
			System.out.println("}\n");
		}
	}
}
