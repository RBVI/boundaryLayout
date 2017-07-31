package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import prefuse.util.force.ForceSimulator;

import org.cytoscape.model.CyNetwork;
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
		CyNetwork network = cNV.getModel();
		
		Map<Object, List<View<CyNode>>> categories = new HashMap<Object,List<View<CyNode>>>();
		
		for (View<CyNode> nv: nodesToLayout) {
			CyNode node = nv.getModel();
			
			Object cat = network.getRow(node).getRaw(categoryColumn);
			if(!categories.containsKey(cat)) 
				categories.put(cat, new ArrayList<View<CyNode>>());
			categories.get(cat).add(nv);
		}
		
		double spacing;
		
		// 2. For each category create a rounded rectangle annotation of the correct size
		return null;
	}
	
	
}
