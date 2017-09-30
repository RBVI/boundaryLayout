package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import  java.awt.geom.Point2D;
import  java.awt.geom.Rectangle2D;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutTask;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import edu.ucsf.rbvi.boundaryLayout.internal.algorithms.BoundaryContainsAlgorithm;
import prefuse.util.force.DragForce;
import prefuse.util.force.EllipseWallForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.RectangularWallForce;
import prefuse.util.force.SpringForce;

public class ForceDirectedLayoutTask extends AbstractLayoutTask {

	private ForceDirectedLayout.Integrators integrator;
	private Map<CyNode,ForceItem> forceItems;
	private ForceDirectedLayoutContext context;
	private CyServiceRegistrar registrar;
	private final List<View<CyNode>> nodeViewList;
	private final List<View<CyEdge>> edgeViewList;
	private final String chosenCategory;
	final CyNetworkView netView;
	private Map<Object, ShapeAnnotation> shapeAnnotations; 
	private Map<ShapeAnnotation, Rectangle2D.Double> annotationBoundingBox;
	private Map<ShapeAnnotation, List<Point2D.Double>> initializingNodeLocations;
	private TaskMonitor taskMonitor;
	private Random RANDOM = new Random();

	public ForceDirectedLayoutTask( final String displayName,
			final CyNetworkView netView,
			final Set<View<CyNode>> nodesToLayOut,
			final ForceDirectedLayoutContext context,
			final String layoutAttribute,
			final ForceDirectedLayout.Integrators integrator,
			final CyServiceRegistrar registrar, 
			final UndoSupport undo) {
		super(displayName, netView, nodesToLayOut, layoutAttribute, undo);

		if (nodesToLayOut != null && nodesToLayOut.size() > 0)
			nodeViewList = new ArrayList<>(nodesToLayOut);
		else
			nodeViewList = new ArrayList<>(netView.getNodeViews());

		edgeViewList = new ArrayList<>(netView.getEdgeViews());

		this.netView = netView;
		this.context = context;
		this.integrator = integrator;
		this.registrar = registrar;
		this.chosenCategory = layoutAttribute;
		// We don't want to recenter or we'll move all of our nodes away from the annotations
		recenter = false; // This is provided by AbstractLayoutTask

		initializingNodeLocations = new HashMap<>();
		shapeAnnotations = getShapeAnnotations();
		forceItems = new HashMap<CyNode, ForceItem>();

		if (shapeAnnotations == null) 
			shapeAnnotations = AutoMode.createAnnotations(netView, 
					nodeViewList, layoutAttribute, registrar);
		if(context.wallGravitationalConstant < 0)
			context.wallGravitationalConstant *= -1;
		//if(context.avoidOverlap == false)
		//context.wallGravitationalConstant = -12f;
	}

