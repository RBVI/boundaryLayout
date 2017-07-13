package prefuse.util.force;

import java.awt.geom.Point2D;

/**
 * Uses a gravitational force model to act as a circular "wall". Can be used to
 * construct circles which either attract or repel items.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class CircularWallForce extends AbstractForce {

    private static String[] pnames = new String[] { "GravitationalConstant" };
    
    public static final float DEFAULT_GRAV_CONSTANT = -500f;
    public static final int GRAVITATIONAL_CONST = 0;
    
    private Point2D.Double center;
	private float radius;

    /**
     * Create a new CircularWallForce.
     * @param gravConst the gravitational constant to use
     * @param x the center x-coordinate of the circle
     * @param y the center y-coordinate of the circle
     * @param r the radius of the circle
     */
    public CircularWallForce(float gravConst, 
        Point2D.Double center, float radius) 
    {
        params = new float[] { gravConst };
        this.center = center;
        this.radius = radius;
    }
    
    /**
     * Create a new CircularWallForce with default gravitational constant.
     * @param x the center x-coordinate of the circle
     * @param y the center y-coordinate of the circle
     * @param r the radius of the circle
     */
    public CircularWallForce(Point2D.Double center, float radius) {
        this(DEFAULT_GRAV_CONSTANT,center,radius);
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
        float dx = (float)center.getX()-n[0];
        float dy = (float)center.getY()-n[1];
        float d = (float)Math.sqrt(dx*dx+dy*dy);
        float dr = radius-d;
        float c = dr > 0 ? -1 : 1;
        float v = c*params[GRAVITATIONAL_CONST]*item.mass / (dr*dr);
        if ( d == 0.0 ) {
            dx = ((float)Math.random()-0.5f) / 50.0f;
            dy = ((float)Math.random()-0.5f) / 50.0f;
            d  = (float)Math.sqrt(dx*dx+dy*dy);
        }
        item.force[0] += v*dx/d;
        item.force[1] += v*dy/d;
 
        //System.out.println(dx/d+","+dy/d+","+dr+","+v);
    }

} // end of class CircularWallForce
