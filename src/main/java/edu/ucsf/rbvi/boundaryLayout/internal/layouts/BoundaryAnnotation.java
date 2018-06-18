package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

public class BoundaryAnnotation {
	private ShapeAnnotation shape;
	private Rectangle2D boundingBox;
	private List<Point2D> initLocations;
	private List<BoundaryAnnotation> intersections;
	private Random RANDOM = new Random();

	public BoundaryAnnotation(ShapeAnnotation shape, List<Point2D> initLocations, List<BoundaryAnnotation> intersections) {
		this.shape = shape;
		this.initBoundingBox();
		this.initLocations = initLocations;
		this.intersections = intersections;
	}

	public BoundaryAnnotation(ShapeAnnotation shape) {
		this(shape, null, null);
	}
	
	private void initBoundingBox() {
		Map<String, String> argMap = shape.getArgMap();
		double xCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.X));
		double yCoordinate = Double.parseDouble(argMap.get(ShapeAnnotation.Y));
		double width = Double.parseDouble(argMap.get(ShapeAnnotation.WIDTH)) / shape.getZoom();
		double height = Double.parseDouble(argMap.get(ShapeAnnotation.HEIGHT)) / shape.getZoom();
		if(shape.getShapeType().equals("Ellipse")) { //only want inner rectangle
			double xCenter = xCoordinate + (width / 2);
			double yCenter = yCoordinate + (height / 2);
			width = width * Math.sqrt(2) / 2;
			height = height * Math.sqrt(2) / 2;
			xCoordinate = xCenter - (width / 2);
			yCoordinate = yCenter - (height / 2);
		} 

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
	
	protected List<Point2D> getInitLocations() {
		return this.initLocations;
	}
	
	protected Point2D getRandomNodeInit() {
		if(initLocations == null || initLocations.isEmpty())
			return new Point2D.Double(0., 0.);
		return initLocations.get(RANDOM.nextInt(initLocations.size()));
	}
}