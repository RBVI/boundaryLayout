package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
	private File bTemplate;

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
		if(!bTemplate.exists())
			bTemplate.createNewFile();
		BufferedWriter bTempWriter = new BufferedWriter(new FileWriter(bTemplate.getAbsolutePath()));
		char shapeAnnotationIndex = 'a';
		//bTempWriter.write(bTemplate.getName());
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

	/*private void saveToFile(List<ShapeAnnotation> shapeAnnotations) throws IOException {
		templateName = "\"" + templateName + "\"";
		if(!bTemplate.exists())
			bTemplate.createNewFile();
		BufferedWriter bTempWriter = new BufferedWriter(new FileWriter(bTemplate.getAbsolutePath()));

		List<String> tempLines = new ArrayList<>();
		boolean isOverWritten = checkOverwrite(tempLines);
		File newbTemplate = new File("temp.txt");
		newbTemplate.renameTo(bTemplate);
		bTemplate.delete();
		if(tempLines.get(0).equals(""))
			tempLines.add(templateName + ",");

		/*Need to add: Method of overwriting a previous save -- check if the name has already been used -- 
		 * should also store the list of already saved templates in the text file to easily see.*/

	/*	char shapeAnnotationIndex = 'a';
		bTempWriter.newLine();
		bTempWriter.write(templateName);
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

	private static List<String> getFileLines(File templateFile) throws IOException {
		Scanner bTempScanner = new Scanner(new BufferedReader(new BufferedReader(new 
				FileReader(templateFile.getAbsolutePath()))));
		List<String> templateLines = new ArrayList<>();
		while(bTempScanner.hasNext()) 
			templateLines.add(bTempScanner.nextLine());
		bTempScanner.close();
		return templateLines;
	}

	private boolean checkOverwrite(List<String> templateLines) throws IOException {
		templateLines = getFileLines(bTemplate);
		if(templateLines.get(0).contains(templateName)) 
			for(int lineIndex = 0; lineIndex < templateLines.size(); lineIndex++) 
					if(templateLines.get(lineIndex).charAt(0) == '\"' && 
				templateLines.get(lineIndex).contains(templateName)) {
					while(lineIndex < templateLines.size() && templateLines.get(lineIndex).charAt(0) != '\"')
						templateLines.remove(lineIndex);
					return true;
				}
		return false;
	}*/
}