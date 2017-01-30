package org.cheminfo.hook.framework;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ProcessingAnimationPanel extends JPanel
{

	public ProcessingAnimationPanel()
	{
/*		Image image=null;
		try { 
			InputStream in = getClass().getResourceAsStream("/org/cheminfo/hook/gif/loading.gif"); 
			if (in != null) { 
				byte[] buffer = new byte[in.available()]; 
				in.read(buffer); 
				image = Toolkit.getDefaultToolkit().createImage(buffer); 
			} else {
				System.out.println("input stream is null");
			}
		} catch (java.io.IOException e) {e.printStackTrace(); }

		ImageIcon icon=new ImageIcon(image);
*/		
		URL url = this.getClass().getClassLoader().getResource("org/cheminfo/hook/gif/loading.gif");
		if (url != null)
		{
			ImageIcon icon=new ImageIcon(url);
	
			this.setSize(100, 100);
			this.setOpaque(false);
			this.setLayout(new BorderLayout());
	
			JLabel label=new JLabel(icon);
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			this.add(label,BorderLayout.CENTER );
		}
	}
	
	public void addNotify()
	{
		super.addNotify();
		this.setLocation( (this.getParent().getWidth()-this.getWidth())/2, (this.getParent().getHeight()-this.getHeight())/2);
		this.doLayout();

	}

}
