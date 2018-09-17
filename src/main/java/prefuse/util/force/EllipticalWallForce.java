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
		if(!isActive(item.category))
			return;
		float[] itemDim = item.dimensions;
		float dx = (float) center.getX() - item.location[0];
		float dy = (float) center.getY() - item.location[1];

		if(dx == 0f && dy == 0f) {
			dx = getRandDisplacement();
			dy = getRandDisplacement();
		}

		//initialize dimensions and displacements
		float width = (float) dimensions.getX();
		float height = (float) dimensions.getY();
		float heightRatio = (dy * dy) / (height * height);
		float widthRatio = (dx * dx) / (width * width);
		boolean insideEllipse = widthRatio + heightRatio <= 0.25f;
		float gravConst = 0f;

		if(insideEllipse) { //item is contained within the shape
			gravConst = params[IN_GRAVITATIONAL_CONST];
			float effectiveXWidth = (width) * (float) Math.sqrt(0.25f - heightRatio);
			float effectiveYHeight = (height) * (float) Math.sqrt(0.25f - widthRatio);

			float drLeft = effectiveXWidth - dx - itemDim[0] / 2f;
			float drTop = effectiveYHeight - dy - itemDim[1] / 2f;
			float drRight = 2 * effectiveXWidth - drLeft - itemDim[0]; 
			float drBottom = 2 * effectiveYHeight - drTop - itemDim[1];

			float vLeft = Math.abs(gravConst * item.mass / (drLeft * drLeft));
			float vTop = Math.abs(gravConst * item.mass / (drTop * drTop));
			float vRight = Math.abs(gravConst * item.mass / (drRight * drRight));
			float vBottom = Math.abs(gravConst * item.mass / (drBottom * drBottom));
			vLeft = (vLeft > ABS_MAX_FORCE ? ABS_MAX_FORCE : vLeft);
			vTop = (vTop > ABS_MAX_FORCE ? ABS_MAX_FORCE : vTop);
			vRight = (vRight > ABS_MAX_FORCE ? ABS_MAX_FORCE : vRight);
			vBottom = (vBottom > ABS_MAX_FORCE ? ABS_MAX_FORCE : vBottom);
			
			item.force[0] += vLeft - vRight;
			item.force[1] += vTop - vBottom;
		} else {
			gravConst = params[OUT_GRAVITATIONAL_CONST];
			float slope = dy / dx;
			float denominator = (1 / (width * width * 4f) + (slope * slope) / (height * height * 4f));
			float xVec = (float) Math.sqrt(1f / denominator);
			float yVec = slope * xVec;
			float xDiff = Math.abs(dx - xVec) - itemDim[0] / 2f;
			float yDiff = Math.abs(dy - yVec) - itemDim[1] / 2f;
			float resDiff = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
			float force = Math.abs(gravConst * item.mass / (resDiff * resDiff));
			force = (force > ABS_MAX_FORCE ? ABS_MAX_FORCE : force);
			
			item.force[0] += force * (dx < 0 ? 1 : -1);
			item.force[1] += force * (dy < 0 ? 1 : -1);
		}
	}
}