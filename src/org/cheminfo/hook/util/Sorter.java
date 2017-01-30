package org.cheminfo.hook.util;


public class Sorter
{
	private static void QuickSort(double a[], Object[] data, int l, int r) throws Exception
	{
		int M = 4;
		int i;
		int j;
		double v;
	
		if ((r-l)>M)
		{
			i = (r+l)/2;
			if (a[l]>a[i]) swap(a, data,l,i);	// Tri-Median Methode!
			if (a[l]>a[r]) swap(a, data,l,r);
			if (a[i]>a[r]) swap(a, data,i,r);
		
			j = r-1;
			swap(a, data,i,j);
			i = l;
			v = a[j];
			for(;;)
			{
				while(a[++i]<v);
				while(a[--j]>v);
				if (j<i) break;
				swap (a, data,i,j);
			}
			swap(a, data,i,r-1);
			QuickSort(a, data,l,j);
			QuickSort(a, data,i+1,r);
		}
	}

	private static void swap(double a[], Object[] data, int i, int j)
	{
		double T;
		Object tempObject;
		T = a[i];
		tempObject=data[i];
		a[i] = a[j];
		data[i]=data[j];
		a[j] = T;
		data[j]=tempObject;
	}

	private static void InsertionSort(double a[], Object[] data, int lo0, int hi0) throws Exception
	{
		int i;
		int j;
		double v;
		Object tempObject;
		
		for (i=lo0+1;i<=hi0;i++)
		{
			v = a[i];
			tempObject=data[i];
			j=i;
			while ((j>lo0) && (a[j-1]>v))
			{
				a[j] = a[j-1];
				data[j]=data[j-1];
				j--;
			}
			a[j] = v;
			data[j]=tempObject;
		}
	}

	public static void sort(double a[], Object[] data) throws Exception
	{
		QuickSort(a, data, 0, a.length - 1);
		InsertionSort(a, data, 0,a.length-1);
	}
	    

	private static void QuickSort(int a[], Object[] data, int l, int r) throws Exception
	{
		int M = 4;
		int i;
		int j;
		double v;
	
		if ((r-l)>M)
		{
			i = (r+l)/2;
			if (a[l]>a[i]) swap(a, data,l,i);	// Tri-Median Methode!
			if (a[l]>a[r]) swap(a, data,l,r);
			if (a[i]>a[r]) swap(a, data,i,r);
		
			j = r-1;
			swap(a, data,i,j);
			i = l;
			v = a[j];
			for(;;)
			{
				while(a[++i]<v);
				while(a[--j]>v);
				if (j<i) break;
				swap (a, data,i,j);
			}
			swap(a, data,i,r-1);
			QuickSort(a, data,l,j);
			QuickSort(a, data,i+1,r);
		}
	}

	private static void swap(int a[], Object[] data, int i, int j)
	{
		int T;
		Object tempObject;
		T = a[i];
		tempObject=data[i];
		a[i] = a[j];
		data[i]=data[j];
		a[j] = T;
		data[j]=tempObject;
	}

	private static void InsertionSort(int a[], Object[] data, int lo0, int hi0) throws Exception
	{
		int i;
		int j;
		int v;
		Object tempObject;
		
		for (i=lo0+1;i<=hi0;i++)
		{
			v = a[i];
			tempObject=data[i];
			j=i;
			while ((j>lo0) && (a[j-1]>v))
			{
				a[j] = a[j-1];
				data[j]=data[j-1];
				j--;
			}
			a[j] = v;
			data[j]=tempObject;
		}
	}

	public static void sort(int a[], Object[] data) throws Exception
	{
		QuickSort(a, data, 0, a.length - 1);
		InsertionSort(a, data, 0,a.length-1);
	}
	
	
	//------------------Comparable as key--------------------------------------
	private static void QuickSort(Comparable a[], Object[] data, int l, int r) throws Exception
	{
		int M = 4;
		int i;
		int j;
		Comparable v;
	
		if ((r-l)>M)
		{
			i = (r+l)/2;
			if (Sorter.compare(a[l], a[i]) > 0) swap(a, data,l,i);	// Tri-Median Method
			if (Sorter.compare(a[l], a[r]) > 0) swap(a, data,l,r);
			if (Sorter.compare(a[i], a[r]) > 0) swap(a, data,i,r);
		
			j = r-1;
			swap(a, data,i,j);
			i = l;
			v = a[j];
			for(;;)
			{
				while(Sorter.compare(a[++i], v) < 0);
				while(Sorter.compare(a[--j], v) > 0);
				if (j<i) break;
				swap (a, data,i,j);
			}
			swap(a, data,i,r-1);
			QuickSort(a, data,l,j);
			QuickSort(a, data,i+1,r);
		}
	}

	private static void swap(Comparable a[], Object[] data, int i, int j)
	{
		Comparable T;
		Object tempObject;
		T = a[i];
		tempObject=data[i];
		a[i] = a[j];
		data[i]=data[j];
		a[j] = T;
		data[j]=tempObject;
	}

	private static void InsertionSort(Comparable a[], Object[] data, int lo0, int hi0) throws Exception
	{
		int i;
		int j;
		Comparable v;
		Object tempObject;
		
		for (i=lo0+1;i<=hi0;i++)
		{
			v = a[i];
			tempObject=data[i];
			j=i;
			while ((j>lo0) && (Sorter.compare(a[j-1], v) > 0) )
			{
				a[j] = a[j-1];
				data[j]=data[j-1];
				j--;
			}
			a[j] = v;
			data[j]=tempObject;
		}
	}

	public static void sort(Comparable a[], Object[] data) throws Exception
	{
		QuickSort(a, data, 0, a.length - 1);
		InsertionSort(a, data, 0,a.length-1);
	}
	
	public static int compare(Comparable a, Comparable b)
	{
		if (a == null && b == null) return 0;
		
		if (a == null) return -1;
		
		if (b == null) return 1;
		
		return a.compareTo(b);
	}
}
