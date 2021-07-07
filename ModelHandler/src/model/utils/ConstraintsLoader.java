/**
 * 
 */
package model.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class that loads OCL constraints from the file and store according to the state
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class ConstraintsLoader {

	public static void main(String[] args) {
		HashMap<String, String> constraints = loadConstraints("constraints/copter-constraints.ocl");
		System.out.println("Loaded constraints");
		for(String k:constraints.keySet())
			System.out.println(k+" - "+constraints.get(k));
	}

	/**
	 * A method that reads OCL constraints from file and store according to the state
	 * 
	 * @param filePath
	 * @return HashMap of state and corresponding constraints
	 */
	public static HashMap<String, String> loadConstraints(String filePath) {
		HashMap<String, String> constraints = new HashMap<String, String>();
		ArrayList<String> rawConstraints = new ArrayList<String>(1);
		BufferedReader file=null;
		try {
			file=new BufferedReader(new FileReader(filePath));
			String line=null;
			while((line = file.readLine()) != null) {
				if(!line.contains("--") && !line.contains("\t") && !line.isEmpty() && !rawConstraints.contains(line))
					rawConstraints.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		processConstraints(constraints, rawConstraints);
		return constraints;
	}

	/**
	 * A method that process raw OCL constraints from file and store according to the state
	 * 
	 * @param constraints
	 * @param rawConstraints
	 */
	private static void processConstraints(HashMap<String, String> constraints, ArrayList<String> rawConstraints) {
		for(String rc : rawConstraints) {
			//load state invariants 
			if(rc.indexOf("oclIsInState")>-1) {
				String state = rc.substring(rc.indexOf("oclIsInState")+13, rc.indexOf(")"));
				String sub = "self.oclIsInState("+state+") and ";
				boolean found=false;
				if (!constraints.isEmpty()) {
					if (constraints.get(state) != null && !constraints.get(state).isEmpty()) {
						String newC = constraints.get(state) +";"+ rc.replace(sub, "");
						constraints.put(state, newC);
						found=true;
					} 
				}
				if(!found)
					constraints.put(state, rc.replace(sub, ""));
			}
			else {  //load general constraints
				if (constraints.get("General") != null && !constraints.get("General").isEmpty()) {
					String newC = constraints.get("General") +";"+rc.trim();
					constraints.put("General", newC);
				}else
					constraints.put("General", rc.trim());
			}
		}
	}
}
