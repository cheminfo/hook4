package org.cheminfo.hook.util.arrays.tests;

import org.cheminfo.hook.util.arrays.ArraySearch;
import org.junit.Assert;
import org.junit.Test;

public class TestArraySearch {

	@Test
	public void testBinarySearchDouble() {
		double[] data = {1.0,1.5,2.0,2.7,2.8,3.1,3.7};
		int index = ArraySearch.binarySearch(data, 2.1, 1, 4);
		Assert.assertTrue("wrong index returned="+index, index == 3);
	}

	@Test
	public void testBinarySearchDoubleFirstElement() {
		double[] data = {1.0,1.5,2.0,2.7,2.8,3.1,3.7};
		int index = ArraySearch.binarySearch(data, 0.5, 0, data.length);
		Assert.assertTrue("wrong index returned="+index, index == 0);
	}

	@Test
	public void testBinarySearchDoubleAfterLastElement() {
		double[] data = {1.0,1.5,2.0,2.7,2.8,3.1,3.7};
		int index = ArraySearch.binarySearch(data, 8, 0, data.length);
		Assert.assertTrue("wrong index returned="+index, index == 7);
	}
	
	@Test
	public void testBinarySearchDoubleLastElement() {
		double[] data = {1.0,1.5,2.0,2.7,2.8,3.1,3.7};
		int index = ArraySearch.binarySearch(data, 3.6, 0, data.length);
		Assert.assertTrue("wrong index returned="+index, index == 6);
	}


	@Test
	public void testBinarySearchDoubleZeroRange() {
		double[] data = {1.0,1.5,2.0,2.7,2.8,3.1,3.7};
		int offset = 3; 
		int index = ArraySearch.binarySearch(data, -3.0, offset, 0);
		Assert.assertTrue("wrong index returned="+index, index == offset);
	}


	@Test
	public void testBinarySearchLong() {
		long[] data = {10,15,20,27,28,31,37};
		int index = ArraySearch.binarySearch(data, 21, 1, 4);
		Assert.assertTrue("wrong index returned="+index, index == 3);
	}

	@Test
	public void testBinarySearchLongFirstElement() {
		long[] data = {10,15,20,27,28,31,37};
		int index = ArraySearch.binarySearch(data, 5, 0, data.length);
		Assert.assertTrue("wrong index returned="+index, index == 0);
	}

	@Test
	public void testBinarySearchLongAfterLastElement() {
		long[] data = {10,15,20,27,28,31,37};
		int index = ArraySearch.binarySearch(data, 100, 0, data.length);
		Assert.assertTrue("wrong index returned="+index, index == 7);
	}
	
	@Test
	public void testBinarySearchLongLastElement() {
		long[] data = {10,15,20,27,28,31,37};
		int index = ArraySearch.binarySearch(data, 36, 0, data.length);
		Assert.assertTrue("wrong index returned="+index, index == 6);
	}

	@Test
	public void testBinarySearchLongZeroRange() {
		long[] data = {10,15,20,27,28,31,37};
		int offset = 3; 
		int index = ArraySearch.binarySearch(data, -3, offset, 0);
		Assert.assertTrue("wrong index returned="+index, index == offset);
	}

	
}
