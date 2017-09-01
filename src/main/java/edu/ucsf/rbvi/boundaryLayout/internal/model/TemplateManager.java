package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

public class TemplateManager {
	private Map<String, List<String>> templates;
	private final CyServiceRegistrar registrar;
	public static final String NETWORK_TEMPLATES = "Templates Applied";

	public TemplateManager(CyServiceRegistrar registrar) {
		templates = new HashMap<>();
		this.registrar = registrar;
	}

	public boolean addTemplate(String templateName, 
			List<Annotation> annotations) {
		List<String> annotationsInfo = 
				getAnnotationInformation(annotations);
		return addTemplateStrings(templateName, annotationsInfo);
	}

	public boolean addTemplateStrings(String templateName, List<String> annotations) {
		if(templates.containsKey(templateName))
			return overwriteTemplateStrings(templateName, annotations);
		templates.put(templateName, annotations);
		if(templates.containsKey(templateName)) 
			return true;
		return false;
	}

	public boolean deleteTemplate(String templateName) {
		if(!templates.containsKey(templateName))
			return false;
		templates.remove(templateName);
		if(!templates.containsKey(templateName))
			return true;
		return false;
	}

	public boolean overwriteTemplateStrings(String templateName, 
			List<String> annotations) {
		if(!templates.containsKey(templateName))
			return false;
		templates.replace(templateName, annotations);
		return true;
	}

	public boolean overwriteTemplate(String templateName, 
			List<Annotation> annotations) {
		List<String> annotationsInfo = getAnnotationInformation(annotations);
		return overwriteTemplateStrings(templateName, annotationsInfo);
	}

	public boolean useTemplate(String templateName, 
			CyNetworkView networkView) {
		if(!templates.containsKey(templateName))
			return false;
		AnnotationManager annotationManager = registrar.getService(
				AnnotationManager.class);	
		List<String> templateInformation = templates.get(templateName);

		for(String annotationInformation : templateInformation) {
			String[] argsArray = annotationInformation.substring(
					annotationInformation.indexOf(')') + 3, 
					annotationInformation.length() - 1).split(", ");
			Map<String, String> argMap = new HashMap<>();
			for(String arg : argsArray) {
				String[] keyValuePair = arg.split("=");
				argMap.put(keyValuePair[0], keyValuePair[1]);
			}
			Annotation addedShape = getCreatedAnnotation(
					templateName, networkView, argMap);
			System.out.println(addedShape);
			System.out.println(addedShape.getArgMap().get("name") + " is the name! ahhhhh");
			addedShape.setName(argMap.get("name"));
			annotationManager.addAnnotation(addedShape);
			addedShape.update();
		}
		appendTemplatesActive(networkView, templateName);

		networkView.updateView();
		return true;
	}

	public boolean importTemplate(String templateName,
			File templateFile) throws IOException {
		if(!templateFile.exists())
			return false;
		BufferedReader templateReader = new BufferedReader(
				new FileReader(templateFile.getAbsolutePath()));
		List<String> templateInformation = new ArrayList<>();
		String annotationInformation = "";
		while((annotationInformation = templateReader.readLine()) 
				!= null)
			templateInformation.add(annotationInformation);
		if(!templates.containsKey(templateName))
			templates.put(templateName, templateInformation);
		else
			templates.replace(templateName, templateInformation);
		try {
			templateReader.close();
		} catch (IOException e) {
			throw new IOException("Problems writing to stream: " + 
					templateReader.toString() + 
					"[" + e.getMessage()+ "]");
		}
		System.out.println(templateFile.getAbsolutePath());
		if(templates.containsKey(templateName))
			return true;
		return false;
	}

	public boolean exportTemplate(String templateName, 
			String absoluteFilePath) throws IOException {
		if(!templates.containsKey(templateName))
			return false;
		File exportedFile = new File(absoluteFilePath);
		if(!exportedFile.exists())
			exportedFile.createNewFile();
		BufferedWriter templateWriter = new 
				BufferedWriter(new FileWriter(exportedFile));
		for(String annotationInformation : templates.get(templateName)) {
			templateWriter.write(annotationInformation);
			templateWriter.newLine();
		}
		try {
			templateWriter.close();
		} catch (IOException e) {
			throw new IOException("Problems writing to stream: " + 
					templateWriter.toString() + 
					"[" + e.getMessage()+ "]");
		}
		return true;
	}

