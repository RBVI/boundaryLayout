package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.awt.geom.Line2D;
import  java.awt.geom.Point2D;
import  java.awt.geom.Rectangle2D;

import org.cytoscape.application.CyVersion;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutTask;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import edu.ucsf.rbvi.boundaryLayout.internal.algorithms.BoundaryContainsAlgorithm;
import prefuse.util.force.BoundaryWallForce;
import prefuse.util.force.EllipticalWallForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.RectangularWallForce;
import prefuse.util.force.SpringForce;

/**
 * This class contains all the logic corresponding to the force-directed aspects of boundary layout
 */
public class ForceDirectedLayoutTask extends AbstractLayoutTask {
	private ForceDirectedLayout.Integrators integrator;
	private Map<CyNode,ForceItem> forceItems;
	private ForceDirectedLayoutContext context;
	private CyServiceRegistrar registrar;
	private final List<View<CyNode>> nodeViewList;
	private final List<View<CyEdge>> edgeViewList;
	private final String chosenCategory;
	final CyNetworkView netView;
	private Map<Object, BoundaryAnnotation> boundaries;
	private static final String OUTER_UNION_KEY = "Default Outer Boundary";

	private Rectangle2D unionOfBoundaries;

	/**
	 * Construct a force directed layout task, holding the information relevant to the 
	 * network view including: nodes, edges, boundaries.
	 * @param displayName is the name of the algorithm
	 * @param netView is the network view on which the user has chosen to execute the algorithm
	 * @param nodesToLayOut is the list of nodes which are to be layed out
	 * @param context is the context of this execution which the user has tuned or is the default context
	 * @param layoutAttribute is the attribute by which the user chose to run the algorithm
	 * @param integrator is the force integrator to be used for the force-directed layout
	 * @param registrar provides the services used by this task including various managers
	 * @param undo allows the user to undo this layout
	 */
	public ForceDirectedLayoutTask(final String displayName, final CyNetworkView netView,
			final Set<View<CyNode>> nodesToLayOut, final ForceDirectedLayoutContext context,
			final String layoutAttribute, final ForceDirectedLayout.Integrators integrator,
			final CyServiceRegistrar registrar, final UndoSupport undo) {
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

		// We don't want to recenter or we'll move all of our nodes away from their boundaries
		recenter = false; // This is provided by AbstractLayoutTask
		forceItems = new HashMap<CyNode, ForceItem>();
	}

	/**
	 * The layout initializes nodes to their respective boundaries and runs a force-directed
	 * layout consisting of a variety of forces: 
	 * 1) Wall forces: this repulsive force corresponds to the boundaries and push nodes away from the wall
	 * 2) NBody force: this repulsive force pushes nearby nodes away from each other and when enabled,
	 * handles avoiding node overlapping
	 * 3) Spring force: this force acts as a spring with a desired length
	 * 4) Drag force: this force slows nodes down so they do not move very quickly
	 */
	@Override
	protected void doLayout(TaskMonitor taskMonitor) {
		getBoundaries();
		this.unionOfBoundaries = null;
		if ((boundaries == null || boundaries.isEmpty()) && layoutAttribute != null)  
			boundaries = AutoMode.createAnnotations(netView, nodeViewList, layoutAttribute, registrar);
		if(boundaries.containsKey(null))
			boundaries.remove(null);

		//initialize simulation and add the various forces
		ForceSimulator m_fsim = new ForceSimulator();
		m_fsim.speedLimit = context.speedLimit;
		m_fsim.addForce(new SpringForce());
		forceItems.clear();

		//initialize initial node locations
		if (boundaries != null) {
			this.unionOfBoundaries = getUnionofBoundaries(boundaries.values());
			this.initializeOuterBoundary();
			for(BoundaryAnnotation boundary : boundaries.values()) 
				initNodeLocations(boundary);
		}

		// initialize node locations and properties
		for (View<CyNode> nodeView : nodeViewList) {
			ForceItem fitem = forceItems.get(nodeView.getModel()); 
			if (fitem == null) {
				fitem = new ForceItem();
				forceItems.put(nodeView.getModel(), fitem);
			}

			fitem.mass = (float) context.nodeMass;

			Object group = null;
			if(chosenCategory != null)
				group = netView.getModel().getRow(nodeView.getModel()).getRaw(chosenCategory);

			double width = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
			double height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
			fitem.dimensions[0] = (float) width;
			fitem.dimensions[1] = (float) height;
			fitem.category = group;

			if(unionOfBoundaries != null) {
				if(boundaries.containsKey(fitem.category)) {
					Rectangle2D intersectionUnion = boundaries.get(fitem.category).getUnionOfIntersections();
					fitem.location[0] = (float) intersectionUnion.getCenterX();
					fitem.location[1] = (float) intersectionUnion.getCenterY();
				} else {
					fitem.category = OUTER_UNION_KEY;
					fitem.location[0] = (float) unionOfBoundaries.getCenterX();
					fitem.location[1] = (float) unionOfBoundaries.getCenterY();
				}
			} else {
				fitem.location[0] = 0f;
				fitem.location[1] = 0f;
			}

			m_fsim.addItem(fitem);
		}


		//initialize edges
		for (View<CyEdge> edgeView : edgeViewList) {
			CyEdge edge = edgeView.getModel();
			CyNode n1 = edge.getSource();
			ForceItem f1 = forceItems.get(n1); 
			CyNode n2 = edge.getTarget();
			ForceItem f2 = forceItems.get(n2); 
			if ( f1 == null || f2 == null )
				continue;
			m_fsim.addSpring(f1, f2, (float) context.defaultSpringCoefficient, (float) context.defaultSpringLength); 
		}
		
		// perform layout and check center at intervals
		int j = 0;
		long timestep = 1000L;
		for (int i = 0; i < context.numIterations / 6 && !cancelled; i++) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep + 50;
			m_fsim.runSimulator(step);
			taskMonitor.setProgress((int)(((double)i/(double)context.numIterations)*90.+5));
		}

