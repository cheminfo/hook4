package org.cheminfo.hook.appli;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;
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
import org.cheminfo.hook.converter.Converter;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.HTMLPeakTableFormater;
import org.cheminfo.hook.nemo.Spectra;
import org.cheminfo.hook.nemo.SpectraData;
import org.cheminfo.hook.nemo.SpectraDisplay;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
 * we need to make a redirect and a cache. The servlet will keep in memory the
 * last XXX spectrum. Various parameters are allowed. One of the more difficult
 * is "options" that currently allows the following parameter : - print - htmlString -
 * nmrTable
 */

public class HookServlet extends HttpServlet {
	final static boolean DEBUG = false;
	final static int PNG = 0;
	final static int JPG = 1;
	final static int SVG = 2;
	final static int PDF = 3;
	final static int JSON = 4;

	private static Cache cache;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (cache == null) cache = new Cache();

	
		String servletRelativeURL = request.getServletPath().replaceAll("^.*/", "");

		String serverName = request.getParameter("serverName");
		String stringImageID = request.getParameter("imageID");
		String noRedirectString = request.getParameter("noRedirect");
		// should we redirect the embeded image ? This allows to print without problem but
		// gives some trouble to fetch the data ...
		boolean noRedirect=Boolean.valueOf(noRedirectString);

		if (DEBUG) System.out.println("HookServlet: noRedirectString: "+noRedirectString);
		if (DEBUG) System.out.println("HookServlet: noRedirect: "+noRedirect);
		if (DEBUG) System.out.println("HookServlet: stringImageID: "+stringImageID);

		SpectraDisplay virtualDisplay=null;
		
		if (stringImageID != null) {
			stringImageID = stringImageID.replaceAll("[^0-9-]", "");
		}
		if (serverName != null) {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println("<html><head></head><body>");
			out.println(getCode(serverName));
			out.println("</body></html>");
			out.close();
		} else if (stringImageID==null && !noRedirect) {
			ImageParameter imageParameter = new ImageParameter(request);

			if (imageParameter.imageFormat==JSON) {
				PrintWriter out = response.getWriter();
				response.setContentType("text/text");
				
				virtualDisplay=getSpectraDisplay(request, imageParameter);
				try {
				JSONObject json=new JSONObject();
				JSONArray jsonSpectraDisplay=new JSONArray();
				json.put("spectraDisplay", jsonSpectraDisplay);
				JSONObject jsonMainDisplay=new JSONObject();
				jsonSpectraDisplay.put(jsonMainDisplay);
				jsonMainDisplay.put("label","mainDisplay");
				virtualDisplay.appendJSON(jsonMainDisplay);
				out.println(json.toString());
				} catch (JSONException e) {
					e.printStackTrace(out);
				}
				out.close();
			} else {
		
			
				// we generate a unique ID for this picture
				Long imageID = new Long(new Random().nextLong());
	
				// we will add this instance to our static Cache vector
				cache.put(imageID, imageParameter);
	
				if (imageParameter.imageFormat == PDF) {
					response.sendRedirect(servletRelativeURL + "?imageID=" + imageID + "." + getFormatExtension(imageParameter.imageFormat));
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
					out.println("<img src=\"" + servletRelativeURL + "?imageID=" + imageID + "."
							+ getFormatExtension(imageParameter.imageFormat)
							+ "\" height=" + height + " width=" + width + ">");
				}
	
				String options = request.getParameter("options");
	
				// should we put the peak picking or the in-line assignment ?
				if ((imageParameter.XMLString != null) && (!imageParameter.XMLString.equals(""))) {
					
					virtualDisplay=getSpectraDisplay(request, imageParameter);					
					
					if ((options != null) && (options.toLowerCase().indexOf("nmrtable") >= 0)) {
						if (virtualDisplay.getFirstSpectra().getSpectraData().getDataType() == SpectraData.TYPE_NMR_SPECTRUM)
							out.println("<pre>" + HTMLPeakTableFormater.getNmrTable(virtualDisplay.getFirstSpectra(), true, true)+"</pre>");
					}
	
					if ((options != null) && (options.toLowerCase().indexOf("htmlstring") >= 0)) {
						out.println("<p>"+HTMLPeakTableFormater.getPeakHtml(virtualDisplay.getFirstSpectra(), null, false)+"</p>");
					}
				}

	
				if ((options != null) && (options.toLowerCase().indexOf("print") >= 0)) {
					out.println("<script>setTimeout('self.print()',1000);</script>");
				}
	
				out.println("</body></html>");
				out.close();
			}
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
			} else if (noRedirect) {
				imageParameter = new ImageParameter(request);
			}

