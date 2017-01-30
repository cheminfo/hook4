package org.cheminfo.hook.ui;

import java.io.File;
import java.io.IOException;

import org.cheminfo.hook.util.NemoPreferences;

import com.actelion.research.nemo.jcamp.Jcamp;

public class FileLoadSaveHandler implements LoadSaveHandler<File> {	

	public Jcamp load(File ressource, NemoInstance<File> nemoInstance) throws IOException {
		NemoPreferences.getInstance().set(NemoPreferences.getInstance().MOST_RECENT_PATH, ressource);
		return new Jcamp(ressource);
	}

	public File save(File ressource, Jcamp jdx, NemoInstance<File> nemoInstance) throws IOException {
		NemoPreferences.getInstance().set(NemoPreferences.getInstance().MOST_RECENT_PATH, ressource);
		jdx.save(ressource);
		return ressource;
	}
}
