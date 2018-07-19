package prefuse.util.force;

import java.awt.geom.Point2D;

/** 
 * Represents a rectangular bounding box of a boundary and applies a force on 
 * the force items in the force simulation, pushing them away from the walls of
 * this rectangle. The walls may or may not be of variable wall force, depending
 * on the user's choice
 */
public class EllipticalWallForce extends BoundaryWallForce {

	/**
	 * Create a new RectangularWallForce with given parameters
	 * @param center is a 2D point of the center of the rectangle
	 * @param dimensions is a 2D field representing the width and height
	 * @param gravConst represents the initial gravity constant of the rectangle
	 * @param variableWall tells whether or not the wall changes gravitational constants
	 * @param scaleFactor is the scale by which the wall force changes
	 */
	public EllipticalWallForce(Point2D center, Point2D dimensions, float gravConst, boolean variableWall, double scaleFactor) {
		super(center, dimensions, gravConst, variableWall, scaleFactor);
	}

	public EllipticalWallForce(Point2D center, Point2D dimensions, float gravConst, boolean variableWall) {
		super(center, dimensions, gravConst, variableWall);
	}

	/**
	 * @see prefuse.util.force.Force#getForce(prefuse.util.force.ForceItem)
	 */
	@Override
	public void getForce(ForceItem item) {
		float[] n = item.location;
		float dx = (float) center.getX() - n[0];
		float dy = (float) center.getY() - n[1];

		if(dx == 0f && dy == 0f) {
			dx = getRandDisplacement();
			dy = getRandDisplacement();
		}

		//initialize dimensions and displacements
		float width = (float) this.dimensions.getX();
		float height = (float) this.dimensions.getY();
		float heightRatio = (dy * dy) / (height * height);
		float widthRatio = (dx * dx) / (width * width);
		boolean insideEllipse = widthRatio + heightRatio <= 0.25f;
		float gravConst = 0f;

		if(insideEllipse) { //item is contained within the shape
			gravConst = params[IN_GRAVITATIONAL_CONST];
			float effectiveXWidth = (width) * (float) Math.sqrt(0.25f - heightRatio);
			float effectiveYHeight = (height) * (float) Math.sqrt(0.25f - widthRatio);

			float drLeft = effectiveXWidth - dx - item.dimensions[0] / 2f;
			float drTop = effectiveYHeight - dy - item.dimensions[1] / 2f;
			if(drLeft < 0.01f) drLeft = 0.01f;
			if(drTop < 0.01f) drTop = 0.01f;
			float drRight = 2 * effectiveXWidth - drLeft - item.dimensions[0]; 
			float drBottom = 2 * effectiveYHeight - drTop - item.dimensions[1];
			if(drRight < 0.01f) drRight = 0.01f;
			if(drBottom < 0.01f) drBottom = 0.01f;

			float vLeft = -gravConst * item.mass / (drLeft * drLeft * drLeft);
			float vTop = -gravConst * item.mass / (drTop * drTop * drTop);
			float vRight = gravConst * item.mass / (drRight * drRight * drRight);
			float vBottom = gravConst * item.mass / (drBottom * drBottom * drBottom);

			item.force[0] += vLeft;
			item.force[1] += vTop;
			item.force[0] += vRight;
			item.force[1] += vBottom;
		} else {
			gravConst = params[OUT_GRAVITATIONAL_CONST];
			double xVec = (dx * dx) / (width * width / 4.);
			double yVec = (dy * dy) / (height * height / 4.);
			double scale = 1 / Math.sqrt(xVec + yVec);
			double[] closestPoint = {dx * scale, dy * scale};
			double xDiff = closestPoint[0] - dx;
			double yDiff = closestPoint[1] - dy;
			if(Math.abs(xDiff) < 0.01f) xDiff = 0.01f * (xDiff < 0 ? -1 : 1);
			if(Math.abs(yDiff) < 0.01f) yDiff = 0.01f * (yDiff < 0 ? -1 : 1);
			float resDiff = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
			float force = -gravConst * item.mass / (resDiff * resDiff * resDiff);
			item.force[0] += force * (xDiff < 0 ? -1 : 1);
			item.force[1] += force * (yDiff < 0 ? -1 : 1);
		}
	}
}