		if(boundaries != null && !boundaries.isEmpty()) 
			for(BoundaryAnnotation boundary : boundaries.values()) 
				addAnnotationForce(m_fsim, boundary);
		
		// perform layout at desired speedlimit with boundary forces
		for (int i = context.numIterations / 6; i < 2 * context.numIterations / 3 && !cancelled; i++) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep + 50;
			checkCenter(m_fsim);
			m_fsim.runSimulator(step);
			updateNodeViews();
			taskMonitor.setProgress((int)(((double)i/(double)context.numIterations)*90.+5));
		}
		updateNodeViews();

		// perform layout while looking at NBodyForce interactions
		m_fsim.addForce(new NBodyForce(context.nodeRepulsionConst));
		for(int i = 2 * context.numIterations / 3; i < context.numIterations && !cancelled; i++) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep + 50;
			checkCenter(m_fsim);
			m_fsim.runSimulator(step);
			taskMonitor.setProgress((int)(((double)i/(double)context.numIterations)*90.+5));
		}
		checkCenter(m_fsim);

		if(boundaries.containsKey(OUTER_UNION_KEY)) 
			boundaries.remove(OUTER_UNION_KEY);
		updateNodeViews();
	}

	/** Private method
	 * Update positions of nodes
	 */
	private void updateNodeViews() {
		for (CyNode node : forceItems.keySet()) {
			ForceItem fitem = forceItems.get(node); 
			View<CyNode> nodeView = netView.getNodeView(node);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, (double) fitem.location[0]);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, (double) fitem.location[1]);
		}
	}

	/*Functions related to node projections*/

	/** Private method
	 * This method projects all the nodes which have left their boundary
	 * or entered into another boundary to their nearest respective locations
	 * in their own boundaries.
	 * @param m_fsim is the simulator that is currently running
	 * @precondition boundaries is initialized 
	 * @precondition m_fsim != null
	 */
	private void checkCenter(ForceSimulator m_fsim) {
		if(boundaries.size() != 0) {
			Iterator itemsIterator = m_fsim.getItems();
			while(itemsIterator.hasNext()) {
				ForceItem nextItem = (ForceItem) itemsIterator.next();
				double x = (double) (nextItem.location[0] - nextItem.dimensions[0] / 2f);
				double y = (double) (nextItem.location[1] - nextItem.dimensions[1] / 2f);

				Rectangle2D bbox = new Rectangle2D.Double(x, y, (double) nextItem.dimensions[0], (double) nextItem.dimensions[1]);
				BoundaryAnnotation nextBoundary;
				if(boundaries.containsKey(nextItem.category))
					nextBoundary = boundaries.get(nextItem.category);
				else
					nextBoundary = boundaries.get(OUTER_UNION_KEY);

				int moveDir = BoundaryWallForce.IN_PROJECTION;
				if(!contains(nextBoundary, bbox, moveDir)) {
					// We moved the node outside of the shape. Find the closest point in the bound and move back
					nextBoundary.newProjection(moveDir);
					Point2D nearestPoint = getNearestPoint(nextBoundary, bbox, moveDir);
					updateItemInfo(nextItem, nearestPoint, bbox);
				} 

				//look at each intersecting shape annotation and project accordingly
				if(nextBoundary.hasIntersections()) {
					moveDir = BoundaryWallForce.OUT_PROJECTION;
					for(BoundaryAnnotation intersectingBoundary : nextBoundary.getIntersections()) {
						if(contains(intersectingBoundary, bbox, moveDir)) {
							Point2D nearestPoint = getNearestPoint(intersectingBoundary, bbox, moveDir);
							updateItemInfo(nextItem, nearestPoint, bbox);
							intersectingBoundary.newProjection(moveDir);
						}
					}
					for(BoundaryAnnotation intersectingBoundary : nextBoundary.getIntersections()) 
						if(contains(intersectingBoundary, bbox, moveDir)) 
							updateItemInfo(nextItem, nextBoundary.getRandomNodeInit(), bbox);
					moveDir = BoundaryWallForce.IN_PROJECTION;
					if(!contains(nextBoundary, bbox, moveDir)) 
						updateItemInfo(nextItem, nextBoundary.getRandomNodeInit(), bbox);
				} 
			}
		}
	}

	/** Private method
	 * This method is used by checkCenter() and @return true iff the @param nodebbox, taking into account its dimensions
	 * and projection direction (@param moveDir), is respectively within the @param boundary
	 */
	private boolean contains(BoundaryAnnotation boundary, Rectangle2D nodebbox, int moveDir) {
		boolean contained = true;
		Rectangle2D shapeBox = boundary.getBoundingBox();
		double[] diffVector = {Math.abs(nodebbox.getCenterX() - shapeBox.getCenterX()), Math.abs(nodebbox.getCenterY() - shapeBox.getCenterY())};
		diffVector[0] += nodebbox.getWidth() / 2 * moveDir;
		diffVector[1] += nodebbox.getHeight() / 2 * moveDir;

		switch(boundary.getShapeType()) {
		case "Rounded Rectangle":
		case "Rectangle": 
			if(diffVector[0] > shapeBox.getWidth() / 2. || diffVector[1] > shapeBox.getHeight() / 2.) 
				contained = false;
			break;

		case "Ellipse":
			double xVec = (diffVector[0] * diffVector[0]) / (shapeBox.getWidth() * shapeBox.getWidth() / 4.);
			double yVec = (diffVector[1] * diffVector[1]) / (shapeBox.getHeight() * shapeBox.getHeight() / 4.);
			if(xVec + yVec >= 1)
				contained = false;
			break;

		default:
			if(diffVector[0] < shapeBox.getWidth() / 2. && diffVector[1] < shapeBox.getHeight() / 2.) {
				if(moveDir == BoundaryWallForce.OUT_PROJECTION && !boundary.intersects(nodebbox)) 
					contained = false;
				else if(moveDir == BoundaryWallForce.IN_PROJECTION && !boundary.contains(nodebbox))
					contained = false;
			} else
				contained = false;
			break;
		}
		return contained;
	}

	/** Private method
	 * @param item is the force item that is to be set to the location of
	 * @param loc, a point2D representing a point in the network.
	 */
	private void updateItemInfo(ForceItem item, Point2D loc, Rectangle2D bbox) {
		item.location[0] = (float) loc.getX();
		item.location[1] = (float) loc.getY();
		item.plocation[0] = item.location[0];
		item.plocation[1] = item.location[1];
		bbox.setRect((double) item.location[0], (double) item.location[1], item.dimensions[0], item.dimensions[1]);
	} 

	/** Private method
	 * @return the point at the border of the @param boundary closest to @param bbox, the node bounding box. This
	 * takes into account the direction of the projection @param moveDir
	 */
	private Point2D getNearestPoint(BoundaryAnnotation boundary, Rectangle2D bbox, int moveDir) {
		Rectangle2D shapeBox = boundary.getBoundingBox();
		double[] diffVector = { bbox.getCenterX() - shapeBox.getCenterX(), bbox.getCenterY() - shapeBox.getCenterY()};
		double scale = 1.;

		switch(boundary.getShapeType()) {
		case "Rounded Rectangle":
		case "Rectangle":
		case "Ellipse":
			scale = getScaleSpecial(shapeBox, bbox, diffVector, moveDir, boundary.getShapeType());
			break;

		default:
			scale = getScalePolygon(boundary, bbox, diffVector, moveDir);
			break;
		}	

		scale *= (moveDir == 1 ? 0.985 : 1.015);
		diffVector[0] = diffVector[0] * scale; 
		diffVector[1] = diffVector[1] * scale;
		diffVector[0] -= bbox.getWidth() / 2 * moveDir * (diffVector[0] < 0 ? -1 : 1);
		diffVector[1] -= bbox.getHeight() / 2 * moveDir * (diffVector[1] < 0 ? -1 : 1);
		return new Point2D.Double(shapeBox.getCenterX() + diffVector[0], shapeBox.getCenterY() + diffVector[1]);
	}

	private double getScalePolygon(BoundaryAnnotation boundary, Rectangle2D node, double[] diffVector, int moveDir) {
		double scale = 1.;
		List<VertexData> vertices = boundary.getVertices();
		if(vertices != null && !vertices.isEmpty()) {
			VertexData vertexNode = new VertexData(new Point2D.Double(node.getCenterX(), node.getCenterY()), boundary.getBoundingBox());
			double nodeAngle = vertexNode.getRelativeAngle();

			VertexData vertexA = vertices.get(0);
			VertexData vertexB = null;
			vertices.add(vertexA);
			for(int vertex = 1; vertex < vertices.size(); vertex++) {
				vertexB = vertices.get(vertex);

				double angleA = vertexA.getRelativeAngle();
				double angleB = vertexB.getRelativeAngle();
				boolean isBetween = nodeAngle >= angleA ^ nodeAngle >= angleB;
				if(isBetween ^ Math.abs(angleA - angleB) > Math.PI)
					break;

				vertexA = vertexB;
			}

			Line2D line = new Line2D.Double(vertexA.getPoint(), vertexB.getPoint());
			double nodeSideDist = line.ptSegDist(vertexNode.getPoint());
			double nodeBoundaryDist = Math.sqrt(diffVector[0] * diffVector[0] + diffVector[1] * diffVector[1]);

			scale = (nodeBoundaryDist - nodeSideDist * moveDir) / nodeBoundaryDist; 
		}
		return scale;
	}

	private double getScaleSpecial(Rectangle2D shape, Rectangle2D bbox, double[] diffVector, int moveDir, String type) {
		diffVector[0] += bbox.getWidth() / 2 * moveDir * (diffVector[0] < 0 ? -1 : 1);
		diffVector[1] += bbox.getHeight() / 2 * moveDir * (diffVector[1] < 0 ? -1 : 1);
		if(type.equals("Ellipse"))
			return getScaleEllipse(shape, bbox, diffVector);
		else
			return getScaleRectangle(shape, diffVector);
	}

	/** Private method
	 * @return the scale factor by which to scale the @param diffVector of a node
	 * with respect to @param shape, corresponding to a rectangle
	 */
	private double getScaleRectangle(Rectangle2D shape, double[] diffVector) {
		double scale = 1.;
		//if top or bottom are sides are closer -> scale based on height, otherwise based on width
		if(shape.getHeight() / shape.getWidth() <= Math.abs(diffVector[1] / diffVector[0])) 
			scale = (shape.getHeight() / 2) / Math.abs(diffVector[1]);
		else 
			scale = (shape.getWidth() / 2) / Math.abs(diffVector[0]);
		return scale;
	}

	/** Private method
	 * @return the scale factor by which to scale the @param diffVector of a node
	 * with respect to @param shape, corresponding to an ellipse
	 */
	private double getScaleEllipse(Rectangle2D shape, Rectangle2D bbox, double[] diffVector) { 
		double scale = 1.;
		double xVec = (diffVector[0] * diffVector[0]) / (shape.getWidth() * shape.getWidth() / 4.);
		double yVec = (diffVector[1] * diffVector[1]) / (shape.getHeight() * shape.getHeight() / 4.);
		scale = 1 / Math.sqrt(xVec + yVec);
		return scale;
	}

	/** Private method
	 * @return the HashMap shapeAnnotations which consists of 
	 * all of the Shape Annotations in the current network view and
	 * maps them to their respective name. null is returned if
	 * the user did not create any Shape Annotations, which would
	 * means AutoMode must be run.
	 */
	private void getBoundaries() {
		AnnotationManager manager = registrar.getService(AnnotationManager.class);

		/* At least one foreground annotation must exist in order to get the correct x and y positions of the annotation. 
		 * So, we add a trivial foreground shape and then later remove it. This applies only for versions before 3.7.0 */
		int minorVersion = registrar.getService(CyVersion.class).getMinorVersion();
		ShapeAnnotation shape = null;
		if(minorVersion < 7) {
			AnnotationFactory<ShapeAnnotation> shapeFactory = registrar.getService(
					AnnotationFactory.class, "(type=ShapeAnnotation.class)");
			shape = shapeFactory.createAnnotation(ShapeAnnotation.class, netView, new HashMap<>());
			shape.setBorderWidth(0);
			shape.setCanvas(ShapeAnnotation.FOREGROUND);
			manager.addAnnotation(shape);
			shape.setName(OUTER_UNION_KEY);
			shape.update();
			netView.updateView();
		}

		//get the boundaries in the network view
		List<Annotation> annotations = manager.getAnnotations(netView);
		if(boundaries == null)
			boundaries = new HashMap<>();
		if(annotations != null) {
			for(Annotation annotation : annotations) {
				if(annotation instanceof ShapeAnnotation && !annotation.getName().equals(OUTER_UNION_KEY)) {
					ShapeAnnotation shapeAnnotation = (ShapeAnnotation) annotation;
					BoundaryAnnotation boundary = new BoundaryAnnotation(shapeAnnotation);
					boundaries.put(shapeAnnotation.getName(), boundary);
				}
			}
		}

		if(minorVersion < 7) {
			manager.removeAnnotation(shape);
			shape.removeAnnotation();
			netView.updateView();
		}
	}

	/** Private method
	 * This method adds a wall force, otherwise known as an annotation force corresponding to the given 
	 * boundary within the passed simulation. This wall force is in the shape of a bounding rectangle. For
	 * ellipses, the bounds of the wall are within the annotation.
	 * @param m_fsim is the ForceSimulator instance that this added force belongs to.
	 * @param shapeAnnotation stores an existing ShapeAnnotation.
	 */
	private void addAnnotationForce(ForceSimulator m_fsim, BoundaryAnnotation boundary) {
		Point2D dimensions = getAnnotationDimensions(boundary);
		Point2D center = getAnnotationCenter(boundary);
		BoundaryWallForce wall;

		if(boundary.getShapeType().equals("Ellipse")) {
			wall = new EllipticalWallForce(center, dimensions, context.gravConst,
					context.variableWallForce, context.wallScale);
		} else {
			wall = new RectangularWallForce(center, dimensions, context.gravConst, 
					context.variableWallForce, context.wallScale);
		}

		wall.setActiveCategories(getWallForceCategories(boundary));
		boundary.setWallForce(wall);
		m_fsim.addForce(wall);
	}

	private List<String> getWallForceCategories(BoundaryAnnotation boundary) {
		Rectangle2D boundingbox = boundary.getBoundingBox();
		List<String> activeCategories = new ArrayList<>();

		activeCategories.add(boundary.getName());
		if(boundary.hasIntersections()) 
			for(BoundaryAnnotation intersection : boundary.getIntersections()) 
				if(!boundingbox.contains(intersection.getBoundingBox()))
					activeCategories.add(intersection.getName());

		BoundaryAnnotation closestContaining = null;
		for(BoundaryAnnotation compared : boundaries.values()) {
			Rectangle2D comparedbbox = compared.getBoundingBox();
			if(compared != boundary && comparedbbox.contains(boundingbox) && 
					(closestContaining == null || closestContaining.getBoundingBox().contains(comparedbbox)))
				closestContaining = compared;
		}
		if(closestContaining != null)
			activeCategories.add(closestContaining.getName());
		return activeCategories;
	}

	/** Private method
	 * @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Point2D dimensions of shapeAnnotation where 
	 * the x value of the point holds the width and the y value of the 
	 * point holds the height.
	 */
	private Point2D getAnnotationDimensions(BoundaryAnnotation boundary) {
		Rectangle2D bb = boundary.getBoundingBox();
		return new Point2D.Double(bb.getWidth(), bb.getHeight());
	}

	/** Private method
	 * @param shapeAnnotation stores an existing ShapeAnnotation.
	 * @return the Point2D location where the center of the shapeAnnotation
	 * is.
	 */
	private Point2D getAnnotationCenter(BoundaryAnnotation boundary) { 
		Rectangle2D bb = boundary.getBoundingBox();
		return new Point2D.Double(bb.getX() + (bb.getWidth() / 2.), bb.getY() + (bb.getHeight() / 2.));
	}

	/** Private method
	 * This method calculates and initializes node initialization locations for a given boundary
	 * @param shapeAnnotation stores an existing ShapeAnnotation.
	 * where all of the nodes of that respective shape annotation are to be initialized.
	 */
	private void initNodeLocations(BoundaryAnnotation boundary) { 
		Rectangle2D boundingBox = boundary.getBoundingBox();
		List<BoundaryAnnotation> specialInit = applySpecialInit(boundary);
		double xCenter = boundingBox.getX() + boundingBox.getWidth() / 2.;
		double yCenter = boundingBox.getY() + boundingBox.getHeight() / 2.;
		List<Point2D> initNodes = new ArrayList<>();
		initNodes.add(new Point2D.Double(xCenter, yCenter));
		if(!specialInit.isEmpty()) {
			initNodes.remove(0);
			List<Rectangle2D> initRectangles = BoundaryContainsAlgorithm.doAlgorithm(boundingBox, specialInit);
			for(Rectangle2D initRectangle : initRectangles) 
				initNodes.add(new Point2D.Double(initRectangle.getCenterX(), initRectangle.getCenterY()));
		}
		boundary.setInitializations(initNodes);
	}

	/** Private method
	 * Used to determine if and if so, which boundaries intersect with the given boundary.
	 * @param shapeAnnotation stores an existing ShapeAnnotation
	 * @param boundingBox stores the Rectangle2D of the shapeAnnotation
	 * @return list of Rectangle2D's of shape annotations that boundingBox contains
	 */
	private List<BoundaryAnnotation> applySpecialInit(BoundaryAnnotation boundary) {
		Rectangle2D boundingBox = boundary.getBoundingBox();
		List<BoundaryAnnotation> intersections = new ArrayList<>();
		for(BoundaryAnnotation comparedBoundary : boundaries.values()) {
			Rectangle2D comparedbbox = comparedBoundary.getBoundingBox();
			if(comparedBoundary.getName().equals(boundary.getName())) {} 
			else if(boundingBox.intersects(comparedbbox) && !comparedbbox.contains(boundingBox)) {
				boolean add = true;
				if(!intersections.isEmpty()) {
					List<BoundaryAnnotation> nested = new ArrayList<>();
					for(BoundaryAnnotation intersection : intersections) {
						Rectangle2D intersectRect = intersection.getBoundingBox();
						if(intersectRect.contains(comparedbbox)) {
							add = false;
							break;
						} else if(comparedbbox.contains(intersectRect))
							nested.add(intersection);
					}
					if(!nested.isEmpty())
						for(BoundaryAnnotation toRemove : nested)
							intersections.remove(toRemove);
				}
				if(add)
					intersections.add(comparedBoundary);
			}
		}
		boundary.setIntersections(intersections);
		return intersections;
	}

	/** Private method
	 * @return a Rectangle2D representing the union of the boundaries in the boundaries map
	 */
	private Rectangle2D getUnionofBoundaries(Collection<BoundaryAnnotation> boundaries) {
		if(boundaries.size() == 0)
			return null;

		Rectangle2D union = new Rectangle2D.Double();
		Iterator<BoundaryAnnotation> unionIterate = boundaries.iterator();
		BoundaryAnnotation initBoundary = unionIterate.next();
		union.setRect(initBoundary.getBoundingBox());

		while(unionIterate.hasNext()) {
			BoundaryAnnotation nextBoundary = unionIterate.next();
			Rectangle2D nextRect = nextBoundary.getBoundingBox();
			union.setRect(union.createUnion(nextRect));
		}
		return union;
	}

	/** Private method
	 * Initialize the outer boundary for this execution
	 */
	private void initializeOuterBoundary() {
		Rectangle2D union = unionOfBoundaries;
		if(union == null)
			return;
		double newWidth = union.getWidth() * context.outerBoundsThickness;
		double newHeight = union.getHeight() * context.outerBoundsThickness;
		double newX = (union.getCenterX() - newWidth / 2.);
		double newY = (union.getCenterY() - newHeight / 2.);

		Rectangle2D outerBox = new Rectangle2D.Double(newX, newY, newWidth, newHeight);

		BoundaryAnnotation boundary = new BoundaryAnnotation(OUTER_UNION_KEY, outerBox);
		boundaries.put(OUTER_UNION_KEY, boundary);
	}
}
