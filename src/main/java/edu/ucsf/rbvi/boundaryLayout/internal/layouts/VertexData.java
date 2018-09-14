package edu.ucsf.rbvi.boundaryLayout.internal.layouts;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VertexData {
	private Point2D point;
	private double relativeAngle;
	
	public VertexData(Point2D point, Rectangle2D shape) {
		this.point = point;
		double xDiff = shape.getCenterX() - point.getX();
		double yDiff = shape.getCenterY() - point.getY();
		relativeAngle = Math.atan2(yDiff, xDiff) + Math.PI;
	}
	
	public Point2D getPoint() {
		return point;
	}
	
	public double getRelativeAngle() {
		return relativeAngle;
	}
}
