/**
 * <p></p>
 *
 * <pre>
 * Copyright 1997-2012 Actelion Ltd., Inc.
 * Gewerbestrasse 16
 * CH-4123 Allschwil, Switzerland
 *
 * All Rights Reserved.
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.
 * </pre>
 * @author baerr
 * @version 1.0
 */
package org.cheminfo.hook.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public abstract class Preferences implements Iterable<Preferences.Property>, Serializable{
	private static final long serialVersionUID = 1L;

	public final class Property<T> implements Serializable{
		private static final long serialVersionUID = 1L;
				
		private String group;
		private String label;
		private T defaultValue;
				
		protected Property(String label, T defaultValue) {
			this(label, "General", defaultValue);
		}

		protected Property(String label, String group, T defaultValue) {
			this.label = label;
			this.group = group;
			this.defaultValue = defaultValue;
		}
		
		public String getId(){
			return toString();
		}

		public String getLabel() {
			return label;
		}

		public String getGroup() {
			return group;
		}
		
		public T getDefaultValue() {
			return defaultValue;
		}
		
		@Override
		public int hashCode() {
			return (label + group).hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Property)
				return hashCode()==(((Property)obj).hashCode());
			else
				return false;			
		}
		
		@Override
		public String toString() {
			return group + "/" + label;
		}
	}
	
	private Map<Property, Object> preferences = new LinkedHashMap<Property, Object>();

	protected Preferences() {
	}

	private synchronized void read(Property property) {
		java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
		
		Object o;
		if(property.defaultValue instanceof Integer)
			o = new Integer(prefs.getInt(property.getId(), (Integer)property.defaultValue));
		else if(property.defaultValue instanceof Short)
			o = new Short((short) prefs.getInt(property.getId(), (Short)property.defaultValue));
		else if(property.defaultValue instanceof Long)
			o = new Long(prefs.getLong(property.getId(), (Long)property.defaultValue));
		else if(property.defaultValue instanceof Float)
			o = new Float(prefs.getFloat(property.getId(), (Float)property.defaultValue));
		else if(property.defaultValue instanceof Double)
			o = new Double(prefs.getDouble(property.getId(), (Double)property.defaultValue));
		else if(property.defaultValue instanceof Boolean)
			o = new Boolean(prefs.getBoolean(property.getId(), (Boolean)property.defaultValue));
		else if(property.defaultValue instanceof String)
			o = prefs.get(property.getId(), property.defaultValue.toString());
		else if(property.defaultValue instanceof byte[])
			o = prefs.getByteArray(property.getId(), (byte[])property.defaultValue);
		else if(property.defaultValue instanceof Serializable)
		{
			byte[] objectBytes = prefs.getByteArray(property.getId(), null);
			if(objectBytes == null)
			{
				o = (Serializable)property.defaultValue;
			}
			else
			{				
				ByteArrayInputStream bis = null;
				ObjectInputStream in = null;
				try{
				    // Deserialize from a byte array
					bis = new ByteArrayInputStream(objectBytes);
					in = new ObjectInputStream(bis);
				    o = in.readObject();
				}
				catch(Exception e)
				{
					throw(new RuntimeException("Could not deserialize object for Property '" + property.getId() + "'!"));
				}
				finally
				{
					try{in.close();}catch(Throwable t){}
					try{bis.close();}catch(Throwable t){}
				}
			}
		}
		else
			throw(new RuntimeException("Property with type " + property.defaultValue.getClass() + " is not supported!"));

		preferences.put(property, o);
	}

	private synchronized void write() {
		java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(this.getClass());
		
		for (Entry<Property, Object> entry : preferences.entrySet()) {
			if(entry.getKey().defaultValue instanceof Integer)
				prefs.putInt(entry.getKey().getId(), (Integer) entry.getValue());
			else if(entry.getKey().defaultValue instanceof Short)
				prefs.putInt(entry.getKey().getId(), (Short) entry.getValue());
			else if(entry.getKey().defaultValue instanceof Long)
				prefs.putLong(entry.getKey().getId(), (Long) entry.getValue());
			else if(entry.getKey().defaultValue instanceof Float)
				prefs.putFloat(entry.getKey().getId(), (Float) entry.getValue());
			else if(entry.getKey().defaultValue instanceof Double)
				prefs.putDouble(entry.getKey().getId(), (Double) entry.getValue());
			else if(entry.getKey().defaultValue instanceof Boolean)
				prefs.putBoolean(entry.getKey().getId(), (Boolean) entry.getValue());
			else if(entry.getKey().defaultValue instanceof String)
				prefs.put(entry.getKey().getId(), (String) entry.getValue());
			else if(entry.getKey().defaultValue instanceof byte[])
				prefs.putByteArray(entry.getKey().getId(), (byte[]) entry.getValue());
			else if(entry.getKey().defaultValue instanceof Serializable)
			{
				ByteArrayOutputStream bos = null;
				ObjectOutputStream out = null;
				try{
				    // Serialize to a byte array
				    bos = new ByteArrayOutputStream() ;
				    out = new ObjectOutputStream(bos) ;
				    out.writeObject((Serializable) entry.getValue());				   
	
				    // Get the bytes of the serialized object
				    byte[] objectBytes = bos.toByteArray();

				    prefs.putByteArray(entry.getKey().getId(), objectBytes);
				}
				catch(IOException e)
				{
					throw(new RuntimeException("Could not serialize object " + entry.getValue()));
				}
				finally
				{
					try{out.close();}catch(Throwable t){}
					try{bos.close();}catch(Throwable t){}
				}
			}
			else
				throw(new RuntimeException("Property with type " + entry.getKey().defaultValue.getClass() + " is not supported!"));
		}
	}

	public void save() {
		write();
	}
	
	public <T> T get(Property<T> property)
	{		
		return (T)preferences.get(property);
	}

	public <T> void set(Property<T> property, T value)
	{
		preferences.put(property, value);
	}

	public Iterator<Property> iterator() {
		return preferences.keySet().iterator();
	}
	
	protected <T> Property<T> createProperty(String label, T defaultValue)
	{
		Property<T> property = new Property<T>(label, defaultValue);
		
		if(preferences.keySet().contains(property))
			throw(new RuntimeException("Property " + property + " is already defined!"));
		read(property);
		return property;
	}
	protected <T> Property<T> createProperty(String label, String group, T defaultValue)
	{
		Property<T> property = new Property<T>(label, group, defaultValue);
		
		if(preferences.keySet().contains(property))
			throw(new RuntimeException("Property " + property + " is already defined!"));
		read(property);
		return property;
	}

}
