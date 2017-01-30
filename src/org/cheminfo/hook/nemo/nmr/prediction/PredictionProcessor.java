package org.cheminfo.hook.nemo.nmr.prediction;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.cheminfo.hook.appli.ConsoleNemoWrapper;
import org.cheminfo.hook.converter.Converter;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.Integral;
import org.cheminfo.hook.nemo.Nemo;
import org.cheminfo.hook.nemo.SmartPeakLabel;
import org.cheminfo.hook.nemo.Spectra;
import org.cheminfo.hook.nemo.SpectraData;
import org.cheminfo.hook.nemo.SpectraDisplay;
import org.cheminfo.hook.nemo.nmr.Nucleus;

public class PredictionProcessor {

	private String type;
	private File molDir;
	private boolean isInteractive = false;

	public PredictionProcessor(String type, String molDir, boolean isInteractive)
			throws Exception {
		this.type = type;
		if (!molDir.matches("^.*\\/[0-9]+$"))
			throw new Exception("not a valid directory (re)" + molDir);
		this.molDir = new File(molDir);
		if (!this.molDir.exists() || !this.molDir.isDirectory())
			throw new Exception("not a valid directory "
					+ this.molDir.getAbsolutePath());
		this.isInteractive = isInteractive;
	}

	public void process() {
		ConsoleNemoWrapper wrapper = new ConsoleNemoWrapper();
		Nemo nemo = wrapper.getNemo();
		nemo.addMolfile(this.readMolfile(this.molDir.getAbsolutePath()
				+ "/reference/structure.mol"));
		Nucleus nucleus = Nucleus.NUC_1H;
		String specFilename = nucleus.toString() + "_nmr_spectrum.dx";
		nemo.addJcamp("file://" + this.molDir.getAbsolutePath() + "/reference/"
				+ specFilename.toLowerCase());
		// nemo.addSpectrum(this.loadSpectrum(this.molDir.getAbsolutePath()
		// + "/reference/" + specFilename.toLowerCase()));
		nemo.addSimulatedSpectrum(this.getSimulatedSpectrum(nucleus),1);
		if (!this.isInteractive) {
			if (nemo.getInteractions().getActiveDisplay() instanceof SpectraDisplay) {
				SpectraDisplay display = (SpectraDisplay) nemo
						.getInteractions().getActiveDisplay();
				Spectra spectrum = display.getLastSpectra();
				int nEntities = spectrum.getEntitiesCount();
				LinkedList<BasicEntity> entities = new LinkedList<BasicEntity>();
				for (int ent = 0; ent < nEntities; ent++) {
					if (spectrum.getEntity(ent) instanceof SmartPeakLabel)
						entities.add(spectrum.getEntity(ent));
					if (spectrum.getEntity(ent) instanceof Integral)
						entities.add(spectrum.getEntity(ent));
				}
				Iterator<BasicEntity> iterator = entities.iterator();
				while (iterator.hasNext()) {
					spectrum.remove(iterator.next());
				}
			}
		}
		//
		int width = 1024;
		int height = 768;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - width) / 2;
		int y = (screen.height - height) / 2;
		wrapper.setBounds(x, y, width, height);
		InteractiveSurface interactions = nemo.getInteractions();
		BufferedImage tempImage = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D tempGraphics = tempImage.createGraphics();
		interactions.paint(tempGraphics);
		try {
			ImageIO.write(tempImage, "png", new File(this.molDir
					.getAbsolutePath()
					+ "/eval/" + this.type + "_overlay.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		wrapper.setVisible(this.isInteractive);
	}

	public static void main(String[] argv) {
		try {
			if (argv.length < 2 || argv.length > 3) {
				System.out
						.println("no params! Usage: java -jar xxx.jar <type> <dir>");
				System.exit(-1);
			}
			boolean isInteractive = false;
			if (argv.length == 3)
				isInteractive = true;
			PredictionProcessor processor = new PredictionProcessor(argv[0],
					argv[1], isInteractive);
			processor.process();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public String getSimulatedSpectrum(Nucleus nucleus) {
		String spectrum = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					this.molDir.getAbsoluteFile() + "/eval/" + this.type
							+ "_SPECTRA_STRINGS"));
			String line;
			String nucleusString = nucleus.toString();
			while ((line = reader.readLine()) != null) {
				if (line.indexOf(nucleusString) != -1) {
					spectrum = line;
					break;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return spectrum;
	}

	public String readMolfile(String molFile) {
		String contents = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(molFile));
			String line;
			while ((line = reader.readLine()) != null)
				contents += line + "\n";
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contents;
	}

	public SpectraData loadSpectrum(String spectrumFile) {
		String stringUrl = "file://" + spectrumFile;
		System.out.println("[" + stringUrl + "]");
		Converter tempConverter = Converter.getConverter("Jcamp");
		SpectraData spectraData = new SpectraData();
		URL correctUrl = null;
		// we need the URL
		// we first try to convert it directly
		try {
			correctUrl = new URL(stringUrl);
		} catch (MalformedURLException e) {
			System.out.println("Not found : " + stringUrl);
			return null;
		}
		System.out.println("Loading from " + String.valueOf(correctUrl));
		if (tempConverter == null)
			System.out.println("Converter -> null");
		if (tempConverter.convert(correctUrl, spectraData)) {
			spectraData.setActiveElement(0);
		} else {
			System.out.println("Could Not Access File " + correctUrl);
			return null;
		}
		return spectraData;
	}

}
