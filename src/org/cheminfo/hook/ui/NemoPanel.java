package org.cheminfo.hook.ui;

import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import org.cheminfo.hook.nemo.Nemo;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.List;

public class NemoPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public final static int TOOLBAR_SIZE = 24;
	
	protected NemoInstance nemoInstance;	
	
	public NemoPanel (NemoInstance nemoInstance) {
		this.nemoInstance = nemoInstance;
		
		// Allows to have the menu and tooltips over the applet
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent ce) {
				refresh();
			}
		});
			
		setLayout(new BorderLayout());
		add(nemoInstance.getNemo(), BorderLayout.CENTER);

//		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, mda, true);
		setTransferHandler(new MoleculeTranferHandler());
		
		setVisible(true);

		int width=800;
		int height=600;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width-width)/2;
		int y = (screen.height-height)/2;
		setBounds(x,y,width,height);

		doLayout();

		
		validate();		
		refresh();
	}
	
	protected void refresh() {
		try {
			Nemo nemo = nemoInstance.getNemo();
			Dimension newSize = getSize();
			
			nemo.getMainDisplay()
					.getInteractiveSurface()
					.setSize(newSize.width - TOOLBAR_SIZE,
							newSize.height - 2 * TOOLBAR_SIZE);
			nemo.getMainDisplay().checkSizeAndPosition();
			nemo.getMainDisplay().getInteractiveSurface().repaint();
			nemo.repaint();
			repaint();
		} catch(Exception e){}
	}
	
//	MoleculeDropAdapter mda = new MoleculeDropAdapter(){
//		public void onDropMolecule(com.actelion.research.chem.StereoMolecule m, java.awt.Point pt) {
//			nemoInstance.addMolecule(m, pt.getX()-TOOLBAR_SIZE-100, pt.getY()-TOOLBAR_SIZE-100, 200.0, 200.0);
//		}
//	};
	
	private class MoleculeTranferHandler extends TransferHandler
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean canImport(TransferSupport support)
		{
			List<DataFlavor> flavorList = Arrays.asList(support.getDataFlavors());
            
			if (flavorList.contains(MoleculeFlavors.DF_SERIALIZEDOBJECT))
				return true;
			return false;
		}
		
		@Override
		public boolean importData(TransferSupport support) {
			try{
				if(canImport(support))
				{
			        StereoMolecule mol = null;
			        List<DataFlavor> flavors = Arrays.asList(support.getDataFlavors());
			        
			        try{
						if (flavors.contains(MoleculeFlavors.DF_MDLMOLFILE)) {
							mol = createFromDataFlavor(MoleculeFlavors.DF_MDLMOLFILE, support.getTransferable());
							System.out.println("Molecule pasted as " + MoleculeFlavors.DF_MDLMOLFILE);
						}
				    }
			        catch(Exception e)
				    {
			        	System.err.println(e);
				        try{
							if (flavors.contains(MoleculeFlavors.DF_SERIALIZEDOBJECT)) {
								mol = createFromDataFlavor(MoleculeFlavors.DF_SERIALIZEDOBJECT, support.getTransferable());
								System.out.println("Molecule pasted as " + MoleculeFlavors.DF_SERIALIZEDOBJECT);
							}
					    }
				        catch(Exception e2)
					    {
				        	System.err.println(e2);
					        if (flavors.contains(MoleculeFlavors.DF_SMILES)) {
					        	mol = createFromDataFlavor(MoleculeFlavors.DF_SMILES, support.getTransferable());
					        	System.out.println("Molecule pasted as " + MoleculeFlavors.DF_SMILES);
					        }
					    }
				    }
			        
					NemoPanel nemoPanel = (NemoPanel)support.getComponent();
					
					Point pt = support.getDropLocation().getDropPoint();

					nemoPanel.nemoInstance.addMolecule(mol, pt.getX()-TOOLBAR_SIZE-75, pt.getY()-TOOLBAR_SIZE-75, 150.0, 150.0);

					return true;
				}
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}

			return false;
		}
		
		@Override
		public int getSourceActions(JComponent c) {
			return NONE;
		}
		
	    protected StereoMolecule createFromDataFlavor(DataFlavor flavor, Transferable tr) throws Exception
	    {
	        StereoMolecule mol = null;
	        Object o = tr.getTransferData(flavor);
	        if (flavor.equals(MoleculeFlavors.DF_SERIALIZEDOBJECT) && o instanceof Molecule) {
	            mol = new StereoMolecule((Molecule)o);
	        } else if (flavor.equals(MoleculeFlavors.DF_MDLMOLFILE) && o instanceof String) {
	            mol = new StereoMolecule();
	            new MolfileParser().parse(mol, (String)o);
	        } else if (flavor.equals(MoleculeFlavors.DF_SMILES) && o instanceof String) {
	            mol = new StereoMolecule();
	            new SmilesParser().parse(mol, ((String)o).getBytes());
	        } else {
	            System.err.println("Unable to instantiate flavor " + flavor);
//	            throw new InstantiationException("Unable to instantiate flavor " + chosen);
	        }
	        return mol;
	    }
	}
	
	
}


