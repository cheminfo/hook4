package org.cheminfo.hook.fragments.tests;

import org.cheminfo.hook.math.util.SparseVectorInt;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SparseVectorIntTest {
	SparseVectorInt vectorA;
	SparseVectorInt vectorB;

	@Before
	public void setUp() throws Exception {
		vectorA = new SparseVectorInt(5);
		vectorA.setIndexValuePair(0, 1, 12);
		vectorA.setIndexValuePair(1, 3, 1);
		vectorA.setIndexValuePair(2, 4, 8);
		vectorA.setIndexValuePair(3, 5, 2);
		vectorA.setIndexValuePair(4, 7, 11);
		vectorB = new SparseVectorInt(4);
		vectorB.setIndexValuePair(0, 2, 16);
		vectorB.setIndexValuePair(1, 3, 17);
		vectorB.setIndexValuePair(2, 4, 12);
		vectorB.setIndexValuePair(3, 5, 3);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetSize() {
		Assert.assertTrue("Get size vectorA", vectorA.getSize() == 5);
	}

	@Test
	public void testIndeces() {
		int[] expected = { 1, 3, 4, 5, 7 };
		Assert.assertArrayEquals("Indeces vectorA", expected, vectorA
				.getIndeces());
	}

	@Test
	public void testValues() {
		int[] expected = { 12, 1, 8, 2, 11 };
		Assert.assertArrayEquals("Indeces vectorA", expected, vectorA
				.getValues());
	}

	@Test
	public void testGetCartesianInnerProduct() {
		Assert.assertTrue("Cartesian test", vectorA
				.getCartesianInnerProduct(vectorB) == 119);
	}

	@Test
	public void testGetTanimotoInnerProduct() {
		Assert.assertTrue("Tanimoto test", vectorA
				.getTanimotoInnerProduct(vectorB) == 3);
	}

}
