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
	@SuppressWarnings("unchecked")
	public static Map<Object, ShapeAnnotation> createAnnotations(CyNetworkView netView, 
			List<View<CyNode>> nodesToLayout, String categoryColumn, CyServiceRegistrar registrar) {	
		AnnotationFactory<ShapeAnnotation> shapeFactory = registrar.getService(
				AnnotationFactory.class, "(type=ShapeAnnotation.class)");
		AnnotationManager annotationManager = registrar.getService(
				AnnotationManager.class);			                                                   

		CyNetwork network = netView.getModel();

		Map<Object, List<View<CyNode>>> categoryLists = new HashMap<>();
		Map<Object, Point2D.Double> dimensions = new HashMap<>();
		List<Object> categoryNames = new ArrayList<>();

		double spacing = 110.0; 
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

		Point2D.Double maxWidth = getMaxWidth(dimensions, categoryLists);
		Point2D.Double maxHeight = getMaxHeight(dimensions, categoryLists);
		Point2D.Double shapeDimensions = getShapeDimensions(maxWidth, maxHeight);
		
		int categoryNamesIndex = 0;
		double x = 0.0;
		double y = 0.0;
		int numCols = (int) Math.sqrt(categoryLists.size()) + 1;

		while(categoryNamesIndex < categoryLists.size()) {
			Map<String, String> argMap = new HashMap<>();
			argMap.put(ShapeAnnotation.NAME, "" + 
					categoryNames.get(categoryNamesIndex));
			argMap.put(ShapeAnnotation.X, "" + x);
			argMap.put(ShapeAnnotation.Y, "" + y);
			// argMap.put(ShapeAnnotation.Z, "");
			argMap.put(ShapeAnnotation.WIDTH, "" + shapeDimensions.getX());
			argMap.put(ShapeAnnotation.HEIGHT, "" + shapeDimensions.getY());
			argMap.put(ShapeAnnotation.SHAPETYPE, "Rounded Rectangle");
			Annotation addedShape = shapeFactory.createAnnotation(
					ShapeAnnotation.class, netView, argMap);
			addedShape.setName("" + categoryNames.get(categoryNamesIndex));
			annotationManager.addAnnotation(addedShape);
			addedShape.update();
			x += shapeDimensions.getX() + 100;
			categoryNamesIndex++;
			if(categoryNamesIndex % numCols == 0) {
				x = 0.0;
				y += shapeDimensions.getY() + 100;
			}
		}		

		netView.updateView();

		Map<Object, ShapeAnnotation> shapeAnnotations = new HashMap<>();

		initShapeAnnotations(shapeAnnotations, annotationManager, netView);

		return shapeAnnotations;
	}

	private static Point2D.Double getShapeDimensions(Point2D.Double maxWidth, 
			Point2D.Double maxHeight) {
		int quantityInWidth = ((int) Math.sqrt(maxWidth.getY())) + 1;
		int quantityInHeight = ((int) Math.sqrt(maxHeight.getX())) + 1;
		return new Point2D.Double(quantityInWidth * maxWidth.getX(), 
				quantityInHeight * maxHeight.getY());
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
					shapeAnnotation.setName(shapeAnnotation.getArgMap().get(ShapeAnnotation.NAME));
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
	private static Point2D.Double getMaxWidth(Map<Object, Point2D.Double> dimensions, 
			Map<Object, List<View<CyNode>>> categoryLists) { 
		int maxWidthQuantity = 0;
		double maxWidth = 0.;

		for(Object thisDimensionObject : dimensions.keySet()) {
			Point2D.Double thisDimension = dimensions.get(thisDimensionObject);
			if(thisDimension.getX() > maxWidth) {
				maxWidth = thisDimension.getX();
				maxWidthQuantity = categoryLists.get(thisDimensionObject).size();
			}
		}

		return new Point2D.Double(maxWidth / maxWidthQuantity, maxWidthQuantity);
	}
	
	/*
	 * @param Map<Object, Point2D.Double> dimensions is a Map 
	 * of the name of the shape annotation to its width and height
	 * represented by a Point2D object
	 * @return the largest width and height that exist
	 * 
	 * This is used to create shapes of the same size
	 * */
	private static Point2D.Double getMaxHeight(Map<Object, Point2D.Double> dimensions, 
			Map<Object, List<View<CyNode>>> categoryLists) { 
		int maxHeightQuantity = 0;
		double maxHeight = 0.;

		for(Object thisDimensionObject : dimensions.keySet()) {
			Point2D.Double thisDimension = dimensions.get(thisDimensionObject);
			if(thisDimension.getY() > maxHeight) {
				maxHeight = thisDimension.getY();
				maxHeightQuantity = categoryLists.get(thisDimensionObject).size();
			}
		}

		return new Point2D.Double(maxHeightQuantity, maxHeight / maxHeightQuantity);
	}
}
