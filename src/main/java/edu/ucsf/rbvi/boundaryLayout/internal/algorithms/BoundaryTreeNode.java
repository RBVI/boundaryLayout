package edu.ucsf.rbvi.boundaryLayout.internal.algorithms;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/*
 * This BoundaryTreeNode represents a node in the BoundaryTree and its properties include:
 * entry: the Rectangle2D that this node corresponds to
 * parent: the parent node of this node
 * children: the children of this node. Since BoundaryTree is a Quadtree, this node has at 
 * most 4 children. The children are represented as a map, identified by their relative position
 * to each other, contained by the parent: [left, top, right, bottom]
 */
public class BoundaryTreeNode {
	Rectangle2D entry;
	BoundaryTreeNode parent;
	Map<String, BoundaryTreeNode> children;
	static final String LEFTCHILD = "LEFT";
	static final String TOPCHILD = "TOP";
	static final String RIGHTCHILD = "RIGHT";
	static final String BOTTOMCHILD = "BOTTOM";

	/*
	 * Construct a BoundaryTreeNode with a specified entry: children and parent are null. This
	 * constructor is used for the root of the tree.
	 */
	BoundaryTreeNode(Rectangle2D entry) {
		this(entry, null, null, null, null, null);
	}

	/*
	 * Construct a BoundaryTreeNode with a specified entry and parent: children are null.
	 */
	BoundaryTreeNode(Rectangle2D entry, BoundaryTreeNode parent) {
		this(entry, parent, null, null, null, null);
	}

	/*
	 *  Construct a BoundaryTreeNode, specifying entry, parent and children.
	 */
	BoundaryTreeNode(Rectangle2D entry, BoundaryTreeNode parent,
			BoundaryTreeNode leftChild, BoundaryTreeNode topChild, 
			BoundaryTreeNode rightChild, BoundaryTreeNode bottomChild) {
		children = new HashMap<>();
		children.put(LEFTCHILD, leftChild);
		children.put(TOPCHILD, topChild);
		children.put(RIGHTCHILD, rightChild);
		children.put(BOTTOMCHILD, bottomChild);
		this.entry = entry;
		this.parent = parent;
	}

	/*
	 * Express a BoundaryTreeNode as a String.
	 *
	 * @return a String representing the BoundaryTreeNode.
	 */
	@Override
	public String toString() {
		String s = "";
		s = "The location is (" + entry.getX() + "," + entry.getY() + ")";
		s = s + "\nThe dimensions are (w,h): " + entry.getWidth() + "," + entry.getHeight();
		s = s + "\nThe children are (L,T,R,B): ";
		s += "(" + children.get(LEFTCHILD) + "," + children.get(TOPCHILD) + "," + 
				children.get(RIGHTCHILD) + "," + children.get(BOTTOMCHILD) + ")"; 
		return s;
	}
	
	/*
	 * Checks to see if this node has children
	 * 
	 * @return true if this node has at least 1 child
	 */
	public boolean hasChildren() {
		return (children.get(LEFTCHILD) != null || 
				children.get(TOPCHILD) != null || 
				children.get(RIGHTCHILD) != null || 
				children.get(BOTTOMCHILD) != null);
	}
	
	/* 
	 * Adds children to this node
	 * 
	 * @param BoundaryTreeNode[] partitionChildren is assumed to be of
	 * length 4 holding [leftChild, topChild, rightChild, bottomChild]
	 */
	public void addChildren(BoundaryTreeNode[] partitionChildren) {
		children.put(LEFTCHILD, partitionChildren[0]);
		children.put(TOPCHILD, partitionChildren[1]);
		children.put(RIGHTCHILD, partitionChildren[2]);
		children.put(BOTTOMCHILD, partitionChildren[3]);
	}
}
