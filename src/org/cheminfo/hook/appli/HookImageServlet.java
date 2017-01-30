package org.cheminfo.hook.appli;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.w3c.dom.DOMImplementation;

import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This class allows to generate a static picture from the current content of an
 * applet. The problem is that if it is the result of a post and the picture is
 * copied from internet explorer you will not be able to print it ! On the other
 * hand if you make a GET you are limited to something like 1023 characters
 * which is not compatible with the XML description of the spectrum. Therefore
 * we need to make a redirect and a cache. Various parameters are allowed. One of the more difficult
 * is "options" that currently allows the following parameter : - print 
 */

public class HookImageServlet extends HttpServlet {
//	SpectraDisplay mainDisplay;

	final static int PNG = 0;
	final static int JPG = 1;
	final static int SVG = 2;
	final static int PDF = 3;

	private static Cache cache;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		if (cache == null)
			cache = new Cache();

		// put awt in headless mode (for server without X11)
		System.setProperty("java.awt.headless", "true");
		
		String servletRelativeURL = request.getServletPath().replaceAll("^.*/",
				"");

		String serverName = request.getParameter("serverName");
		String stringImageID = request.getParameter("imageID");
		String bigFonts = request.getParameter("bigFonts");
		
		if (stringImageID != null)
			stringImageID = stringImageID.replaceAll("[^0-9-]", "");

