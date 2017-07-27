package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import prefuse.util.force.ForceSimulator;

import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

public class AutoMode { //AutoMode class called to create all the annotations 
	private CyNetworkView cNV;
	private Set<View<CyNode>> nodesToLayout;
	private String categoryColumn;
	
	public AutoMode() { 
		createAnnotations(cNV, nodesToLayout, categoryColumn);
	}
	
	public static void createAnnotations(CyNetworkView cNV, Set<View<CyNode>> nodesToLayout, String categoryColumn) {	
	//to do 
		if(nodesToLayout != null) {
			 		Map<Object, ShapeAnnotation> shapeAnnotations = new HashMap<Object, ShapeAnnotation>();
			    	for(View<CyNode> annotation : nodesToLayout)
			 		if(annotation instanceof ShapeAnnotation)
			 				shapeAnnotations.put(categoryColumn, (ShapeAnnotation)annotation);
			 	}	
	}
}
