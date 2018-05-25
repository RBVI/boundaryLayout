package edu.ucsf.rbvi.boundaryLayout.internal.algorithms;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class BoundaryContainsAlgorithm {
	public static List<Rectangle2D> doAlgorithm(Rectangle2D thisShape, 
			List<Rectangle2D> containedShapes) {
		List<Rectangle2D> largestAreas = new ArrayList<>();
		
		BoundaryTree shapeTree = new BoundaryTree(new BoundaryTreeNode(thisShape));
		
		while(!containedShapes.isEmpty()) {
			Rectangle2D rectangle = containedShapes.remove(0);
			shapeTree.do2DShapePartitioning(shapeTree.find(rectangle), rectangle);
		}
		
		largestAreas = shapeTree.getLargestAreas(); 
			
		return largestAreas;
	}
}
