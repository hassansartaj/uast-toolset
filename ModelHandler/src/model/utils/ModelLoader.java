/**
 * 
 */
package model.utils;

import java.io.File;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.mapping.ecore2xml.Ecore2XMLPackage;
import org.eclipse.emf.mapping.ecore2xml.util.Ecore2XMLResource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UML212UMLResource;
import org.eclipse.uml2.uml.resource.UML22UMLResource;
import org.eclipse.uml2.uml.resource.UMLResource;


/**
 * A class that loads UML/XMI model form the specified file path.
 * 
 * @author Hassan Sartaj
 * @version 1.1
 */
public class ModelLoader {
	private final ResourceSet RESOURCE_SET;
	public ModelLoader() {
		RESOURCE_SET = new ResourceSetImpl();
	}
	
	/**
	 * A method that converts input path to uri and return uri.
	 * 
	 * @param path
	 * @return uri
	 * @throws Exception
	 */
	public String getFileURI(String path) throws Exception {
		File f = new File(path);
		String uri = f.toURI().toString();
		return uri;	
	}
	/**
	 * A method that loads UML model from the input URI.
	 * 
	 * @param uri:String
	 * @return model:Object
	 */
	public Object loadModel(String uri){
		URI model = URI.createURI(uri);
		registerPackages(RESOURCE_SET);
		registerResourceFactories();
		
		EPackage.Registry.INSTANCE.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		
		RESOURCE_SET.getPackageRegistry().put("http://www.eclipse.org/uml2/5.0.0/UML", UMLPackage.eINSTANCE);
		RESOURCE_SET.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		
		//		EPackage.Registry.INSTANCE.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		//		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		Resource resource = null;
		try {
			resource = RESOURCE_SET.getResource(model, true);
		} catch (Exception e) {
//			Logger.getLogger().error(e);
			System.err.println(e);
		}
		Object result;

		Model _model = (Model) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.MODEL);
		result = _model;

		if (_model == null) {
			Object _obj = resource.getContents().get(0);
			Package _Package = (Package) _obj;
			result = _Package;
		}

		return result;
	}	
	
	/**
	 * A method that loads UML profile model from the input URI
	 * 
	 * @param uri
	 * @return uml profile
	 */
	public Profile loadProfile(String uri) {
		URI model = URI.createURI(uri);
		registerPackages(RESOURCE_SET);
		registerResourceFactories();
		EPackage.Registry.INSTANCE.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		
		RESOURCE_SET.getPackageRegistry().put("http://www.eclipse.org/uml2/5.0.0/UML", UMLPackage.eINSTANCE);
		RESOURCE_SET.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		
		Resource resource = null;
		try {
			resource = RESOURCE_SET.getResource(model, true);
		} catch (Exception e) {
//			Logger.getLogger().error(e);
			System.err.println(e);
		}
		Profile _profile = (Profile) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.PROFILE);
		return _profile;
	}
	
	/**
	 * A method that registers different versions of UML, Ecore, XML resources 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void registerResourceFactories() 
	{
		Map extensionFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();		 
		extensionFactoryMap.put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);		 
		extensionFactoryMap.put(Ecore2XMLResource.FILE_EXTENSION, Ecore2XMLResource.Factory.INSTANCE);		 
		extensionFactoryMap.put(UML22UMLResource.FILE_EXTENSION, UML22UMLResource.Factory.INSTANCE);		 		 
		extensionFactoryMap.put(UMLResource.FILE_EXTENSION, UML22UMLResource.Factory.INSTANCE);
		extensionFactoryMap.put(UMLResource.FILE_EXTENSION, UML22UMLResource.Factory.INSTANCE);		
		extensionFactoryMap.put(Resource.Factory.Registry.DEFAULT_EXTENSION,new XMIResourceFactoryImpl());
	}

	/**
	 * A method that registers different versions of UML, Ecore, XML packages
	 * 
	 * @param resourceSet
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void registerPackages(ResourceSet resourceSet) 
	{
		Map packageRegistry = resourceSet.getPackageRegistry();
		packageRegistry.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		packageRegistry.put(Ecore2XMLPackage.eNS_URI, Ecore2XMLPackage.eINSTANCE);
		packageRegistry.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		packageRegistry.put(UML212UMLResource.UML_METAMODEL_NS_URI, UMLPackage.eINSTANCE);
		//for RSA
		packageRegistry.put("http://www.eclipse.org/uml2/2.0.0/UML",UMLPackage.eINSTANCE);
		packageRegistry.put("http://www.eclipse.org/uml2/3.0.0/UML",UMLPackage.eINSTANCE);
		//for Papyrus
		packageRegistry.put("http://www.eclipse.org/uml2/5.0.0/UML",UMLPackage.eINSTANCE);
	}
}