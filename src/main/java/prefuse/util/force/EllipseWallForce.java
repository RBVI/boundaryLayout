package prefuse.util.force;

import java.awt.geom.Point2D;

public class EllipseWallForce extends AbstractForce {
	private static String[] pnames = new String[] { "GravitationalConstant" };

	public static final float DEFAULT_GRAV_CONSTANT = -1f;
	public static final int GRAVITATIONAL_CONST = 0;

	private Point2D.Double center;
	private Point2D.Double r;

	/**
	 * Create a new CircularWallForce.
	 * @param gravConst the gravitational constant to use
	 * @param x the center x-coordinate of the circle
	 * @param y the center y-coordinate of the circle
	 * @param r holds the dimensions of the major and minor axis;
	 */

	public EllipseWallForce(float gravConst, 
			Point2D.Double center, Point2D.Double r) 
	{
		params = new float[] { gravConst };
		this.center = center;
		this.r = r;
	}

	/**
	 * Create a new CircularWallForce with default gravitational constant.
	 * @param x the center x-coordinate of the circle
	 * @param y the center y-coordinate of the circle
	 * @param r the radius of the circle
	 */
	public EllipseWallForce(Point2D.Double center, Point2D.Double r) {
		this(DEFAULT_GRAV_CONSTANT, center, r);
	}


	/**
	 * Returns true.
	 * @see prefuse.util.force.Force#isItemForce()
	 */
	public boolean isItemForce() {
		return true;
	}


	@Override
	protected String[] getParameterNames() {
		// TODO Auto-generated method stub
		return pnames;
	}

	public void getForce(ForceItem item) {
		float[] n = item.location; //current location of forceitem
		float dx = ((float) center.getX()) - n[0];
		float dy = ((float) center.getX()) - n[1];

		if ( dx == 0.0 && dy == 0.0 ) {
			dx = ((float)Math.random()-0.5f) / 50.0f;
			dy = ((float)Math.random()-0.5f) / 50.0f;
		}

		float rX = (float) r.getX();
		float rY = (float) r.getY();
		
		float fociC = (float)(Math.sqrt((rX*rX) - (rY * rY))); 

		float foci1Dist = 0.0f;

		float height = 2 * (rY);
		float width = 2 * rX;

		float cX = 0.0f;
		float cY = 0.0f;

		if (width > height) {
			float foci1X = ((float) center.getX()) + fociC;     
			float foci2X = ((float) center.getX()) - fociC;
			float fociY = ((float) center.getY()); 	
			foci1Dist = (float)(Math.sqrt(Math.pow((n[0] - foci1X), 2)) + Math.pow(n[1] - fociY, 2));

			float drLeft = foci1X - n[0];
			float drRight = foci2X - n[0];

			cX =  drLeft > foci1Dist ? -1 : 1;  

			float vLeft =  -cX * params[GRAVITATIONAL_CONST] * item.mass / (drLeft * drLeft); 
			float vRight = cX * params[GRAVITATIONAL_CONST] * item.mass / (drRight * drRight); 

			item.force[0] += vLeft;
			item.force[0] += vRight;
		} else {
			float foci1Y = ((float) center.getY()) + fociC;     
			float foci2Y = ((float) center.getY()) - fociC;
			float fociX = ((float) center.getX()); 	
			foci1Dist = (float)(Math.sqrt(Math.pow((n[0] - fociX), 2)) + Math.pow(n[1] - foci1Y, 2));

			float drUp = foci1Y - n[1];
			float drDown = foci2Y - n[1];

			cY =  drUp > foci1Dist ? -1 : 1;  

			float vUp =  -cY * params[GRAVITATIONAL_CONST] * item.mass / (drUp * drUp); 
			float vDown = cY * params[GRAVITATIONAL_CONST] * item.mass / (drDown * drDown); 

			item.force[1] += vUp;
			item.force[1] += vDown;		
		}
	}
}