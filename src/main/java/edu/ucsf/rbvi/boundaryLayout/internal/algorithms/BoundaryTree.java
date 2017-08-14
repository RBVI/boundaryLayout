package edu.ucsf.rbvi.boundaryLayout.internal.algorithms;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
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
	 * @param Rectangle2D.Double rectangle holds the rectangle
	 * that will partition our data
	 * @return a list of leaves in the tree that intersect with
	 * the parameter rectangle
	 * */
	public List<BoundaryTreeNode> find(Rectangle2D.Double partitioningRectangle) {
		List<BoundaryTreeNode> intersectedNodes = new ArrayList<>();
		Queue<BoundaryTreeNode> findQueue = new PriorityQueue<>();
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
			} else {//debugging 
				System.out.println("UHOH ALG MESSED UP!");
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
	 * @param Rectangle2D.Double partitioningRectangle is the newly added 
	 * rectangle that partitions all of the intersecting leaves.
	 * 
	 * This method adds children to the tree corresponding to the newly added
	 * rectangle.
	 * */
	public void do2DShapePartitioning(List<BoundaryTreeNode> 
	intersectedNodes, Rectangle2D.Double partitioningRectangle) {
		for(BoundaryTreeNode intersectedNode : intersectedNodes) {
			Rectangle2D.Double intersectedNodeRectangle = intersectedNode.entry;
			//index 0 represents the left side of the rectangle, rotating clockwise
			BoundaryTreeNode[] partitionChildren = new BoundaryTreeNode[4];
			double distanceFromLeft = partitioningRectangle.getX() - 
					intersectedNodeRectangle.getX();
			double distanceFromTop = partitioningRectangle.getY() - 
					intersectedNodeRectangle.getY();
			double distanceFromRight = 
					(intersectedNodeRectangle.getX() + intersectedNodeRectangle.width) - 
					(partitioningRectangle.getX() + partitioningRectangle.width);
			double distanceFromBottom = 
					(intersectedNodeRectangle.getY() + intersectedNodeRectangle.height) -
					(partitioningRectangle.getY() + partitioningRectangle.height);
			if(distanceFromLeft > 0) {//left
				Rectangle2D.Double leftRectangle = new Rectangle2D.Double(
						intersectedNodeRectangle.getX(), intersectedNodeRectangle.getY(), 
						distanceFromLeft, intersectedNodeRectangle.getHeight());
				partitionChildren[0] = new BoundaryTreeNode(leftRectangle, intersectedNode);
				size++;
			}
			if(distanceFromTop > 0) {//top
				Rectangle2D.Double topRectangle = new Rectangle2D.Double(
						intersectedNodeRectangle.getX(), intersectedNodeRectangle.getY(), 
						intersectedNodeRectangle.getWidth(), distanceFromTop);
				partitionChildren[1] = new BoundaryTreeNode(topRectangle, intersectedNode);
				size++;
			}
			if(distanceFromRight > 0) {//right
				Rectangle2D.Double rightRectangle = new Rectangle2D.Double(
						partitioningRectangle.getX() + partitioningRectangle.getWidth(), 
						intersectedNodeRectangle.getY(), distanceFromRight, 
						intersectedNodeRectangle.getHeight());
				partitionChildren[2] = new BoundaryTreeNode(rightRectangle, intersectedNode);
				size++;
			}
			if(distanceFromBottom > 0) {//bottom
				Rectangle2D.Double bottomRectangle = new Rectangle2D.Double(
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
	public List<Rectangle2D.Double> getLargestAreas() {
		List<Rectangle2D.Double> bTree = new ArrayList<>();
		preorder(bTree, root);
		return bTree;
	}

	/*
	 * @param List<Rectangle2D.Double> areas holds the largest area rectangle
	 * leaves in the tree
	 * @param BoundaryTreeNode bNode is the node currently being traversed
	 * 
	 * preorder does a preorder search of the tree and only looks at the leaves
	 * */
	public void preorder(List<Rectangle2D.Double> areas, BoundaryTreeNode bNode) {
		if(bNode == null) 
			return;
		if(!bNode.hasChildren()) {
			double area = bNode.entry.getWidth() * bNode.entry.getHeight();
			if(areas.size() > 4) {
				for(Rectangle2D.Double comparedArea : areas)
					if(area > comparedArea.getWidth() * comparedArea.getHeight()) 
						comparedArea = bNode.entry;
			}
			else 
				areas.add(bNode.entry);
		} else 
			for(BoundaryTreeNode childNode : bNode.children.values())
				preorder(areas, childNode);
	}
}