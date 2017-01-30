package com.actelion.research.nemo.jcamp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Jcamp implements Iterable<Ldr<?>>{
	protected List<Ldr<?>> ldrs;
	protected String lineEnding = System.getProperty("line.separator"); 

	public Jcamp(URL jcamp) throws IOException
	{
		Reader reader = null;
		InputStream is = null;		
		try {
			is = jcamp.openStream();		
			reader = new InputStreamReader(is);
			parse(reader);
		} finally {
			try{reader.close();}catch (Exception e){}
			try{is.close();}catch (Exception e){}
		}
	}
	
	public Jcamp(File jcamp) throws IOException
	{
		Reader reader = null;
		try {
			reader = new FileReader(jcamp);
			parse(reader);
		} finally {
			try{reader.close();}catch (Exception e){}
		}
	}
	
	public Jcamp(String jcamp) throws IOException
	{
		Reader reader = null;
		try {
			reader = new StringReader(jcamp);
			parse(reader);
		} finally {
			try{reader.close();}catch (Exception e){}
		}
	}

	public Jcamp(Reader jcamp) throws IOException
	{
		parse(jcamp);
	}

	protected void parse(Reader reader) throws IOException
	{
		ldrs = new ArrayList<Ldr<?>>();
		lineEnding = null;
		BufferedReader bufferedReader = null;
		
		try {
			bufferedReader = new BufferedReader(reader);
			
			bufferedReader.mark(1000);
			
			char c = (char)bufferedReader.read();
			while(c>=0 && lineEnding==null)
			{
				if(c=='\r')
				{					
					c = (char)bufferedReader.read();
					if(c=='\n')
						lineEnding = "\r\n";
					else
						lineEnding = "\r";
				}
				else if(c=='\n')
				{
					lineEnding = "\n";
				}					
				c = (char)bufferedReader.read();
			}
			bufferedReader.reset();
			

			String line = null;
			StringBuilder raw = new StringBuilder();
			while( (line = bufferedReader.readLine()) != null)
			{
				if(line.trim().startsWith("##") && !raw.toString().trim().equals(""))
				{
					Ldr<?> ldr = LdrFactory.createFromRaw(StringUtils.strip(raw.toString(), "\n\r"));
					ldrs.add(ldr);
					raw = new StringBuilder();
				}
				raw.append(line + lineEnding);
			}
			if(!raw.toString().trim().equals(""))
			{
				Ldr<?> ldr = LdrFactory.createFromRaw(raw.toString().trim());
				ldrs.add(ldr);
			}
		} catch (IOException e) {
			throw(e);
		} finally {
//			try{bufferedReader.close();}catch (Exception e){}
		}
	}

	/**
	 * Gets the first occurrence of the specified Ldr from this list, if it is present.
	 * @param label Label of Ldr to get
	 * @return Ldr with corresponding label of null when not present.
	 */
	public Ldr<?> get(Label label) {
		for(Ldr<?> ldr : ldrs)
			if(ldr.getLabel().equals(label))
				return ldr;
		
		return null;
	}
	
	public <T extends Ldr<?>> T add(Ldr<?> after, T ldr) {
		int index = ldrs.indexOf(after) + 1;
		
		ldrs.add(index, ldr);
		
		return ldr;
	}

	/**
	 * Removes the first occurrence of the specified Ldr from this list, if it is present.
	 * @param ldr Ldr to be removed from this list, if present
	 * @return true when ldr is found and removed, false otherwise
	 */
	public boolean remove(Ldr<?> ldr) {
		return ldrs.remove(ldr);
	}

	/**
	 * Saves changes.
	 * @throws IOException
	 */
	public void save(File jcamp) throws IOException
	{
		Writer writer = null;
		try{
			writer = new FileWriter(jcamp, false);
			save(writer);
		} finally {
			try{writer.close();}catch(Exception e){}
		}
	}
	
	/**
	 * Saves changes.
	 * @throws IOException
	 */
	public void save(Writer writer) throws IOException
	{
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(writer);

			for (Ldr<?> ldr : ldrs) {
				bufferedWriter.write(ldr.getRaw() + lineEnding);
			}
			bufferedWriter.flush();
		} finally {
//			try{writer.close();}catch(Exception e){}
		}
	}

	public String getContents() {
		StringBuilder sb = new StringBuilder();
		for (Ldr<?> ldr : ldrs) {
			sb.append(ldr.getRaw() + lineEnding);
		}
		return sb.toString();
	}
	
	public Iterator<Ldr<?>> iterator() {		
		return ldrs.iterator();
	}	
}
