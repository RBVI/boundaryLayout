package prefuse.util.force;

import java.awt.geom.Point2D;

/**
 * Represents a rectangular bounding box of a boundary and applies a force on 
 * the force items in the force simulation, pushing them away from the walls of
 * this rectangle. The walls may or may not be of variable wall force, depending
 * on the user's choice
 */
public class RectangularWallForce extends BoundaryWallForce {

	/**
	 * Create a new RectangularWallForce with given parameters
	 * @param center is a 2D point of the center of the rectangle
	 * @param dimensions is a 2D field representing the width and height
	 * @param gravConst represents the initial gravity constant of the rectangle
	 * @param variableWall tells whether or not the wall changes gravitational constants
	 * @param scaleFactor is the scale by which the wall force changes
	 */
	public RectangularWallForce(Point2D center, Point2D dimensions, float gravConst, boolean variableWall, double scaleFactor) {
		super(center, dimensions, gravConst, variableWall, scaleFactor);
	}

	public RectangularWallForce(Point2D center, Point2D dimensions, float gravConst, boolean variableWall) {
		super(center, dimensions, gravConst, variableWall);
	}

	/**
	 * @see prefuse.util.force.Force#getForce(prefuse.util.force.ForceItem)
	 */
	@Override
	public void getForce(ForceItem item) {
		if(!isActive(item.category))
			return;
		float[] itemLoc = item.location;
		float[] itemDim = item.dimensions;
		float dx = (float) center.getX() - itemLoc[0];
		float dy = (float) center.getY() - itemLoc[1];
		
		//initialize dimensions
		float width = (float) dimensions.getX();
		float height = (float) dimensions.getY();

		if(dx == 0f && dy == 0f) {
			dx = getRandDisplacement();
			dy = getRandDisplacement();
		}

		//initialize orientation of shape
		int cX = (Math.abs(dx) > width / 2f ? -1 : 1);
		int cY = (Math.abs(dy) > height / 2f ? -1 : 1);

		if(cX + cY != 2)
			return;

		if(cX + cY == -2) {//case where the node is outside the corner of the shape
			float xOrientation = (dx > 0 ? -1f : 1f);
			float yOrientation = (dy > 0 ? -1f : 1f);
			float xCorner = (float) (center.getX() + (width / 2f * xOrientation));
			float yCorner = (float) (center.getY() + (height / 2f * yOrientation));
			float dxCorner = Math.abs(itemLoc[0] - xCorner) - itemDim[0] / 2f;
			float dyCorner = Math.abs(itemLoc[1] - yCorner) - itemDim[1] / 2f;
			float dCorner = (float) Math.sqrt(dxCorner * dxCorner + dyCorner * dyCorner);
			float vCorner = params[OUT_GRAVITATIONAL_CONST] * item.mass / (dCorner * dCorner);
			vCorner = (vCorner > ABS_MAX_FORCE ? ABS_MAX_FORCE : vCorner);
			float vxCorner = vCorner * xOrientation;
			float vyCorner = vCorner * yOrientation;
			item.force[0] += vxCorner;
			item.force[1] += vyCorner;
		} else { 
			float drLeft = Math.abs(width / 2f - dx) - itemDim[0] / 2f;
			float drTop = Math.abs(height / 2f - dy) - itemDim[1] / 2f;
			float drRight = Math.abs(width - drLeft) - itemDim[0]; 
			float drBottom = Math.abs(height - drTop) - itemDim[1];
			
			//calculate forces due to each wall of the rectangle
			float gravConst = (cX == -1 || cY == -1 ? params[OUT_GRAVITATIONAL_CONST] : params[IN_GRAVITATIONAL_CONST]);
			float vLeft = gravConst * item.mass / (drLeft * drLeft);
			float vTop = gravConst * item.mass / (drTop * drTop);
			float vRight = gravConst * item.mass / (drRight * drRight);
			float vBottom = gravConst * item.mass / (drBottom * drBottom);
			vLeft = (vLeft > ABS_MAX_FORCE ? ABS_MAX_FORCE : vLeft);
			vTop = (vTop > ABS_MAX_FORCE ? ABS_MAX_FORCE : vTop);
			vRight = (vRight > ABS_MAX_FORCE ? ABS_MAX_FORCE : vRight);
			vBottom = (vBottom > ABS_MAX_FORCE ? ABS_MAX_FORCE : vBottom);
			
			if(cX == -1) {//case where the node is within the x normal lines of the shape
				if(dx < 0)
					item.force[0] -= vRight;
				else
					item.force[0] += vLeft;
			} else if(cY == -1) {//case where the node is within the y normal lines of the shape
				if(dy < 0)
					item.force[1] -= vBottom;
				else 
					item.force[1] += vTop;
			} else {//case where the node is completely inside the shape
				item.force[0] += vLeft - vRight;
				item.force[1] += vTop - vBottom;
			}
		}
	}
}
