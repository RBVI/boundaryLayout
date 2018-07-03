package prefuse.util.force;

import java.awt.geom.Point2D;

/* 
 * Represents a rectangular bounding box of a boundary and applies a force on 
 * the force items in the force simulation, pushing them away from the walls of
 * this rectangle. The walls may or may not be of variable wall force, depending
 * on the user's choice
 */
public class EllipticalWallForce extends BoundaryWallForce {

	/*
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
		float effectiveXWidth = (width) * (float) Math.sqrt(Math.abs(0.25f - heightRatio));
		float effectiveYHeight = (height) * (float) Math.sqrt(Math.abs(0.25f - widthRatio));

		float drLeft = Math.abs(effectiveXWidth - dx - item.dimensions[0] / 2f);
		float drTop = Math.abs(effectiveYHeight - dy - item.dimensions[1] / 2f);
		float drRight = Math.abs(2 * effectiveXWidth - drLeft - item.dimensions[0]); 
		float drBottom = Math.abs(2 * effectiveYHeight - drTop - item.dimensions[1]);
		if(drLeft < 0.01f) drLeft = 0.01f;
		if(drRight < 0.01f) drRight = 0.01f;
		if(drTop < 0.01f) drTop = 0.01f;
		if(drBottom < 0.01f) drBottom = 0.01f;

		//initialize orientation of shape
		int cX = (0.25f < widthRatio ? -1 : 1);
		int cY = (0.25f < heightRatio ? -1 : 1);
		System.out.println("cX is: " + (cX == -1 ? "outside" : "inside") + "        cY is: " + (cY == -1 ? "outside" : "inside"));

		if(cX + cY != 2)
			return;

		//calculate forces due to each wall of the rectangle
		float gravConst = (cX == -1 || cY == -1 ? params[OUT_GRAVITATIONAL_CONST] : params[IN_GRAVITATIONAL_CONST]);
		float vLeft = -cX * gravConst * item.mass / (drLeft * drLeft * drLeft);
		float vTop = -cY * gravConst * item.mass / (drTop * drTop * drTop);
		float vRight = cX * gravConst * item.mass / (drRight * drRight * drRight);
		float vBottom = cY * gravConst * item.mass / (drBottom * drBottom * drBottom);

		if(cX + cY == -2) {//case where the node is outside the corner of the shape
			float xCorner = (float) center.getX() + (width / 2 * (dx > 0 ? -1 : 1));
			float yCorner = (float) center.getY() + (height / 2 * (dy > 0 ? -1 : 1));
			float dxCorner = n[0] - xCorner;
			float dyCorner = n[1] - yCorner;
			float dCorner = (float) Math.sqrt(Math.pow(dxCorner, 2) + Math.pow(dyCorner, 2));
			float vCorner = params[OUT_GRAVITATIONAL_CONST] * item.mass / (dCorner * dCorner * dCorner);
			float vxCorner = Math.abs(vCorner) * dxCorner;
			float vyCorner = Math.abs(vCorner) * dyCorner;
			item.force[0] += vxCorner;
			item.force[1] += vyCorner;
		} else if(cX == -1) {//case where the node is within the x normal lines of the shape
			if(dx < 0)
				item.force[0] += vRight;
			else
				item.force[0] += vLeft;
		} else if(cY == -1) {//case where the node is within the y normal lines of the shape
			if(dy < 0)
				item.force[1] += vBottom;
			else 
				item.force[1] += vTop;
		} else {//case where the node is completely inside the shape
			item.force[0] += vLeft;
			item.force[1] += vTop;
			item.force[0] += vRight;
			item.force[1] += vBottom;
		}
	}
}
