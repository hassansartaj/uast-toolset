package model.utils;


import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.internal.impl.LiteralBooleanImpl;
import org.eclipse.uml2.uml.internal.impl.LiteralIntegerImpl;
import org.eclipse.uml2.uml.internal.impl.LiteralStringImpl;

/**
 * A class that creates UML class diagram elements (e.g. package, class, attribute, etc)
 * 
 * @author Hassan Sartaj
 * @version 1.1
 */

public class UMLModelFactory {

	/**
	 * A method that creates and returns UML model and set its name as specified in parameter.
	 * 
	 * @param name: String
	 * @return uml model: Model
	 */
	public static Model createModel(String name) {
		Model model = UMLFactory.eINSTANCE.createModel();
		model.setName(name);
		return model;
	}

	/**
	 * A method that creates and returns UML package and set its name as specified in parameter.
	 * 
	 * @param name: String
	 * @return _package: org.eclipse.uml2.uml.Package
	 */
	public static org.eclipse.uml2.uml.Package createPackage(String name) {
		org.eclipse.uml2.uml.Package _package= UMLFactory.eINSTANCE.createPackage();
		_package.setName(name);
		return _package;
	}

	/**
	 * A method that creates and returns UML class in specified package in parameter. 
	 * 
	 * @param _package: org.eclipse.uml2.uml.Package
	 * @param name: String
	 * @param isAbstract: boolean
	 * @return _class: org.eclipse.uml2.uml.Class
	 */
	public static org.eclipse.uml2.uml.Class createClass(org.eclipse.uml2.uml.Package _package, String name, boolean isAbstract) {
		org.eclipse.uml2.uml.Class _class = _package.createOwnedClass(name, isAbstract);
		return _class;
	}

	/**
	 * A method that creates and returns UML stereotype.
	 * 
	 * @param name: stereotype name
	 * @return stereotype
	 */
	public static Stereotype createStereotype(String name) {
		Stereotype stereotype = UMLFactory.eINSTANCE.createStereotype();
		stereotype.setName(name);
		return stereotype;
	}

	/**
	 * A method that creates and returns UML property in specified class. 
	 * 
	 * @param _class: org.eclipse.uml2.uml.Class
	 * @param name: String
	 * @param type: Type
	 * @param lowerBound: int
	 * @param upperBound: int
	 * @return attribute: Property
	 */
	public static Property createProperty(org.eclipse.uml2.uml.Class _class, String name, Type type, int lowerBound, int upperBound,int low,int up) {
		Property attribute = _class.createOwnedAttribute(name, type, lowerBound, upperBound);
		return attribute;
	}

	/**
	 * A method that creates boolean default value for the given attribute
	 * 
	 * @param attribute
	 * @param _package
	 * @param newValue
	 */
	public static void createBooleanDefaultValue(Property attribute, org.eclipse.uml2.uml.Package _package, Object newValue) {
		LiteralBoolean lb = UMLFactory.eINSTANCE.createLiteralBoolean();
		LiteralBooleanImpl val = (LiteralBooleanImpl) attribute.createDefaultValue(lb.getName(), createPrimitiveType(_package, "Boolean"), lb.eClass());
		newValue = newValue.toString().toLowerCase();
		val.setValue(Boolean.parseBoolean((String) newValue));
		attribute.setDefaultValue(val);
	}
	/**
	 * A method that creates integer default value for the given attribute
	 * 
	 * @param attribute
	 * @param _package
	 * @param newValue
	 */
	public static void createIntegerDefaultValue(Property attribute, org.eclipse.uml2.uml.Package _package, Object newValue) {
		LiteralInteger li = UMLFactory.eINSTANCE.createLiteralInteger();
		LiteralIntegerImpl val = (LiteralIntegerImpl) attribute.createDefaultValue(li.getName(), createPrimitiveType(_package, "Integer"), li.eClass());
		val.setValue((int) newValue);
		attribute.setDefaultValue(val);
	}
	
	/**
	 * A method that creates string default value for the given attribute
	 * 
	 * @param attribute
	 * @param _package
	 * @param newValue
	 */
	public static void createStringDefaultValue(Property attribute, org.eclipse.uml2.uml.Package _package, Object newValue) {
		LiteralString ls = UMLFactory.eINSTANCE.createLiteralString();
		LiteralStringImpl val = (LiteralStringImpl) attribute.createDefaultValue(ls.getName(), createPrimitiveType(_package, "String"), ls.eClass());
		val.setValue((String) newValue);
		attribute.setDefaultValue(val);
	}

	/**
	 * A method that creates and returns EnumerationLiteral element in specified enumeration in parameter. 
	 * 
	 * @param enumeration: Enumeration
	 * @param name: String
	 * @return enumerationLiteral: EnumerationLiteral
	 */
	public static EnumerationLiteral createEnumerationLiteral(Enumeration enumeration, String name) {
		EnumerationLiteral enumerationLiteral = enumeration.createOwnedLiteral(name);
		return enumerationLiteral;
	}

	/**
	 * A method that creates and returns PrimitiveType element in specified package with given name in parameter. 
	 * 
	 * @param package_: org.eclipse.uml2.uml.Package
	 * @param name: String
	 * @return primitiveType: PrimitiveType
	 */
	public static PrimitiveType createPrimitiveType(org.eclipse.uml2.uml.Package package_, String name) {
		PrimitiveType primitiveType = package_.createOwnedPrimitiveType(name);
		return primitiveType;
	}

	/**
	 * A method that creates and returns Enumeration element in specified package with given name in parameter.
	 * 
	 * @param package_: org.eclipse.uml2.uml.Package
	 * @param name: String
	 * @return enumeration: Enumeration
	 */
	public static Enumeration createEnumeration(org.eclipse.uml2.uml.Package package_, String name) {
		Enumeration enumeration = package_.createOwnedEnumeration(name);
		return enumeration;
	}

	/**
	 * A method that creates and returns UML Association element with the specified properties in parameter.
	 * 
	 * @param type1: Type
	 * @param end1IsNavigable: boolean
	 * @param end1Aggregation: AggregationKind
	 * @param end1Name: String
	 * @param end1LowerBound: int
	 * @param end1UpperBound: int
	 * @param type2: Type
	 * @param end2IsNavigable: boolean
	 * @param end2Aggregation: AggregationKind
	 * @param end2Name: String
	 * @param end2LowerBound: int
	 * @param end2UpperBound: int
	 * @return association: Association
	 */
	public static Association createAssociation(Type type1, boolean end1IsNavigable, AggregationKind end1Aggregation, String end1Name, int end1LowerBound, int end1UpperBound,
			Type type2, boolean end2IsNavigable, AggregationKind end2Aggregation, String end2Name, int end2LowerBound, int end2UpperBound) {

		Association association = type1.createAssociation(end1IsNavigable, end1Aggregation, end1Name, end1LowerBound, end1UpperBound,
				type2, end2IsNavigable, end2Aggregation, end2Name, end2LowerBound, end2UpperBound);
		return association;
	}
}
