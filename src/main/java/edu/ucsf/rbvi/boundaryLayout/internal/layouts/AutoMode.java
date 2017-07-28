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
	
	public static Map<Object, ShapeAnnotation> createAnnotations(CyNetworkView cNV, 
					                                                     Set<View<CyNode>> nodesToLayout, 
						                                                   String categoryColumn) {	
		//to do 
		// 1. Get the list of categories and required space for each category
		// 2. For each category create a rounded rectangle annotation of the correct size
		return null;
	}
}
