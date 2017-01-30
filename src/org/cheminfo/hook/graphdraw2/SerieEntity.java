package org.cheminfo.hook.graphdraw2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.util.ElementProvider;
import org.cheminfo.hook.util.HookStatistics;
import org.cheminfo.hook.util.Image2DTileManager;
import org.cheminfo.hook.util.QuadTreeElement;
import org.cheminfo.hook.util.QuadTreeNode;
import org.cheminfo.hook.util.XMLCoDec;

public class SerieEntity extends BasicEntity
		implements
		ElementProvider {
	QuadTreeNode rootNode = null;

	private Image2DTileManager tileManager = null;
	private XYPointEntity overEntity;
	private Vector<BasicEntity> tempPoints;
	private String serieName;
	private double top,
			bottom,
			left,
			right;
	private boolean visible;
	private SerieData serieData;
	private Trendline trendline;
	private HighlightEntity highlight = null;

	private final static int NBTILES	=	1;
	
	public SerieEntity(SerieData data) {

		long time = System.currentTimeMillis();

		this.trendline = null;

		this.serieName = data.getName();
		this.serieData = data;

		tempPoints = new Vector<BasicEntity>();

		XYPointEntity tempPoint;

		if (data.getAddon() == SerieData.ADDON_3SIGMA) {
			Point2D.Double[] points = data.getAsArray();

			double mean = HookStatistics.meanY(points);
			double sigma = HookStatistics.standardDeviationY(points);

			// tempPoints.add(new HighlightEntity(mean+3*sigma, mean-3*sigma,
			// new Color(data.getColor().getRed(), data.getColor().getGreen(),
			// data.getColor().getBlue(), 50)));

			this.top = mean + 3 * sigma;
			this.bottom = mean - 3 * sigma;

			this.highlight = new HighlightEntity(mean + 3 * sigma, mean - 3 * sigma, new Color(data.getColor().getRed(), data.getColor().getGreen(), data.getColor().getBlue(), 50));
		}

		Color color = data.getColor();
		this.left = this.right = data.getX(0);

		for (int point = 0; point < data.getNbPoints(); point++) {
			System.out.println(point);
			tempPoint = new XYPointEntity(data.getX(point), data.getY(point), data.getErrX(point), data.getErrY(point), data.getComment(point), data.getReference(point), color, data.getDataAspect());
			tempPoints.add(tempPoint);

			if (data.getX(point) + data.getErrX(point) > right)
				right = data.getX(point) + data.getErrX(point);
			if (data.getX(point) - data.getErrX(point) < left)
				left = data.getX(point) - data.getErrX(point);
			if (data.getY(point) + data.getErrY(point) > top)
				top = data.getY(point) + data.getErrY(point);
			if (data.getY(point) - data.getErrY(point) < bottom)
				bottom = data.getY(point) - data.getErrY(point);
		}

		/*
		 * if (right - left < 1) { right += 0.5; left -= 0.5; } if (top - bottom
		 * < 1) { top += 0.5; bottom -= 0.5; }
		 */
		double width = right - left;
		double height = bottom - top;
		this.rootNode = new QuadTreeNode((float) (left - width / 10), (float) (right + width / 10), (float) (top - height / 10), (float) (bottom + height / 10), tempPoints, 20, 10);

		this.visible = true;
	}

	public SerieEntity(String XMLTag, Hashtable helpers) {
		XMLCoDec tempCodec = new XMLCoDec(XMLTag);
		tempCodec.shaveXMLTag();

		this.serieName = tempCodec.getParameterAsString("name");
		this.setMovementType(BasicEntity.FIXED);

		this.tempPoints = new Vector<BasicEntity>();
		Color serieColor = tempCodec.getParameterAsColor("serieColor");

		if (tempCodec.getParameterAsColor("highlightColor") != null) {
			Color hlColor = tempCodec.getParameterAsColor("highlightColor");
			this.highlight = new HighlightEntity(tempCodec.getParameterAsDouble("highlightTop"), tempCodec.getParameterAsDouble("highlightBottom"), new Color(hlColor.getRed(), hlColor.getGreen(), hlColor.getBlue(), 50));
		}

		int elements = tempCodec.getRootElementsCount();

		for (int elem = 0; elem < elements; elem++) {
			XMLCoDec tempCodec2 = new XMLCoDec(tempCodec.readXMLTag());

			tempCodec2.shaveXMLTag();
			try {
				Class entityClass = Class.forName("org.cheminfo.hook." + tempCodec2.getParameterAsString("tagName").trim());
				Class[] parameterClasses = { String.class, Hashtable.class };
				java.lang.reflect.Constructor entityConstructor = entityClass.getConstructor(parameterClasses);

				Object[] parameters = { tempCodec.popXMLTag(), helpers };
				if (tempCodec2.getParameterAsString("tagName").trim().compareTo("graphdraw2.XYPointEntity") == 0) {
					tempPoints.add((BasicEntity) entityConstructor.newInstance(parameters));
				} else if (tempCodec2.getParameterAsString("tagName").trim().compareTo("graphdraw2.Trendline") == 0) {
					this.trendline = (Trendline) entityConstructor.newInstance(parameters);
				}
			} catch (ClassNotFoundException ex1) {
				System.out.println("SerieEntity XML constructor e: " + ex1);
			} catch (IllegalAccessException ex2) {
				System.out.println("SerieEntity XML constructor e: " + ex2);
			} catch (InstantiationException ex3) {
				System.out.println("SerieEntity XML constructor e: " + ex3);
			} catch (InvocationTargetException ex4) {
				System.out.println("SerieEntity XML constructor e: " + ex4 + " -> " + ex4.getTargetException());
			} catch (NoSuchMethodException ex5) {
				System.out.println("SerieEntity XML constructor e: " + ex5);
			}
			;
		}

		XYPointEntity currPoint;
		this.serieData = new SerieData();
		this.serieData.init(tempPoints.size());
		this.serieData.setColor(serieColor);

		for (int point = 0; point < tempPoints.size(); point++) {
			currPoint = (XYPointEntity) tempPoints.get(point);
			serieData.setPoint(point, currPoint.getX(), currPoint.getY(), currPoint.getErrX(), currPoint.getErrY(), "", currPoint.getReference());
			if (point == 0) {
				left = right = currPoint.getX();
				top = bottom = currPoint.getY();
			} else {
				if (currPoint.getX() + currPoint.getErrX() > right)
					right = currPoint.getX() + currPoint.getErrX();
				if (currPoint.getX() - currPoint.getErrX() < left)
					left = currPoint.getX() - currPoint.getErrX();
				if (currPoint.getY() + currPoint.getErrY() > top)
					top = currPoint.getY() + currPoint.getErrY();
				if (currPoint.getY() - currPoint.getErrY() < bottom)
					bottom = currPoint.getY() - currPoint.getErrY();
			}
		}

		if (right - left < 1) {
			right += 0.5;
			left -= 0.5;
		}
		if (top - bottom < 1) {
			top += 0.5;
			bottom -= 0.5;
		}

		double tempWidth = right - left;
		double tempHeight = top - bottom;

		left -= tempWidth / 10;
		right += tempWidth / 10;

		top += tempHeight / 10;
		bottom -= tempHeight / 10;

		this.rootNode = new QuadTreeNode((float) left, (float) right, (float) top, (float) bottom, tempPoints, 20, 10);

		Vector tempVec = new Vector();
		this.rootNode.getAllElements(tempVec);

		this.visible = true;
	}

	public SerieData getSerieData() {
		return this.serieData;
	}

	public void setVisible(boolean isVisible) {
		this.visible = isVisible;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public double getTop() {
		return this.top;
	}

	public double getBottom() {
		return this.bottom;
	}

	public double getRight() {
		return this.right;
	}

	public double getLeft() {
		return this.left;
	}

	public void setHighlight(String highlightType) {
		if (highlightType.toUpperCase().indexOf("SIGMA") != -1) {
			String factorString = highlightType.substring(0, highlightType.toUpperCase().indexOf("SIGMA"));
			int factor = Integer.parseInt(factorString);

			Point2D.Double[] points = this.serieData.getAsArray();

			double mean = HookStatistics.meanY(points);
			double sigma = HookStatistics.standardDeviationY(points);

			// tempPoints.add(new HighlightEntity(mean+3*sigma, mean-3*sigma,
			// new Color(data.getColor().getRed(), data.getColor().getGreen(),
			// data.getColor().getBlue(), 50)));

			this.top = mean + factor * sigma;
			this.bottom = mean - factor * sigma;

			this.highlight = new HighlightEntity(mean + factor * sigma, mean - factor * sigma, new Color(this.serieData.getColor().getRed(), this.serieData.getColor().getGreen(), this.serieData.getColor().getBlue(), 50));

			this.forceRedraw();
			this.getInteractiveSurface().repaint();
		} else
			System.out.println("Highlight unknown");

	}

	public void setHighlight(HighlightEntity highlightEntity) {
		this.highlight = highlightEntity;

		if (this.highlight.getTopValue() > this.top)
			top = this.highlight.getTopValue();
		if (this.highlight.getBottomValue() > bottom)
			bottom = this.highlight.getBottomValue();

	}

	public void setTrendline(Trendline newTrendline) {
		this.trendline = newTrendline;
		this.getInteractiveSurface().repaint();
	}

	public void addNotify() {
		this.checkSizeAndPosition();
	}

	public String getOverMessage() {
		return this.toString();
	}

	public void refreshSensitiveArea() {
		super.refreshSensitiveArea();
		Vector elements = new Vector();

		if (this.rootNode != null) {
			this.rootNode.getAllElements(elements);

			for (int el = 0; el < elements.size(); el++) {
				((BasicEntity) elements.get(el)).refreshSensitiveArea();
			}
		}
	}

	public void delete() {
		if (this.tileManager != null)
			this.tileManager.reset();
	}

	public void checkSizeAndPosition() {
		GraphDisplay2 parentDisplay = (GraphDisplay2) this.getParentEntity();

		double tempWidth = (1 - GraphDisplay2.AXIS_RELATIVE_SIZE) * parentDisplay.getWidth();
		double tempHeight = (1 - GraphDisplay2.AXIS_RELATIVE_SIZE) * parentDisplay.getHeight();

		double tempXPos = 0;// GraphDisplay.AXIS_RELATIVE_SIZE*parentDisplay.getWidth();
		double tempYPos = GraphDisplay2.AXIS_RELATIVE_SIZE * parentDisplay.getHeight();

		if (parentDisplay.hasHAxis()) {
			tempHeight -= GraphDisplay2.AXIS_RELATIVE_SIZE * parentDisplay.getHeight();
		}

		if (parentDisplay.hasVAxis()) {
			tempWidth -= GraphDisplay2.AXIS_RELATIVE_SIZE * parentDisplay.getWidth();
			tempXPos = GraphDisplay2.AXIS_RELATIVE_SIZE * parentDisplay.getWidth();
		}

		tempWidth = Math.ceil(tempWidth / 4) * 4;
		tempHeight = Math.ceil(tempHeight / 4) * 4;

		this.setLocation(tempXPos, tempYPos);
		this.setSize(tempWidth, tempHeight);

		super.checkSizeAndPosition();
	}

	public BasicEntity returnOverEntity(double x, double y, int currentLayer, int highestLayer) {
		if (visible) {
			GraphDisplay2 parentDisplay = (GraphDisplay2) this.getParentEntity();

			if (this.rootNode != null) {
				Vector elements = new Vector();

				this.rootNode.getElements(parentDisplay.absolutePixelsToUnitsH(x - 3), parentDisplay.absolutePixelsToUnitsH(x + 3), parentDisplay.absolutePixelsToUnitsV(y - 3), parentDisplay.absolutePixelsToUnitsV(y + 3), elements);

				if (elements.size() > 0) {
					for (int p = 0; p < elements.size(); p++) {
						if (elements.get(p) instanceof XYPointEntity) {
							if (((XYPointEntity) elements.get(p)).getX() > Math.min(parentDisplay.absolutePixelsToUnitsH(x - 3), parentDisplay.absolutePixelsToUnitsH(x + 3)) && ((XYPointEntity) elements.get(p)).getX() < Math.max(parentDisplay.absolutePixelsToUnitsH(x - 3), parentDisplay.absolutePixelsToUnitsH(x + 3)) && ((XYPointEntity) elements.get(p)).getY() > Math.min(parentDisplay.absolutePixelsToUnitsV(y - 3), parentDisplay.absolutePixelsToUnitsV(y + 3)) && ((XYPointEntity) elements.get(p)).getY() < Math.max(parentDisplay.absolutePixelsToUnitsV(y - 3), parentDisplay.absolutePixelsToUnitsV(y + 3))) {
								this.overEntity = (XYPointEntity) elements.get(p);
								return (BasicEntity) elements.get(p);
							}
						}
					}
				}
			}
			this.overEntity = null;
			return null;
		}

		return null;
	}

	public String getName() {
		return this.serieName;
	}

	public XYPointEntity getPoint(int index) {
		return (XYPointEntity) this.tempPoints.get(index);
	}

	public void update() {
		// Recompile quadtree
		Vector elements = new Vector();
		this.rootNode.getAllElements(elements);

		XYPointEntity currentPoint;
		double newLeft, newRight, newTop, newBottom;

		newLeft = newRight = ((XYPointEntity) elements.get(0)).getX();
		newTop = newBottom = ((XYPointEntity) elements.get(0)).getY();

		for (int p = 1; p < elements.size(); p++) {
			if (elements.get(p) instanceof XYPointEntity) {
				currentPoint = (XYPointEntity) elements.get(p);

				if (currentPoint.getX() < newLeft)
					newLeft = currentPoint.getX();
				if (currentPoint.getX() > newRight)
					newRight = currentPoint.getX();
				if (currentPoint.getY() > newTop)
					newTop = currentPoint.getY();
				if (currentPoint.getY() < newBottom)
					newBottom = currentPoint.getY();
			} else if (elements.get(p) instanceof HighlightEntity) {

			}

		}

		this.rootNode = new QuadTreeNode((float) newLeft, (float) newRight, (float) newTop, (float) newBottom, elements, 20, 10);
		if (this.highlight != null) {
			if (this.highlight.getTopValue() > newTop)
				newTop = this.highlight.getTopValue();
			if (this.highlight.getBottomValue() > newBottom)
				newBottom = this.highlight.getBottomValue();
		}
		left = newLeft;
		right = newRight;
		top = newTop;
		bottom = newBottom;
	}

	public void forceRedraw() {
		System.out.println("YOOP SE size: " + this.getWidth() + ", " + this.getHeight());
		if (this.tileManager == null) {
			Vector<ElementProvider> providers = new Vector<ElementProvider>();
			providers.add(this);

			tileManager = new Image2DTileManager(providers, (float) this.getWidth(), (float) this.getHeight(), (float) ((GraphDisplay2) this.getParentEntity()).getLeftLimit(), (float) ((GraphDisplay2) this.getParentEntity()).getRightLimit(), (float) ((GraphDisplay2) this.getParentEntity()).getTopLimit(), (float) ((GraphDisplay2) this.getParentEntity()).getBottomLimit(), Color.black, Color.red, NBTILES, NBTILES);
		}
		this.tileManager.forceRedraw();
	}

	public void paint(Graphics2D g) {
		if (visible) {
			GraphDisplay2 parentDisplay = (GraphDisplay2) this.getParentEntity();

			if (this.trendline != null) {
				g.setColor(Color.BLACK);
				this.trendline.paint(g, this);
			}

			if (this.rootNode != null) {
				if (this.tileManager == null) {
					Vector<ElementProvider> providers = new Vector<ElementProvider>();
					providers.add(this);

					tileManager = new Image2DTileManager(providers, (float) this.getWidth(), (float) this.getHeight(), (float) ((GraphDisplay2) this.getParentEntity()).getLeftLimit(), (float) ((GraphDisplay2) this.getParentEntity()).getRightLimit(), (float) ((GraphDisplay2) this.getParentEntity()).getTopLimit(), (float) ((GraphDisplay2) this.getParentEntity()).getBottomLimit(), Color.black, Color.red, NBTILES, NBTILES);
				}

				g.drawImage(tileManager.getImage((float) parentDisplay.getLeftLimit(), (float) parentDisplay.getRightLimit(), (float) parentDisplay.getTopLimit(), (float) parentDisplay.getBottomLimit(), (float) Math.ceil(this.getWidth() / NBTILES) * NBTILES, (float) Math.ceil(this.getHeight() / NBTILES) * NBTILES), 0, 0, null);
			}

			if (this.overEntity != null) {
				double pixelsPerUnitH = this.getWidth() / (parentDisplay.getRightLimit() - parentDisplay.getLeftLimit());
				double pixelsPerUnitV = this.getHeight() / (parentDisplay.getBottomLimit() - parentDisplay.getTopLimit());

				double tempXPos = (this.overEntity.getX() - parentDisplay.getLeftLimit()) * pixelsPerUnitH;
				double tempYPos = (this.overEntity.getY() - parentDisplay.getTopLimit()) * pixelsPerUnitV;

				g.setColor(Color.yellow);
				g.fill(new Rectangle2D.Double(tempXPos - 3, tempYPos - 3, 7, 7));
			}

			super.paint(g);
		}
	}

	public void paintSB(Graphics2D g) {
		g.setClip(new Rectangle2D.Double(-1, -1, this.getWidth() + 2, this.getHeight() + 2));
		if (visible) {
			GraphDisplay2 parentDisplay = (GraphDisplay2) this.getParentEntity();

			if (this.trendline != null) {
				g.setColor(Color.BLACK);
				this.trendline.paint(g, this);
			}

			double pixelsPerUnitH = this.getWidth() / (parentDisplay.getRightLimit() - parentDisplay.getLeftLimit());
			double pixelsPerUnitV = this.getHeight() / (parentDisplay.getBottomLimit() - parentDisplay.getTopLimit());

			if (this.highlight != null) {
				double tempTopPos = (this.highlight.getTopValue() - parentDisplay.getTopLimit()) * pixelsPerUnitV;
				double tempBottomPos = (this.highlight.getBottomValue() - parentDisplay.getTopLimit()) * pixelsPerUnitV;

				g.setColor(this.highlight.getColor());
				g.fill(new Rectangle2D.Double(0, tempTopPos, this.getWidth(), tempBottomPos - tempTopPos));
				g.setColor(Color.BLACK);
				g.draw(new Line2D.Double(0, tempTopPos, this.getWidth(), tempTopPos));
				g.setColor(Color.BLACK);
				g.draw(new Line2D.Double(0, tempBottomPos, this.getWidth(), tempBottomPos));
				g.setColor(Color.BLACK);
				g.draw(new Line2D.Double(0, (tempTopPos + tempBottomPos) / 2, this.getWidth(), (tempTopPos + tempBottomPos) / 2));
			}

			if (this.serieData != null) {
				for (int point = 0; point < this.tempPoints.size(); point++) {
					if (this.tempPoints.get(point) instanceof XYPointEntity)
						((XYPointEntity) this.tempPoints.get(point)).paint(g, (float) ((GraphDisplay2) this.getParentEntity()).getLeftLimit(), (float) ((GraphDisplay2) this.getParentEntity()).getRightLimit(), (float) ((GraphDisplay2) this.getParentEntity()).getTopLimit(), (float) ((GraphDisplay2) this.getParentEntity()).getBottomLimit(), (float) this.getWidth(), (float) this.getHeight());
				}
			}

			if (this.overEntity != null) {

				double tempXPos = (this.overEntity.getX() - parentDisplay.getLeftLimit()) * pixelsPerUnitH;
				double tempYPos = (this.overEntity.getY() - parentDisplay.getTopLimit()) * pixelsPerUnitV;

				g.setColor(Color.yellow);
				g.fill(new Rectangle2D.Double(tempXPos - 3, tempYPos - 3, 7, 7));
			}

			super.paint(g);
		}
		g.setClip(null);
	}

	public String getXmlTag(Hashtable xmlProperties) {
		XMLCoDec tempCodec = new XMLCoDec();
		String tempTag = "";

		tempCodec.addParameter("name", this.serieName);
		tempCodec.addParameter("serieColor", this.serieData.getColor().getRed() + "," + this.serieData.getColor().getGreen() + "," + this.serieData.getColor().getBlue());
		if (this.highlight != null) {
			tempCodec.addParameter("highlightTop", new Double(this.highlight.getTopValue()));
			tempCodec.addParameter("highlightBottom", new Double(this.highlight.getBottomValue()));
			tempCodec.addParameter("highlightColor", this.highlight.getColor().getRed() + "," + this.highlight.getColor().getGreen() + "," + this.highlight.getColor().getBlue());
		}

		tempTag += "<graphdraw2.SerieEntity ";
		tempTag += tempCodec.encodeParameters();
		tempTag += ">\r\n";

		StringBuffer pointBuffer = new StringBuffer();
		for (int point = 0; point < this.tempPoints.size(); point++) {
			pointBuffer.append(((BasicEntity) this.tempPoints.get(point)).getXmlTag(xmlProperties));
			// tempTag += ((BasicEntity)
			// this.tempPoints.get(point)).getXmlTag(xmlProperties);
		}
		tempTag += pointBuffer.toString();

		if (this.trendline != null) {
			tempTag += this.trendline.getXmlTag(xmlProperties);
		}
		tempTag += "</graphdraw2.SerieEntity>\r\n";

		return tempTag;
	}

	// ElementProvider interface
	public void getElements(double leftLimit, double rightLimit, double topLimit, double bottomLimit, Vector<QuadTreeElement> outVector) {
		this.rootNode.getElements(leftLimit, rightLimit, topLimit, bottomLimit, outVector);
		if (this.highlight != null)
			outVector.add(this.highlight);
	}

}