package org.cheminfo.hook.util;

import java.util.Vector;

public class QuadTreeNode {
	private float leftLimit, rightLimit, topLimit, bottomLimit;
	private QuadTreeNode[] childNodes = null;
	private Vector data = null;

	private final static boolean VERBOSE = false;

	/**
	 * Creates a new node. Checks the number of elements provided and is
	 * necessary continues by creating four child nodes. Otherwise stores the
	 * data.
	 * 
	 * @param leftLimit
	 *            Left limit of the current node.
	 * @param rightLimit
	 *            Right limit of the current node.
	 * @param topLimit
	 *            Top limit of the current node.
	 * @param bottomLimit
	 *            Bottom limit of the current node.
	 * @param data
	 *            Data to be stored in this or the child nodes. The data is raw,
	 *            segments provided might not be contained in the node area.
	 * @param maxElements
	 *            Maximum number of elements to be stored in a single node.
	 * @param maxDepth
	 *            Maximum tree depth. Has priority on maxElements.
	 */
	public QuadTreeNode(float leftLimit, float rightLimit, float topLimit,
			float bottomLimit, Vector inData, int maxElements, int maxDepth) {
		this.leftLimit = leftLimit;
		this.rightLimit = rightLimit;
		this.topLimit = topLimit;
		this.bottomLimit = bottomLimit;

		this.compileNode(inData, maxElements, maxDepth);
	}

	private void compileNode(Vector testData, int maxElements, int maxDepth) {
		Vector tempVector = new Vector();
		QuadTreeElement tempElement;

		for (int element = 0; element < testData.size(); element++) {
			tempElement = (QuadTreeElement) testData.get(element);

			if (tempElement.isContained(this.leftLimit, this.rightLimit,
					this.topLimit, this.bottomLimit)) {
				tempVector.add(tempElement);
			}
		}

		this.childNodes = null;
		if (tempVector.size() < maxElements) {
			if (tempVector.size() != 0) {
				if (VERBOSE)
					System.out.println("Storing for size, depth: " + maxDepth);
				this.data = tempVector;
			}
		} else {
			if (maxDepth > 0) {
				this.childNodes = new QuadTreeNode[4];
				this.childNodes[0] = new QuadTreeNode(leftLimit,
						(leftLimit + rightLimit) / 2, topLimit,
						(topLimit + bottomLimit) / 2, tempVector, maxElements,
						maxDepth - 1);
				this.childNodes[1] = new QuadTreeNode(
						(leftLimit + rightLimit) / 2, rightLimit, topLimit,
						(topLimit + bottomLimit) / 2, tempVector, maxElements,
						maxDepth - 1);
				this.childNodes[2] = new QuadTreeNode(leftLimit,
						(leftLimit + rightLimit) / 2,
						(topLimit + bottomLimit) / 2, bottomLimit, tempVector,
						maxElements, maxDepth - 1);
				this.childNodes[3] = new QuadTreeNode(
						(leftLimit + rightLimit) / 2, rightLimit,
						(topLimit + bottomLimit) / 2, bottomLimit, tempVector,
						maxElements, maxDepth - 1);
			} else {
				if (tempVector.size() != 0) {
					if (VERBOSE)
						System.out
								.println("Storing because of depth, tV size: "
										+ tempVector.size());
					this.data = tempVector;
				}
			}
		}

	}

	public void getAllElements(Vector outVector) {
		if (this.data != null)
			outVector.addAll(this.data);

		if (this.childNodes != null) {
			for (int childID = 0; childID < this.childNodes.length; childID++) {
				this.childNodes[childID].getAllElements(outVector);
			}
		}
	}

	public void getElements(double queryLeft, double queryRight,
			double queryTop, double queryBottom, Vector outVector) {
		if (this.intersects(queryLeft, queryRight, queryTop, queryBottom)) {
			if (this.data != null) // normally this should be a leaf node
			{
				for (int d = 0; d < this.data.size(); d++) {
					//LP20090424 not checking the unicity increase the spead
					//of quadtree generation a lot. Less than 10% of redundant elements are drawn
					//if (!outVector.contains(this.data.get(d)))
						outVector.add(this.data.get(d));
				}
				// outVector.addAll(this.data);
			} else {
				// run the tree
				if (this.childNodes != null) // this could be an empty node
				{
					for (int childID = 0; childID < 4; childID++) {
						this.childNodes[childID].getElements(queryLeft,
								queryRight, queryTop, queryBottom, outVector);
					}
				}
			}
		}
	}

	private boolean intersects(double queryLeft, double queryRight,
			double queryTop, double queryBottom) {
		double interLeft = Math.min(Math.max(queryLeft, queryRight), Math.max(
				this.leftLimit, this.rightLimit));
		double interRight = Math.max(Math.min(queryRight, queryLeft), Math.min(
				this.rightLimit, this.leftLimit));
		double interTop = Math.max(Math.min(queryTop, queryBottom), Math.min(
				this.topLimit, this.bottomLimit));
		double interBottom = Math.min(Math.max(queryBottom, queryTop), Math
				.max(this.bottomLimit, this.topLimit));

		/*
		 * System.out.println("query limits: "+queryLeft+", "+queryRight+",
		 * "+queryTop+", "+queryBottom); System.out.println("this limits:
		 * "+this.leftLimit+", "+this.rightLimit+", "+this.topLimit+",
		 * "+this.bottomLimit); System.out.println("inters: "+interLeft+",
		 * "+interRight+", "+interTop+", "+interBottom);
		 */if ((interLeft - interRight) > 0 && (interBottom - interTop) > 0)
			return true;
		else
			return false;
	}

	public void shift(double dX, double dY) {
		this.leftLimit -= dX;
		this.rightLimit -= dX;
		this.topLimit -= dY;
		this.bottomLimit -= dY;
		if (this.data != null) {
			for (int i = 0; i < this.data.size(); i++) {
				if (this.data.get(i) instanceof Segment) {
					Segment seg = (Segment) this.data.get(i);
					seg.x1 -= dX;
					seg.x2 -= dX;
					seg.y1 -= dY;
					seg.y2 -= dY;
				}
			}
		}
		if (this.childNodes != null) {
			for (int i = 0; i < this.childNodes.length; i++) {
				if (this.childNodes[i] != null)
					this.childNodes[i].shift(dX, dY);
			}
		}

	}
}