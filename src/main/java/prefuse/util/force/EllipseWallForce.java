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
	
	public void getForce(ForceItem item) {
		float[] n = item.location; //current location of forceitem
		float dx = x - n[0];
		float dy = y - n[1];

		if ( dx == 0.0 && dy == 0.0 ) {
			dx = ((float)Math.random()-0.5f) / 50.0f;
			dy = ((float)Math.random()-0.5f) / 50.0f;
		}

		float fociC = (float)(Math.sqrt((rX*rX) - (rY * rY))); 
		
		float foci1Dist = 0.0f;

		float height = 2 * (rY);
		float width = 2 * rX;

		if (width > height) {
			float foci1X = x + fociC;     
			float foci2X = x - fociC;
			float fociY = y; 	
			foci1Dist = (float)(Math.sqrt(Math.pow((n[0] - foci1X), 2)) + Math.pow(n[1] - fociY, 2));
		/*	float foci2Dist = (float)(Math.abs(n[0] - fociC));	     
			float foci1DistSq = (float)(Math.pow(foci1Dist, 2));
			float foci2DistSq = (float)(Math.pow(foci2Dist, 2)); */

			float drLeft = foci1X - n[0];
			float drRight = foci2X - n[0];

			float cX =  drLeft > foci1Dist ? -1 : 1;  
			float cY; 

			float vLeft =  -cX * params[GRAVITATIONAL_CONST] * item.mass / (drLeft * drLeft); 
			float vRight = cX * params[GRAVITATIONAL_CONST] * item.mass / (drRight * drRight); 

			item.force[0] += vLeft;
			item.force[0] += vRight;
		}
		else {
			float foci1Y = y + fociC;     
			float foci2Y = y - fociC;
			float fociX = x; 	
			foci1Dist = (float)(Math.sqrt(Math.pow((n[0] - fociX), 2)) + Math.pow(n[1] - foci1Y, 2));
			
			float drUp = foci1Y - n[1];
			float drDown = foci2Y - n[1];

			float cX =  drUp > foci1Dist ? -1 : 1;  
			float cY; 

			float vUp =  -cX * params[GRAVITATIONAL_CONST] * item.mass / (drUp * drUp); 
			float vDown = cX * params[GRAVITATIONAL_CONST] * item.mass / (drDown * drDown); 

			item.force[1] += vUp;
			item.force[1] += vDown;		
		}
	}
	    
}
 