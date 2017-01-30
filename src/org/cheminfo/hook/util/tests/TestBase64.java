package org.cheminfo.hook.util.tests;

import java.io.IOException;
import java.util.Arrays;

import org.cheminfo.hook.util.Base64;



public class TestBase64 {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		String[] tests={"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21",
		                	"A short test",
		                	"utf8 èàé°ê",
		                	"這兩個字是甚麼"};
		
		
		
		for (String test : tests) {
			System.out.println("Test:               "+test);
			String base64=Base64.encodeBytes(test.getBytes());
			System.out.println("Base 64:            "+base64);
			String compressedBase64=Base64.encodeBytes(test.getBytes(), Base64.GZIP);
			System.out.println("Compressed Base 64: "+compressedBase64);
			System.out.println("Decode:             "+new String(Base64.decode(base64)));
			System.out.println("Decode compressed:  "+new String(Base64.decode(compressedBase64)));
		}
		
		double[] x={123,12,1234,2,1,2,3,4,56,6,2,3,4,6,7,8,1,3,2};
		System.out.println(Arrays.toString(x));
		String test=Base64.encodeObject(x);
		System.out.println(test);
		double[] y=(double[])Base64.decodeToObject(test);
		System.out.println(Arrays.toString(x));
	}
	
}
