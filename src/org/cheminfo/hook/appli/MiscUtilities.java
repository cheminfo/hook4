package org.cheminfo.hook.appli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MiscUtilities {
	
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
    	return ext;
	}
	
	/** Converts a value between 0 to 675 in a 2 letter code AA ... ZZ
	 */
	
	public static String int2char(int value) {
		int b1=value%26;
		int b2=(value-b1)/26;
		byte bytes[]=new byte[2];
		bytes[1]=(new Integer(b1+65)).byteValue();
		bytes[0]=(new Integer(b2+65)).byteValue();
		String char2=new String(bytes);
		return char2;
	}	
	
	
	public static int char2int(String string) {
		byte bytes[]=string.getBytes();
		return bytes[0]*26+bytes[1];
	}

	public static void zipFile (File inputFile, File zipFile, String entryName) {
	
		try {
		    ZipOutputStream tempZipStream = new ZipOutputStream(new FileOutputStream(zipFile));
			byte[] buffer = new byte[1024];
			int bytesRead;
			FileInputStream file = new FileInputStream(inputFile);
			ZipEntry zipEntry = new ZipEntry(entryName);
			zipEntry.setCompressedSize(100);
			tempZipStream.putNextEntry(zipEntry);
			while ((bytesRead = file.read(buffer)) != -1) {
			   tempZipStream.write(buffer, 0, bytesRead);
			}
			file.close();
			tempZipStream.close();
		} catch (Exception e) {System.out.println (e.toString());}
	}




	
}