package org.cheminfo.hook.som;

import java.awt.Color;

import org.cheminfo.hook.math.util.SparseVectorDouble;

public class SparseDoubleSomObject extends SomObject {

	private SparseVectorDouble sparseVector;
	private int dimension;
	final static double LEARNING_SPEED = 0.1;

	public SparseDoubleSomObject() {

	}

	public SparseDoubleSomObject(int dimension) {
		this.dimension = dimension;
	}

	@Override
	public double distanceTo(SomObject object) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Color getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize() {
		this.sparseVector = new SparseVectorDouble(this.dimension);
		for (int i = 0; i < this.dimension; i++)
			this.sparseVector.setIndexValuePair(i, i, Math.random());
	}

	@Override
	public void learn(SomObject object, double distance, double epoch) {
		this.sparseVector.shiftTowards(((SparseDoubleSomObject) object)
				.getSparseVector(), LEARNING_SPEED * distance * epoch);
	}

	public SparseVectorDouble getSparseVector() {
		return sparseVector;
	}

	public void setSparseVector(SparseVectorDouble sparseVector) {
		this.sparseVector = sparseVector;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

}
