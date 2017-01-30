package org.cheminfo.hook.fragments.tests;

import com.actelion.research.chem.Depictor;
import com.actelion.research.chem.Depictor2D;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;
import org.cheminfo.hook.fragments.EPFLIDCodeParser;
import org.cheminfo.hook.fragments.FragmentGenerator;
import org.cheminfo.hook.fragments.Occurrence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

public class FragmentGeneratorTester {
	private File inputDir;
	private File outputDir;
	// internal stuff
	private long timestamp;
	LinkedList<String> molFiles;
	StereoMolecule molecule;
	private File currentOutputDir;
	private PrintStream htmlStream;

	private long idCounter;
	
	
	public FragmentGeneratorTester() {
		inputDir = null;
		outputDir = null;
		molFiles = new LinkedList<String>();
	}

	public void runTest() {
		this.timestamp = System.currentTimeMillis();
		if (!inputDir.isDirectory())
			die("not an input directory");
		String[] files = inputDir.list();
		if (files == null)
			die("no mol files found!");
		for (int i = 0; i < files.length; i++) {
			if (files[i].matches(".*\\.mol$")) {
				molFiles.add(files[i]);
			}
		}
		if (molFiles.size() == 0)
			die("no mol files found!");
		Iterator<String> iterator = molFiles.iterator();
		while (iterator.hasNext())
			this.runTest4File(iterator.next());
	}

	private void runTest4File(String molFile) {
		System.out.println("file=" + molFile);
		this.idCounter = 0;
		this.molecule = new StereoMolecule();
		MolfileParser parser = new MolfileParser();
		if (!parser.parse(this.molecule, new File(inputDir.getAbsolutePath()
				+ "/" + molFile)))
			die("failed to parse " + molFile);
		openHTMLFile(molFile);

		FragmentGenerator generator = new FragmentGenerator();
		generator.setMolecule(this.molecule);
		int maxAtoms = this.molecule.getAllAtoms() > 6 ? 6 : this.molecule.getAllAtoms();
		for (int nAtoms = 1; nAtoms <= maxAtoms; nAtoms++) {
			generator.setMinAtoms(nAtoms);
			generator.setMaxAtoms(nAtoms);
			TreeMap<String, Occurrence> fragments;
			try {
				fragments = generator.generate();
				addFragmentSection(fragments, nAtoms);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		closeHTMLFile();
	}

	private void openHTMLFile(String file) {
		String dir = this.outputDir.getAbsolutePath() + "/" + file;
		this.currentOutputDir = new File(dir);
		this.currentOutputDir.mkdir();
//		if (!this.currentOutputDir.mkdir())
//			die("");
		try {
			this.htmlStream = new PrintStream(this.currentOutputDir
					.getAbsoluteFile()
					+ "/index.html");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			die("");
		}
		this.htmlStream.println("<html>");
		this.htmlStream.println("<body>");
		this.htmlStream.println("<h1>Test for: "
				+ this.inputDir.getAbsolutePath() + "/" + file + "</h1>");
		this.Molecule2Image(this.molecule, "original");
		this.htmlStream.println("<img src=\"original.png\"/>");
	}

	private void Molecule2Image(StereoMolecule molecule, String filename) {
		int width = 200;
		int height = 100;
		String path = this.currentOutputDir.getAbsolutePath() + "/" + filename;
		Depictor2D depictor = new Depictor2D(molecule);

		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, width, height);
		Rectangle2D.Double viewRect = new Rectangle2D.Double();
		viewRect.setRect(0, 0, width, height);
		depictor.simpleUpdateCoords(viewRect, Depictor.cModeInflateToMaxAVBL);
		depictor.paint(g2d);
		try {
			ImageIO.write(bufferedImage, "png", new File(path + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void closeHTMLFile() {
		this.htmlStream.println("</body>");
		this.htmlStream.println("</html>");
		this.htmlStream.close();
	}

	private void addFragmentSection(TreeMap<String, Occurrence> fragments,
			int size) throws UnsupportedEncodingException {
		System.out.println("fragmentSize=" + size);
		this.htmlStream.println("<h2>Fragment size: " + size + "</h2>");
		this.htmlStream.println("<table border=\"1\">");
		Set<String> idCodes = fragments.keySet();
		Iterator<String> iterator = idCodes.iterator();
		while (iterator.hasNext()) {
			String idCode = iterator.next();
			StereoMolecule fragment = EPFLIDCodeParser.parseIdCode(idCode);
			Occurrence occurrence = fragments.get(idCode);
			String filename = "fragment" + "_size=" + size + "_occ="
					+ occurrence.getOccurrence()
					+ "_" + (++this.idCounter);
			filename = filename.replace('%', '_');
			Molecule2Image(fragment, filename);
			this.htmlStream.println("<tr>");
			this.htmlStream.println("<td><img src=\"" + filename
					+ ".png\"/></td>" + "<td>" + idCode + "</td>");
			this.htmlStream.println("<td>" + occurrence.getOccurrence()
					+ "</td>");
			this.htmlStream.println("</tr>");
		}
		this.htmlStream.println("</table>");
	}

	public static void die(String message) {
		System.out.println(message);
		System.exit(1);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FragmentGeneratorTester tester = new FragmentGeneratorTester();
		String inputDir = "/home/engeler/source/molFiles/";
		tester.setInputDir(inputDir);
		String outputDir = "/home/engeler/source/molFiles/debug/";
		tester.setOutputDir(outputDir);
		tester.runTest();
	}

	public File getInputDir() {
		return inputDir;
	}

	public void setInputDir(String inputDir) {
		this.inputDir = new File(inputDir);
	}

	public File getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = new File(outputDir);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
