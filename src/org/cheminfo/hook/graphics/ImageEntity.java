package org.cheminfo.hook.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Hashtable;

import org.apache.commons.codec.binary.Base64;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.util.XMLCoDec;

public class ImageEntity extends BasicEntity {

	protected Image image;
	
	public ImageEntity(Image image) {
		setImage(image);
	}

	public ImageEntity(String XMLString, Hashtable helpers) {
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();

		setLocation(tempCodec.getParameterAsDouble("x"), tempCodec.getParameterAsDouble("y"));

		int width = tempCodec.getParameterAsInt("width");
		int height = tempCodec.getParameterAsInt("height");
		String dataString = tempCodec.getParameterAsString("data");
		
		byte[] dataBytes = Base64.decodeBase64(dataString);
		
        //Pad the size to multiple of 4
        int size = (dataBytes.length / 4) + ((dataBytes.length % 4 == 0) ? 0 : 1); 
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(size * 4);        
        byteBuffer.put(dataBytes);
        byteBuffer.rewind();
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        
        int[] data = new int[size];
        
        intBuffer.get(data);
        
		setImage(getImageFromArray(data, width, height));

//		EntityResizer eResizer=new EntityResizer(EntityResizer.SE_RESIZER);
//		this.addEntity(eResizer);
//		eResizer.checkSizeAndPosition();
	}
	
	public void setImage(Image image) {
		this.image = image;
		setSize(image.getWidth(null), image.getHeight(null));
		
		super.checkSizeAndPosition();
        this.refreshSensitiveArea();
	}
	
	public Image getImage() {
		return image;
	}

	@Override
	public String getOverMessage() {
		return getClass().getSimpleName();
	}
	
	@Override
	public void paint(Graphics2D g) {		
		if ((this.isSelected()) || (this.isMouseover()))
		{
			g.setColor(Color.black);
			g.draw(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
		}

		g.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
		super.paint(g);
	}

	@Override
	public String getXmlTag(Hashtable xmlProperties) {
		String tempTag="";
		XMLCoDec tempCodec=new XMLCoDec();

	    try {
	    	int width = image.getWidth(null);
	    	int height = image.getHeight(null);	    			
	    	int[] data = getArrayFromImage(image, width, height);
	
	        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);        
	        IntBuffer intBuffer = byteBuffer.asIntBuffer();
	        intBuffer.put(data);

	        byte[] dataBytes = byteBuffer.array();
	        byte[] dataEncoded = Base64.encodeBase64Chunked(dataBytes);
	        String dataString = new String(dataEncoded);
	    	
	    	tempCodec.addParameter("width", width);
			tempCodec.addParameter("height", height);
			tempCodec.addParameter("data", dataString);
	    	tempCodec.addParameter("x", getLocation().x);
	    	tempCodec.addParameter("y", getLocation().y);
			
			tempTag+="<graphics.ImageEntity ";
			tempTag+=tempCodec.encodeParameters();
			tempTag+=">\r\n";
			
			tempTag+="</graphics.ImageEntity>\r\n";
			return tempTag;

	    } catch (InterruptedException e) {
			throw(new RuntimeException(e));
		}
	}
	 
	private int[] getArrayFromImage(Image img, int width, int height) throws InterruptedException {
		int[] pixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0,	width);
		pg.grabPixels();
		return pixels;
	}

	private Image getImageFromArray(int[] pixels, int width, int height) {
		MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0,	width);
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.createImage(mis);
	}
}