		if (serverName != null) {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println("<html><head></head><body>");
			out.println(getCode(serverName));
			out.println("</body></html>");
			out.close();
		} else if (stringImageID == null) {
			ImageParameter imageParameter = new ImageParameter(request);

			// we generate a unique ID for this picture
			Long imageID = new Long(new Random().nextLong());

			// we will add this instance to our static Cache vector
			cache.put(imageID, imageParameter);

			if (imageParameter.imageFormat == PDF) {
				response.sendRedirect(servletRelativeURL + "?imageID="
						+ imageID + "."
						+ getFormatExtension(imageParameter.imageFormat));
				return;
			}

			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println("<html><head></head><body>");

			int width, height;
			if ((imageParameter.rotate % 2) == 0) {
				height = imageParameter.height * 72 / imageParameter.resolution;
				width = imageParameter.width * 72 / imageParameter.resolution;
			} else {
				width = imageParameter.height * 72 / imageParameter.resolution;
				height = imageParameter.width * 72 / imageParameter.resolution;
			}

			if (imageParameter.imageFormat == SVG) {
				out.println("<embed src=\"" + servletRelativeURL + "?imageID="
						+ imageID + "."
						+ getFormatExtension(imageParameter.imageFormat)
						+ "\" height=\"" + height + "px\" width=\"" + width
						+ "px\" type=\"image/svg+xml\">");
				// } else if (imageParameter.imageFormat==PDF) {
				// out.println("<embed
				// src=\"org.cheminfo.hook.appli.HookServlet?imageID="+imageID+"."+getFormatExtension(imageParameter.imageFormat)+"\"
				// height=\""+height+"px\" width=\""+width+"px\"
				// type=\"application/pdf\">");
			} else {
				out.println("<img src=\"" + servletRelativeURL + "?imageID="
						+ imageID + "."
						+ getFormatExtension(imageParameter.imageFormat)
						+ "\" height=" + height + " width=" + width + ">");
			}
			
			String options = request.getParameter("options");

			if ((imageParameter.XMLString != null) && (!imageParameter.XMLString.equals("")))
			{
				InteractiveSurface interactions = new InteractiveSurface();
				interactions.setSize(imageParameter.width, imageParameter.height);
				
				if (bigFonts != null && bigFonts.toLowerCase().trim().equals("big"))
				{
					interactions.useBigFonts(true);
				}
				
				interactions.setXML(imageParameter.XMLString);
				interactions.isLegal=true;
				interactions.setDoubleBuffered(false);
				interactions.addNotify();
				interactions.validate();
			}

			if ((options != null) && (options.toLowerCase().indexOf("print") >= 0))
			{
				out.println("<script>setTimeout('self.print()',1000);</script>");
			}

			out.println("</body></html>");
			out.close();
		} else {

			ImageParameter imageParameter = null;
			Long imageID = null;
			try {
				imageID = Long.decode(stringImageID);
			} catch (Exception e) {
			}
			if (imageID != null) {
				// System.out.println(imageID);
				// System.out.println(cache.get(imageID));
				imageParameter = (ImageParameter) cache.get(imageID);
				// System.out.println(imageParameter);
			}

			if (imageParameter != null) {

				InteractiveSurface interactions = new InteractiveSurface();
				interactions.setSize(imageParameter.width, imageParameter.height);
				if (imageParameter.stringBigFonts != null && imageParameter.stringBigFonts.toLowerCase().trim().equals("big"))
					interactions.useBigFonts(true);
				interactions.setXML(imageParameter.XMLString);
				interactions.grabFocus();
				interactions.isLegal=true;
				
				
				sendAsImage(interactions, response, imageParameter.width, imageParameter.height, imageParameter.imageFormat, imageParameter.rotate);

			} else {
				PrintWriter out = response.getWriter();
				response.setContentType("text/text");
				out.println("error, image not in cache");
				out.close();
			}

		}
	}

	private String getFormatExtension(int format) {
		if (format == PNG)
			return "png";
		if (format == JPG)
			return "jpg";
		if (format == SVG)
			return "svg";
		if (format == PDF)
			return "pdf";
		return "";
	}

	public void sendAsImage(InteractiveSurface interactions,
			HttpServletResponse response, int width, int height,
			int imageFormat, int rotate) throws IOException {
		if ((imageFormat == PNG) || (imageFormat == JPG)) {

			BufferedImage tempImage = new BufferedImage(width, height,
					BufferedImage.TYPE_3BYTE_BGR);
			Graphics tempGraphics = tempImage.getGraphics();

			interactions.addNotify();
			interactions.validate();

			interactions.paintSB(tempGraphics);
			tempImage = rotate(tempImage, rotate);

			OutputStream out = response.getOutputStream();

			if (imageFormat == PNG) {
				response.setContentType("image/png");
				try {
					ImageIO.write(tempImage, "png", out);
				} catch (IOException e) {
					System.out.println(e.toString());
				}
			} else if (imageFormat == JPG) {
				response.setContentType("image/jpeg");
				try {
					ImageIO.write(tempImage, "jpg", out);
				} catch (IOException e) {
					System.out.println(e.toString());
				}
			}
			out.close();
		} else if (imageFormat == SVG) {
			DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
			// Create an instance of org.w3c.dom.Document
			org.w3c.dom.Document document = domImpl.createDocument(null, "svg",	null);
			// Create an instance of the SVG Generator
			SVGGraphics2D tempGraphics = new SVGGraphics2D(document);

			interactions.addNotify();
			interactions.validate();

			interactions.paintSB(tempGraphics);

			try {
				Writer writer = response.getWriter();
				response.setContentType("image/svg+xml");
				// Writer writer = new OutputStreamWriter(System.out, "UTF-8");
				tempGraphics.stream(writer, false);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		} else if (imageFormat == PDF) {
			try {
				com.lowagie.text.Document document;
				if (rotate == 1) {
					document = new com.lowagie.text.Document(PageSize.A4
							.rotate(), 50, 50, 50, 50);
				} else {
					document = new com.lowagie.text.Document(PageSize.A4, 50,
							50, 50, 50);
				}
				PdfWriter writer = PdfWriter.getInstance(document, response
						.getOutputStream());
				document.open();
				response.setContentType("application/pdf");

				PdfContentByte cb = writer.getDirectContent();
				PdfTemplate tp = cb.createTemplate(width, height);
				Graphics2D g2 = tp.createGraphics(width, height);

				interactions.addNotify();
				interactions.validate();
				interactions.paintSB(g2);

				g2.dispose();
				cb.addTemplate(tp, 10, 0);
				document.close();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}

	public BufferedImage rotate(BufferedImage image, double angle) {

		int width;
		int height;
		int width1;
		int height1;

		width = image.getWidth();
		height = image.getHeight();

		if (angle % 2 == 0) {
			width1 = width;
			height1 = height;
		} else {
			width1 = height;
			height1 = width;
		}

		AffineTransform transform = AffineTransform.getTranslateInstance(
				width / 2, height / 2);
		if (angle != 0) {
			transform.rotate(angle * Math.PI / 2);
		}

		transform.translate(-width1 / 2 - (angle == 3 ? width - width1 : 0),
				-height1 / 2 - (angle == 1 ? height - height1 : 0));

		AffineTransformOp op = new AffineTransformOp(transform,
				new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,
						RenderingHints.VALUE_COLOR_RENDER_QUALITY));

		BufferedImage newImage = new BufferedImage(width1, height1,
				BufferedImage.TYPE_3BYTE_BGR);
		op.filter(image, newImage);
		return newImage;

		/*
		 * AffineTransform trans = new AffineTransform(); int imgWidth =
		 * image.getWidth(); int imgHeight = image.getHeight();
		 * 
		 * trans.rotate( Math.toRadians(angle), imgWidth / 2, imgHeight / 2 );
		 * 
		 * AffineTransformOp op = new AffineTransformOp(trans, new
		 * RenderingHints(RenderingHints.KEY_COLOR_RENDERING,
		 * RenderingHints.VALUE_COLOR_RENDER_QUALITY));
		 * 
		 * System.out.println(imgHeight+" - "+ imgWidth);
		 * 
		 * BufferedImage retval = new BufferedImage(imgWidth, imgHeight,
		 * BufferedImage.TYPE_3BYTE_BGR); op.filter(image, retval); return
		 * retval;
		 */
	}

	private long getCode(String hostName) {
		try {
			long code2 = (long) (Math.pow((double) 13, (double) 11) - 113);
			long code = InetAddress.getByName(hostName).hashCode();
			long theCode = Math.abs((code * 223 + 49843) ^ (code2));
			return theCode;
		} catch (UnknownHostException e) {
			return 0;
		}
	}


	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}

	class Cache extends LinkedHashMap {

		private static final int MAX_ENTRIES = 100;

		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > MAX_ENTRIES;
		}
	}

	class ImageParameter {
		String url = "";
		String XMLString = "";
		String jcamp = "";
		int width = 1200;
		int height = 800;
		int resolution = 150;
		int imageFormat = PNG;
		int rotate = 0; // 1: 90�

		String stringBigFonts="";
		
		ImageParameter(HttpServletRequest request) {
			url = request.getParameter("url");
			XMLString = request.getParameter("xmlString");
			jcamp = request.getParameter("jcamp");
			String stringHeight = request.getParameter("height");
			String stringWidth = request.getParameter("width");
			String stringResolution = request.getParameter("resolution");
			String stringImageFormat = request.getParameter("format");
			stringBigFonts = request.getParameter("bigFonts");
			String stringRotate = request.getParameter("rotate");
			try {
				if (stringImageFormat.toLowerCase().equals("png")) {
					imageFormat = PNG;
				} else if (stringImageFormat.toLowerCase().equals("svg")) {
					imageFormat = SVG;
				} else if (stringImageFormat.toLowerCase().equals("pdf")) {
					imageFormat = PDF;
				} else {
					imageFormat = JPG;
				}
			} catch (Exception e) {
				imageFormat = JPG;
			}
			try {
				height = Integer.parseInt(stringHeight);
			} catch (NumberFormatException e) {
			}
			try {
				width = Integer.parseInt(stringWidth);
			} catch (NumberFormatException e) {
			}
			try {
				resolution = Integer.parseInt(stringResolution);
			} catch (NumberFormatException e) {
			}
			try {
				rotate = Integer.parseInt(stringRotate);
			} catch (NumberFormatException e) {
			}

		}

		public String toString() {
			String toReturn = "";
			toReturn += "URL: " + url + "\r\n";
			toReturn += "width: " + width + "\r\n";
			toReturn += "height: " + height + "\r\n";
			toReturn += "resolution: " + resolution + "\r\n";
			toReturn += "jcamp: " + jcamp + "\r\n";
			toReturn += "imageFormat: " + imageFormat + "\r\n";
			toReturn += "XMLString: " + XMLString + "\r\n";
			toReturn += "URL: " + url + "\r\n";
			return toReturn;
		}
	}
}
