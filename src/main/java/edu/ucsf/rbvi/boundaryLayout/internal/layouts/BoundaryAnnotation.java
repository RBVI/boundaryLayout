package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

import prefuse.util.force.BoundaryWallForce;

/**
 * This class represents a boundary annotation with all of the information corresponding
 * to a shape annotation including its bounding box, initialization locations for its nodes, 
 * a list of other boundaries intersecting it, and its corresponding wall force as well as 
 * its scaling capabilities.
 */
public class BoundaryAnnotation {
	public static final String DEFAULT_TYPE = "Rounded Rectangle";

	private List<VertexData> vertices;

	private Shape shape;
	private String shapeType;
	private String name;
	private Rectangle2D boundingBox;

	private List<Point2D> initLocations;
	private Random RANDOM = new Random();
	private List<BoundaryAnnotation> intersections;
	private Rectangle2D unionOfIntersections;

	private BoundaryWallForce wallForce;
	private int inProjections;
	private int outProjections;
	private static final int DEFAULT_SCALEMOD = 10;
	private int scaleMod;

	/**
	 * Initialize a BoundaryAnnotation, given various properties. A boundary annotation
	 * is the basic component of this layout. A boundary is an annotation created by the user
	 * on which the user wants to layout their data with respect to. All the information about 
	 * boundaries which are required by the layout exist in this class. 
	 * @param shape, its corresponding shape annotation
	 * @param initLocations, the list of Point2D initialization of contained nodes
	 * @param intersections, a list of intersecting boundary annotations
	 * @param wallForce, the wall force associated with this boundary
	 * @param scaleMod, scale the wall force every scaleMod'th interval
	 */
	public BoundaryAnnotation(ShapeAnnotation shapeAnn, List<Point2D> initLocations, 
			List<BoundaryAnnotation> intersections, BoundaryWallForce wallForce, int scaleMod) {
		this.shape = shapeAnn.getShape();
		this.shapeType = shapeAnn.getShapeType();
		this.name = shapeAnn.getName();
		this.initBoundingBox(shapeAnn);
		this.initLocations = initLocations;
		this.intersections = intersections;
		this.wallForce = wallForce;
		this.inProjections = 0;
		this.outProjections = 0;
		this.scaleMod = scaleMod;
	}

	/**
	 * This is the main constructor used by boundary layout. The default value of scale mod
	 * suffices in the general case and so it is used.
	 * @param shape is the shape annotation corresponding to this boundary
	 */
	public BoundaryAnnotation(ShapeAnnotation shape) {
		this(shape, null, null, null, DEFAULT_SCALEMOD);
	}

	/**
	 * Construct a more generalized boundary annotation: one which does not have a shape annotation
	 * associated with it, yet has a bounding box and the properties of a boundary i.e. wall force and
	 * scaling logic. This is used by Boundary Layout's outer boundary
	 * @param name is the name of the boundary defined by the user
	 * @param boundingBox is the Rectangle2D representing the bounding box of this boundary
	 */
	public BoundaryAnnotation(String name, Rectangle2D boundingBox) {
		this.name = name;
		this.boundingBox = boundingBox;
		this.inProjections = 0;
		this.outProjections = 0;
		this.scaleMod = DEFAULT_SCALEMOD;
		this.shapeType = DEFAULT_TYPE;
	}

