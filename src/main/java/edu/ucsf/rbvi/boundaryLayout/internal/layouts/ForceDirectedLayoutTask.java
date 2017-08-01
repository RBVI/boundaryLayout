package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import  java.awt.geom.Point2D;
import  java.awt.geom.Rectangle2D;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPoint;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import prefuse.util.force.CircularWallForce;
import prefuse.util.force.DragForce;
import prefuse.util.force.EllipseWallForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.RectangularWallForce;
import prefuse.util.force.SpringForce;

//TO DO: (1) AutoMode, (2) line 111 to sort nodes, (3) test on data

public class ForceDirectedLayoutTask extends AbstractLayoutTask {

	private ForceDirectedLayout.Integrators integrator;
	private Map<View<CyNode>,ForceItem> forceItems;
	private ForceDirectedLayoutContext context;
	private CyServiceRegistrar registrar;
	private final List<View<CyNode>> nodeViewList;
	private final List<View<CyEdge>> edgeViewList;
	private final String chosenCategory;
	final CyNetworkView netView;
	private Map<Object, ShapeAnnotation> shapeAnnotations; 
	private Map<ShapeAnnotation, Rectangle2D.Double> annotationBoundingBox;

	public ForceDirectedLayoutTask( final String displayName,
			final CyNetworkView netView,
			final Set<View<CyNode>> nodesToLayOut,
			final ForceDirectedLayoutContext context,
			final String layoutAttribute,
			final ForceDirectedLayout.Integrators integrator,
			final CyServiceRegistrar registrar, 
			final UndoSupport undo) {
		super(displayName, netView, nodesToLayOut, layoutAttribute, undo);

		if (nodesToLayOut.size() > 0)
			nodeViewList = new ArrayList<View<CyNode>>(nodesToLayOut);
		else
			nodeViewList = new ArrayList<View<CyNode>>(netView.getNodeViews());

		edgeViewList = new ArrayList<View<CyEdge>>(netView.getEdgeViews());

		this.netView = netView;
		this.context = context;
		this.integrator = integrator;
		this.registrar = registrar;
		this.chosenCategory = layoutAttribute;

		shapeAnnotations = getShapeAnnotations();
		if (shapeAnnotations == null) {
			// Use AutoMode
			shapeAnnotations = AutoMode.createAnnotations(netView, nodesToLayOut, layoutAttribute);
		}

		forceItems = new HashMap<View<CyNode>, ForceItem>();
	}

	@Override
	protected void doLayout(TaskMonitor taskMonitor) {
		initializeAnnotationCoordinates();
		ForceSimulator m_fsim = new ForceSimulator();

		m_fsim.setIntegrator(integrator.getNewIntegrator());
		m_fsim.clear();

		//initialize shape annotations and their forces
		if(shapeAnnotations != null)
			for(Object category : shapeAnnotations.keySet())
				addAnnotationForce(m_fsim, shapeAnnotations.get(category));

		m_fsim.addForce(new NBodyForce(context.avoidOverlap, context.overlapForce));
		m_fsim.addForce(new SpringForce());
		m_fsim.addForce(new DragForce());

		forceItems.clear();

		if(context.isDeterministic){
			//sort nodes views in some way ADD <---------------
		}

		// initialize node locations and properties

		for (View<CyNode> nodeView : nodeViewList) {
			ForceItem fitem = forceItems.get(nodeView); 
			if ( fitem == null ) {
				fitem = new ForceItem();
				forceItems.put(nodeView, fitem);
			}

			fitem.mass = (float) context.defaultNodeMass;

			//place each node in its respective ShapeAnnotation
			Object group = null;
			if(chosenCategory != null)
				group = netView.getModel().getRow(nodeView.getModel()).getRaw(chosenCategory);

			if(group != null) {
				if(shapeAnnotations.keySet().contains(group)) {
					Point2D.Double centerOfShape = getAnnotationCenter(shapeAnnotations.get(group));
					fitem.location[0] = (float) centerOfShape.getX(); 
					fitem.location[1] = (float) centerOfShape.getY(); 
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, centerOfShape.getX());
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, centerOfShape.getY());
				}
			}

