/**
 * 
 */
package model.profile;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;
import org.eclipse.uml2.uml.internal.impl.EnumerationImpl;
import org.eclipse.uml2.uml.internal.impl.PrimitiveTypeImpl;

import model.utils.ModelLoader;


/**
 * A class that loads the UML profile
 * 
 * @author Hassan Sartaj
 * @version 1.0
 */
public class ProfileModelLoader {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String profilePath = "models/UAVProfile.profile.uml";
		System.out.println("UAV Profile is loading...");
		ArrayList<Stereotype> stereotypes = getProfileStereotypes(profilePath);
//		printStereotypes(stereotypes);
		
		profilePath = "models/UAVBehvioralProfile.profile.uml";
		System.out.println("\nUAV Behvioral Profile is loading...");
		stereotypes = getProfileStereotypes(profilePath);
		printStereotypes(stereotypes);
	}

	private static void printStereotypes(ArrayList<Stereotype> stereotypes) {
		for(Stereotype st:stereotypes) {
			System.out.println("[St] - "+st.getName());
			for(Property prop: st.getAttributes()) {
				String type = null;
				if(prop.getType() instanceof PrimitiveTypeImpl) {
					PrimitiveTypeImpl pt = (PrimitiveTypeImpl) prop.getType();
					type = pt.eProxyURI().fragment();
					System.out.println("[Pt] - "+prop.getName()+" : "+ type);
				}
				else if(prop.getType() instanceof EnumerationImpl) {
					EnumerationImpl pt = (EnumerationImpl) prop.getType();
					type = pt.getName();
					System.out.println("[Et] - "+prop.getName()+" : "+ type);
				}
				else if(prop.getType() instanceof ClassImpl){
					ClassImpl metaClass = (ClassImpl) prop.getType();
					System.out.println("[MC] - "+metaClass.eProxyURI().fragment());
				}
			}
		}
	}

	
	public static ArrayList<Stereotype> getProfileStereotypes(String profilePath) {
		ArrayList<Stereotype> stereotypes = new ArrayList<>(1);
		ModelLoader modelLoader = new ModelLoader();
		String uri = null;
		try {
			uri = modelLoader.getFileURI(profilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Profile profile = modelLoader.loadProfile(uri);
		EList<PackageableElement> profileElements = profile.getPackagedElements();
		for (PackageableElement element : profileElements){
			if(element.eClass() == UMLPackage.Literals.STEREOTYPE){
				Stereotype st = (Stereotype) element;
				stereotypes.add(st);
			}
		}
		return stereotypes;
	}

}