	/**
	 * Initialize the bounding box of this boundary annotation. The information of the bounding box
	 * is assumed to be stored in the shape annotation
	 * @precondition shape != null
	 */
	protected void initBoundingBox(ShapeAnnotation shapeAnn) {
		Map<String, String> argMap = shapeAnn.getArgMap();
		double xCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.X));
		double yCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.Y));
		double width = Double.parseDouble(argMap.get(ShapeAnnotation.WIDTH)) / shapeAnn.getZoom();
		double height = Double.parseDouble(argMap.get(ShapeAnnotation.HEIGHT)) / shapeAnn.getZoom();

		if(boundingBox == null)
			boundingBox = new Rectangle2D.Double(xCoordinate, yCoordinate, width, height);
		else
			boundingBox.setRect(xCoordinate, yCoordinate, width, height);
		
		Rectangle2D shapebbox = shape.getBounds2D();
		AffineTransform transform = new AffineTransform();
		double scaleX = width / (shapebbox.getWidth() + 2 * shapebbox.getX());
		double scaleY = height / (shapebbox.getHeight() + 2 * shapebbox.getY());
		transform.scale(scaleX, scaleY);
		transform.translate(xCoordinate / scaleX, yCoordinate / scaleY);
		shape = transform.createTransformedShape(shape);
		
		if(vertices == null)
			vertices = new ArrayList<>();
		if(!shapeType.equals("Rectangle") && !shapeType.equals("Rounded Rectangle") && !shapeType.equals("Ellipse")) {
			PathIterator path = shape.getPathIterator(null);
			while(!path.isDone()) {
				double[] point = new double[2];
				int interaction = path.currentSegment(point);
				if(interaction == PathIterator.SEG_MOVETO || interaction == PathIterator.SEG_LINETO)
					vertices.add(new VertexData(new Point2D.Double(point[0], point[1]), boundingBox));
				path.next();
			}
		}
	}
	
	public boolean intersects(Rectangle2D boundingbox) {
		if(shape == null)
			return false;
		return shape.intersects(boundingbox);
	}
	
	public boolean contains(Rectangle2D boundingbox) {
		if(shape == null)
			return false;
		return shape.contains(boundingbox);
	}

	public List<VertexData> getVertices() {
		return vertices;
	}

	/** Private method
	 * Initialize the union of intersections, which is inclusive of this bounding box
	 */
	private void setUnionOfIntersections() {
		if(unionOfIntersections == null)
			unionOfIntersections = new Rectangle2D.Double();
		if(boundingBox == null)
			return;

		unionOfIntersections.setRect(boundingBox);
		if(intersections != null && !intersections.isEmpty())
			for(BoundaryAnnotation boundary : intersections) 
				unionOfIntersections.setRect(unionOfIntersections.createUnion(boundary.getBoundingBox()));
	}

	/**
	 * Gets the union of intersections. This is a Rectangle2D object which is the union of this bounding
	 * box along with the bounding boxes of all the intersecting boundaries
	 * @return union of this shape and all intersecting boundaries
	 */
	protected Rectangle2D getUnionOfIntersections() {
		return unionOfIntersections;
	}

	/**
	 * @return name of this boundary annotation, which is also the name of the shape annotation
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the type of the shape of this boundary, whether it's rectangle, elliptical or otherwise.
	 * By default this boundary is a Rounded Rectangle
	 * @return the shape type of this boundary
	 */
	public String getShapeType() {
		return shapeType;
	}

	/**
	 * Assume the bounding box is initialized by the constructor.
	 * @return the corresponding bounding box, a Rectangle2D consisting of
	 * the location and dimensions of the boundary annotation
	 */
	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}

	/*Functions dealing with intersecting boundary annotations*/

	/**
	 * Sets the list of boundary intersections given a list of boundary annotations
	 * @param intersections represents the list of boundaries intersecting with this boundary
	 */
	protected void setIntersections(List<BoundaryAnnotation> intersections) {
		this.intersections = intersections;
		setUnionOfIntersections();
	}

	/**
	 * @return true if this boundary annotation has intersecting boundary annotations
	 */
	protected boolean hasIntersections() {
		if(intersections == null || intersections.isEmpty())
			return false;
		return true;
	}

	/**
	 * @return true if boundary is an intersecting boundary
	 */
	protected boolean containsIntersection(BoundaryAnnotation boundary) {
		if(intersections == null || !intersections.contains(boundary))
			return false;
		return true;
	}

	/**
	 * Get a list of boundaries which are intersecting with this boundary. Intersecting
	 * is defined as a boundary whose bounding box physically intersects with this boundary's
	 * bounding box, yet this is not contained within the other boundary. However, the converse
	 * may be the case
	 * @return the list of intersecting boundary annotations
	 */
	protected List<BoundaryAnnotation> getIntersections() {
		return intersections;
	}

	/*Functions deal with handling initializing node locations for this boundary*/

	/**
	 * Initialize the list of node initialization locations corresponding to this boundary
	 * @param initLocations is the list of node initialization locations corresponding to 
	 * this boundary
	 */
	protected void setInitializations(List<Point2D> initLocations) {
		this.initLocations = initLocations;
	}

	/**
	 * Add a new node initialization location to the list of initializations for this boundary
	 * @param initLocation, the Point2D node initialization location to add to the list
	 */
	protected void addInitialization(Point2D initLocation) {
		if(initLocations == null)
			initLocations = new ArrayList<>();
		initLocations.add(initLocation);
	}

	/**
	 * Add a node initialization location from the list of initializations
	 * @param initLocation, the Point2D to remove from the list
	 * @precondition initLocation is in the list of initializations
	 */
	protected void removeInitialization(Point2D initLocation) {
		if(initLocations != null && initLocations.contains(initLocation))
			initLocations.remove(initLocation);
	}

	/**
	 * Get an node initialization location associated with this boundary
	 * @return a node initialization location chosen randomly from the list of node initializations 
	 * @precondition list of initialization != null and is not empty
	 */
	protected Point2D getRandomNodeInit() {
		if(initLocations == null || initLocations.isEmpty())
			return new Point2D.Double(0., 0.);
		return initLocations.get(RANDOM.nextInt(initLocations.size()));
	}

	/*WallForce-related methods dealing with force-based aspect of the boundary*/

	/**
	 * This method sets this boundary's wall force and sets the scale factor of that
	 * wall force.
	 * @param wallForce is the wall force corresponding to this boundary
	 * @param scaleFactor is the factor by which to scale the wall force when needed
	 */
	protected void setWallForce(BoundaryWallForce wallForce, double scaleFactor) {
		setWallForce(wallForce);
		setScaleFactor(scaleFactor);
	}

	/**
	 * Setter method for this boundary's wall force, given the wall force
	 * @param wallForce is the rectangular wall force corresponding to this boundary annotation
	 */
	protected void setWallForce(BoundaryWallForce wallForce) {
		this.wallForce = wallForce;
	}

	/**
	 * Setter method setting the scale factor, given the scaling factor of the wall force 
	 * corresponding to this boundary annotation
	 * @param scaleFactor is the factor by which to scale the wall force when needed
	 */
	protected void setScaleFactor(double scaleFactor) {
		wallForce.setScaleFactor(scaleFactor);
	}

	/**
	 * Setter method for this boundary's interval by which to scale the wall force corresponding
	 * to this boundary
	 * @param scaleMod is the number which is used to scale the wall force at discrete values
	 * of the counters: inProjections and outProjections
	 */
	protected void setScaleMod(int scaleMod) {
		if(scaleMod >= 1)
			this.scaleMod = scaleMod;
	}

	/**
	 * Increments one of the two projection counters, in or out, depending on the direction.
	 * Then, if the incremented projection counter is at the scale interval scaleMod, this 
	 * method calls with wall class to scale this boundary's wall force
	 * @param dir is the direction of the projection of a node, inside or outside the boundary
	 * @precondition dir == -1 or dir == 1
	 */
	protected void newProjection(int dir) {
		if(dir == BoundaryWallForce.IN_PROJECTION) {
			this.inProjections++;
			if(inProjections % scaleMod == 0)
				scaleWallForce(dir);
		} else if(dir == BoundaryWallForce.OUT_PROJECTION) {
			this.outProjections++;
			if(outProjections % scaleMod == 0)
				scaleWallForce(dir);
		}
	}

	/** Private method
	 * Scale the wall force of this boundary in the direction of @param dir
	 */
	private void scaleWallForce(int dir) {
		if(this.wallForce != null)
			wallForce.scaleStrength(dir);
	}
}