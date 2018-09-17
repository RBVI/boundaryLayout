package prefuse.util.force;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a rectangular bounding box of a boundary and applies a force on 
 * the force items in the force simulation, pushing them away from the walls of
 * this rectangle. The walls may or may not be of variable wall force, depending
 * on the user's choice
 */
public abstract class BoundaryWallForce extends AbstractForce {
	private static String[] pnames = new String[] { "GravitationalConstant" };
	public static final float ABS_MAX_FORCE = 1e10f;
	private List<Object> activeOn = new ArrayList<>();

	public static final int IN_GRAVITATIONAL_CONST = 0;
	public static final int OUT_GRAVITATIONAL_CONST = 1;
	public static final int IN_PROJECTION = 1;
	public static final int OUT_PROJECTION = -1;
	private static final double DEFAULT_SCALEFACTOR = 2.5;

	private boolean variableStrength;
	private float scaleFactor;

	protected Point2D center;
	protected Point2D dimensions;

	/**
	 * Create a new RectangularWallForce with given parameters
	 * @param center is a 2D point of the center of the rectangle
	 * @param dimensions is a 2D field representing the width and height
	 * @param gravConst represents the initial gravity constant of the rectangle
	 * @param variableWall tells whether or not the wall changes gravitational constants
	 * @param scaleFactor is the scale by which the wall force changes
	 */
	public BoundaryWallForce(Point2D center, Point2D dimensions, float gravConst, boolean variableWall, double scaleFactor) {
		this.center = center;
		this.dimensions = dimensions;
		params = new float[] { gravConst, gravConst };
		this.variableStrength = variableWall;
		this.scaleFactor = (float) scaleFactor;
	}

	public BoundaryWallForce(Point2D center, Point2D dimensions, float gravConst, boolean variableWall) {
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

	/**
	 * This method sets the scaling factor of this wall force. The scaling factor
	 * is the value that the gravitational constant is multiplied by when the wall
	 * is scaled.
	 * @param scaleFactor is the new scale factor of this wall force
	 * @precondition 0.1 <= scaleFactor <= 10
	 */
	public boolean setScaleFactor(double scaleFactor) {
		if(Math.abs(scaleFactor) >= 0.1 && Math.abs(scaleFactor) <= 10.) {
			this.scaleFactor = (float) scaleFactor;
			return true;
		}
		return false;
	}

	/** 
	 * This method scales the strength of the wall force in the direction of @param dir, 
	 * only if variableStrength is true
	 */
	public void scaleStrength(int dir) {
		if(this.variableStrength) {
			if(dir == IN_PROJECTION) 
				params[IN_GRAVITATIONAL_CONST] *= scaleFactor;
			else if(dir == OUT_PROJECTION) 
				params[OUT_GRAVITATIONAL_CONST] *= scaleFactor;
		}
	}
	
	public void setActiveCategories(List<Object> categories) {
		activeOn = categories;
	}
	
	public boolean isActive(Object category) {
		if(activeOn != null || activeOn.isEmpty())
			return false;
		return activeOn.contains(category);
	}

	/**
	 * @see prefuse.util.force.Force#getForce(prefuse.util.force.ForceItem)
	 */
	public abstract void getForce(ForceItem item);	

	/**
	 * @return a random displacement 
	 */
	protected float getRandDisplacement() {
		return ((float)Math.random() - 1f) / 50.0f;
	}
}