			if (imageParameter != null) {
				virtualDisplay=getSpectraDisplay(request, imageParameter);
				
				if (DEBUG) System.out.println("HookServlet: XMLString: "+imageParameter.XMLString);
				
				sendDisplayAsImage(virtualDisplay, response,
						imageParameter.width, imageParameter.height,
						imageParameter.imageFormat,
						imageParameter.rotate);
				

			} else {
				PrintWriter out = response.getWriter();
				response.setContentType("text/text");
				out.println("error, image not in cache");
				out.close();
			}

		}
	}

	private SpectraDisplay getSpectraDisplay(HttpServletRequest request, ImageParameter imageParameter) {		
		InteractiveSurface interactions = new InteractiveSurface();
		interactions.setSize(imageParameter.width, imageParameter.height);
		SpectraDisplay virtualDisplay = new SpectraDisplay();
		interactions.addEntity(virtualDisplay);
		if ((imageParameter.XMLString != null) && (!imageParameter.XMLString.equals(""))) {
			Hashtable helpers = new Hashtable();
			virtualDisplay.init(imageParameter.XMLString, imageParameter.width, imageParameter.height, helpers);
			virtualDisplay.checkInteractiveSurface();
			virtualDisplay.getInteractiveSurface().isLegal=true;
			virtualDisplay.restoreLinks();
		} else if (imageParameter.url!=null && imageParameter.url.length()>0) {
			virtualDisplay.init();
			virtualDisplay.getInteractiveSurface().isLegal=true;
			addJcamp(request, imageParameter, virtualDisplay);
		}

		return virtualDisplay;
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

	public void sendDisplayAsImage(SpectraDisplay mainDisplay,
			HttpServletResponse response, int width, int height,
			int imageFormat, int rotate) throws IOException {
		if ((imageFormat == PNG) || (imageFormat == JPG)) {

			BufferedImage tempImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			Graphics tempGraphics = tempImage.getGraphics();

			mainDisplay.getInteractiveSurface().addNotify();
			mainDisplay.setPrimaryColor(MyTransparency.createTransparentColor(mainDisplay.getPrimaryColor()));
			mainDisplay.getInteractiveSurface().paint(tempGraphics);
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
			org.w3c.dom.Document document = domImpl.createDocument(null, "svg",
					null);
			// Create an instance of the SVG Generator
			SVGGraphics2D tempGraphics = new SVGGraphics2D(document);

			mainDisplay.getInteractiveSurface().addNotify();
			mainDisplay.setPrimaryColor(MyTransparency.createTransparentColor(mainDisplay.getPrimaryColor()));
			mainDisplay.getInteractiveSurface().paintSB(tempGraphics);

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
					document = new com.lowagie.text.Document(PageSize.A4.rotate(), 50, 50, 50, 50);
				} else {
					document = new com.lowagie.text.Document(PageSize.A4, 50, 50, 50, 50);
				}
				PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
				document.open();
				response.setContentType("application/pdf");

				PdfContentByte cb = writer.getDirectContent();
				PdfTemplate tp = cb.createTemplate(width, height);
				Graphics2D g2 = tp.createGraphics(width, height);

				mainDisplay.getInteractiveSurface().addNotify();
				mainDisplay.setPrimaryColor(MyTransparency.createTransparentColor(mainDisplay.getPrimaryColor()));
				mainDisplay.getInteractiveSurface().paintSB(g2);

				g2.dispose();
				cb.addTemplate(tp, 10, 0);
				document.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
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

	public boolean addJcamp(HttpServletRequest request, ImageParameter imageParameter, SpectraDisplay virtualDisplay) {
		String stringUrl=imageParameter.url;
	
		if (DEBUG) System.out.println("URL String: "+stringUrl);

		Converter tempConverter = Converter.getConverter("Jcamp");
		SpectraData spectraData = new SpectraData();

		boolean convertResult=false;
		URL correctUrl = null;
		if (stringUrl.indexOf("##")>-1) {
			if (DEBUG) System.out.println("IT IS A JCAMP");
			convertResult=tempConverter.convert(new BufferedReader(new StringReader(stringUrl)), spectraData);
		} else {
			if (DEBUG) System.out.println("IT IS A URL");
			// we need the URL
			// we first try to convert it directly
			try {
				correctUrl = new URL(stringUrl);
			} catch (MalformedURLException e) {
				try {
					URL originURL = new URL(request.getRequestURL().toString());
					correctUrl = new URL(originURL.getProtocol(), originURL.getHost(),stringUrl);
				} catch (MalformedURLException er) {
					System.out.println("Not ffound : " + stringUrl);
					return false;
				}
			}
			System.out.println("Loading from " + String.valueOf(correctUrl));
			convertResult=tempConverter.convert(correctUrl, spectraData);
		}
		
		if (convertResult) {
			spectraData.setActiveElement(0);
			// if (spectraData.getDataType()==SpectraData.TYPE_NMR_SPECTRUM)
			// spectraData.convertHzToPPM();
			spectraData.prepareSpectraData();
			spectraData.updateDefaults();
			
			if (spectraData.getDefaults().needsVScale) {
				if (DEBUG) System.out.println("Needs V Scale");
				virtualDisplay.hasVScale(true);
			}
			if (spectraData.getDefaults().needsHScale) {
				if (DEBUG) System.out.println("Needs H Scale");
				virtualDisplay.hasHScale(true);
			}
			if (spectraData.getDefaults().absoluteYScale) {
				if (DEBUG) System.out.println("Absolute V Scale");
				virtualDisplay.setAbsoluteYScale(true);
			}
			
			Spectra spectra = new Spectra(spectraData);
			spectra.setLocation(spectra.getLocation().x, -10);
			virtualDisplay.addSpectra(spectra);


			
			spectra.refreshSensitiveArea();
			spectra.refreshSensitiveArea();
			if (DEBUG) System.out.println("Spectrum loaded");
			
		} else {
			System.out.println("Could Not Access File " + correctUrl);
			return false;
		}
		return convertResult;
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
		String xmlUrl="";
		String XMLString = "";
		String jcamp = "";
		int width = 1200;
		int height = 800;
		int resolution = 150;
		int imageFormat = PNG;
		int rotate = 0; // 1: 90�

		ImageParameter(HttpServletRequest request) {
			url = request.getParameter("url");
			XMLString = request.getParameter("xmlString");
			xmlUrl = request.getParameter("xmlUrl");
			if ((xmlUrl!=null) && (xmlUrl.length()>0)) {
				getXmlFromURL(request, xmlUrl);
			}
			jcamp = request.getParameter("jcamp");
			String stringHeight = request.getParameter("height");
			String stringWidth = request.getParameter("width");
			String stringResolution = request.getParameter("resolution");
			String stringImageFormat = request.getParameter("format");
			String stringRotate = request.getParameter("rotate");
			try {
				if (stringImageFormat.toLowerCase().equals("png")) {
					imageFormat = PNG;
				} else if (stringImageFormat.toLowerCase().equals("svg")) {
					imageFormat = SVG;
				} else if (stringImageFormat.toLowerCase().equals("pdf")) {
					imageFormat = PDF;
				} else if (stringImageFormat.toLowerCase().equals("json")) {
					imageFormat = JSON;
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
			if (XMLString==null) XMLString="";

		}

		private void getXmlFromURL(HttpServletRequest request, String url) {
			String sessionID=request.getSession().getId();
			if (DEBUG) System.out.println("SessionID: "+sessionID);
			if (DEBUG) System.out.println("URL String: "+url);
			URL u=null;
			URL originURL=null;
			HttpURLConnection urlc = null; 
			try { 
				u = new URL (url); 
			} catch (MalformedURLException mfurle) {
				try {
					originURL=new URL(request.getRequestURL().toString());
					if (DEBUG) System.out.println("Origin URL: "+originURL.toString());
					u=new URL(originURL.getProtocol(),originURL.getHost(), url);
					if (DEBUG) System.out.println("New URL: "+u.toString());
				} catch (MalformedURLException mfurle2) {
					System.err.println ("URL: " + url + "\t\tMalformed URL " + mfurle2.getMessage()); 
				}
			} catch (Exception e) { 
				System.err.println ("URL: " + url + "\t\tException: " + e.getMessage()); 
			} 
			if (DEBUG) System.out.println("URL query part: "+u.getQuery());
			String baseURL=u.toString().replaceAll("[;?].*", "");
			// we could try to add the sessionID in the URL
			String urlStr=baseURL+";jsessionid="+sessionID+"?"+u.getQuery();
			if (DEBUG) System.out.println("URL with sessionID: "+urlStr);
			try { 
				u=new URL(urlStr);
			} catch (Exception e) { 
				System.err.println ("URL: " + urlStr + "\t\tException: " + e.getMessage()); 
			} 
			
			if (DEBUG) System.out.println("Is URL query part still there ...: "+u.getQuery());
			// create HttpURLConnection object and then connect to the page 
			try { 
				urlc = (HttpURLConnection)u.openConnection (); 
			} catch (UnknownHostException uhe) { 
				System.err.println (url + "\tUnknown Host " + uhe.getMessage()); 
			} catch (NoRouteToHostException nrthe) { 
				System.err.println (url + "\tNo Route to Host " + nrthe.getMessage()); 
			} catch (Exception e) { 
				System.err.println (url + "\tException " + e.getMessage()); 
			} 
			try { 
				urlc.connect (); 
				// Load page contents using a buffered reader and the URLConnection.getInputStream() 
				// method. You should use this technique for first loading the robots.txt file for 
				// the site. If it exists, pay attention to it! 
				this.XMLString="";
				String line; 
				BufferedReader br = new BufferedReader (new InputStreamReader (urlc.getInputStream ())); 
				while ( (line = br.readLine ()) != null ) { 
					this.XMLString+=line+"\r\n";
				} 
	
				// Close the stream when you're done, and then disconnect 
				br.close (); 
				urlc.disconnect ();
				
				if (DEBUG) System.out.println("This is the XMLString: "+this.XMLString);
				// we need to replace the sessionID in the XMLString !!!
				String currentServer=request.getRequestURL().toString().replaceAll("([^/]*//)([^/]*).*","$1$2");
				this.XMLString=this.XMLString.replaceAll("(url=.)([^/]*//)([^/]*)(/[^&;]*)(;jsessionid)?&ques;","$1"+currentServer+"$4;jsessionid="+sessionID+"&ques;");
				if (DEBUG) System.out.println(this.XMLString);

				
				
			} catch (IOException ioe) { 
				System.out.println ("Connection Failure " + url + "\tException: " + ioe.getMessage()); 
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
