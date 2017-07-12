package prefuse.util.force;

import java.awt.geom.Point2D;

public class RectangularWallForce extends AbstractForce {
	private static String[] pnames = new String[] { "GravitationalConstant" };

	public static final float DEFAULT_GRAV_CONSTANT = -1f;
	public static final int GRAVITATIONAL_CONST = 0;

	private Point2D center;
	private float height, width;

	/**
	 * Create a new CircularWallForce.
	 * @param gravConst the gravitational constant to use
	 * @param x the center x-coordinate of the circle
	 * @param y the center y-coordinate of the circle
	 * @param r the radius of the circle
	 */
	public RectangularWallForce(float gravConst, 
			Point2D center, float height, float width) {
		params = new float[] { gravConst };
		this.center = center;
		this.height = height;
		this.width = width;
	}

	/**
	 * Create a new CircularWallForce with default gravitational constant.
	 * @param x the center x-coordinate of the circle
	 * @param y the center y-coordinate of the circle
	 * @param r the radius of the circle
	 */
	public RectangularWallForce(Point2D center, 
			float height, float width) {
		this(DEFAULT_GRAV_CONSTANT,center,height,width);
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
		float drLeft = (this.width / 2) - dx;
		float drTop = (this.height / 2) - dy;
		float drRight = this.width - drLeft; 
		float drBottom = this.height - drTop;
		int cX = (Math.abs(dx) > width / 2 ? -1 : 1);
		int cY = (Math.abs(dy) > height / 2 ? -1 : 1);
		float vLeft = -cX * params[GRAVITATIONAL_CONST]*item.mass / (drLeft * drLeft);
		float vTop = -cY * params[GRAVITATIONAL_CONST]*item.mass / (drTop * drTop);
		float vRight = cX * params[GRAVITATIONAL_CONST]*item.mass / (drRight * drRight);
		float vBottom = cY * params[GRAVITATIONAL_CONST]*item.mass / (drBottom * drBottom);
		item.force[0] += vLeft;
		item.force[1] += vTop;
		item.force[0] += vRight;
		item.force[1] += vBottom;
	}
	
	private float getRandDisplacement() {
		return ((float)Math.random()-0.5f) / 50.0f;
	}
}