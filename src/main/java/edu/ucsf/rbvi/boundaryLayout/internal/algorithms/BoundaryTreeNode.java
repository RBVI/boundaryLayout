package edu.ucsf.rbvi.boundaryLayout.internal.algorithms;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

public class BoundaryTreeNode {
	Rectangle2D.Double entry;
	BoundaryTreeNode parent;
	Map<String, BoundaryTreeNode> children;
	final String LEFTCHILD = "LEFT";
	final String TOPCHILD = "TOP";
	final String RIGHTCHILD = "RIGHT";
	final String BOTTOMCHILD = "BOTTOM";

	/*
	 * Construct a BoundaryTreeNoded with a specified entry: children and parent are null.
	 * 
	 */
	BoundaryTreeNode(Rectangle2D.Double entry) {
		this(entry, null, null, null, null, null);
	}

	/*
	 *  Construct a BoundaryTreeNode with a specified entry and parent: children
	 *  are null.
	 */
	BoundaryTreeNode(Rectangle2D.Double entry, BoundaryTreeNode parent) {
		this(entry, parent, null, null, null, null);
	}

	/**
	 *  Construct a BoundaryTreeNode, specifying entry, parent and children.
	 **/
	BoundaryTreeNode(Rectangle2D.Double entry, BoundaryTreeNode parent,
			BoundaryTreeNode leftChild, BoundaryTreeNode topChild, 
			BoundaryTreeNode rightChild, BoundaryTreeNode bottomChild) {
		children = new HashMap<>();
		children.put(this.LEFTCHILD, leftChild);
		children.put(this.TOPCHILD, topChild);
		children.put(this.RIGHTCHILD, rightChild);
		children.put(this.BOTTOMCHILD, bottomChild);
		this.entry = entry;
		this.parent = parent;
	}

	/**
	 *  Express a BoundaryTreeNode as a String.
	 *
	 *  @return a String representing the BoundaryTreeNode.
	 **/
	public String toString() {
		String s = "";
		s = "The location is (" + entry.getX() + "," + entry.getY() + ")";
		s = s + "\nThe dimensions are (w,h): " + entry.getWidth() + "," + entry.getHeight();
		s = s + "\nThe children are (L,T,R,B): ";
		s += "(" + children.get(this.LEFTCHILD) + "," + children.get(this.TOPCHILD) + "," + 
				children.get(this.RIGHTCHILD) + "," + children.get(this.BOTTOMCHILD) + ")"; 
		return s;
	}
}