			double width = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH) / 2;
			double height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT) / 2;
			fitem.dimensions[0] = (float) width;
			fitem.dimensions[1] = (float) height;
			m_fsim.addItem(fitem);
		}

		// initialize edges
		for (View<CyEdge> edgeView : edgeViewList) {
			CyEdge edge = edgeView.getModel();
			CyNode n1 = edge.getSource();
			ForceItem f1 = forceItems.get(n1); 
			CyNode n2 = edge.getSource();
			ForceItem f2 = forceItems.get(n2); 
			if ( f1 == null || f2 == null )
				continue;
			m_fsim.addSpring(f1, f2, (float) context.defaultSpringCoefficient, (float) context.defaultSpringLength); 
		}

		// perform layout
		long timestep = 1000L;
		for ( int i = 0; i < context.numIterations && !cancelled; i++ ) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep+50;
			m_fsim.runSimulator(step);
			taskMonitor.setProgress((int)(((double)i/(double)context.numIterations)*90.+5));
		}

		// update positions
		for (View<CyNode> nodeView : forceItems.keySet()) {
			ForceItem fitem = forceItems.get(nodeView); 
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, fitem.location[0]);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, fitem.location[1]);
		}
	}

	//gets all the shape annotations in the network view 
	//and returns a HashMap of key category name with its
	//corresponding ShapeAnnotation value
	protected Map<Object, ShapeAnnotation> getShapeAnnotations() {
		List<Annotation> annotations = 
				registrar.getService(AnnotationManager.class).getAnnotations(netView);
		if(annotations != null) {
			Map<Object, ShapeAnnotation> shapeAnnotations = new HashMap<Object, ShapeAnnotation>();
			for(Annotation annotation : annotations)
				if(annotation instanceof ShapeAnnotation) {
					ShapeAnnotation shapeAnnotation = (ShapeAnnotation) annotation;
					shapeAnnotations.put(shapeAnnotation.getName(), shapeAnnotation);
				}
			return shapeAnnotations;
		}
		else return null;
	}

	//add a force annotation for each of the shape annotations depending on type
	//of annotation
	protected void addAnnotationForce(ForceSimulator m_fsim, ShapeAnnotation shapeAnnotation) {
		Point2D.Double annotationDimensions = getAnnotationDimensions(shapeAnnotation);
		Point2D.Double annotationCenter = getAnnotationCenter(shapeAnnotation);
		switch(shapeAnnotation.getShapeType()) {
		case "RECTANGLE":
			m_fsim.addForce(new RectangularWallForce(annotationCenter, annotationDimensions));
			break;
		case "ELLIPSE":
			if(annotationDimensions.getX() == annotationDimensions.getY())
				m_fsim.addForce(new CircularWallForce(annotationCenter, 
						(float) annotationDimensions.getX()));
			//add else for ellipse
			else {
				m_fsim.addForce(new EllipseWallForce(annotationCenter, annotationDimensions));
			}
			break;
		}
	}

	//gets dimensions for the shape annotation passed
	private static Point2D.Double getAnnotationDimensions(ShapeAnnotation shapeAnnotation) {
		Point2D.Double annotationDimensions = new Point2D.Double((float)
				shapeAnnotation.getShape().getBounds2D().getWidth(), 
				(float)shapeAnnotation.getShape().getBounds2D().getHeight());
		return annotationDimensions;
	}

	//gets centerpoint for the shape annotation passed
	private Point2D.Double getAnnotationCenter(ShapeAnnotation shapeAnnotation) { 
		// return annotationCoordinates.get(shapeAnnotation);
		Rectangle2D boundingBox = getShapeBoundingBox(shapeAnnotation);
		double centerX = boundingBox.getX()+boundingBox.getWidth()/2.0;
		double centerY = boundingBox.getY()+boundingBox.getHeight()/2.0;
		return new Point2D.Double(centerX, centerY);
	}

	//initializes the annotationCoordinates HashMap (key is shapeannotation and value is
	//its respectful Point2D Location)
	private void initializeAnnotationCoordinates() {
		annotationBoundingBox = new HashMap<ShapeAnnotation, Rectangle2D.Double>();
		for(ShapeAnnotation shapeAnnotation : shapeAnnotations.values()) {
			Map<String, String> argMap = shapeAnnotation.getArgMap();
			double xCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.X));
			double yCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.Y));
			double width = Double.parseDouble(argMap.get(ShapeAnnotation.WIDTH));
			double height = Double.parseDouble(argMap.get(ShapeAnnotation.HEIGHT));
			annotationBoundingBox.put(shapeAnnotation, new Rectangle2D.Double(
					xCoordinate, yCoordinate, width, height));
		}
	}

	private Rectangle2D getShapeBoundingBox(ShapeAnnotation shape) {
		return annotationBoundingBox.get(shape);
	}
}
