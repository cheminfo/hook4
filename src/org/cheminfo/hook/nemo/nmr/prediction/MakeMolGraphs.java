package org.cheminfo.hook.nemo.nmr.prediction;

import com.actelion.research.chem.*;
import com.actelion.research.chem.coords.CoordinateInventor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class MakeMolGraphs {
	public final static String defaultTempDir = "/home/mynmrdb/public_html/data";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MakeMolGraphs.makeGraphs();
	}

	public static void makeGraphs() {
		File startDir = new File(defaultTempDir);
		if (!startDir.isDirectory() || !startDir.isAbsolute()) {
			System.err.println("not an absolute directory path: "
					+ startDir.getAbsolutePath());
			System.exit(-1);
		}
		MolfileParser parser = new MolfileParser();
		String[] files = startDir.list();
		for (int iFile = 0; iFile < files.length; iFile++) {
			if (files[iFile].endsWith(".mol")) {
				StereoMolecule molecule = new StereoMolecule();
				if (parser.parse(molecule, new File(startDir
						.getAbsolutePath()
						+ "/" + files[iFile]))) {
					String dbKey = files[iFile].replaceAll("\\.mol", "");
					molecule.ensureHelperArrays(3);
					Canonizer canonizer = new Canonizer(molecule);
					String idCode = canonizer.getIDCode();
					try {
						PrintWriter idWriter = new PrintWriter(defaultTempDir+"/"+dbKey+".id");
						idWriter.print(idCode);
						idWriter.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					
					 CoordinateInventor inventor = new CoordinateInventor();
					 inventor.invent(molecule);
					 int width = 300;
					 int height = 300;
					 BufferedImage image = new BufferedImage(width, height,
					 BufferedImage.TYPE_INT_RGB);
					 Graphics2D g2d = image.createGraphics();
					 g2d.setColor(Color.WHITE);
					 g2d.fillRect(0, 0, width, height);
					 g2d.setColor(Color.BLACK);
					 Depictor2D depictor = new Depictor2D(molecule);
					 Rectangle2D.Double viewRect = new Rectangle2D.Double();
					 viewRect.setRect(0, 0, width, width);
					 depictor.simpleUpdateCoords(viewRect,
					 Depictor.cModeInflateToMaxAVBL);
					 depictor.paint(g2d);
					 try {
						ImageIO.write(image, "png", new File(defaultTempDir + "/"
						 + dbKey + ".png"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}				
			}
		}		
	}


}
