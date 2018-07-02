package prefuse.util.force;

import java.awt.geom.Point2D;

/* 
 * Represents a rectangular bounding box of a boundary and applies a force on 
 * the force items in the force simulation, pushing them away from the walls of
 * this rectangle. The walls may or may not be of variable wall force, depending
 * on the user's choice
 */
public class RectangularWallForce extends AbstractForce {
	private static String[] pnames = new String[] { "GravitationalConstant" };

	public static final int IN_GRAVITATIONAL_CONST = 0;
	public static final int OUT_GRAVITATIONAL_CONST = 1;
	public static final int IN_PROJECTION = 1;
	public static final int OUT_PROJECTION = -1;
	private static final double DEFAULT_SCALEFACTOR = 2.5;

	private boolean variableStrength;
	private float scaleFactor;

	private Point2D center;
	private Point2D dimensions;

	/**
	 * Create a new RectangularWallForce with given parameters
	 * @param center is a 2D point of the center of the rectangle
	 * @param dimensions is a 2D field representing the width and height
	 * @param gravConst represents the initial gravity constant of the rectangle
	 * @param variableWall tells whether or not the wall changes gravitational constants
	 * @param scaleFactor is the scale by which the wall force changes
	 */
	public RectangularWallForce(Point2D center, Point2D dimensions, float gravConst, boolean variableWall, double scaleFactor) {
		this.center = center;
		this.dimensions = dimensions;
		params = new float[] { gravConst, gravConst };
		this.variableStrength = variableWall;
		this.scaleFactor = (float) scaleFactor;
	}

	public RectangularWallForce(Point2D center, Point2D dimensions, float gravConst, boolean variableWall) {
		this(center, dimensions, gravConst, variableWall, DEFAULT_SCALEFACTOR);
	}

	/**
	 * Returns true.
	 * @see prefuse.util.force.Force#isItemForce()
	 */
	public boolean isItemForce() {
		return true;
	}

	/**
	 * @see prefuse.util.force.AbstractForce#getParameterNames()
	 */
	protected String[] getParameterNames() {
		return pnames;
	}

	/*
	 * This method sets the scaling factor of this wall force. The scaling factor
	 * is the value that the gravitational constant is multiplied by when the wall
	 * is scaled.
	 * 
	 * @param scaleFactor is the new scale factor of this wall force
	 * @precondition 0.1 <= scaleFactor <= 10
	 * */
	public boolean setScaleFactor(double scaleFactor) {
		if(Math.abs(scaleFactor) >= 0.1 && Math.abs(scaleFactor) <= 10.) {
			this.scaleFactor = (float) scaleFactor;
			return true;
		}
		return false;
	}

	/* This method scales the strength of the wall force in the direction of @param dir, 
	 * only if variableStrength is true*/
	public void scaleStrength(int dir) {
		if(this.variableStrength) {
			if(dir == IN_PROJECTION) 
				params[IN_GRAVITATIONAL_CONST] *= scaleFactor;
			else if(dir == OUT_PROJECTION) 
				params[OUT_GRAVITATIONAL_CONST] *= scaleFactor;
		}
	}

	/**
	 * @see prefuse.util.force.Force#getForce(prefuse.util.force.ForceItem)
	 */
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
		float drLeft = Math.abs((width / 2f) - dx - item.dimensions[0] / 2);
		float drTop = Math.abs((height / 2f) - dy - item.dimensions[1] / 2);
		float drRight = width - drLeft - item.dimensions[0]; 
		float drBottom = height - drTop - item.dimensions[1];
		if(drLeft < 0.01f) drLeft = 0.01f;
		if(drRight < 0.01f) drRight = 0.01f;
		if(drTop < 0.01f) drTop = 0.01f;
		if(drBottom < 0.01f) drBottom = 0.01f;

		//initialize orientation of shape
		int cX = (Math.abs(dx) > width / 2 ? -1 : 1);
		int cY = (Math.abs(dy) > height / 2 ? -1 : 1);

		if(cX + cY != 2)
			return;

		//calculate forces due to each wall of the rectangle
		float gravConst = (cX == -1 || cY == -1 ? params[OUT_GRAVITATIONAL_CONST] : params[IN_GRAVITATIONAL_CONST]);
		float vLeft = -cX * gravConst * item.mass / (drLeft * drLeft * drLeft);
		float vTop = -cY * gravConst * item.mass / (drTop * drTop * drTop);
		float vRight = cX * gravConst * item.mass / (drRight * drRight * drTop);
		float vBottom = cY * gravConst * item.mass / (drBottom * drBottom * drBottom);

		if(cX + cY == -2) {//case where the node is outside the corner of the shape
			float xCorner = (float) center.getX() + (width / 2 * (dx > 0 ? -1 : 1));
			float yCorner = (float) center.getY() + (height / 2 * (dy > 0 ? -1 : 1));
			float dxCorner = n[0] - xCorner;
			float dyCorner = n[1] - yCorner;
			float dCorner = (float) Math.sqrt(Math.pow(dxCorner, 2) + Math.pow(dyCorner, 2));
			float vCorner = params[OUT_GRAVITATIONAL_CONST] * item.mass / (dCorner * dCorner * dCorner);
			float vxCorner = vCorner * dxCorner;
			float vyCorner = vCorner * dyCorner;
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

	/*
	 * @return a random displacement 
	 * */
	private float getRandDisplacement() {
		return ((float)Math.random() - 1f) / 50.0f;
	}
}
