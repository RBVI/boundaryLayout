package edu.ucsf.rbvi.boundaryLayout.internal.algorithms;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This class corresponds to the algorithm performed when the boundary has intersecting boundaries.
 */
public class BoundaryContainsAlgorithm {
	
	/**
	 * Given a certain rectangle and intersecting rectangles, this method finds a list of the largest
	 * areas. These areas are rectangles sorted in high-low order by their area. Important to note is these
	 * rectangles, which are subrectangles of the particular given rectangle, are not intersected by the
	 * list of intersecting rectangles.
	 * 
	 * @param thisShape is the boundary bounding box, of which the method finds the list of areas
	 * @param intersectingShapes is a list of bounding boxes that intersecting with thisShape bounding box
	 * @return list of rectangles corresponding to areas where nodes can be initialized safely
	 * @precondition thisShape != null and intersecting shapes !- null
	 */
	public static List<Rectangle2D> doAlgorithm(Rectangle2D thisShape, List<Rectangle2D> intersectingShapes) {
		List<Rectangle2D> boundingAreas = new ArrayList<>();
		
		BoundaryTree shapeTree = new BoundaryTree(new BoundaryTreeNode(thisShape));
		
		while(!intersectingShapes.isEmpty()) 
			shapeTree.do2DShapePartitioning(intersectingShapes.remove(0));
		
		boundingAreas = shapeTree.getLargestAreas(); 
		return boundingAreas;
	}
}
