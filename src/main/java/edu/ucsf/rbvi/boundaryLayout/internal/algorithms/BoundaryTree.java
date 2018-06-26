package edu.ucsf.rbvi.boundaryLayout.internal.algorithms;

import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class BoundaryTree {
	private BoundaryTreeNode root;
	private int size;

	/*
	 * Construct a BoundaryTree without a root.
	 */
	public BoundaryTree() {
		this(null);
	}

	/*
	 * Construct a BoundaryTree with a specified root.
	 */
	public BoundaryTree(BoundaryTreeNode root) {
		this.root = root;
		size = 0;
	}	

	/*
	 * @param Rectangle2D rectangle holds the rectangle
	 * that will partition our data
	 * @return a list of leaves in the tree that intersect with
	 * the parameter rectangle
	 * */
	public List<BoundaryTreeNode> find(Rectangle2D partitioningRectangle) {
		List<BoundaryTreeNode> intersectedNodes = new ArrayList<>();
		Queue<BoundaryTreeNode> findQueue = new ArrayDeque<>();
		findQueue.add(root);

		while(!findQueue.isEmpty()) {
			BoundaryTreeNode thisNode = findQueue.poll();
			if(thisNode.entry.intersects(partitioningRectangle)) {//if statement theoretically not needed 
				if(thisNode.hasChildren()) {
					for(BoundaryTreeNode childNode : thisNode.children.values())
						if(childNode != null)
							if(childNode.entry.intersects(partitioningRectangle)) 
								findQueue.add(childNode);
				}
				else {
					intersectedNodes.add(thisNode);
				}
			}
		}

		return intersectedNodes;
	}

	/*
	 * @return size of the tree.
	 */
	public int getSize() {
		return size;
	}

	/*
	 * @param List<BoundaryTreeNode> intersectedNodes is a list of leaf nodes
	 * that intersect with the newly added partitioning rectangle.
	 * @param Rectangle2D partitioningRectangle is the newly added 
	 * rectangle that partitions all of the intersecting leaves.
	 * 
	 * This method adds children to the tree corresponding to the newly added
	 * rectangle.
	 * */
	public void do2DShapePartitioning(List<BoundaryTreeNode> intersectedNodes, 
			Rectangle2D partitioningRectangle) {
		for(BoundaryTreeNode intersectedNode : intersectedNodes) {
			Rectangle2D intersectedNodeRectangle = intersectedNode.entry;
			//index 0 represents the left side of the rectangle, rotating clockwise
			BoundaryTreeNode[] partitionChildren = new BoundaryTreeNode[4];
			for(BoundaryTreeNode boundaryNode : partitionChildren)
				boundaryNode = null;
			double distanceFromLeft = partitioningRectangle.getX() - 
					intersectedNodeRectangle.getX();
			double distanceFromTop = partitioningRectangle.getY() - 
					intersectedNodeRectangle.getY();
			double distanceFromRight = 
					(intersectedNodeRectangle.getX() + intersectedNodeRectangle.getWidth()) - 
					(partitioningRectangle.getX() + partitioningRectangle.getWidth());
			double distanceFromBottom = 
					(intersectedNodeRectangle.getY() + intersectedNodeRectangle.getHeight()) -
					(partitioningRectangle.getY() + partitioningRectangle.getHeight());
			if(distanceFromLeft > 0) {//left
				Rectangle2D leftRectangle = new Rectangle2D.Double(
						intersectedNodeRectangle.getX(), intersectedNodeRectangle.getY(), 
						distanceFromLeft, intersectedNodeRectangle.getHeight());
				partitionChildren[0] = new BoundaryTreeNode(leftRectangle, intersectedNode);
				size++;
			}
			if(distanceFromTop > 0) {//top
				Rectangle2D topRectangle = new Rectangle2D.Double(
						intersectedNodeRectangle.getX(), intersectedNodeRectangle.getY(), 
						intersectedNodeRectangle.getWidth(), distanceFromTop);
				partitionChildren[1] = new BoundaryTreeNode(topRectangle, intersectedNode);
				size++;
			}
			if(distanceFromRight > 0) {//right
				Rectangle2D rightRectangle = new Rectangle2D.Double(
						partitioningRectangle.getX() + partitioningRectangle.getWidth(), 
						intersectedNodeRectangle.getY(), distanceFromRight, 
						intersectedNodeRectangle.getHeight());
				partitionChildren[2] = new BoundaryTreeNode(rightRectangle, intersectedNode);
				size++;
			}
			if(distanceFromBottom > 0) {//bottom
				Rectangle2D bottomRectangle = new Rectangle2D.Double(
						intersectedNodeRectangle.getX(), partitioningRectangle.getY() + 
						partitioningRectangle.getHeight(), intersectedNodeRectangle.getWidth(), 
						distanceFromBottom);
				partitionChildren[3] = new BoundaryTreeNode(bottomRectangle, intersectedNode);
				size++;
			}
			intersectedNode.addChildren(partitionChildren);
		}
	}

	/*
	 * @return a list of the largest rectangles which are leaves
	 * 
	 * Method call to preorder which does a recursive preorder search and gets
	 * a list of the largest areas which is put into bTree variable
	 * */
	public List<Rectangle2D> getLargestAreas() {
		List<Rectangle2D> bTreeAreas = new ArrayList<>();
		preorderArea(bTreeAreas, root);
		
		double totalArea = getTotalArea(bTreeAreas);
		double summedArea = 0.;
		Iterator<Rectangle2D> recIterator = bTreeAreas.iterator();
		List<Rectangle2D> largestAreas = new ArrayList<>();
		
		while(recIterator.hasNext() && summedArea < totalArea * 0.85) {
			Rectangle2D nextRect = recIterator.next();
			largestAreas.add(nextRect);
			summedArea += getNonIntersectedArea(nextRect, largestAreas, largestAreas.size() - 1);
		}

		return largestAreas;
	}
	
	/*
	 * @param areas: a list of rectangles of which you want to find the total area
	 * 
	 * @return the total area, taking into account intersections between rectangles*/
	public static double getTotalArea(List<Rectangle2D> areas) {
		double totalArea = 0.;
		int areasCounter = 0;
		Iterator<Rectangle2D> recIterator = areas.iterator();
		
		while(recIterator.hasNext()) {
			Rectangle2D nextRect = recIterator.next();
			totalArea += getNonIntersectedArea(nextRect, areas, areasCounter++);
		}
		
		return totalArea;
	}
	
	/*
	 * @param nextRect is a rectangle you want to find the area of
	 * @param areas is a list of rectangles that you must compare nextRect with to determine
	 * any intersections
	 * @param areasCounter is a counter for the upper limit in the list areas for the method to look at
	 * 
	 * @return the non-intersected area of nextRect with respect to the list areas*/
	private static double getNonIntersectedArea(Rectangle2D nextRect, List<Rectangle2D> areas, int areasCounter) {
		List<Rectangle2D> intersections = new ArrayList<>();
		for(int intersect = 0; intersect < areasCounter; intersect++) {
			Rectangle2D intersectRect = areas.get(intersect);
			if(nextRect.intersects(intersectRect)) {
				Rectangle2D intersection = new Rectangle2D.Double();
				Rectangle2D.intersect(nextRect, intersectRect, intersection);
				intersections.add(intersection);
			}
		}
		double repeatArea = getTotalArea(intersections);
		return nextRect.getWidth() * nextRect.getHeight() - repeatArea;
	}

	/*
	 * @param List<Rectangle2D> areas holds the largest area rectangle
	 * leaves in the tree
	 * @param BoundaryTreeNode bNode is the node currently being traversed
	 * 
	 * preorder does a preorder search of the tree and only looks at the leaves
	 * */
	private void preorderArea(List<Rectangle2D> areas, BoundaryTreeNode bNode) {
		if(bNode != null) {
			if(!bNode.hasChildren()) {
				appendSorted(areas, bNode.entry);//keep sorted in order of decreasing area
			} else {
				for(BoundaryTreeNode childNode : bNode.children.values())
					preorderArea(areas, childNode);
			}
		}
	}

	/*
	 * @param areas is a sorted list and this method keeps the sorted property
	 * @param leafRect is the rectangle you want to add to areas
	 * 
	 * This method adds leafRect to list areas, which is sorted in terms of decreasing area*/
	private static void appendSorted(List<Rectangle2D> areas, Rectangle2D leafRect) { 
		if(areas == null)
			areas = new ArrayList<>();
		if(areas.size() == 0) {
			areas.add(leafRect);
		} else {
			int areaCount = 0;
			double leafArea = leafRect.getWidth() * leafRect.getHeight();
			double area = areas.get(areaCount).getWidth() * areas.get(areaCount).getHeight();
			while(areaCount < areas.size() && leafArea < area) {
				areaCount++;
				if(areaCount < areas.size())
					area = areas.get(areaCount).getWidth() * areas.get(areaCount).getHeight();
			}
			areas.add(areaCount, leafRect);
		}
	}
}