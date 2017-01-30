package org.cheminfo.hook.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Vector;



public class Image2DTileManager
{
	Vector elementProviders;
	Vector tiles;
	int hTileNb, vTileNb;
//	BufferedImage masterImage;
	Color primaryColor, secondaryColor;
	float fll, frl, ftl, fbl;
	float imageWidth, imageHeight;
	float currentTileWidthU, currentTileHeightU;
	float currentLeftLimit, currentRightLimit, currentTopLimit, currentBottomLimit;
	final int factor=1;
	final static boolean DEBUG=false;
	
	boolean running;
	
	/**
	 * Constructor: Creates all the necessary structures and draws the master image.
	 * @param width width of the Spectra entity. 
	 * @param height height of the Spectra entity.
	 * @param foLeftLimit fullout left limit of the space in units.
	 * @param foRightLimit fullout right limit of the space in units.
	 * @param foTopLimit fullout top limit of the space in units.
	 * @param foBottomLimit fullout bottom limit of the space in units.
	 * @param primaryColor
	 * @param secondaryColor
	 * @param hResolution nb of horizontal tiles for the returned image.
	 * @param vResolution nb of vertical tiles for the returned image.
	 */
	public Image2DTileManager(Vector elementProviders, float width, float height, float foLeftLimit, float foRightLimit, float foTopLimit, float foBottomLimit, Color primaryColor, Color secondaryColor, int hResolution, int vResolution)
	{
		this.elementProviders=elementProviders;
		if (this.elementProviders.size() == 0) System.out.println("no Element Providers");
		
		this.tiles=new Vector();
		this.hTileNb=hResolution;
		this.vTileNb=vResolution;
		
		this.fll=foLeftLimit;
		this.frl=foRightLimit;
		this.ftl=foTopLimit;
		this.fbl=foBottomLimit;
		
		this.imageWidth=factor*width;
		this.imageHeight=factor*height;
		
		this.currentTileWidthU=0;
		this.currentTileHeightU=0;
		
		this.primaryColor=primaryColor;
		this.secondaryColor=secondaryColor;
		
//		this.masterImage=new BufferedImage((int)imageWidth, (int)imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
	}
	
	public void forceRedraw()
	{
		this.update(currentLeftLimit, currentRightLimit, currentTopLimit, currentBottomLimit, true);
	}
	
