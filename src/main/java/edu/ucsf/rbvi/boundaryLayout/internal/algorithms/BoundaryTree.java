package edu.ucsf.rbvi.boundaryLayout.internal.algorithms;

public class BoundaryTree {
	private BoundaryTreeNode root;
	private int size;
	
	public BoundaryTree() {
		this(null);
	}
	
	public BoundaryTree(BoundaryTreeNode root) {
		this.root = root;
		size = 0;
	}	
	
	//implement method of iterating through the tree
}