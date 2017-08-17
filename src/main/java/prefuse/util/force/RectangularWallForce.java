package prefuse.util.force;

import java.awt.geom.Point2D;

public class RectangularWallForce extends AbstractForce {
	private static String[] pnames = new String[] { "GravitationalConstant" };

	public static final int GRAVITATIONAL_CONST = 0;

	private Point2D.Double center;
	private Point2D.Double dimensions;

	/**
	 * Create a new CircularWallForce.
	 * @param gravConst the gravitational constant to use
	 * @param x the center x-coordinate of the circle
	 * @param y the center y-coordinate of the circle
	 * @param r the radius of the circle
	 */
	public RectangularWallForce(float gravConst, 
			Point2D.Double center, Point2D.Double dimensions) {
		params = new float[] { gravConst };
		this.center = center;
		this.dimensions = dimensions;
	}

	/**
	 * Create a new CircularWallForce with default gravitational constant.
	 * @param x the center x-coordinate of the circle
	 * @param y the center y-coordinate of the circle
	 * @param r the radius of the circle
	 */
	public RectangularWallForce(Point2D.Double center, 
			Point2D.Double dimensions, float wallGravitationalConstant) {
		this(wallGravitationalConstant, center, dimensions);
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
		float drLeft = (width / 2) - dx;
		float drTop = (height / 2) - dy;
		float drRight = width - drLeft; 
		float drBottom = height - drTop;

		//	System.out.println("Node position: "+n[0]+","+n[1]);
		//	System.out.println("Annotation center: "+center.getX()+","+center.getY());
		//	System.out.println("drLeft: "+drLeft+", drTop: "+drTop+", drRight: "+drRight+", drBottom: "+drBottom);

		//initialize orientation of shape
		int cX = (Math.abs(dx) > width / 2 ? -1 : 1);
		int cY = (Math.abs(dy) > height / 2 ? -1 : 1);

		if(cX + cY != 2)
			return;
		
		float vLeft = -cX * params[GRAVITATIONAL_CONST] * item.mass / (drLeft * drLeft);
		float vTop = -cY * params[GRAVITATIONAL_CONST] * item.mass / (drTop * drTop);
		float vRight = cX * params[GRAVITATIONAL_CONST] * item.mass / (drRight * drRight);
		float vBottom = cY * params[GRAVITATIONAL_CONST] * item.mass / (drBottom * drBottom);
		
		if(cX + cY == -2) {//case where the node is outside the corner of the shape
			float xPlaneDimensions = (float) (dx > 0 ? -dimensions.getX() : dimensions.getX());
			float yPlaneDimensions = (float) (dy > 0 ? -dimensions.getY() : dimensions.getY());
			float xCorner = (float) center.getX() + xPlaneDimensions;
			float yCorner = (float) center.getY() + yPlaneDimensions;
			float dxCorner = n[0] - xCorner;
			float dyCorner = n[1] - yCorner;
			float dCorner = (float) Math.sqrt(Math.pow(dxCorner, 2) + Math.pow(dyCorner, 2));
			float vCorner = params[GRAVITATIONAL_CONST] * item.mass / (dCorner * dCorner * dCorner);
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
