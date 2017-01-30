package org.cheminfo.hook.framework;

import java.awt.AWTEventMulticaster;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cheminfo.hook.util.XMLCoDec;

/**
 * This class will manage a panel containing both formatted comments and input
 * fields. There will be mainly 3 components on this class, TextBox and
 * TextFields
 * <ul>
 * <li>Tags concerning color :
 * &lt;black&gt;&lt;gray&gt;&lt;yellow&gt;&lt;red&gt;&lt;orange&gt;&lt;blue&gt;&lt;green&gt;&lt;cyan&gt;
 * &lt;magenta&gt;&lt;white&gt;&lt;lightGray&gt;&lt;darkGray&gt;&lt;pink&gt;
 * <li>Font attribute : &lt;bold&gt;&lt;italic&gt;&lt;plain&gt;
 * <li>Font size : &lt;bigger&gt;&lt;smaller&gt;
 * <li>Font face : &lt;arial&gt;&lt;times&gt;&lt;symbol&gt;&lt;courier&gt;
 * <li>Input field : &lt;paramName=defaultValue&gt;
 * </ul>
 */
public class UserDialog extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4131744321297411079L;
	private String fontName; // the font name for TextBox
	private int fontSize; // the size for the font
	private int fontAttribute; // the attributes for the font
	private String outputString; // the outputString
	private boolean containsInputField; // true if there is some input fields
	private static boolean DEBUG=false;
	
	private ImageButton okButton;

	private ActionListener aListener = null;
	private InteractiveSurface interactions = null;
	private int nextRadioGroup = 1;

	Vector elements;
	int x, y;

	public UserDialog(InteractiveSurface interactions) {
		super();

		FlowLayout layout= new FlowLayout();
		layout.setVgap(0);
		layout.setHgap(10);
		layout.setAlignment(FlowLayout.LEFT);
		this.setLayout(layout);

		this.setBackground(Color.LIGHT_GRAY);
		this.interactions = interactions;
		this.addActionListener(interactions);
	}

	/**
	 * Set the text of the dialog box and repaint it if there is currently no
	 * input field displayed.. The message will contain a mixture of text,
	 * formatting instructions and input fields.
	 * 
	 * @param message
	 *            the value of the dialog box
	 */
	public void setMessageText(String message) {
		if (!containsInputField) {
			setText(message);
		}
	}

	/**
	 * Set the text of the dialog box and repaint it. The message will contain a
	 * mixture of text, formatting instructions and input fields.
	 * 
	 * @param message
	 *            the value of the dialog box
	 */
	public void setText(String message) {
		if (message != null) {
			if (message.length() == 0 || message.charAt(0) != (char) 60)
				outputString = "<Text type=\"plain\">" + message + " </Text>";
			else
				outputString = message;

			removeAll();
			containsInputField = false;
			parse(outputString);
			x = 50;
			formatString(this.getGraphics());
			repaint();
		}
	}

	/**
	 * Erase the dialog box and repaint it if there is currently no input field
	 * displayed.
	 */
	protected void clearText() {
		if (!containsInputField) {
			setText(" ");
			removeAll();
			elements = new Vector();
			repaint();
		}
	}

	/**
	 * Define the OK button that will be used if there is an input field.
	 * 
	 * @param newButton
	 *            an ImageButton
	 */
	public void setOkButton(ImageButton newButton) {
		this.okButton = newButton;
		newButton.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getID() == ActionEvent.ACTION_PERFORMED) {
			DefaultActionButton activeButton = (DefaultActionButton) e
					.getSource();
			if (((DefaultActionButton) e.getSource()).getGroupNb() > 0) {
				for (int el = 0; el < this.elements.size(); el++) {
					if (this.elements.elementAt(el) instanceof DefaultActionButton) {
						DefaultActionButton tempButton = (DefaultActionButton) this.elements.elementAt(el);
						if (tempButton.getGroupNb() == activeButton.getGroupNb()) {
							tempButton.setStatus(DefaultActionButton.UP);
						}
					}
				}
				activeButton.setStatus(DefaultActionButton.DOWN);
			}

			if (((DefaultActionButton) e.getSource()).getActionClassName() != null
					&& ((DefaultActionButton) e.getSource()).getActionClassName().compareTo("NONE") != 0)
			{
				try {
					try {
						// Class
						// actionClass=Class.forName("org.cheminfo.applet.nemo."+((DefaultActionButton)e.getSource()).getActionClassName());

						Class actionClass = Class.forName(((DefaultActionButton) e.getSource()).getActionClassName());

						Class[] argumentTypes = { InteractiveSurface.class };
						Method actionMethod = actionClass.getMethod("performAction", argumentTypes);
						Object[] arguments = { this.interactions };

						try {
							actionMethod.invoke(null, arguments);
						} catch (IllegalAccessException ie) {
							System.out.println("IllegalAccessException");
						} catch (IllegalArgumentException ie) {
							System.out.println("IllegalArgumentException");
						} catch (InvocationTargetException ie) {
							System.out.println("From UserDialog " + ((DefaultActionButton) e.getSource()).getActionClassName() + " InvocationTargetException :" + ie.getTargetException());
						}
					} catch (NoSuchMethodException ne) {
						System.out.println("No such method");
					}
				} catch (ClassNotFoundException ce) {
					System.out.println("Class not found");
				}

				if (aListener != null)
					aListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
			}
		}
	}

	public void addActionListener(ActionListener listener) {
		aListener = AWTEventMulticaster.add(aListener, listener);
	}

	protected void removeActionListener(ActionListener listener) {
		aListener = AWTEventMulticaster.remove(aListener, listener);
	}

	/**
	 * Get a String corresponding to the current value of the dialog box. If
	 * there is some input fields with values, the values will be encoded in the
	 * String. The symbol < > & are encoded in this string respectively as &lt;
	 * &gt; &amp;
	 * 
	 * @return String corresponding to the dialog box
	 */
	private String getText() {
		String returnString = "";
		for (int i = 0; i < elements.size(); i++) {
			Object o = elements.elementAt(i);
			if (o instanceof String) {
				returnString += encodeGreater((String) o);
			} else if (o instanceof AnAttribute) {
				returnString += "<" + ((AnAttribute) elements.elementAt(i)).name + ">";
			} else if (o instanceof Entry) {
				returnString += "<" + ((Entry) o).name + "=" + encodeGreater(((Entry) o).textField.getText()) + ">";
			}
		}
		return returnString;
	}

	/**
	 * Get a parameter (the value of a input field).
	 * 
	 * @paramName the name of the parameter
	 * @return String corresponding to the dialog box
	 */
	public String getParameter(String paramName) {
		String outString = "";
		if (DEBUG) System.out.println("param name "+paramName);
		if (DEBUG) System.out.println(" elements "+this.elements.size());
		for (int i = 0; i < this.elements.size(); i++) {
			if (elements.elementAt(i) instanceof Entry) {
				if (DEBUG) System.out.println(((Entry) this.elements.elementAt(i)).name);
				if (((Entry) this.elements.elementAt(i)).name.compareTo(paramName) == 0) {
					outString = ((Entry) this.elements.elementAt(i)).getCurrentValue();
				}
			} else if (elements.elementAt(i) instanceof Choice) {
				if (((Choice) this.elements.elementAt(i)).getName().compareTo(paramName) == 0) {
					outString = ((Choice) this.elements.elementAt(i)).getSelectedItem();
				}
			}
		}
		return outString;
	}

	/**
	 * Returns true if the dialog box contains an input field..
	 * 
	 * @return true if the dialog box contains an input field
	 */
	protected boolean containsInputField() {
		return containsInputField;
	}

	public void paint(Graphics g) {
		int width = getSize().width;
		int height = getSize().height;

		fontName = "SansSerif";
		fontSize = 12;
		fontAttribute = 0;

		// we add the "shadow"
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.darkGray);
		g.drawLine(0, height - 1, width - 1, height - 1);
		g.drawLine(width - 1, 0, width - 1, height - 1);

		super.paint(g);
	}

	public void repaint(Graphics g) {
		this.paint(g);
	}

	public void update(Graphics g) {
		this.paint(g);
	}

	/**
	 * Allows to decompose a String in its various elements. An element is
	 * composed of a tag (<red><symbol><bold> ...)
	 */
	protected void parse(String string) {
		elements = new Vector();

		XMLCoDec globalCodec = new XMLCoDec(string);
		XMLCoDec localCodec = new XMLCoDec();

		int totElements = globalCodec.getRootElementsCount();

		if (DEBUG) System.out.println("Message: "+this.outputString);
		for (int elem = 0; elem < totElements; elem++) {
			localCodec.clearParameters();

			localCodec.setXMLString(globalCodec.popXMLTag());
			String shavedTag = localCodec.shaveXMLTag();

			
			if (localCodec.getParameterAsString("tagName").trim().compareTo("Text") == 0) {
				elements.addElement(shavedTag);
			} else if (localCodec.getParameterAsString("tagName").trim().compareTo("Button") == 0) {
				DefaultActionButton valButton = new DefaultActionButton(getImage(localCodec.getParameterAsString("image")));
				valButton.addActionListener(this);
				valButton.setActionClassName(localCodec.getParameterAsString("action"));
				elements.addElement(valButton);
				this.containsInputField = true;
			} else if (localCodec.getParameterAsString("tagName").trim().compareTo("Input") == 0) {
				elements.addElement(new Entry(localCodec.getParameterAsString("name"), shavedTag, localCodec.getParameterAsInt("size")));
				// System.out.println()
				this.containsInputField = true;
			} else if (localCodec.getParameterAsString("tagName").trim().compareTo("Radio") == 0) {
				int nbButtons = localCodec.getParameterAsInt("nbButtons");
				for (int button = 0; button < nbButtons; button++) {
					DefaultActionButton thisButton = new DefaultActionButton(getImage(localCodec.getParameterAsString("image" + button)));
					thisButton.addActionListener(this);
					thisButton.setButtonType(DefaultActionButton.RADIOBUTTON);
					thisButton.setGroupNb(this.nextRadioGroup);
					thisButton.setActionClassName(localCodec.getParameterAsString("action" + button));
					elements.addElement(thisButton);
					if (localCodec.getParameterAsInt("default") == button) {
						thisButton.setStatus(DefaultActionButton.DOWN);
					}
				}
				this.nextRadioGroup++;
				this.containsInputField = true;
			} else if (localCodec.getParameterAsString("tagName").trim().compareTo("Checkbox") == 0) {
				DefaultActionButton thisButton = new DefaultActionButton(getImage(localCodec.getParameterAsString("image")));
				if (DEBUG) System.out.println(localCodec.getParameterAsString("image"));
				thisButton.addActionListener(this);
				thisButton.setButtonType(DefaultActionButton.CHECKBUTTON);
				thisButton.setActionClassName(localCodec.getParameterAsString("action"));
				elements.addElement(thisButton);
				if (localCodec.getParameterAsString("default").compareTo("DOWN") == 0) {
					thisButton.setStatus(DefaultActionButton.DOWN);
				}
			} else if (localCodec.getParameterAsString("tagName").trim().compareTo("Choice") == 0) {
				Choice thisChoice = new Choice();
				int nbChoices = localCodec.getParameterAsInt("nbChoices");
				thisChoice.setName(localCodec.getParameterAsString("name"));
				for (int ch = 0; ch < nbChoices; ch++) {
					thisChoice.add(localCodec.getParameterAsString("choice"	+ ch));
					//System.out.println(localCodec.getParameterAsString("choice"	+ ch));
				}
				thisChoice.select(localCodec.getParameterAsString("active"));
				Insets i = this.getInsets();
				Dimension d = this.getSize();
				d.height -= i.bottom + i.top;
				d.width = 100;
//				thisChoice.setSize(d);
				elements.addElement(thisChoice);
			}
		}
	}


	private void formatString(Graphics g) {
		// if(containsInputField) {
		for (Object o: elements) {
			if (o instanceof String) {
				JLabel label=new JLabel((String)o);
				label.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
				this.add(label);
			} else if (o instanceof AnAttribute) {
				int attributeValue = ((AnAttribute)o).value;
				setAttribute(attributeValue, g);
			} else if (o instanceof Entry) {
				JTextField tf = ((Entry) o).textField;
				tf.setFont(new Font(fontName, fontAttribute, fontSize));
				tf.setText(((Entry) o).value);
				
				// tf.setPreferredSize(new Dimension(30,20));
				this.add(tf);
			} else if (o instanceof ImageButton) {
				ImageButton bt = (ImageButton) o;
				add(bt);
//				bt.setSize(24, 24);
			} else if (o instanceof Choice) {
				Choice tempChoice = (Choice) o;
				add(tempChoice);
			}
		}
		this.doLayout();
	}

	private void setAttribute(int attributeValue, Graphics g) {
		switch (attributeValue) {
		case 1: // black
			g.setColor(Color.black);
			break;
		case 2: // gray
			g.setColor(Color.gray);
			break;
		case 3: // yellow
			g.setColor(Color.yellow);
			break;
		case 4: // red
			g.setColor(Color.red);
			break;
		case 5: // orange
			g.setColor(Color.orange);
			break;
		case 6: // blue
			g.setColor(Color.blue);
			break;
		case 7: // green
			g.setColor(Color.green);
			break;
		case 8: // cyan
			g.setColor(Color.cyan);
			break;
		case 9: // magenta
			g.setColor(Color.magenta);
			break;
		case 10: // white
			g.setColor(Color.white);
			break;
		case 11: // lightGray
			g.setColor(Color.lightGray);
			break;
		case 12: // darkGray
			g.setColor(Color.darkGray);
			break;
		case 13: // pink
			g.setColor(Color.pink);
			break;
		case 30: // bold
			fontAttribute |= Font.BOLD;
			g.setFont(new Font(fontName, fontAttribute, fontSize));
			break;
		case 31: // italic
			fontAttribute |= Font.ITALIC;
			g.setFont(new Font(fontName, fontAttribute, fontSize));
			break;
		case 32: // plain
			fontAttribute = 0;
			g.setFont(new Font(fontName, fontAttribute, fontSize));
			break;
		case 40: // bigger
			fontSize += 2;
			g.setFont(new Font(fontName, fontAttribute, fontSize));
			break;
		case 41: // smaller
			fontSize -= 2;
			g.setFont(new Font(fontName, fontAttribute, fontSize));
			break;
		case 60: // arial
			fontName = "SansSerif";
			g.setFont(new Font(fontName, fontAttribute, fontSize));
			break;
		case 61: // times
			fontName = "Serif";
			g.setFont(new Font(fontName, fontAttribute, fontSize));
			break;
		case 62: // symbol
			fontName = "Symbol";
			g.setFont(new Font(fontName, fontAttribute, fontSize));
			break;
		case 63: // courrier
			fontName = "Monospaced";
			g.setFont(new Font(fontName, fontAttribute, fontSize));
			break;
		default:
		}
	}

	private AnAttribute getAttributeCode(String attribute) {
		if (attribute.equals("black")) {
			return new AnAttribute(attribute, 1);
		} else if (attribute.equals("gray")) {
			return new AnAttribute(attribute, 2);
		} else if (attribute.equals("yellow")) {
			return new AnAttribute(attribute, 3);
		} else if (attribute.equals("red")) {
			return new AnAttribute(attribute, 4);
		} else if (attribute.equals("orange")) {
			return new AnAttribute(attribute, 5);
		} else if (attribute.equals("blue")) {
			return new AnAttribute(attribute, 6);
		} else if (attribute.equals("green")) {
			return new AnAttribute(attribute, 7);
		} else if (attribute.equals("cyan")) {
			return new AnAttribute(attribute, 8);
		} else if (attribute.equals("magenta")) {
			return new AnAttribute(attribute, 9);
		} else if (attribute.equals("white")) {
			return new AnAttribute(attribute, 10);
		} else if (attribute.equals("lightGray")) {
			return new AnAttribute(attribute, 11);
		} else if (attribute.equals("darkGray")) {
			return new AnAttribute(attribute, 12);
		} else if (attribute.equals("pink")) {
			return new AnAttribute(attribute, 13);
		} else if (attribute.equals("bold")) {
			return new AnAttribute(attribute, 30);
		} else if (attribute.equals("italic")) {
			return new AnAttribute(attribute, 31);
		} else if (attribute.equals("plain")) {
			return new AnAttribute(attribute, 32);
		} else if (attribute.equals("bigger")) {
			return new AnAttribute(attribute, 40);
		} else if (attribute.equals("smaller")) {
			return new AnAttribute(attribute, 41);
		} else if (attribute.equals("arial")) {
			return new AnAttribute(attribute, 60);
		} else if (attribute.equals("times")) {
			return new AnAttribute(attribute, 61);
		} else if (attribute.equals("symbol")) {
			return new AnAttribute(attribute, 62);
		} else if (attribute.equals("courier")) {
			return new AnAttribute(attribute, 63);
		}
		return null;
	}

	private static String decodeGreater(String string) {
		string = string.replaceAll("&gt;", ">");
		string = string.replaceAll("&lt;", "<");
		return string;
	}

	private static String encodeGreater(String string) {
		string = string.replaceAll(">", "&gt;");
		string = string.replaceAll("<", "&lt;");
		return string;
	}

	/**
	 * Method to load an image from the file pointed by imageName Makes use of
	 * urlLocation.
	 */
	private Image getImage(String imageName) {
		Image image = null;

		try {
			InputStream in = getClass().getResourceAsStream(
					"/org/cheminfo/hook/gif/" + imageName);
			if (in != null) {
				byte[] buffer = new byte[in.available()];
				in.read(buffer);
				image = Toolkit.getDefaultToolkit().createImage(buffer);
			}
		} catch (java.io.IOException e) {
		}

		/*
		if (image == null) {
			// System.out.println("YOOP
			// "+interactions.getAppletURL().toString()+imageName);
			// image=getImage(interactions.getAppletURL().toString()+imageName);
		}
		*/
		// if (image==null)
		// image=getImage(interactions.getAppletURL().toString()+imageName);

		return image;
	}

	class Entry {
		String name = "";
		String value = "";
		int length = 10;
		JTextField textField;

		Entry(String name, String value, int length) {
			this.name = name;
			this.value = value;
			this.length = length;
			this.textField = new JTextField(length);
			add(textField);
		}

		String getEntryCode() {
			return "<" + name + "=" + value + ">";
		}

		protected String getCurrentValue() {
			return this.textField.getText();
		}
	}

	class AnAttribute {
		String name = "";
		int value = 0;

		AnAttribute(String name, int value) {
			this.name = name;
			this.value = value;
		}
	}

}