	public void networkRemoveTemplates(CyNetworkView networkView, 
			List<String> templateRemoveNames) {
		AnnotationManager annotationManager = registrar.
				getService(AnnotationManager.class);
		List<Annotation> annotations = annotationManager.
				getAnnotations(networkView);
		List<String> uuidsToRemove = new ArrayList<>();
		for(String templateRemoveName : templateRemoveNames) {
			if(templates.containsKey(templateRemoveName)) {
				List<String> templateInformation =
						templates.get(templateRemoveName);
				for(String annotationInformation : templateInformation) {
					String[] argsArray = annotationInformation.substring(
							annotationInformation.indexOf(')') + 3, 
							annotationInformation.length() - 1).split(", ");
					Map<String, String> argMap = new HashMap<>();
					for(String arg : argsArray) {
						String[] keyValuePair = arg.split("=");
						argMap.put(keyValuePair[0], keyValuePair[1]);
					}
					uuidsToRemove.add(argMap.get("uuid"));
				}
			}
		}

		//make sure that the annotation is only deleted in the specific network view and 
		//not all views
		if(annotations != null) {
			for(Annotation annotation : annotations)  
				if(uuidsToRemove.contains(annotation.getUUID().toString())) {
					annotationManager.removeAnnotation(annotation);
					annotation.removeAnnotation();
				}
			removeTemplatesActive(networkView, templateRemoveNames);
			networkView.updateView();
		}
	}
 
	private static List<String> getAnnotationInformation(
			List<Annotation> annotations) {
		List<String> annotationsInfo = new ArrayList<>();
		for(Annotation annotation : annotations)
			annotationsInfo.add(annotation.getArgMap().toString());
		return annotationsInfo;
	}

	public List<String> getTemplateNames() {
		return new ArrayList<>(templates.keySet());
	}

	@SuppressWarnings("unchecked")
	private void appendTemplatesActive(CyNetworkView networkView, 
			String templateName) {
		CyTable networkTable = networkView.getModel().getDefaultNetworkTable();
		if(!columnAlreadyExists(networkTable, NETWORK_TEMPLATES))
			networkTable.createListColumn(NETWORK_TEMPLATES, String.class, false);
		CyRow networkRow = networkTable.getRow(networkView.getSUID());
		List<String> activeTemplates = (List<String>) 
				networkRow.getRaw(NETWORK_TEMPLATES);
		if(activeTemplates == null)
			activeTemplates = new ArrayList<>();
		activeTemplates.add(templateName);
		networkRow.set(NETWORK_TEMPLATES, activeTemplates);		
	}

	@SuppressWarnings("unchecked")
	private void removeTemplatesActive(CyNetworkView networkView, 
			List<String> templateRemoveNames) {
		CyTable networkTable = networkView.getModel().getDefaultNetworkTable();
		if(!columnAlreadyExists(networkTable, NETWORK_TEMPLATES))
			networkTable.createListColumn(NETWORK_TEMPLATES, String.class, false);
		CyRow networkRow = networkTable.getRow(networkView.getSUID());
		List<String> activeTemplates = (List<String>) 
				networkRow.getRaw(NETWORK_TEMPLATES);
		for(String templateRemoveName : templateRemoveNames)
			if(activeTemplates.contains(templateRemoveName))
				activeTemplates.remove(templateRemoveName);
		networkRow.set(NETWORK_TEMPLATES, activeTemplates);		
	}

	private boolean columnAlreadyExists(CyTable networkTable, String columnName) {
		for(CyColumn networkColumn : networkTable.getColumns())
			if(networkColumn.getName().equals(columnName))
				return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	private Annotation getCreatedAnnotation(String templateName, 
			CyNetworkView networkView, Map<String, String> argMap) {
		String annotationType = argMap.get("type");
		Annotation addedShape = null;
		if(annotationType.contains("ShapeAnnotation")) {
			AnnotationFactory<ShapeAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=ShapeAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(
					ShapeAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("TextAnnotation")) {
			AnnotationFactory<TextAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=TextAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(
					TextAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("ImageAnnotation")) {
			AnnotationFactory<ImageAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=ImageAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(
					ImageAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("GroupAnnotation")) {
			AnnotationFactory<GroupAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=GroupAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(
					GroupAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("ArrowAnnotation")) {
			AnnotationFactory<ArrowAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=ArrowAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(
					ArrowAnnotation.class, networkView, argMap);
		} else if(annotationType.contains("BoundedTextAnnotation")) {
			AnnotationFactory<BoundedTextAnnotation> annotationFactory = registrar.getService(
					AnnotationFactory.class, "(type=BoundedTextAnnotation.class)");
			addedShape = annotationFactory.createAnnotation(
					BoundedTextAnnotation.class, networkView, argMap);
		}
		return addedShape;
	}
	
	public Map<String, List<String>> getTemplateMap() {
		return templates;
	}
}
