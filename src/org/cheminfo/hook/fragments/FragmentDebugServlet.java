package org.cheminfo.hook.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;

public class FragmentDebugServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2707856191703678818L;
	private static final String graphicServletUrl = "http://cheminfo.epfl.ch/cheminfo/servlet/com.actelion.research.chem.MEAServlet?moleculeId=";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String attribute = req.getParameter("id");

		PrintWriter output = resp.getWriter();
		output.print("hello world");
		output.print(attribute);
		output.close();

	}

	public static void main(String[] argv) {
		FragmentDebugServlet servlet = new FragmentDebugServlet();
		if (argv.length == 2) {
			StereoMolecule molecule = new StereoMolecule();
			MolfileParser parser = new MolfileParser();
			parser.parse(molecule, new File(argv[0]));
			Canonizer canonizer = new Canonizer(molecule);
			String idCode = canonizer.getIDCode();
			String content = servlet.generatHtmlContent(idCode);
			try {
				PrintWriter printWriter = new PrintWriter(argv[1]);
				printWriter.print(content);
				printWriter.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Usage: <ID> <output file>");
			System.exit(1);
		}
	}

	public String generatHtmlContent(String idCode) {
		String result = "";
		result += "<html>\n";
		result += "<body>\n";
		result += "<h1>Original molecule<h1>\n";
		result += this.getImgUrl(idCode);

		IDCodeParser parser = new IDCodeParser();
		StereoMolecule inputMolecule = new StereoMolecule();
		parser.parse(inputMolecule, idCode);

		result += "<h1>Fragments<h1>\n";
		int nAtoms = inputMolecule.getAllAtoms() > 6 ? 6 : inputMolecule.getAllAtoms();
		FragmentGeneratorActelionId generator = new FragmentGeneratorActelionId();
		generator.setMolecule(inputMolecule);
		for (int iFragmentSize = 1; iFragmentSize <= nAtoms; iFragmentSize++) {
			result += "<h2> size: " + iFragmentSize + "</h2>\n";
			generator.setMinAtoms(iFragmentSize);
			generator.setMaxAtoms(iFragmentSize);
			TreeMap<String, Occurrence> fragments = generator.generate();
			Set<String> keys = fragments.keySet();
			Iterator<String> iterator = keys.iterator();
			result += "<table border=\"1\">\n";
			result += "<tr><td>fragment</td><td>occurrence</td></tr>\n";
			while (iterator.hasNext()) {
				String key = iterator.next();
				result += "<tr><td>" + this.getImgUrl(key) + "</td><td>"
						+ fragments.get(key).getOccurrence() + "</td><tr>\n";
			}
			result += "</table>\n";
		}
		result += "</body>\n";
		result += "</html>\n";
		return result;
	}
	
	private String getImgUrl(String idCode) {
		return "<img src=\"" + FragmentDebugServlet.graphicServletUrl + idCode + "\">";
	}

}
