package org.cheminfo.hook.util;

import java.awt.Graphics2D;

public interface QuadTreeElement
{
	public boolean isContained(float queryX1, float queryX2, float queryY1, float queryY2);
	
	public void paint(Graphics2D g, float tileLeft, float tileRight, float tileTop, float tileBottom, float tileWidth, float tileHeight);
}