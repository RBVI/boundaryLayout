package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.util.ArrayList;
import java.util.Arrays;
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
	private Map<ShapeAnnotation, Rectangle2D> annotationBoundingBox;
	private Map<ShapeAnnotation, List<Point2D>> initializingNodeLocations;
	private TaskMonitor taskMonitor;
	private Random RANDOM = new Random();
	//private Map<Object, Double> annotationArea;
	//private Map<Object, List<View<CyNode>>> groupedNodes;
	private double boundaryPadding;

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

		if (shapeAnnotations == null && layoutAttribute != null) 
			shapeAnnotations = AutoMode.createAnnotations(netView, 
					nodeViewList, layoutAttribute, registrar);

		if(context.wallGravitationalConstant < 0)
			context.wallGravitationalConstant *= -1;

		boundaryPadding = context.padding;
	}

	// TODO: I think it would be better to layout each group in separate passes
	// Um, maybe not...
	@Override
	protected void doLayout(TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
		initializeAnnotationCoordinates();

		Rectangle2D unionOfBoundaries = null;
		if (shapeAnnotations != null) {
			for(ShapeAnnotation shapeAnnotation : shapeAnnotations.values())
				initNodeLocations(shapeAnnotation);
			unionOfBoundaries = this.getUnionofBoundaries();
		}
		
		ForceSimulator m_fsim = new ForceSimulator();
		m_fsim.speedLimit = context.speedLimit;
		m_fsim.setIntegrator(integrator.getNewIntegrator());
		m_fsim.clear();
		m_fsim.addForce(new NBodyForce(context.avoidOverlap, context.overlapForce));
		m_fsim.addForce(new SpringForce());
		m_fsim.addForce(new DragForce());
		forceItems.clear();

		if(context.isDeterministic){}

		if(shapeAnnotations != null) 
			for(Object category : shapeAnnotations.keySet()) 
				addAnnotationForce(m_fsim, shapeAnnotations.get(category));

		// initialize node locations and properties
		for (View<CyNode> nodeView : nodeViewList) {
			ForceItem fitem = forceItems.get(nodeView.getModel()); 
			if (fitem == null) {
				fitem = new ForceItem();
				forceItems.put(nodeView.getModel(), fitem);
			}

			fitem.mass = (float) context.defaultNodeMass;

			Object group = null;
			if(chosenCategory != null)
				group = netView.getModel().getRow(nodeView.getModel()).getRaw(chosenCategory);

			if(shapeAnnotations != null && group != null) {
				if(shapeAnnotations.keySet().contains(group)) {
					Point2D initPosition = getNodeLocation(shapeAnnotations.get(group));
					fitem.location[0] = (float) initPosition.getX(); 
					fitem.location[1] = (float) initPosition.getY(); 
				} else if(unionOfBoundaries != null) {
					fitem.location[0] = (float) (unionOfBoundaries.getX() - (unionOfBoundaries.getWidth() / 4));
					fitem.location[1] = (float) (unionOfBoundaries.getY() + (unionOfBoundaries.getHeight() / 2));
				} 
			}
			
			double width = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
			double height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
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
		if(annotationBoundingBox.size() != 0) {
			Iterator itemsIterator = m_fsim.getItems();
			while(itemsIterator.hasNext()) {
				ForceItem nextItem = (ForceItem) itemsIterator.next();
				ShapeAnnotation nextShape = shapeAnnotations.get(nextItem.category);
				if(annotationBoundingBox.containsKey(nextShape)) {
					Point2D location = new Point2D.Double((double) nextItem.location[0],
							(double) nextItem.location[1]);
					if(!annotationBoundingBox.get(nextShape).contains(location)) {
						// We moved the node outside of the shape
						// Find the closest point in the bounding box and move back
						Rectangle2D bbox = new Rectangle2D.Double(location.getX(), location.getY(),
								nextItem.dimensions[0], nextItem.dimensions[1]);

						Point2D nearestPoint = getNearestPoint(annotationBoundingBox.get(nextShape), bbox);

						nextItem.location[0] = (float) nearestPoint.getX();
						nextItem.location[1] = (float) nearestPoint.getY();
						nextItem.plocation[0] = nextItem.location[0];
						nextItem.plocation[1] = nextItem.location[1];
					}
				}
			}
		}
	}

	private Point2D getNearestPoint(Rectangle2D shape, Rectangle2D bbox) {
		double[] diffVector = { bbox.getX() - shape.getCenterX(), bbox.getY() - shape.getCenterY()};
		double scale = 1.;

		//if top or bottom are sides are closer -> scale based on height, otherwise based on width
		if(shape.getHeight() / shape.getWidth() <= Math.abs(diffVector[1] / diffVector[0])) 
			scale = (shape.getHeight() / 2) / Math.abs(diffVector[1]);
		else 
			scale = (shape.getWidth() / 2) / Math.abs(diffVector[0]);	

		diffVector[0] = diffVector[0] * scale; 
		diffVector[1] = diffVector[1] * scale;
		nodeScaleVector(shape, bbox, diffVector);
		
		return new Point2D.Double(shape.getCenterX() + diffVector[0], shape.getCenterY() + diffVector[1]);
	}

	private void nodeScaleVector(Rectangle2D shape, Rectangle2D bbox, double[] diffVector) {
		if(Math.abs(diffVector[0]) + bbox.getWidth() / 2 > shape.getWidth() / 2) 
			diffVector[0] += shape.getWidth() / 2 * (diffVector[0] > 0 ? -1 : 1);
		if(Math.abs(diffVector[1]) + bbox.getHeight() / 2 > shape.getHeight() / 2) 
			diffVector[1] += shape.getHeight() / 2 * (diffVector[1] > 0 ? -1 : 1);
	}
	
	/*
	private Point2D nearestPoint(Rectangle2D shape, Rectangle2D bbox) {
		// First, find the nearest side
		double topPoint[] = new double[2];
		double rightPoint[] = new double[2];
		double bottomPoint[] = new double[2];
		double leftPoint[] = new double[2];

		double topLeft[] = { shape.getX(), shape.getY() };
		double topRight[] = { shape.getX()+shape.getWidth(), shape.getY() };
		double bottomLeft[] = { shape.getX(), shape.getY()+shape.getHeight() };
		double bottomRight[] = { shape.getX()+shape.getWidth(), shape.getY()+shape.getHeight() };

		double topDist = nearestPoint(topPoint, topLeft, topRight, bbox);
		topPoint[1] = topPoint[1]-bbox.getHeight();
		double leftDist = nearestPoint(leftPoint, bottomLeft, topLeft, bbox);
		leftPoint[0] = leftPoint[0]+bbox.getWidth();
		double bottomDist = nearestPoint(leftPoint, bottomLeft, bottomRight, bbox);
		bottomPoint[1] = bottomPoint[1]+bbox.getHeight();
		double rightDist = nearestPoint(leftPoint, bottomRight, topRight, bbox);
		rightPoint[0] = rightPoint[0]-bbox.getWidth();

		double dist = topDist;
		double[] intersection = topPoint;

		if (leftDist < dist) {
			intersection = leftPoint;
			dist = leftDist;
		}
		if (bottomDist < dist) {
			intersection = bottomPoint;
			dist = bottomDist;
		}
		if (rightDist < dist) {
			intersection = rightPoint;
			dist = rightDist;
		}

		return new Point2D.Double(intersection[0], intersection[1]);
	}

	private double nearestPoint(double[] closest, double[] a, double[] b, Rectangle2D bbox) {
		double[] p = { bbox.getX(), bbox.getY() };
		double[] a_to_p = { p[0]-a[0], p[1]-a[1] };
		double[] a_to_b = { b[0]-a[0], b[1]-a[1] };

		double atb2 = a_to_b[0]*a_to_b[0] + a_to_b[1]*a_to_b[1];

		double atp_dot_atb = a_to_p[0]*a_to_b[0] + a_to_p[1]*a_to_b[1];

		double t = atp_dot_atb / atb2;
		if (t < 0) t = 0;
		if (t > 1) t = 1;

		closest[0] = a[0]+a_to_b[0]*t;
	 	closest[1] = a[1]+a_to_b[1]*t;

		return atb2;
	}*/

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
		Point2D annotationDimensions = getAnnotationDimensions(shapeAnnotation);
		Point2D annotationCenter = getAnnotationCenter(shapeAnnotation);
		// System.out.println("Shape '"+shapeAnnotation.getName()+"' type: "+shapeAnnotation.getShapeType());
		m_fsim.addForce(new RectangularWallForce(annotationCenter, 
				annotationDimensions, -1 * context.wallGravitationalConstant));
		/*
		switch(shapeAnnotation.getShapeType()) {
		case "Rounded Rectangle":
		case "Rectangle":
			// System.out.println("Rectangle dimensions"+annotationDimensions);
			m_fsim.addForce(new RectangularWallForce(annotationCenter, 
					annotationDimensions, -1 * context.wallGravitationalConstant));
			break;
		case "Ellipse":
			// System.out.println("Ellipse dimensions:"+annotationDimensions);
			// System.out.println("Ellipse center:"+annotationCenter);
			// This will set the bounds to be the inner dimensions of the ellipse
			// annotationDimensions.setLocation(annotationDimensions.getX()*Math.sqrt(2)/2,
			//                                  annotationDimensions.getY()*Math.sqrt(2)/2);
			// System.out.println("Inner rectangle dimensions"+annotationDimensions);
			m_fsim.addForce(new RectangularWallForce(annotationCenter, 
					annotationDimensions, -1 * context.wallGravitationalConstant));
			break;
		}
		 */
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Point2D dimensions of shapeAnnotation where 
	 * the x value of the point holds the width and the y value of the 
	 * point holds the height.
	 * */
	private Point2D getAnnotationDimensions(ShapeAnnotation shapeAnnotation) {
		System.out.println(shapeAnnotation.getName() + "\n\n --");
		Rectangle2D bb = annotationBoundingBox.get(shapeAnnotation);
		return new Point2D.Double(bb.getWidth(), bb.getHeight());
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Point2D location where the center of the shapeAnnotation
	 * is.
	 * */
	private Point2D getAnnotationCenter(ShapeAnnotation shapeAnnotation) { 
		Rectangle2D bb = annotationBoundingBox.get(shapeAnnotation);
		return new Point2D.Double(bb.getX()+bb.getWidth()/2, bb.getY()+bb.getHeight()/2);
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * This method calculates and initializes a HashMap of key ShapeAnnotation
	 * and value Point2D where the Point2D is the location
	 * where all of the nodes of that respective shape annotation are to be
	 * initialized.
	 * */
	private void initNodeLocations(ShapeAnnotation shapeAnnotation) { 
		Rectangle2D boundingBox = getShapeBoundingBox(shapeAnnotation);
		List<Rectangle2D> applySpecialInitialization = 
				applySpecialInitialization(shapeAnnotation, boundingBox);
		double xCenter = boundingBox.getX() + boundingBox.getWidth() / 2.0;
		double yCenter = boundingBox.getY() + boundingBox.getHeight() / 2.0;
		List<Point2D> initNodes = new ArrayList<>();
		initNodes.add(new Point2D.Double(xCenter, yCenter));
		if(!applySpecialInitialization.isEmpty()) {
			// System.out.println("Special initialization");
			initNodes.remove(0);
			/*Rectangle2D thisBoundingBox = getShapeBoundingBox(shapeAnnotation);
			if(shapeAnnotation.getShapeType().equals("Ellipse")) {
				double width = Math.sqrt(2) * thisBoundingBox.getWidth()/2;
				double height = Math.sqrt(2) * thisBoundingBox.getHeight()/2;
				double x = thisBoundingBox.getX();
				double y = thisBoundingBox.getY();
				thisBoundingBox = new Rectangle2D.Double(x, y, width, height);
			}*/
			List<Rectangle2D> initRectangles = BoundaryContainsAlgorithm.doAlgorithm(
					annotationBoundingBox.get(shapeAnnotation), applySpecialInitialization);
			for(Rectangle2D initRectangle : initRectangles) {
				xCenter = initRectangle.getX() + initRectangle.getWidth() / 2;
				yCenter = initRectangle.getY() + initRectangle.getHeight() / 2;
				initNodes.add(new Point2D.Double(xCenter, yCenter));
				// System.out.println(initRectangle.getWidth() * initRectangle.getHeight());
			}
		}
		initializingNodeLocations.put(shapeAnnotation, initNodes);
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Point2D location where the node that belongs to 
	 * shapeAnnotation should be initialized, using the previously initialized
	 * HashMap initializingNodeLocations
	 * */
	private Point2D getNodeLocation(ShapeAnnotation shapeAnnotation) {
		List<Point2D> possibleInit = initializingNodeLocations.get(shapeAnnotation);
		return possibleInit.get(RANDOM.nextInt(possibleInit.size()));
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @param boundingBox stores the Rectangle2D of the shapeAnnotation.
	 * @return list of Rectangle2D's of shape annotations that boundingBox
	 * contains.
	 * */
	private List<Rectangle2D> applySpecialInitialization(ShapeAnnotation 
			shapeAnnotation, Rectangle2D boundingBox) {
		List<Rectangle2D> listOfContainments = new ArrayList<>();
		for(ShapeAnnotation comparedShape : annotationBoundingBox.keySet()) {
			Rectangle2D comparedBoundingBox = annotationBoundingBox.get(comparedShape);
			if(comparedShape.getName().equals(shapeAnnotation.getName())) {} 
			else if(boundingBox.intersects(comparedBoundingBox) 
					&& !comparedBoundingBox.contains(boundingBox))  {
				// System.out.println("Overlap between "+shapeAnnotation.getName()+": "+boundingBox+" and "+comparedShape.getName()+": "+comparedBoundingBox);
				listOfContainments.add(comparedBoundingBox);
			}
		}
		return listOfContainments;
	}

	/* This method calculates and initializes a HashMap of key ShapeAnnotation
	 * and value Rectangle2D where the Rectangle2D is the rectangle
	 * that represents its respective shape annotation and stores the width, height,
	 * location and other features of its respective shape annotation. 
	 * */
	private void initializeAnnotationCoordinates() {
		annotationBoundingBox = new HashMap<>();
		if (shapeAnnotations == null) return;
		for(ShapeAnnotation shapeAnnotation : shapeAnnotations.values()) {
			Map<String, String> argMap = shapeAnnotation.getArgMap();
			double xCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.X));
			double yCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.Y));
			double width = Double.parseDouble(argMap.get(ShapeAnnotation.WIDTH)) / shapeAnnotation.getZoom();
			double height = Double.parseDouble(argMap.get(ShapeAnnotation.HEIGHT)) / shapeAnnotation.getZoom();
			if(shapeAnnotation.getShapeType().equals("Ellipse")) {
				//only want inner rectangle
				double xCenter = xCoordinate+width/2;
				double yCenter = yCoordinate+height/2;
				width = width * Math.sqrt(2) / 2;
				height = height * Math.sqrt(2) / 2;
				xCoordinate = xCenter-width/2;
				yCoordinate = yCenter-height/2;
			} else {
				// Give a little padding
				xCoordinate = xCoordinate+boundaryPadding;
				yCoordinate = yCoordinate+boundaryPadding;
				width = width-(boundaryPadding*2);
				height = height-(boundaryPadding*2);
			}

			annotationBoundingBox.put(shapeAnnotation, 
					new Rectangle2D.Double(xCoordinate, yCoordinate, width, height));
		}
	}

	/* @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Rectangle2D representation of its respective
	 * shapeAnnotation, which is the passed parameter.
	 * */
	private Rectangle2D getShapeBoundingBox(ShapeAnnotation shapeAnnotation) {
		return annotationBoundingBox.get(shapeAnnotation);
	}

	private Rectangle2D getUnionofBoundaries() {
		if(annotationBoundingBox.size() == 0)
			return null;
		Rectangle2D unionOfBoundaries = new Rectangle2D.Double();
		for(Rectangle2D newBoundary : this.annotationBoundingBox.values()) 
			unionOfBoundaries.setRect(unionOfBoundaries.createUnion(newBoundary));
		return unionOfBoundaries;
	}
}
