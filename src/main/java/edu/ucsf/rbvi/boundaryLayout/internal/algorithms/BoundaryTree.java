package edu.ucsf.rbvi.boundaryLayout.internal.algorithms;

import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/*
 * BoundaryTree is a Quadtree data structure with the following invariants:
 * 1) Each leaf has no intersected area
 * 2) Each parent contains its children, meaning each child is simply a subrectangle of its parent
 * 3) Area of parent > area of child
 * 4) Every point contained in a child is contained within its parent, but the converse is strictly false
 * 
 * @var root holds the root of the tree
 * @var size represents the total number of non-null nodes within the tree
 */
public class BoundaryTree {
	private BoundaryTreeNode root;
	private int size;

	/*
	 * Construct an empty Boundary Quadtree with no root
	 */
	public BoundaryTree() {
		this(null);
	}

	/*
	 * Construct a Boundary Quadtree with a root and size 1
	 * 
	 * @param root is the root of the tree
	 */
	public BoundaryTree(BoundaryTreeNode root) {
		this.root = root;
		size = 1;
	}	

	/*
	 * Given a partitioning rectangle, this method finds every leaf
	 * node, representing a rectangle, which intersects with this 
	 * partitioning rectangle and returns a list of intersecting leaves.
	 * 
	 * @param Rectangle2D rectangle holds the rectangle that will partition our data
	 * @return a list of leaves in the tree that intersect with the parameter rectangle
	 * @precondition this tree is not empty
	 * */
	public List<BoundaryTreeNode> find(Rectangle2D partition) {
		if(root == null)
			return null;
		
		List<BoundaryTreeNode> intersectedNodes = new ArrayList<>();
		Queue<BoundaryTreeNode> findQueue = new ArrayDeque<>();
		findQueue.add(root);

		while(!findQueue.isEmpty()) {
			BoundaryTreeNode thisNode = findQueue.poll();
			if(thisNode.entry.intersects(partition)) {//if statement theoretically not needed 
				if(thisNode.hasChildren()) {
					for(BoundaryTreeNode childNode : thisNode.children.values())
						if(childNode != null)
							if(childNode.entry.intersects(partition)) 
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
	 * Given a partitioning rectangle, this function finds the intersecting leaf nodes
	 * in this tree and for each of the intersecting nodes, it partitions the rectangle
	 * creating 1-4 children for that node. The children describe the resultant rectangles
	 * that encompass the area which is not intersecting by the partitioning rectangle. 
	 * 
	 * @param partition is the partitioning rectangle that partitions the intersecting leaves, 
	 * which are assigned children and no longer become leaves
	 * 
	 * Rectangle children configuration: 
	 * index 0 is the left side | index 1 is the top side
	 * index 2 is the right side| index 3 is the bottom side 
	 */
	public void do2DShapePartitioning(Rectangle2D partition) {
		List<BoundaryTreeNode> intersectedNodes = find(partition);
		for(BoundaryTreeNode intersectedNode : intersectedNodes) {
			Rectangle2D nodeBox = intersectedNode.entry;
			BoundaryTreeNode[] partitionChildren = new BoundaryTreeNode[4];
			for(BoundaryTreeNode boundaryNode : partitionChildren)
				boundaryNode = null;
			double distanceFromLeft = partition.getX() - nodeBox.getX();
			double distanceFromTop = partition.getY() - nodeBox.getY();
			double distanceFromRight = (nodeBox.getX() + nodeBox.getWidth()) - 
					(partition.getX() + partition.getWidth());
			double distanceFromBottom = (nodeBox.getY() + nodeBox.getHeight()) -
					(partition.getY() + partition.getHeight());
			if(distanceFromLeft > 0) {//left
				Rectangle2D leftRectangle = new Rectangle2D.Double(nodeBox.getX(), 
						nodeBox.getY(), distanceFromLeft, nodeBox.getHeight());
				partitionChildren[0] = new BoundaryTreeNode(leftRectangle, intersectedNode);
				size++;
			}
			if(distanceFromTop > 0) {//top
				Rectangle2D topRectangle = new Rectangle2D.Double(nodeBox.getX(), 
						nodeBox.getY(), nodeBox.getWidth(), distanceFromTop);
				partitionChildren[1] = new BoundaryTreeNode(topRectangle, intersectedNode);
				size++;
			}
			if(distanceFromRight > 0) {//right
				Rectangle2D rightRectangle = new Rectangle2D.Double(partition.getX() + 
						partition.getWidth(), nodeBox.getY(), distanceFromRight, nodeBox.getHeight());
				partitionChildren[2] = new BoundaryTreeNode(rightRectangle, intersectedNode);
				size++;
			}
			if(distanceFromBottom > 0) {//bottom
				Rectangle2D bottomRectangle = new Rectangle2D.Double(nodeBox.getX(), partition.getY() + 
						partition.getHeight(), nodeBox.getWidth(), distanceFromBottom);
				partitionChildren[3] = new BoundaryTreeNode(bottomRectangle, intersectedNode);
				size++;
			}
			intersectedNode.addChildren(partitionChildren);
		}
	}

	/* 
	 * This method does a preorder search through all the available areas and 
	 * returns a list of areas, in the form of Rectangle2D's. This list is sorted
	 * from highest - lowest area. This list includes no less than 85% of the
	 * collective, available area in this tree.
	 * 
	 * @return a list of the largest rectangles, which are leaves
	 * */
	public List<Rectangle2D> getLargestAreas() {
		List<Rectangle2D> bTreeAreas = new ArrayList<>();
		preorderArea(bTreeAreas, root);

		//double totalArea = getTotalArea(bTreeAreas);
		//double summedArea = 0.;
		//Iterator<Rectangle2D> recIterator = bTreeAreas.iterator();
		//List<Rectangle2D> largestAreas = new ArrayList<>();

		return bTreeAreas.subList(0, 1);
		/*while(recIterator.hasNext()) {
			Rectangle2D nextRect = recIterator.next();
			largestAreas.add(nextRect);
			//summedArea += getNonIntersectedArea(nextRect, largestAreas, largestAreas.size() - 1);
		}

		return largestAreas;*/
	}

	/*
	 * This method calculates the total area of a list of rectangles. This calculation
	 * takes into account the intersection of rectangles, in which case the intersected area
	 * is not duplicated.
	 * 
	 * @param areas: a list of rectangles of which you want to find the total area
	 * @return the total area, taking into account intersections between rectangles
	 * */
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

	/* Private method
	 * @param nextRect is a rectangle you want to find the area of
	 * @param areas is a list of rectangles that you must compare nextRect with to determine
	 * any intersections
	 * @param areasCounter is a counter for the upper limit in the list areas for the method to look at
	 * 
	 * @return the non-intersected area of nextRect with respect to the list areas
	 * */
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

	/* Private method
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

	/* Private method
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