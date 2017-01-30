package org.cheminfo.hook.moldraw.tests;

import com.actelion.research.chem.Depictor;
import com.actelion.research.chem.Depictor2D;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.contrib.HydrogenHandler;
import com.actelion.research.epfl.EPFLUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CreateMolPicture {
	public static void main(String argv[]) {
		File file = new File(argv[0]);
		MolfileParser parser = new MolfileParser();
		StereoMolecule mol = new StereoMolecule();
		parser.parse(mol, file);
		
		StereoMolecule canMolecule = EPFLUtils.getCanonicalForm(mol);
		HydrogenHandler.addImplicitHydrogens(canMolecule);
		
		BufferedImage bufferedImage = new BufferedImage(300,300,BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = bufferedImage.createGraphics();
		
		
		graphics.setColor(Color.white);
		graphics.fillRect(0,0,300,300);
		Depictor2D depictor = new Depictor2D(canMolecule);
		Rectangle2D.Double viewRect = new Rectangle2D.Double();
		viewRect.setRect(0, 0, 300, 300);
		depictor.updateCoords(graphics, viewRect, Depictor.cModeInflateToMaxAVBL);
		depictor.paint(graphics);
		try {
			ImageIO.write(bufferedImage, "png", new File(argv[1]+".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
