package org.cheminfo.hook.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Vector;

public class FlexiDataConverter
{
	public FlexiDataPoint[] convert(URL url, Vector descriptions)
	{
		BufferedReader inputFile;
		
		try	{
			InputStreamReader stream=new InputStreamReader(url.openStream());

			inputFile=new BufferedReader(stream);
		} catch (Exception e) 
			{
				System.out.println (e.toString()+", cause: url: "+url);
				return null; 
			}
		
		return convert(inputFile, descriptions);
	}
	
	public FlexiDataPoint[] convert(BufferedReader inputFile, Vector descriptions)
	{
		String currentLine=null;
		Class[] constructorClasses={String.class};
		String[] constructorParams=new String[1];

		try {
			currentLine=inputFile.readLine();
		} catch (IOException e) {currentLine=null;}

		TabTokenizer lineTokenizer;
		FlexiDataDescription description=null;
		Vector<String> tokens;
		
		int[] types;
		Class[] dataTypes=null;
		Constructor[] constructors=null;
		boolean[] visibilities;
		boolean[] encoded=null;
		Object[] data;
		
		int counter=1;
		Vector<FlexiDataPoint> dataPoints = new Vector<FlexiDataPoint>();
		
		while (currentLine!=null)
		{
//			System.out.println("line: "+counter);
			lineTokenizer=new TabTokenizer(currentLine, "\t");

			// first token defines the line type
			String firstToken=lineTokenizer.nextToken();
			tokens=new Vector<String>();

			if (firstToken.compareTo("%columnName") == 0)
			{
				while (lineTokenizer.hasMoreTokens())
				{
					tokens.add(lineTokenizer.nextToken());
				}
				
				description=new FlexiDataDescription(tokens.size());
				
				description.setFieldNames((String[])tokens.toArray(new String[tokens.size()]));
				
				descriptions.add(description);
			}
			else if (firstToken.compareTo("%dbColumnName") == 0)
			{
				while (lineTokenizer.hasMoreTokens())
				{
					tokens.add(lineTokenizer.nextToken());
				}
				if (description == null)	// this should never happend
				{
					description=new FlexiDataDescription(tokens.size());
				}
				
				description.setDbFieldNames( (String[])tokens.toArray(new String[tokens.size()]) );
			}
			else if (firstToken.compareTo("%mimetype") == 0)
			{
				while (lineTokenizer.hasMoreTokens())
				{
					tokens.add(lineTokenizer.nextToken());
				}
				if (description == null)	// this should never happend
					description=new FlexiDataDescription(tokens.size());
				
				description.setMimetypes( (String[])tokens.toArray(new String[tokens.size()]) );
			}
			else if (firstToken.compareTo("%base64encoded") == 0)
			{
				while (lineTokenizer.hasMoreTokens())
				{
					tokens.add(lineTokenizer.nextToken());
				}

				encoded=new boolean[description.getNbFields()];
				
				for (int token=0; token < tokens.size(); token++)
				{
					if ( (tokens.get(token)).toLowerCase().compareTo("true") == 0)
						encoded[token]=true;
					else
						encoded[token]=false;
				}
			}
			else if (firstToken.compareTo("%datatype") == 0)
			{
				while (lineTokenizer.hasMoreTokens())
				{
					tokens.add(lineTokenizer.nextToken());
				}

				dataTypes=new Class[description.getNbFields()];
				constructors=new Constructor[description.getNbFields()];
				
				try {
					for (int token=0; token < tokens.size(); token++)
					{
						if (tokens.get(token).contains("."))
							dataTypes[token]=Class.forName(tokens.get(token));
						else
							dataTypes[token]=Class.forName("java.lang."+tokens.get(token));
						constructors[token]=dataTypes[token].getConstructor(constructorClasses);
					}
				} catch (ClassNotFoundException cnfe) {System.out.println(cnfe);}
				catch (NoSuchMethodException ex5) {System.out.println("FlexiDataConverter data constructor e: "+ex5);ex5.printStackTrace();};
				
				description.setDataTypes(dataTypes);
			}
			else if (firstToken.compareTo("%type") == 0)
			{
				while (lineTokenizer.hasMoreTokens())
				{
					tokens.add(lineTokenizer.nextToken());
				}

				types=new int[description.getNbFields()];
				
				for (int token=0; token < tokens.size(); token++)
				{
					if ( (tokens.get(token)).compareTo("barcode") == 0)
						types[token]=FlexiDataDescription.TYPE_BARCODE;
					else if ( (tokens.get(token)).compareTo("col") == 0)
						types[token]=FlexiDataDescription.TYPE_COLUMN;
					else if ( (tokens.get(token)).compareTo("row") == 0)
						types[token]=FlexiDataDescription.TYPE_ROW;
					else if ( (tokens.get(token)).compareTo("containerType") == 0)
						types[token]=FlexiDataDescription.TYPE_CONTAINER_TYPE;
					else if ( (tokens.get(token)).compareTo("measure") == 0)
						types[token]=FlexiDataDescription.TYPE_MEASURE;
					else if ( (tokens.get(token)).compareTo("recordID") == 0)
						types[token]=FlexiDataDescription.TYPE_RECORD_ID;
					else if ( (tokens.get(token)).compareTo("id") == 0)
						types[token]=FlexiDataDescription.TYPE_ID;
					else if ( (tokens.get(token)).compareTo("value") == 0)
						types[token]=FlexiDataDescription.TYPE_VALUE;
					else
						types[token]=FlexiDataDescription.TYPE_GENERIC;
				}
				
				description.setTypes(types);
			}
			else if (firstToken.compareTo("%showAttribute") == 0)
			{
				while (lineTokenizer.hasMoreTokens())
				{
					tokens.add(lineTokenizer.nextToken());
				}

				visibilities=new boolean[description.getNbFields()];
				
				for (int token=0; token < tokens.size(); token++)
				{
					if ( (tokens.get(token)).toLowerCase().compareTo("false") == 0)
						visibilities[token]=false;
					else
						visibilities[token]=true;
				}
				
				description.setFieldVisibility(visibilities);
				
			}
			else if (firstToken.compareTo("%data") == 0)
			{
				dataPoints.add(new FlexiDataPoint(description));

				while (lineTokenizer.hasMoreTokens())
				{
					tokens.add(lineTokenizer.nextToken());
				}

				data=new Object[description.getNbFields()];

				for (int token=0; token < tokens.size(); token++)
				{
					if (encoded != null && encoded[token] == true) {
						try {
							tokens.set(token, new String(Base64.decode(tokens.get(token))));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					constructorParams[0]=tokens.get(token);
					
					if (constructorParams[0].compareTo("null") == 0)
						data[token]=null;
					else
					{
						try {
							data[token]=constructors[token].newInstance( constructorParams );
						}	catch (IllegalAccessException ex2) {System.out.println("FlexiDataConverter data constructor e: \n");ex2.printStackTrace();}
							catch (InstantiationException ex3) {System.out.println("FlexiDataConverter data constructor e: \n");ex3.printStackTrace();}
							catch (InvocationTargetException ex4) {
									System.out.println("FlexiDataConverter data constructor "+token+": "+tokens.get(token)+" e:\n");
									System.out.println(description);
									ex4.printStackTrace();
								}
							catch (NullPointerException e){ /*System.out.println("line "+counter+" token: "+token+" -> "+constructors[token]);*/ e.printStackTrace();break;}
					}
				}
				
				((FlexiDataPoint)dataPoints.lastElement()).setData(data);
			}
			
			counter++;
			try {
				currentLine=inputFile.readLine();
			} catch (IOException e) {currentLine=null;}
		}
		
		try {
			inputFile.close();
		} catch (IOException e) {e.printStackTrace();}


		return (FlexiDataPoint[])dataPoints.toArray(new FlexiDataPoint[dataPoints.size()]);
	}
}