	@Override
	protected void doLayout(TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
		initializeAnnotationCoordinates();
		ForceSimulator m_fsim = new ForceSimulator();
		m_fsim.speedLimit = context.speedLimit;

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

		for(ShapeAnnotation shapeAnnotation : shapeAnnotations.values())
			initNodeLocations(shapeAnnotation);
		Rectangle2D.Double unionofBoundaries = this.getUnionofBoundaries();

		// initialize node locations and properties
		for (View<CyNode> nodeView : nodeViewList) {
			ForceItem fitem = forceItems.get(nodeView.getModel()); 
			if ( fitem == null ) {
				fitem = new ForceItem();
				forceItems.put(nodeView.getModel(), fitem);
			}

			fitem.mass = (float) context.defaultNodeMass;

			//place each node in its respective ShapeAnnotation
			Object group = null;
			if(chosenCategory != null)
				group = netView.getModel().getRow(nodeView.getModel()).getRaw(chosenCategory);

			if(group != null) {
				if(shapeAnnotations.keySet().contains(group)) {
					Point2D.Double initPosition = getNodeLocation(shapeAnnotations.get(group));
					fitem.location[0] = (float) initPosition.getX(); 
					fitem.location[1] = (float) initPosition.getY(); 
				}
				else {
					fitem.location[0] = (float) (unionofBoundaries.getX() - (unionofBoundaries.getWidth() / 4));
					fitem.location[1] = (float) (unionofBoundaries.getY() + (unionofBoundaries.getHeight() / 2));
				}
			}

			double width = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH) / 2;
			double height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT) / 2;
			fitem.dimensions[0] = (float) width;
			fitem.dimensions[1] = (float) height;
			fitem.category = group;
			m_fsim.addItem(fitem);
		}

		// initialize edges
		for (View<CyEdge> edgeView : edgeViewList) {
			CyEdge edge = edgeView.getModel();
			CyNode n1 = edge.getSource();
			ForceItem f1 = forceItems.get(n1); 
			CyNode n2 = edge.getTarget();
			ForceItem f2 = forceItems.get(n2); 
			if ( f1 == null || f2 == null )
				continue;
			m_fsim.addSpring(f1, f2, (float) context.defaultSpringCoefficient, 
					(float) context.defaultSpringLength); 
		}

		final int checkCenter = (context.numIterations / 10) + 1;
		
		// perform layout
		long timestep = 1000L;
		for ( int i = 0; i < context.numIterations && !cancelled; i++ ) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep + 50;
			m_fsim.runSimulator(step);
			if(i % checkCenter == 0) 
				checkCenter(m_fsim);
			taskMonitor.setProgress((int)(((double)i/(double)context.numIterations)*90.+5));
		}
		checkCenter(m_fsim);

		// update positions
		for (CyNode node : forceItems.keySet()) {
			ForceItem fitem = forceItems.get(node); 
			View<CyNode> nodeView = netView.getNodeView(node);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, (double)fitem.location[0]);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, (double)fitem.location[1]);
		}
	}
	
	private void checkCenter(ForceSimulator m_fsim) {
		Iterator itemsIterator = m_fsim.getItems();
		while(itemsIterator.hasNext()) {
			ForceItem nextItem = (ForceItem) itemsIterator.next();
			ShapeAnnotation nextShape = shapeAnnotations.get(nextItem.category);
			if(!annotationBoundingBox.get(nextShape).contains(new 
					Point2D.Double((double) nextItem.location[0], 
							(double) nextItem.location[1]))) {
				List<Point2D.Double> reInitializations = 
						initializingNodeLocations.get(nextShape);
				Point2D.Double reInitialize = 
						reInitializations.get(RANDOM.nextInt(reInitializations.size()));
				nextItem.location[0] = (float) reInitialize.getX();
				nextItem.location[1] = (float) reInitialize.getY();
				nextItem.plocation[0] = nextItem.location[0];
				nextItem.plocation[1] = nextItem.location[1];
			}
		}
	}

	/* @return the HashMap shapeAnnotations which consists of 
	 * all of the Shape Annotations in the current network view and
	 * maps them to their respective name. null is returned if
	 * the user did not create any Shape Annotations, which would
	 * means AutoMode must be run.
	 * */
	protected Map<Object, ShapeAnnotation> getShapeAnnotations() {
		List<Annotation> annotations = 
				registrar.getService(AnnotationManager.class).getAnnotations(netView);
		if(annotations != null) {
			Map<Object, ShapeAnnotation> shapeAnnotations = new HashMap<>();
			for(Annotation annotation : annotations)
				if(annotation instanceof ShapeAnnotation) {
					ShapeAnnotation shapeAnnotation = (ShapeAnnotation) annotation;
					shapeAnnotations.put(shapeAnnotation.getName(), shapeAnnotation);
				}
			return shapeAnnotations;
		}
		else return null;
	}

	/* @param m_fsim is the ForceSimulator instance that this
	 * added force belongs to.
	 * @param shapeAnnotation stores an existing ShapeAnnotation.
	 * This method adds the wall force associated with shapeAnnotation parameter.
	 * */
	protected void addAnnotationForce(ForceSimulator m_fsim, ShapeAnnotation shapeAnnotation) {
		Point2D.Double annotationDimensions = getAnnotationDimensions(shapeAnnotation);
		Point2D.Double annotationCenter = getAnnotationCenter(shapeAnnotation);
		switch(shapeAnnotation.getShapeType()) {
		case "Rounded Rectangle":
		case "Rectangle":
			m_fsim.addForce(new RectangularWallForce(annotationCenter, 
					annotationDimensions, -1 * context.wallGravitationalConstant));
			break;
		case "Ellipse":
			m_fsim.addForce(new EllipseWallForce(annotationCenter, 
					annotationDimensions, -1 * context.wallGravitationalConstant));
			break;
		}
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Point2D.Double dimensions of shapeAnnotation where 
	 * the x value of the point holds the width and the y value of the 
	 * point holds the height.
	 * */
	private static Point2D.Double getAnnotationDimensions(ShapeAnnotation shapeAnnotation) {
		Point2D.Double annotationDimensions = new Point2D.Double((float)
				shapeAnnotation.getShape().getBounds2D().getWidth(), 
				(float)shapeAnnotation.getShape().getBounds2D().getHeight());
		return annotationDimensions;
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Point2D.Double location where the center of the shapeAnnotation
	 * is.
	 * */
	private Point2D.Double getAnnotationCenter(ShapeAnnotation shapeAnnotation) { 
		Rectangle2D.Double boundingBox = getShapeBoundingBox(shapeAnnotation);
		double xCenter = boundingBox.getX() + (boundingBox.getWidth() / 2.0);
		double yCenter = boundingBox.getY() + (boundingBox.getHeight() / 2.0);
		return new Point2D.Double(xCenter, yCenter);
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * This method calculates and initializes a HashMap of key ShapeAnnotation
	 * and value Point2D.Double where the Point2D.Double is the location
	 * where all of the nodes of that respective shape annotation are to be
	 * initialized.
	 * */
	private void initNodeLocations(ShapeAnnotation shapeAnnotation) { 
		Rectangle2D.Double boundingBox = getShapeBoundingBox(shapeAnnotation);
		List<Rectangle2D.Double> applySpecialInitialization = 
				applySpecialInitialization(shapeAnnotation, boundingBox);
		double xPos = boundingBox.getX() + boundingBox.getWidth() / 2.0;
		double yPos = boundingBox.getY() + boundingBox.getHeight() / 2.0;
		List<Point2D.Double> initNodes = new ArrayList<>();
		initNodes.add(new Point2D.Double(xPos, yPos));
		if(!applySpecialInitialization.isEmpty()) {
			initNodes.remove(0);
			Rectangle2D.Double thisBoundingBox = getShapeBoundingBox(shapeAnnotation);
			if(shapeAnnotation.getShapeType().equals("Ellipse")) {
				double width = Math.sqrt(2 * (thisBoundingBox.getWidth()));
				double height = Math.sqrt(2 * (thisBoundingBox.getHeight()));
				double x = thisBoundingBox.getX() + ((thisBoundingBox.getWidth() - width) / 2);
				double y = thisBoundingBox.getY() + ((thisBoundingBox.getHeight() - height) / 2);
				thisBoundingBox = new Rectangle2D.Double(x, y, width, height);
			}
			List<Rectangle2D.Double> initRectangles = BoundaryContainsAlgorithm.doAlgorithm(
					annotationBoundingBox.get(shapeAnnotation), applySpecialInitialization);
			for(Rectangle2D.Double initRectangle : initRectangles) {
				xPos = initRectangle.getX() + initRectangle.getWidth() / 2;
				yPos = initRectangle.getY() + initRectangle.getHeight() / 2;
				initNodes.add(new Point2D.Double(xPos, yPos));
				// System.out.println(initRectangle.getWidth() * initRectangle.getHeight());
			}
		}
		initializingNodeLocations.put(shapeAnnotation, initNodes);
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Point2D.Double location where the node that belongs to 
	 * shapeAnnotation should be initialized, using the previously initialized
	 * HashMap initializingNodeLocations
	 * */
	private Point2D.Double getNodeLocation(ShapeAnnotation shapeAnnotation) {
		List<Point2D.Double> possibleInit = initializingNodeLocations.get(shapeAnnotation);
		return possibleInit.get(RANDOM.nextInt(possibleInit.size()));
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @param boundingBox stores the Rectangle2D of the shapeAnnotation.
	 * @return list of Rectangle2D's of shape annotations that boundingBox
	 * contains.
	 * */
	private List<Rectangle2D.Double> applySpecialInitialization(ShapeAnnotation 
			shapeAnnotation, Rectangle2D.Double boundingBox) {
		List<Rectangle2D.Double> listOfContainments = new ArrayList<>();
		for(ShapeAnnotation comparedShape : annotationBoundingBox.keySet()) {
			Rectangle2D.Double comparedBoundingBox = annotationBoundingBox.get(comparedShape);
			if(comparedShape.getName().equals(shapeAnnotation.getName())) {} 
			else if(boundingBox.intersects(comparedBoundingBox) 
					&& !comparedBoundingBox.contains(boundingBox)) 
				listOfContainments.add(comparedBoundingBox);
		}
		return listOfContainments;
	}

	/* This method calculates and initializes a HashMap of key ShapeAnnotation
	 * and value Rectangle2D.Double where the Rectangle2D.Double is the rectangle
	 * that represents its respective shape annotation and stores the width, height,
	 * location and other features of its respective shape annotation. 
	 * */
	private void initializeAnnotationCoordinates() {
		annotationBoundingBox = new HashMap<>();
		for(ShapeAnnotation shapeAnnotation : shapeAnnotations.values()) {
			Map<String, String> argMap = shapeAnnotation.getArgMap();
			double xCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.X));
			double yCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.Y));
			double width = Double.parseDouble(argMap.get(ShapeAnnotation.WIDTH)) / shapeAnnotation.getZoom();
			double height = Double.parseDouble(argMap.get(ShapeAnnotation.HEIGHT)) / shapeAnnotation.getZoom();
			annotationBoundingBox.put(shapeAnnotation, new Rectangle2D.Double(
					xCoordinate, yCoordinate, width, height));
		}
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Rectangle2D.Double representation of its respective
	 * shapeAnnotation, which is the passed parameter.
	 * */
	private Rectangle2D.Double getShapeBoundingBox(ShapeAnnotation shapeAnnotation) {
		return annotationBoundingBox.get(shapeAnnotation);
	}
	
	private Rectangle2D.Double getUnionofBoundaries() {
		Rectangle2D.Double unionofBoundaries = new Rectangle2D.Double();
		for(Rectangle2D.Double newBoundary : this.annotationBoundingBox.values()) 
			unionofBoundaries.setRect(unionofBoundaries.createUnion(newBoundary));
		return unionofBoundaries;
	}
}