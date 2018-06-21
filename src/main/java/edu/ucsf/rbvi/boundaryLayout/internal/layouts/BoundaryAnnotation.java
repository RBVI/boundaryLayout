package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

import prefuse.util.force.RectangularWallForce;

public class BoundaryAnnotation {
	private ShapeAnnotation shape;
	private Rectangle2D boundingBox;
	
	private List<Point2D> initLocations;
	private Random RANDOM = new Random();
	
	private List<BoundaryAnnotation> intersections;
	
	private RectangularWallForce wallForce;
	private int inProjections;
	private int outProjections;
	private static final int DEFAULT_SCALEMOD = 10;
	private int scaleMod;

	public BoundaryAnnotation(ShapeAnnotation shape, List<Point2D> initLocations, 
			List<BoundaryAnnotation> intersections, RectangularWallForce wallForce, int scaleMod) {
		this.shape = shape;
		this.initBoundingBox();
		this.initLocations = initLocations;
		this.intersections = intersections;
		this.wallForce = wallForce;
		this.inProjections = 0;
		this.outProjections = 0;
		this.scaleMod = scaleMod;
	}

	public BoundaryAnnotation(ShapeAnnotation shape) {
		this(shape, null, null, null, DEFAULT_SCALEMOD);
	}
	
	public BoundaryAnnotation(ShapeAnnotation shape, int scaleMod) {
		this(shape, null, null, null, scaleMod);
	}
	
	private void initBoundingBox() {
		Map<String, String> argMap = shape.getArgMap();
		double xCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.X));
		double yCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.Y));
		double width = Double.parseDouble(argMap.get(ShapeAnnotation.WIDTH)) / shape.getZoom();
		double height = Double.parseDouble(argMap.get(ShapeAnnotation.HEIGHT)) / shape.getZoom();

		boundingBox = new Rectangle2D.Double(xCoordinate, yCoordinate, width, height);
	}
	
	public String getName() {
		return this.shape.getName();
	}
	
	public ShapeAnnotation getShapeAnnotation() {
		return this.shape;
	}

	protected Rectangle2D getBoundingBox() {
		return this.boundingBox;
	}
	
	/*Functions deal with intersecting boundary annotations*/
	protected void setIntersections(List<BoundaryAnnotation> intersections) {
		this.intersections = intersections;
	}
	
	protected void addIntersection(BoundaryAnnotation intersection) {
		if(intersections == null)
			intersections = new ArrayList<>();
		intersections.add(intersection);
	}
	
	protected void removeIntersection(BoundaryAnnotation intersection) {
		if(intersections != null && intersections.contains(intersection))
			intersections.remove(intersection);
	}
	
	protected boolean hasIntersections() {
		if(intersections == null || intersections.isEmpty())
			return false;
		return true;
	}
	
	protected boolean containsIntersection(BoundaryAnnotation boundary) {
		if(intersections == null || !intersections.contains(boundary))
			return false;
		return true;
	}
	
	protected List<BoundaryAnnotation> getIntersections() {
		return this.intersections;
	}
	
	/*Functions deal with handling initializing node locations for this boundary*/
	protected void setInitializations(List<Point2D> initLocations) {
		this.initLocations = initLocations;
	}
	
	protected void addInitialization(Point2D initLocation) {
		if(initLocations == null)
			initLocations = new ArrayList<>();
		initLocations.add(initLocation);
	}
	
	protected void removeInitialization(Point2D initLocation) {
		if(initLocations != null && initLocations.contains(initLocation))
			initLocations.remove(initLocation);
	}

	protected Point2D getRandomNodeInit() {
		if(initLocations == null || initLocations.isEmpty())
			return new Point2D.Double(0., 0.);
		return initLocations.get(RANDOM.nextInt(initLocations.size()));
	}
	
	/*WallForce-related methods dealing with force-based aspect of the boundary*/
	protected void setWallForce(RectangularWallForce wallForce, double scaleFactor) {
		this.wallForce = wallForce;
		this.setScaleFactor(scaleFactor);
	}
	
	protected void setWallForce(RectangularWallForce wallForce) {
		this.wallForce = wallForce;
	}
	
	protected void setScaleFactor(double scaleFactor) {
		wallForce.setScaleFactor(scaleFactor);
	}
	
	protected void setScaleMod(int scaleMod) {
		if(scaleMod >= 1)
			this.scaleMod = scaleMod;
	}
	
	protected void newProjection(int dir) {
		if(dir == 1) {
			this.inProjections++;
			if(inProjections % scaleMod == 0)
				scaleWallForce(dir);
		} else if(dir == -1) {
			this.outProjections++;
			if(outProjections % scaleMod == 0)
				scaleWallForce(dir);
		}
	}
	
	protected void scaleWallForce(int dir) {
		if(this.wallForce != null)
			wallForce.scaleStrength(dir);
	}
}