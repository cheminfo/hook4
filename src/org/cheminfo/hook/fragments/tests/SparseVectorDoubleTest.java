package org.cheminfo.hook.fragments.tests;

import org.cheminfo.hook.math.util.SparseVectorDouble;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SparseVectorDoubleTest {
	SparseVectorDouble vectorA;
	SparseVectorDouble vectorB;

	@Before
	public void setUp() throws Exception {
		vectorA = new SparseVectorDouble(5);
		vectorA.setIndexValuePair(0, 1, 12.0);
		vectorA.setIndexValuePair(1, 3, 1.0);
		vectorA.setIndexValuePair(2, 4, 8.0);
		vectorA.setIndexValuePair(3, 5, 2.0);
		vectorA.setIndexValuePair(4, 7, 11.0);
		vectorB = new SparseVectorDouble(4);
		vectorB.setIndexValuePair(0, 2, 16.0);
		vectorB.setIndexValuePair(1, 3, 17.0);
		vectorB.setIndexValuePair(2, 4, 12.0);
		vectorB.setIndexValuePair(3, 5, 3.0);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIndeces() {
		int[] expected = { 1, 3, 4, 5, 7 };
		Assert.assertArrayEquals("Indeces vectorA", expected, vectorA
				.getIndeces());
	}

	@Test
	public void testGetSize() {
		Assert.assertTrue("Get size vectorA", vectorA.getSize() == 5);
	}

	@Test
	public void testGetCartesianInnerProduct() {
		Assert.assertTrue("Cartesian test", (int)Math.round(vectorA
				.getCartesianInnerProduct(vectorB)) == 119);
	}

	@Test
	public void testGetTanimotoInnerProduct() {
		Assert.assertTrue("Tanimoto test", vectorA
				.getTanimotoInnerProduct(vectorB) == 3);
	}

}
