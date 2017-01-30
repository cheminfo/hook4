package org.cheminfo.hook.framework;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Vector;

public class StackLayout implements LayoutManager, java.io.Serializable
{
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	
	private Vector components;
	private Dimension preferredSize;
	private int stackDirection;
	private int hGap, vGap;
	private boolean fixedSize;
	
	public StackLayout()
	{
		this(StackLayout.HORIZONTAL, 0, 0);
	}
	
	public StackLayout(int stackDirection)
	{
		this(stackDirection, 0, 0);
	}
	
	public StackLayout(int stackDirection, int hGap, int vGap)
	{
		this.components = new Vector();
		this.preferredSize = new Dimension(0,0);
		this.hGap=hGap;
		this.vGap=vGap;
		this.stackDirection=stackDirection;
		this.fixedSize=false;
	}

	public void setPreferredSize(int width, int height)
	{
		fixedSize=true;
		
		this.preferredSize=new Dimension(width, height);
	}
		
	public void addLayoutComponent(String name, Component comp)
	{
		this.components.addElement(comp);
	}
	
	public void removeLayoutComponent(Component comp)
	{
		if (this.components.contains(comp))	this.components.removeElement(comp);
	}
	
	public Dimension minimumLayoutSize(Container parent)
	{
		return parent.size();
	}
	
	public Dimension preferredLayoutSize(Container parent)
	{
		if (!fixedSize)
		{
			int lastXPos=0;
			int lastYPos=0;
			int maxHeight=0;
			int maxWidth=0;
			Component currentComponent=null;
			if (this.stackDirection == this.HORIZONTAL)
			{
				for (int comp=0; comp < parent.getComponentCount(); comp++)
				{
					currentComponent=(Component)parent.getComponent(comp);
					lastXPos+=this.hGap+currentComponent.getPreferredSize().width;
					if (currentComponent.getPreferredSize().height > maxHeight) maxHeight=currentComponent.getPreferredSize().height;
				}
				this.preferredSize=new Dimension(lastXPos, maxHeight);
			}
			else 
			{
				for (int comp=0; comp < parent.getComponentCount(); comp++)
				{
					currentComponent=(Component)parent.getComponent(comp);
					lastYPos+=this.vGap+currentComponent.getPreferredSize().height;
					if (currentComponent.getPreferredSize().width > maxWidth) maxWidth=currentComponent.getPreferredSize().width;
				}
				this.preferredSize=new Dimension(maxWidth, lastYPos);
			}
		}
		
		return this.preferredSize;
	}
	
	public void layoutContainer(Container parent)
	{
		int lastXPos=0;
		int lastYPos=0;
		int maxHeight=0;
		int maxWidth=0;
		Component currentComponent=null;
		if (this.stackDirection == this.HORIZONTAL)
		{
			for (int comp=0; comp < parent.getComponentCount(); comp++)
			{
				currentComponent=(Component)parent.getComponent(comp);
				currentComponent.setLocation(lastXPos, lastYPos);
				lastXPos+=this.hGap+currentComponent.getPreferredSize().width;
				if (currentComponent.getPreferredSize().height > maxHeight) maxHeight=currentComponent.getPreferredSize().height;
			}
		}
		else 
		{
			for (int comp=0; comp < parent.getComponentCount(); comp++)
			{
				currentComponent=(Component)parent.getComponent(comp);
				currentComponent.setLocation(lastXPos, lastYPos);
				lastYPos+=this.vGap+currentComponent.getPreferredSize().height;
				if (currentComponent.getPreferredSize().width > maxWidth) maxWidth=currentComponent.getPreferredSize().width;
			}
		}

//		parent.setSize(parent.getPreferredSize());
	}
	
	public int getHGap()
	{
		return this.hGap;
	}
	
	public int getVGap()
	{
		return this.vGap;
	}
}
		