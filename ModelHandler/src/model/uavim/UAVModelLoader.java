/**
 * 
 */
package model.uavim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Vertex;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;
import org.eclipse.uml2.uml.internal.impl.EnumerationImpl;
import org.eclipse.uml2.uml.internal.impl.PrimitiveTypeImpl;
import org.eclipse.uml2.uml.resource.UMLResource;

import model.utils.ConstraintsLoader;
import model.utils.ModelLoader;
import model.utils.UMLModelFactory;

/**
 * @author Hassan Sartaj
 * @version 1.0
 */
public class UAVModelLoader {
	private static ArrayList<String> createdClasses = new ArrayList<>(1);
	private static UAVDomainModel uavDM = new UAVDomainModel();
	private static UAVBehavioralModel uavBehModel = new UAVBehavioralModel();
	private static HashMap<String, String> constraints = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ModelLoader umlModel = new ModelLoader();
		String umlFilePath = "models/ArduCopter-DM.uml";
		String uri = null;
		try {
			uri = umlModel.getFileURI(umlFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Object objModel = umlModel.loadModel(uri);
		Model sourceModel;
		String pkgName = null;
		EList<PackageableElement> sourcePackagedElements = null;
		if (objModel instanceof Model) {
			sourceModel = (Model) objModel;
			pkgName = sourceModel.getName();
			sourcePackagedElements = sourceModel.getPackagedElements();
		} else if (objModel instanceof Package) {
			Package sourcePackage = (Package) objModel;
			pkgName = sourcePackage.getName();
			sourcePackagedElements = sourcePackage.getPackagedElements();
		}

		Package new_pkg = UMLModelFactory.createPackage(pkgName);
		constraints = ConstraintsLoader.loadConstraints("constraints/copter-constraints.ocl");
		
		for (PackageableElement element : sourcePackagedElements){
			//for nested package
			if(element.eClass() == UMLPackage.Literals.PACKAGE){
				org.eclipse.uml2.uml.Package nestedPackage = (org.eclipse.uml2.uml.Package) element;
				EList<PackageableElement> nestedPackagedElements = nestedPackage.getPackagedElements();
				for (PackageableElement nestedElement : nestedPackagedElements){
					readModel(new_pkg, nestedElement);
				}
			}
			else
				readModel(new_pkg, element);
		}
		
		uavBehModel.updateStateActions(uavDM, constraints);
		UAVModel uavModel = new UAVModel(uavDM, uavBehModel);
		uavModel.printUAVModel();
		saveModel(new_pkg, URI.createURI("models").appendSegment("UAVModel").appendFileExtension(UMLResource.FILE_EXTENSION));
	}
	
	public static UAVModel loadModel(String umlFilePath) {
		ModelLoader umlModel = new ModelLoader();
		String uri = null;
		try {
			uri = umlModel.getFileURI(umlFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Object objModel = umlModel.loadModel(uri);
		Model sourceModel;
		String pkgName = null;
		EList<PackageableElement> sourcePackagedElements = null;
		if (objModel instanceof Model) {
			sourceModel = (Model) objModel;
			pkgName = sourceModel.getName();
			sourcePackagedElements = sourceModel.getPackagedElements();
		} else if (objModel instanceof Package) {
			Package sourcePackage = (Package) objModel;
			pkgName = sourcePackage.getName();
			sourcePackagedElements = sourcePackage.getPackagedElements();
		}

		Package new_pkg = UMLModelFactory.createPackage(pkgName);
		constraints = ConstraintsLoader.loadConstraints("constraints/copter-constraints.ocl");
		
		for (PackageableElement element : sourcePackagedElements){
			//for nested package
			if(element.eClass() == UMLPackage.Literals.PACKAGE){
				org.eclipse.uml2.uml.Package nestedPackage = (org.eclipse.uml2.uml.Package) element;
				EList<PackageableElement> nestedPackagedElements = nestedPackage.getPackagedElements();
				for (PackageableElement nestedElement : nestedPackagedElements){
					readModel(new_pkg, nestedElement);
				}
			}
			else
				readModel(new_pkg, element);
		}
		
		uavBehModel.updateStateActions(uavDM, constraints);
		UAVModel uavModel = new UAVModel(uavDM, uavBehModel);
//		uavModel.printUAVModel();
		saveModel(new_pkg, URI.createURI("models").appendSegment("UAVModel").appendFileExtension(UMLResource.FILE_EXTENSION));
		return uavModel;
	}

	private static void handleClass(Package pkg, Class clas) {
		Class newClass = null;
		ArrayList<UAVProperty> attributes = new ArrayList<>(1);
		if(!createdClasses.contains(clas.getName())) {
			newClass = UMLModelFactory.createClass(pkg, clas.getName(), clas.isAbstract());
			createdClasses.add(clas.getName());
		}else {
			for (PackageableElement pElem : pkg.getPackagedElements()){
				if (pElem.eClass() == UMLPackage.Literals.CLASS)
				{
					Class pkg_clas = (Class)pElem;
					if(pkg_clas.getName().equals(clas.getName()))
						newClass = pkg_clas;
				}
			}
		}
		EList<Stereotype> stereotypes = clas.getAppliedStereotypes();
		for(Stereotype st:stereotypes) {
			ArrayList<Property> allProps = new ArrayList<>(1);
			allProps.addAll(st.getAttributes());
			allProps.addAll(clas.getOwnedAttributes());
			if(!st.getGeneralizations().isEmpty()) {
				EList<Generalization> genStereotypes = st.getGeneralizations();
				for(Generalization genSt:genStereotypes) {
					Stereotype stGen = (Stereotype) genSt.getGeneral();
					allProps.addAll(stGen.getAttributes());
				}
			}
			for(Property prop: allProps) {
				if(prop.getType() instanceof PrimitiveTypeImpl) {
					PrimitiveTypeImpl primitiveType = (PrimitiveTypeImpl) prop.getType();
					String type = primitiveType.eProxyURI().fragment();
					UAVProperty uavProp = new UAVProperty(prop.getName(), type, prop.getDefaultValue());
					attributes.add(uavProp);
					//TODO: check if need to change PT for float
					UMLModelFactory.createProperty(newClass, prop.getName(), UMLModelFactory.createPrimitiveType(pkg, "Integer"), 0, 1, 0, 0);
//					UMLModelFactory.createProperty(newClass, prop.getName(), UMLModelFactory.createPrimitiveType(pkg, type), 0, 1, 0, 0);
				}
				else if(prop.getType() instanceof EnumerationImpl) {
					EnumerationImpl enumType = (EnumerationImpl) prop.getType();
					UMLModelFactory.createProperty(newClass, prop.getName(), UMLModelFactory.createEnumeration(pkg, enumType.getName()), 0, 1, 0, 0);
					Enumeration enumeration = null;
					for (PackageableElement element : pkg.getPackagedElements()){
						if(element.eClass() == UMLPackage.Literals.ENUMERATION){
							Enumeration enm = (Enumeration) element;
							if(enm.getName().equals(enumType.getName())){
								enumeration = enm;
								break;
							}
						}
					}
					for(EnumerationLiteral el:enumType.getOwnedLiterals()) {
						UMLModelFactory.createEnumerationLiteral(enumeration, el.getName());
					}
				}
				else if(prop.getType() instanceof ClassImpl) {
					ClassImpl classType = (ClassImpl) prop.getType();
					if(!createdClasses.contains(classType.getName()) && classType.getName()!=null) {
						Class linkedClass = UMLModelFactory.createClass(pkg, classType.getName(), classType.isAbstract());
						UMLModelFactory.createAssociation(newClass, true, prop.getAggregation(), prop.getName(), 
								prop.getLower(), prop.getUpper(), linkedClass, false, AggregationKind.NONE_LITERAL, newClass.getName().toLowerCase(), prop.getLower(), prop.getUpper());
						createdClasses.add(linkedClass.getName());
					}
				}
			}
		}
		UAVDomainClass dmClass = new UAVDomainClass(clas.getName(), attributes);
		if(!uavDM.getDomainClasses().contains(dmClass))
			uavDM.getDomainClasses().add(dmClass);
	}
	
	private static void readModel(Package pkg, PackageableElement element){
		if (element.eClass() == UMLPackage.Literals.CLASS)
		{
			Class clas = (Class)element;
			handleClass(pkg, clas);

			//state machine as class owned behavior
			EList<Behavior> ownedBehavior = clas.getOwnedBehaviors();
			if(ownedBehavior != null && !ownedBehavior.isEmpty()) {
				readBehavioralModel(ownedBehavior);
//				uavBehModel.printUAVFlightModel();
			}
		}
	}

	private static UAVBehavioralModel readBehavioralModel(EList<Behavior> ownedBehavior) {
		ArrayList<UAVFlightState> uavFlightStates = new ArrayList<>(1);
		for (Behavior behavior: ownedBehavior)
		{
			if(behavior.eClass() == UMLPackage.Literals.STATE_MACHINE)
			{
				StateMachine sm = (StateMachine)behavior;
				EList<Region> regions = sm.getRegions();
				for(Region region: regions)
				{
					EList<Vertex> vertices = region.getSubvertices();
					for(Vertex vertex: vertices)
					{
						if(vertex.eClass() == UMLPackage.Literals.STATE)
						{
							State state = (State)vertex;
							//read only stereotyped states
							if(state.getAppliedStereotypes()!=null && !state.getAppliedStereotypes().isEmpty()) {
								//get only one for the state
								String stereoState = state.getAppliedStereotypes().get(0).getName();
								String insState = state.getName();
								boolean isInitial=false, isFinal=false, isComposite=false;
								ArrayList<UAVStateAction> actions = new ArrayList<>(1);
								ArrayList<String> nextStates = new ArrayList<>(1);
								EList<Transition> incomingTransitions=state.getIncomings();
								for (Transition transition: incomingTransitions)
								{	
									Vertex tVertex = transition.getSource();
									if(tVertex.eClass() == UMLPackage.Literals.PSEUDOSTATE) {
										Pseudostate ps = (Pseudostate) tVertex;
										if(ps.getKind().getValue()==PseudostateKind.INITIAL) {
											isInitial=true;
											break;
										}
									}
								}

								ArrayList<UAVFlightState> subSMStates = null;
								EList<Transition> outgoingTransitions=state.getOutgoings();
								for (Transition transition: outgoingTransitions)
								{	
									Vertex tVertex = transition.getTarget();
									if(tVertex.eClass() == UMLPackage.Literals.FINAL_STATE)
										isFinal=true;
									if(tVertex.eClass() == UMLPackage.Literals.STATE) {
										State tState = (State)tVertex;
										if(tState.getSubmachine() != null) {
											subSMStates = readSubStatemachine(stereoState, tState.getSubmachine());
											isComposite=true;
										}

										if(tState.getAppliedStereotypes()!=null && !tState.getAppliedStereotypes().isEmpty()) {
											String nextState = tState.getAppliedStereotypes().get(0).getName();
											//if(!stereoState.equals(nextState) && tState.getSubmachine() == null)
											nextStates.add(nextState);
										}
									}

									if(transition.getTriggers()!=null) {
										actions.addAll(getActions(transition.getTriggers(), nextStates));
									}
								}

								if(subSMStates != null) {
									for(UAVFlightState fs:subSMStates)
										if(!uavFlightStates.contains(fs)) {
											for(UAVStateAction action:actions)
												if(!fs.getActions().contains(action))
													fs.getActions().add(action);
											fs.getNextStates().addAll(nextStates);
											fs.setInitial(isInitial);
											fs.setFinal(isFinal);
											fs.setComposite(isComposite);
											uavFlightStates.add(fs);
										}
								}else {
									UAVFlightState flightState = new UAVFlightState(stereoState, insState, isInitial, isFinal, isComposite, actions, nextStates);
									if(!uavFlightStates.contains(flightState))
										uavFlightStates.add(flightState);
								}
							}
						}
					}
				}
			}
		}
		uavBehModel.setUAVFlightStates(uavFlightStates);
		filterNextStates(uavBehModel);
		return uavBehModel;
	}

	private static ArrayList<UAVFlightState> readSubStatemachine(String rootState, StateMachine subSM) {
		ArrayList<UAVFlightState> subSMStates = new ArrayList<>(1);
		EList<Region> regions = subSM.getRegions();
		for(Region region: regions)
		{
			EList<Vertex> vertices = region.getSubvertices();
			for(Vertex vertex: vertices)
			{
				if(vertex.eClass() == UMLPackage.Literals.STATE)
				{
					State state = (State)vertex;
					if(state.isComposite()) {
						EList<Region> nRegions = state.getRegions();
						for(Region nreg: nRegions)
						{
							EList<Vertex> nVertices = nreg.getSubvertices();
							for(Vertex nv: nVertices)
							{
								if(nv.eClass() == UMLPackage.Literals.STATE)
								{
									state = (State)nv;
									//read only stereotyped states
									if(state.getAppliedStereotypes()!=null && !state.getAppliedStereotypes().isEmpty()) {
										//get only one for the state
										String stereoState = rootState+"_"+state.getAppliedStereotypes().get(0).getName();
										String insState = state.getName();
										boolean isInitial=false, isFinal=false;
										ArrayList<UAVStateAction> actions = new ArrayList<>(1);
										ArrayList<String> nextStates = new ArrayList<>(1);
										EList<Transition> outgoingTransitions=state.getOutgoings();
										for (Transition transition: outgoingTransitions)
										{	
											Vertex tVertex = transition.getTarget();
											if(tVertex.eClass() == UMLPackage.Literals.STATE) {
												State tState = (State)tVertex;
												if(tState.getAppliedStereotypes()!=null && !tState.getAppliedStereotypes().isEmpty()) 
													nextStates.add(rootState+"_"+tState.getAppliedStereotypes().get(0).getName());
											}

											if(transition.getTriggers()!=null) {
												actions.addAll(getActions(transition.getTriggers(), nextStates));
											}
										}

										UAVFlightState flightState = new UAVFlightState(stereoState, insState, isInitial, isFinal, false, actions, nextStates);
										if(!subSMStates.contains(flightState))
											subSMStates.add(flightState);
									}
								}
							}
						}
					}
				}
			}
		}

		return subSMStates;
	}

	private static ArrayList<UAVStateAction> getActions(EList<Trigger> triggers, ArrayList<String> nextStates) {
		ArrayList<UAVStateAction> actions = new ArrayList<>(1);
		for(Trigger trigger:triggers) {
			Event event = trigger.getEvent();
			if(event instanceof CallEvent) {
				CallEvent callEvent = (CallEvent) event;
				EList<Stereotype> stereotypes = callEvent.getAppliedStereotypes();
				for(Stereotype st:stereotypes) {
					//TODO: see if only bottom ns is needed 
					String ns="";
					if(!nextStates.isEmpty())
						ns = nextStates.get(nextStates.size()-1);
					UAVStateAction action = new UAVStateAction(st.getName(), ns);
					if(!actions.contains(action))
						actions.add(action);
				}
			}
		}
		return actions;
	}

	private static void filterNextStates(UAVBehavioralModel uavBehModel) {
		ArrayList<String> states = uavBehModel.getUAVStateNames();
		ArrayList<String> missing = new ArrayList<>(1);
		ArrayList<String> toRemove = new ArrayList<>(1);
		for(UAVFlightState fs:uavBehModel.getUAVFlightStates()) {
			for(String ns:fs.getNextStates()) {
				if(!states.contains(ns)) {
					for(String name:states) {
						if(name.contains(ns)) {
							missing.add(name);
							toRemove.add(ns);
						}
					}
				}
			}
			for(String tr: toRemove)
				fs.getNextStates().remove(tr);
			for(String ms: missing) {
				if(!fs.getNextStates().contains(ms))
					fs.getNextStates().add(ms);
			}
			missing.clear();
			toRemove.clear();
		}
	}

	/**
	 * A method that exports UML model to file
	 * 
	 * @param _package
	 * @param uri
	 */
	private static void saveModel(org.eclipse.uml2.uml.Package _package, URI uri) {
		Resource resource = new ResourceSetImpl().createResource(uri);
		resource.getContents().add(_package);
		try {
			resource.save(null);
			System.out.println("Successfully Saved.");
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
}
