package org.cheminfo.hook.ui;

import java.io.IOException;

import com.actelion.research.nemo.jcamp.Jcamp;

public interface LoadSaveHandler<T> {
	/**
	 * Handles loading of a Jcamp. This function MUST return a valid Jcamp
	 * @param ressource Ressource to get data from
	 * @param nemoInstance Current NemoInstance, may be used to access additional data
	 * @return Jcamp
	 * @throws IOException
	 */
	Jcamp load(T ressource, NemoInstance<T> nemoInstance) throws IOException;
	
	/**
	 * Handles saving of the current Jcamp. This function MUST return the actual ressource!
	 * @param ressource Ressource to handle
	 * @param jdx Jcamp data
	 * @param nemoInstance Current NemoInstance, may be used to access additional data
	 * @return Current Ressource
	 * @throws IOException
	 */
	T save(T ressource, Jcamp jdx, NemoInstance<T> nemoInstance) throws IOException;
}
