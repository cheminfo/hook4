package org.cheminfo.hook.appli;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheminfo.hook.nemo.nmr.ProprietaryTools;

/**
 * This class allows to generate a molfile with exploded hydrogen
 */

public class ConvertMolfileServlet extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		String molfile = request.getParameter("molfile");
		
		//System.out.println(molfile);
		
		String newMolfile=ProprietaryTools.canonizeMolfile(molfile);
	
		PrintWriter writer = response.getWriter();
		writer.print(newMolfile);
		writer.close();
	}


	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}
}
