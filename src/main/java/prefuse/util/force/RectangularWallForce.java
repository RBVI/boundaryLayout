package prefuse.util.force;

import java.awt.geom.Point2D;

public class RectangularWallForce extends AbstractForce {
	private static String[] pnames = new String[] { "GravitationalConstant" };

	public static final int IN_GRAVITATIONAL_CONST = 0;
	public static final int OUT_GRAVITATIONAL_CONST = 1;
	public static final int IN_PROJECTION = 1;
	public static final int OUT_PROJECTION = -1;
	private static final double DEFAULT_SCALEFACTOR = 1.25;
	
	private boolean variableStrength;
	private double scaleFactor;

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
	public RectangularWallForce(Point2D center, Point2D dimensions, float gravConst, 
			boolean variableWall, double scaleFactor) {
		this.center = center;
		this.dimensions = dimensions;
		params = new float[] { gravConst, gravConst };
		this.variableStrength = variableWall;
		this.scaleFactor = scaleFactor;
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
	
	public boolean setScaleFactor(double scaleFactor) {
		if(Math.abs(scaleFactor) >= 0.1 && Math.abs(scaleFactor) <= 10.) {
			this.scaleFactor = scaleFactor;
			return true;
		}
		return false;
	}
	
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
		float dx = ((float) center.getX()) - n[0];
		float dy = ((float) center.getY()) - n[1];
		if ( dx == 0.0 && dy == 0.0) {
			dx = getRandDisplacement();
			dy = getRandDisplacement();
		}

		//initialize dimensions and displacements
		float width = (float) this.dimensions.getX();
		float height = (float) this.dimensions.getY();
		float drLeft = (width / 2f) - dx;
		float drTop = (height / 2f) - dy;
		float drRight = width - drLeft; 
		float drBottom = height - drTop;
		if(drLeft < 1) drLeft = 1;
		if(drRight < 1) drRight = 1;
		if(drTop < 1) drTop = 1;
		if(drBottom < 1) drBottom = 1;

		//initialize orientation of shape
		int cX = (Math.abs(dx) > width / 2 ? -1 : 1);
		int cY = (Math.abs(dy) > height / 2 ? -1 : 1);

		if(cX + cY != 2)
			return;
		
		float gravConst = (cX == -1 || cY == -1 ? params[OUT_GRAVITATIONAL_CONST] : params[IN_GRAVITATIONAL_CONST]);
		float vLeft = -cX * gravConst * item.mass / (drLeft * drLeft);
		float vTop = -cY * gravConst * item.mass / (drTop * drTop);
		float vRight = cX * gravConst * item.mass / (drRight * drRight);
		float vBottom = cY * gravConst * item.mass / (drBottom * drBottom);
		
		if(cX + cY == -2) {//case where the node is outside the corner of the shape
			float xPlaneDimensions = (float) (dx > 0 ? -dimensions.getX() : dimensions.getX());
			float yPlaneDimensions = (float) (dy > 0 ? -dimensions.getY() : dimensions.getY());
			float xCorner = (float) center.getX() + xPlaneDimensions;
			float yCorner = (float) center.getY() + yPlaneDimensions;
			float dxCorner = n[0] - xCorner;
			float dyCorner = n[1] - yCorner;
			float dCorner = (float) Math.sqrt(Math.pow(dxCorner, 2) + Math.pow(dyCorner, 2));
			float vCorner = params[OUT_GRAVITATIONAL_CONST] * item.mass / (dCorner * dCorner * dCorner);
			float vxCorner = vCorner * dxCorner;
			float vyCorner = vCorner * dyCorner;
			item.force[0] += vxCorner;
			item.force[1] += vyCorner;
		} else if(cX == -1) {//case where the node is within the x normal lines of the shape
			item.force[0] += vLeft;
			item.force[0] += vRight;
		} else if(cY == -1) {//case where the node is within the y normal lines of the shape
			item.force[1] += vTop;
			item.force[1] += vBottom;
		} else {//case where the node is completely inside the shape
			item.force[0] += vLeft;
			item.force[1] += vTop;
			item.force[0] += vRight;
			item.force[1] += vBottom;
		}
	}

	private float getRandDisplacement() {
		return ((float)Math.random() - 1f) / 50.0f;
	}
}
