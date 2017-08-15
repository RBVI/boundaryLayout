package edu.ucsf.rbvi.boundaryLayout.internal.algorithms;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class BoundaryContainsAlgorithm {
	public static List<Rectangle2D.Double> doAlgorithm(Rectangle2D.Double thisShape, 
			List<Rectangle2D.Double> containedShapes) {
		List<Rectangle2D.Double> largestAreas = new ArrayList<>();
		
		BoundaryTree shapeTree = new BoundaryTree(new BoundaryTreeNode(thisShape));
		
		while(!containedShapes.isEmpty()) {
			Rectangle2D.Double rectangle = containedShapes.remove(0);
			shapeTree.do2DShapePartitioning(shapeTree.find(rectangle), rectangle);
		}
		
		largestAreas = shapeTree.getLargestAreas(); 
			
		return largestAreas;
	}
}