package org.cheminfo.hook.util;

import java.awt.image.BufferedImage;

public class Image2DTile
{
	private BufferedImage image;
	private double leftLimit, rightLimit, topLimit, bottomLimit;
	private long lastUsed;
	
	public Image2DTile(double leftLimit, double rightLimit, double topLimit, double bottomLimit)
	{
		this.leftLimit=leftLimit;
		this.rightLimit=rightLimit;
		this.topLimit=topLimit;
		this.bottomLimit=bottomLimit;
	}
	
	public void setImage(BufferedImage image)
	{
		this.image=image;
	}
	
	public BufferedImage getImage()
	{
		return this.image;
	}
	
	public void setLastUsed(long systemTime)
	{
		this.lastUsed=systemTime;
	}
	
	public long getLastUsed()
	{
		return this.lastUsed;
	}
	
	public double getLeftLimit()
	{
		return this.leftLimit;
	}
	
	public double getTopLimit()
	{
		return this.topLimit;
	}
	
	/**
	 * Returns whether this tile contains the provided point.
	 * @param x horizontal coordinate of the point in units.
	 * @param y vertical coordinate of the point in units.
	 * @return
	 */
	public boolean contains(double x, double y)
	{
		if (x >= Math.min(this.leftLimit, this.rightLimit) && x <= Math.max(this.leftLimit, this.rightLimit) &&
				y >= Math.min(this.topLimit, this.bottomLimit) && y <= Math.max(this.topLimit, this.bottomLimit)	)
		{
			return true;
		}
		else
			return false;
	}
}