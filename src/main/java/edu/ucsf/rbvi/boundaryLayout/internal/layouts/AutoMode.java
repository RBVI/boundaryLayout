package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.view.presentation.annotations.Annotation;
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
	
	//private Map<ShapeAnnotation, Rectangle2D.Double> annotationBoundingBox;
	
	public static Map<Object, ShapeAnnotation> createAnnotations(CyNetworkView cNV, 
					                                                     Set<View<CyNode>> nodesToLayout, 
						                                                   String categoryColumn) {		
		//to do 
		// 1. Get the list of categories and required space for each category	
		
		double height = 0.0; 
		double width = 0.0; 
		int categoryCount = 0;
		
		CyNetwork network = cNV.getModel();
		
		Map<Object, List<View<CyNode>>> categories = new HashMap<Object,List<View<CyNode>>>();
		Map<Object, Double> heights = new HashMap<Object, Double>();
		Map<Object, Double> widths = new HashMap<Object, Double>();
		//Map<Object, Double> sizes = new HashMap<Object, Double>();
		double spacing = 0.0; 
		
		for (View<CyNode> nv: nodesToLayout) {		
			CyNode node = nv.getModel();
			//size = nv.getVisualProperty(BasicVisualLexicon.NODE_SIZE);
			height = nv.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
			width = nv.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
			
			Object cat = network.getRow(node).getRaw(categoryColumn);
			if(!categories.containsKey(cat)) 
				categories.put(cat, new ArrayList<View<CyNode>>());
			categories.get(cat).add(nv);
			categoryCount++;
			
			spacing = getMaxVal(heights, widths);
			heights.put(cat, height + heights.get(cat) + spacing);
			widths.put(cat, width + widths.get(cat) + spacing);			
		}
	
		// 2. For each category create a rounded rectangle annotation of the correct size
		Double maxW = getMaxWidth(widths); //using the max width 
		Double maxH = getMaxHeight(heights);//using the max height 
		
		int numRows = (int) Math.sqrt(categoryCount); //round down for rows 
		int numCols = (int) Math.sqrt(categoryCount) + 1; ;//round up for columns
		for (Object category: nodesToLayout) {
			for (int i = 0; i < numRows; i++ ) {
				for (int j = 0; j < numCols; j++ ) {
					//create a rounded rectangle annotation with max height and max width 
					//x and y coord are hard coded right now 
					RoundRectangle2D annotation = new RoundRectangle2D.Double(0, 0, maxW, maxH, 50, 50); //still working on the x,y coord				
				}
			}		
		}			
		return null;
	}
	
	
	private static Double getMaxHeight(Map<Object, Double> heights) {
		Object maxHeightKey=null;
		Double maxHeight = 0.0;
		for(Map.Entry<Object,Double> entry : heights.entrySet()) {
		     if(entry.getValue() > maxHeight) {
		         maxHeight = entry.getValue();
		         maxHeightKey = entry.getKey();
		     }
		}
		return maxHeight;
	}
	
	private static Double getMaxWidth(Map<Object, Double> widths) {
		Object maxHeightKey=null;
		Double maxWidth = 0.0;
		for(Map.Entry<Object,Double> entry : widths.entrySet()) {
		     if(entry.getValue() > maxWidth) {
		         maxWidth = entry.getValue();
		         maxHeightKey = entry.getKey();
		     }
		}
		return maxWidth;
	}
	
	
	
	private static Double getMaxVal(Map<Object, Double> heights, Map<Object, Double> widths) {
		Double maxVal = 0.0;
		
		Object maxHeightKey=null;
		Double maxHeightValue = Double.MIN_VALUE; 
		for(Map.Entry<Object,Double> entry : heights.entrySet()) {
		     if(entry.getValue() > maxHeightValue) {
		         maxHeightValue = entry.getValue();
		         maxHeightKey = entry.getKey();
		     }
		}	 
		
		Object maxWidthKey= null;
		Double maxWidthValue = Double.MIN_VALUE; 
		for(Map.Entry<Object,Double> entry : widths.entrySet()) {
		     if(entry.getValue() > maxHeightValue) {
		         maxHeightValue = entry.getValue();
		         maxWidthKey = entry.getKey();
		     }
		}		
		if (maxWidthValue < maxHeightValue ) {
			maxVal = maxHeightValue;
		}
		else {
			maxVal = maxWidthValue;
		}		
		return maxVal;	
	}
	
}