	private Image2DTile createTile(float tileLL, float tileRL, float tileTL, float tileBL, float tileWidth, float tileHeight)
	{
		Image2DTile tile=new Image2DTile(tileLL, tileRL, tileTL, tileBL);
		
		BufferedImage tileImage=new BufferedImage((int)Math.ceil(tileWidth),(int)Math.ceil(tileHeight), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g=(Graphics2D)tileImage.createGraphics();
		
		if (DEBUG)
		{
			g.setColor(Color.blue);
		
			g.drawRect(0,0,(int)tileWidth, (int)tileHeight);
			g.drawLine(0,0, (int)tileWidth, (int)tileHeight);
			g.drawLine(0,(int)tileHeight, (int)tileWidth, 0);
		}

		Vector tempVector=new Vector();
		QuadTreeElement tempElement;
		
		for (int provider=0; provider < this.elementProviders.size(); provider++)
		{
			ElementProvider tempProvider = (ElementProvider)this.elementProviders.elementAt(provider);
			
			tempVector.clear();
			tempProvider.getElements(tileLL, tileRL, tileTL, tileBL, tempVector);
			
			for (int element=0; element < tempVector.size(); element++)
			{
				tempElement=(QuadTreeElement)tempVector.elementAt(element);
				tempElement.paint(g, tileLL, tileRL, tileTL, tileBL, tileWidth, tileHeight);
			}
		}
		
		tile.setImage(tileImage);
		
		return tile;
	}
	
	private void update(float leftLimit, float rightLimit, float topLimit, float bottomLimit, boolean forceUpdate)
	{
		/* this method creates (hTileNb+1)*(vTileNb+1) sample points in the active field
		 * and checks if the tiles vector contains a tile for each of them and if the tile dimensions have changed. If one or more sample points don't
		 * it orders a new tile that aligns to the others from the Creator.
		*/
		this.currentLeftLimit=leftLimit;
		this.currentRightLimit=rightLimit;
		this.currentTopLimit=topLimit;
		this.currentBottomLimit=bottomLimit;
		
		float tileWidthU=(leftLimit-rightLimit)/this.hTileNb;
		float tileHeightU=(topLimit-bottomLimit)/this.vTileNb;
		
		if (forceUpdate || Math.abs(tileWidthU-this.currentTileWidthU) > Math.abs(this.currentTileWidthU)*0.01 || Math.abs(tileHeightU-this.currentTileHeightU) > Math.abs(this.currentTileHeightU)*0.01)
		{
			float firstXOrigin=(float)(this.fll+Math.ceil((leftLimit-this.fll)/tileWidthU)*tileWidthU);
			float firstYOrigin=(float)(this.ftl+Math.ceil((topLimit-this.ftl)/tileHeightU)*tileHeightU);
			
			// window resize or 2D zoom -> create all the tiles from scratch
			this.tiles.clear();
			
			this.currentTileHeightU=tileHeightU;
			this.currentTileWidthU=tileWidthU;
			
			for (int hsample=0; hsample < this.hTileNb+1; hsample++)
			{
				for (int vsample=0; vsample < this.vTileNb+1; vsample++)
				{
					this.tiles.add(this.createTile(firstXOrigin-hsample*tileWidthU, firstXOrigin-(hsample+1)*tileWidthU, firstYOrigin-vsample*tileHeightU, firstYOrigin-(vsample+1)*tileHeightU, this.imageWidth/(this.hTileNb), this.imageHeight/(this.vTileNb)));
				}
			}
		}
		else
		{
			// check sample points
			Image2DTile currentTile;
			boolean found;
			for (int chX=0; chX < this.hTileNb+1; chX++)
			{
				for (int chY=0; chY < this.vTileNb+1; chY++)
				{
					found=false;
					for (int t=0; t < this.tiles.size(); t++)
					{
						currentTile=(Image2DTile)this.tiles.elementAt(t);
						if (currentTile.contains(leftLimit-chX*tileWidthU, topLimit-chY*tileHeightU))
						{
							found=true;
							break;
						}
					}
					
					if (!found)
					{
						float xOrigin=(float)(this.fll+Math.ceil((leftLimit-chX*tileWidthU-this.fll)/tileWidthU)*tileWidthU);
						float yOrigin=(float)(this.ftl+Math.ceil((topLimit-chY*tileHeightU-this.ftl)/tileHeightU)*tileHeightU);
						
						this.tiles.add(this.createTile(xOrigin, xOrigin-tileWidthU, yOrigin, yOrigin-tileHeightU, this.imageWidth/(this.hTileNb), this.imageHeight/(this.vTileNb)));
					}
				}
			}
		}
		
	}
	
	public BufferedImage getImage(float leftLimit, float rightLimit, float topLimit, float bottomLimit, float desiredWidth, float desiredHeight)
	{
//		System.out.println("generating image");
		boolean forceRedraw=false;
		if ( (Math.abs(desiredWidth-this.imageWidth) > Math.abs(this.imageWidth)*0.01) || (Math.abs(desiredHeight-this.imageHeight) > Math.abs(this.imageHeight)*0.01) )
		{
			this.imageWidth=desiredWidth;
			this.imageHeight=desiredHeight;
			forceRedraw=true;
		}
		this.update(leftLimit, rightLimit, topLimit, bottomLimit, forceRedraw);
		BufferedImage returnImage=new BufferedImage((int)desiredWidth, (int)desiredHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D tempGraphics=(Graphics2D)returnImage.createGraphics();

		Image2DTile currentTile=null;

		float tileWidth=(leftLimit-rightLimit)/this.hTileNb;
		float tileHeight=(topLimit-bottomLimit)/this.vTileNb;
		
		AffineTransform settingTransform=new AffineTransform();
		
//		System.out.println("tiles: "+this.tiles.size());
		// check control points
		
		Vector tilesDrawn=new Vector();
		
		for (int chX=0; chX < this.hTileNb+1; chX++)
		{
			for (int chY=0; chY < this.vTileNb+1; chY++)
			{
//				System.out.println("looking for "+(leftLimit-chX*tileWidth)+", "+(topLimit-chY*tileHeight));
				for (int t=0; t < this.tiles.size(); t++)
				{
					currentTile=(Image2DTile)this.tiles.elementAt(t);

//					System.out.println("Testing tile "+t+": "+currentTile.getLeftLimit()+", "+currentTile.getTopLimit());

					if (currentTile.contains(leftLimit-chX*tileWidth, topLimit-chY*tileHeight))
					{
//						System.out.println("FOUND");
						settingTransform=AffineTransform.getTranslateInstance(((leftLimit-currentTile.getLeftLimit())*desiredWidth/(leftLimit-rightLimit)), ((topLimit-currentTile.getTopLimit())*desiredHeight/(topLimit-bottomLimit)));
						
						if ( !tilesDrawn.contains(currentTile) )
						{
//							System.out.println("ADDED");
							tilesDrawn.add(currentTile);
							tempGraphics.drawImage(currentTile.getImage(), settingTransform, null);
						}
//						break;
					}
				}
			}
		}
		
		return returnImage;
	}

	public void reset()
	{
		this.elementProviders.clear();
		this.tiles.clear();
	}
	
}