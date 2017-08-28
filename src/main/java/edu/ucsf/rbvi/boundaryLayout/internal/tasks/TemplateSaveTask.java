package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class TemplateSaveTask extends AbstractTask {
	final CyNetworkView networkView;
	final CyServiceRegistrar registrar;

	@Tunable(description="File containing templates", params="input=true;fileCategory=unspecified")
	public File bTemplates;

	@Tunable(description="Template name")
	public String templateName = "";

	public TemplateSaveTask(CyServiceRegistrar registrar, CyNetworkView networkView) {
		super();
		this.networkView = networkView;
		this.registrar = registrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {	
		saveToFile(getShapeAnnotations());
	}

	/* @return the HashMap shapeAnnotations which consists of 
	 * all of the Shape Annotations in the current network view and
	 * maps them to their respective name. null is returned if
	 * the user did not create any Shape Annotations, which would
	 * means AutoMode must be run.
	 * */
	private List<ShapeAnnotation> getShapeAnnotations() {
		List<Annotation> annotations = 
				registrar.getService(AnnotationManager.class).getAnnotations(networkView);
		if(annotations != null) {
			List<ShapeAnnotation> shapeAnnotations = new ArrayList<>();
			for(Annotation annotation : annotations)
				if(annotation instanceof ShapeAnnotation)
					shapeAnnotations.add((ShapeAnnotation) annotation);
			return shapeAnnotations;
		}
		else return null;
	}

	private void saveToFile(List<ShapeAnnotation> shapeAnnotations) throws IOException {
		if(!bTemplates.exists())
			bTemplates.createNewFile();
		BufferedWriter bTempWriter = new BufferedWriter(new FileWriter(bTemplates.getAbsolutePath()));
		char shapeAnnotationIndex = 'a';
		
		/*ADD: Overwriting a previous save*/
		
		bTempWriter.write("\"" + templateName + "\"");
		for(ShapeAnnotation shapeAnnotation : shapeAnnotations) { 
			bTempWriter.newLine();
			bTempWriter.write("   " + (shapeAnnotationIndex++) + ") " + shapeAnnotation.getArgMap());
		}
		try {
			bTempWriter.close();
		} catch (IOException e) {
			throw new IOException("Problems writing to stream: " + bTempWriter.toString() + 
					"[" + e.getMessage()+ "]");
		}
	}
}