package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/* 
 * AutoMode class called to create shape annotations
 * in the instance where the user does not create any
*/
public class AutoMode {
	public static Map<Object, ShapeAnnotation> createAnnotations(CyNetworkView netView, 
			Set<View<CyNode>> nodesToLayout, String categoryColumn, CyServiceRegistrar registrar) {	
		AnnotationFactory<ShapeAnnotation> shapeFactory = registrar.getService(
				AnnotationFactory.class, "(type=ShapeAnnotation.class)");
		AnnotationManager annotationManager = registrar.getService(
				AnnotationManager.class);			                                                   

		CyNetwork network = netView.getModel();

		Map<Object, List<View<CyNode>>> categoryLists = new HashMap<Object,List<View<CyNode>>>();
		Map<Object, Point2D.Double> dimensions = new HashMap<>();
		List<Object> categoryNames = new ArrayList<>();

		double spacing = 20.0; 
		double height = 0.0; 
		double width = 0.0; 

		for (View<CyNode> nodeView : nodesToLayout) {		
			CyNode node = nodeView.getModel();
			height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
			width = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);

			Object category = network.getRow(node).getRaw(categoryColumn);
			if(!categoryLists.containsKey(category))
				categoryLists.put(category, new ArrayList<View<CyNode>>());
			categoryLists.get(category).add(nodeView);

			if(!dimensions.containsKey(category))
				dimensions.put(category, new Point2D.Double(width + spacing, height + spacing));
			else {
				width += dimensions.get(category).getX() + spacing;
				height += dimensions.get(category).getY() + spacing;
				dimensions.replace(category, new Point2D.Double(width, height));
			}	
		}

		for(Object categoryName : categoryLists.keySet())
			categoryNames.add(categoryName);

		Point2D.Double maxDimensions = getMaxDimensions(dimensions);
		int categoryNamesIndex = 0;
		double x = 0.0;
		double y = 0.0;
		int numRows = (int) Math.sqrt(categoryLists.size());
		int numCols = (int) Math.sqrt(categoryLists.size()) + 1;

		for (int i = 0; i < numRows; i++ ) {
			for (int j = 0; j < numCols; j++ ) {
				Map<String, String> argMap = new HashMap<>();
				argMap.put(ShapeAnnotation.NAME, "" + 
						categoryLists.get(categoryNames.get(categoryNamesIndex)));
				argMap.put(ShapeAnnotation.X, "" + x);
				argMap.put(ShapeAnnotation.Y, "" + y);
				argMap.put(ShapeAnnotation.Z, "");
				argMap.put(ShapeAnnotation.WIDTH, "" + maxDimensions.getX());
				argMap.put(ShapeAnnotation.HEIGHT, "" + maxDimensions.getY());
				argMap.put(ShapeAnnotation.SHAPETYPE, "Rounded Rectangle");
				Annotation addedShape = shapeFactory.createAnnotation(
						ShapeAnnotation.class, netView, argMap);
				annotationManager.addAnnotation(addedShape);
				x += maxDimensions.getX();
			}
			x = 0;
			y += maxDimensions.getY();
		}			

		Map<Object, ShapeAnnotation> shapeAnnotations = new HashMap<>();

		initShapeAnnotations(shapeAnnotations, annotationManager, netView);

		return shapeAnnotations;
	}

	/* @param Map<Object, ShapeAnnotation> shapeAnnotations is a 
	 * map of key name corresponding to a shape annotation
	 * @param AnnotationManager annotationManager is used to get
	 * the annotations
	 * @param CyNetworkView netView is used to get the annotations
	 * the HashMap shapeAnnotations which consists of 
	 * all of the Shape Annotations in the current network view and
	 * maps them to their respective name. 
	 * */
	private static void initShapeAnnotations(Map<Object, ShapeAnnotation> shapeAnnotations, 
			AnnotationManager annotationManager, CyNetworkView netView) {
		List<Annotation> annotations = annotationManager.getAnnotations(netView);
		if(annotations != null) {
			for(Annotation annotation : annotations)
				if(annotation instanceof ShapeAnnotation) {
					ShapeAnnotation shapeAnnotation = (ShapeAnnotation) annotation;
					shapeAnnotations.put(shapeAnnotation.getName(), shapeAnnotation);
				}
		}
	}

	/*
	 * @param Map<Object, Point2D.Double> dimensions is a Map 
	 * of the name of the shape annotation to its width and height
	 * represented by a Point2D object
	 * @return the largest width and height that exist
	 * 
	 * This is used to create shapes of the same size
	 * */
	private static Point2D.Double getMaxDimensions(Map<Object, Point2D.Double> dimensions) { 
		Point2D.Double maxDimensions = new Point2D.Double();

		for(Point2D.Double thisDimensions : dimensions.values()) {
			double maxW = maxDimensions.getX();
			double maxH = maxDimensions.getY();
			if(maxW < thisDimensions.getX())
				maxW = thisDimensions.getX();
			if(maxH < thisDimensions.getY()) 
				maxH = thisDimensions.getY();
			maxDimensions.setLocation(maxW, maxH);
		}

		return maxDimensions;
	}
}
