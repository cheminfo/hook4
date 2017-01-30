package org.cheminfo.hook.nemo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Vector;

import org.cheminfo.hook.util.Image2DTile;
import org.cheminfo.hook.util.Segment;

public class NMR2DTileManager implements Runnable
{
	private volatile Thread thisThread=null;

	Spectra spectra;
	Vector posContourLines, negContourLines;
	Vector<Image2DTile> tiles;
	int hTileNb, vTileNb;
	BufferedImage masterImage;
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
	 * @param spectra original Spectra entity.
	 * @param posContourLines Vector containing the positive contour lines.
	 * @param negContourLines Vector containing the negative contour lines.
	 * @param width width of the Spectra entity. 
	 * @param height height of the Spectra entity.
	 * @param foLeftLimit fullout left limit of the Spectra in units.
	 * @param foRightLimit fullout right limit of the Spectra in units.
	 * @param foTopLimit fullout top limit of the Spectra in units.
	 * @param foBottomLimit fullout bottom limit of the Spectra in units.
	 * @param primaryColor Color for the positive contour lines.
	 * @param secondaryColor Color for the negative contour lines
	 * @param hResolution nb of horizontal tiles for the returned image.
	 * @param vResolution nb of vertical tiles for the returned image.
	 */
	public NMR2DTileManager(Spectra spectra, Vector posContourLines, Vector negContourLines, float width, float height, float foLeftLimit, float foRightLimit, float foTopLimit, float foBottomLimit, Color primaryColor, Color secondaryColor, int hResolution, int vResolution)
	{
		this.spectra=spectra;
		this.posContourLines=posContourLines;
		if(DEBUG)
			if (this.posContourLines.size() == 0) System.out.println("no positive contourLines");
		this.negContourLines=negContourLines;
		
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
		
		//this.masterImage=new BufferedImage((int)imageWidth, (int)imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
	}
	
	public void forceRedraw()
	{
		this.update(currentLeftLimit, currentRightLimit, currentTopLimit, currentBottomLimit, true);
	}
	
	private Image2DTile createTile(float tileLL, float tileRL, float tileTL, float tileBL, float lowerContourline, float tileWidth, float tileHeight)
	{
		if (DEBUG) System.out.println("Creating tile");
		
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

		Vector<Segment> tempVector=new Vector<Segment>();
		
		for (int line=(int)lowerContourline; line < posContourLines.size(); line++)
		{
			
			ContourLine tempLine = (ContourLine)posContourLines.elementAt(line);

			tempVector.clear();
			tempLine.getSegments(tileLL, tileRL, tileTL, tileBL, tempVector);

			if (this.spectra.getPrimaryColor() != null)
			{
				g.setColor(this.spectra.getPrimaryColor());
				
				for (Segment tempSegment : tempVector)
				{
//					g.draw(new Line2D.Double( (tileLL-tempSegment.getP1x())*tileWidth/(tileLL-tileRL), (tileTL-tempSegment.getP1y())*tileHeight/(tileTL-tileBL), (tileLL-tempSegment.getP2x())*tileWidth/(tileLL-tileRL), (tileTL-tempSegment.getP2y())*tileHeight/(tileTL-tileBL)));
					tempSegment.paint(g, tileLL, tileRL, tileTL, tileBL, tileWidth, tileHeight);
				}
			}
			
			if (negContourLines.size() > line)
			{
				tempLine=(ContourLine)negContourLines.elementAt(line);

				tempVector.clear();
				tempLine.getSegments(tileLL, tileRL, tileTL, tileBL, tempVector);
				
				if (this.spectra.getSecondaryColor() != null)
				{
					g.setColor(this.spectra.getSecondaryColor());
					for (Segment tempSegment : tempVector)
					{
//						g.draw(new Line2D.Double( (tileLL-tempSegment.getP1x())*tileWidth/(tileLL-tileRL), (tileTL-tempSegment.getP1y())*tileHeight/(tileTL-tileBL), (tileLL-tempSegment.getP2x())*tileWidth/(tileLL-tileRL), (tileTL-tempSegment.getP2y())*tileHeight/(tileTL-tileBL)));
						tempSegment.paint(g, tileLL, tileRL, tileTL, tileBL, tileWidth, tileHeight);
					}
				}
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
		float coordX, coordY;
		
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
					this.tiles.add(this.createTile(firstXOrigin-hsample*tileWidthU, firstXOrigin-(hsample+1)*tileWidthU, firstYOrigin-vsample*tileHeightU, firstYOrigin-(vsample+1)*tileHeightU, (float)this.spectra.getLowerContourline(), (float)this.spectra.getWidth()/(this.hTileNb), (float)this.spectra.getHeight()/(this.vTileNb)));
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
						
						this.tiles.add(this.createTile(xOrigin, xOrigin-tileWidthU, yOrigin, yOrigin-tileHeightU, (float)this.spectra.getLowerContourline(), (float)(this.spectra.getWidth()/(this.hTileNb)), (float)(this.spectra.getHeight()/(this.vTileNb))));
					}
				}
			}
		}
		
	}
	
	public BufferedImage getImage(float leftLimit, float rightLimit, float topLimit, float bottomLimit)
	{		
		// the real question is if we need to regenerate the image
		// if the limits didn't change we should just do nothing ...
	
		boolean forceRedraw=false;
		if (Math.abs(spectra.getWidth()-this.imageWidth) > Math.abs(this.imageWidth)*0.01 || Math.abs(spectra.getHeight()-this.imageHeight) > Math.abs(this.imageHeight)*0.01)
		{
			this.imageWidth=(float)spectra.getWidth();
			this.imageHeight=(float)spectra.getHeight();
			forceRedraw=true;
		}

		if ((this.currentLeftLimit!=leftLimit) || (this.currentRightLimit!=rightLimit) ||
				(this.currentTopLimit!=topLimit) || (this.currentBottomLimit!=bottomLimit) ||
				(forceRedraw) || spectra.resetNeedsRepaint()) {
		
			
			this.update(leftLimit, rightLimit, topLimit, bottomLimit, forceRedraw);
			masterImage=new BufferedImage((int)spectra.getWidth(), (int)spectra.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D tempGraphics=(Graphics2D)masterImage.createGraphics();
	
			Image2DTile currentTile=null;
	
			float tileWidth=(leftLimit-rightLimit)/this.hTileNb;
			float tileHeight=(topLimit-bottomLimit)/this.vTileNb;
			
			// check control points and construct the image
			for (int chX=0; chX < this.hTileNb+1; chX++)
			{
				for (int chY=0; chY < this.vTileNb+1; chY++)
				{
					for (int t=0; t < this.tiles.size(); t++)
					{
						currentTile=(Image2DTile)this.tiles.elementAt(t);
						if (currentTile.contains(leftLimit-chX*tileWidth, topLimit-chY*tileHeight))
						{
							tempGraphics.drawImage( currentTile.getImage(), (int)((leftLimit-currentTile.getLeftLimit())*spectra.getWidth()/(leftLimit-rightLimit)), (int)((topLimit-currentTile.getTopLimit())*spectra.getHeight()/(topLimit-bottomLimit)), null); 
							break;
						}
					}
				}
			}
		}
		return masterImage;
	}

	public void start()
	{
		if (this.thisThread == null)
		{
			this.thisThread = new Thread(this, "NMR2DTileManager");
			this.thisThread.start();
		}
	}
	
	public void run()
	{
		while (this.thisThread == Thread.currentThread())
		{
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException iex) {}
		}
	}
}