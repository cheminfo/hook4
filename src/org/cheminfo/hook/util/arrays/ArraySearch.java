package org.cheminfo.hook.util.arrays;

public class ArraySearch {

	/**
	 * 
	 * Performs a binary search within the area data. The data in the search
	 * range must be sorted in ascending order. The algorithm returns the
	 * theoretical insertion point for the token token
	 * 
	 * @param data
	 *            the array to be searched
	 * @param token
	 *            the search token
	 * @param offset
	 *            the offset to start the search
	 * @param length
	 *            the number of elements
	 * @return
	 */
	public static int binarySearch(double[] data, double token, int offset,
			int length) {
		int lower = offset;
		int upper = offset + length;
		while (upper > lower) {
			int middle = (upper + lower) / 2;
			if (token > data[middle]) {
				lower = middle + 1;
			} else {
				upper = middle;
			}
		}
		return upper;
	}

	/**
	 * 
	 * Performs a binary search within the area data. The data in the search
	 * range must be sorted in ascending order. The algorithm returns the
	 * theoretical insertion point for the token token
	 * 
	 * @param data
	 *            the array to be searched
	 * @param token
	 *            the search token
	 * @param offset
	 *            the offset to start the search
	 * @param length
	 *            the number of elements
	 * @return
	 */
	public static int binarySearch(long[] data, long token, int offset,
			int length) {
		int lower = offset;
		int upper = offset + length;
		while (upper > lower) {
			int middle = (upper + lower) / 2;
			if (token > data[middle]) {
				lower = middle + 1;
			} else {
				upper = middle;
			}
		}
		return upper;
	}

}
