package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class TemplateLoadTask extends AbstractTask {	
	final CyServiceRegistrar registrar;
	
	@Tunable(description = "Choose the template text file to import")
	private File bTemplate;
	
	public TemplateLoadTask(CyServiceRegistrar registrar) {
		super();
		this.registrar = registrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {	
		AnnotationFactory<ShapeAnnotation> shapeFactory = registrar.getService(
				AnnotationFactory.class, "(type=ShapeAnnotation.class)");
		AnnotationManager annotationManager = registrar.getService(
				AnnotationManager.class);	
		CyNetworkView netView = registrar.getService(CyNetworkView.class);
		if(!bTemplate.exists())
			return;
		Scanner templateScanner = new Scanner(new BufferedReader(new 
				FileReader(bTemplate.getAbsolutePath())));
		List<String> shapeAnnotationLines = new ArrayList<>();
		
		while(templateScanner.hasNext()) 
			shapeAnnotationLines.add(templateScanner.nextLine());
		templateScanner.close();
		
		for(String shapeAnnotationArgs : shapeAnnotationLines) {
			String[] argsArray = shapeAnnotationArgs.substring(
					shapeAnnotationArgs.indexOf(')') + 3).split(", ");
			Map<String, String> argMap = new HashMap<>();
			for(String arg : argsArray) {
				String[] keyValuePair = arg.split("=");
				argMap.put(keyValuePair[0], keyValuePair[1]);
			}
			Annotation addedShape = shapeFactory.createAnnotation(
					ShapeAnnotation.class, netView, argMap);
			addedShape.setName(argMap.get(ShapeAnnotation.NAME));
			annotationManager.addAnnotation(addedShape);
			addedShape.update();
		}
	}
}