package prefuse.util.force;

public class EllipseWallForce extends AbstractForce {
	private static String[] pnames = new String[] { "GravitationalConstant" };

	public static final float DEFAULT_GRAV_CONSTANT = -1f;
	public static final int GRAVITATIONAL_CONST = 0;
	
	private float x, y, rX, rY;

	 /**
     * Create a new CircularWallForce.
     * @param gravConst the gravitational constant to use
     * @param x the center x-coordinate of the circle
     * @param y the center y-coordinate of the circle
     * @param r the radius of the circle
     */
	
	public EllipseWallForce(float gravConst, 
	        float x, float y, float rX, float rY) 
	    {
		 params = new float[] { gravConst };
	     //   minValues = new float[] { DEFAULT_MIN_GRAV_CONSTANT };
	     //   maxValues = new float[] { DEFAULT_MAX_GRAV_CONSTANT };
	        this.x = x;
	        this.y = y;
	        this.rX = rX;
	        this.rY = rY;
     }
	
	  /**
     * Create a new CircularWallForce with default gravitational constant.
     * @param x the center x-coordinate of the circle
     * @param y the center y-coordinate of the circle
     * @param r the radius of the circle
     */
    public EllipseWallForce(float x, float y, float rX, float rY ) {
        this(DEFAULT_GRAV_CONSTANT, x , y, rX, rY);
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
	    
}
 