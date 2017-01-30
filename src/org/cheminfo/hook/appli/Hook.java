package org.cheminfo.hook.appli;


/**
 * Solution/workaround to allow executable jars
 * to change their memory settings as Java cannot 
 * have its command line properties defined in a 
 * Jar file. Why Sun never implemented anything like 
 * this is beyond me.
 */
public class Hook {

	/**
	 * Bare minimum heap memory for starting app and allowing
	 * for reasonable sized images (note deliberate 1 MB
	 * smaller than 512 due to rounding/non accurate free 
	 * heap calculation). The solution allows JVMs with
	 * enough heap already to just start without spawning 
	 * a new process. 
	 */
	private final static int RECOMMENDED_HEAP = 384; 

	public static void main(String[] args) throws Exception {

		int memory=128;

		// Do we have enough memory already (some VMs and later Java 6 
		// revisions have bigger default heaps based on total machine memory)?
		float heapSizeMegs = (Runtime.getRuntime().maxMemory()/1024)/1024;

		// Yes so start
		if (heapSizeMegs > RECOMMENDED_HEAP) {
			memory=RECOMMENDED_HEAP;
		} else {
			memory=(int)heapSizeMegs;
		}

		String pathToJar = Hook.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		// ProcessBuilder pb = new ProcessBuilder("java","-XX:+AggressiveHeap", "-classpath", pathToJar, "org.cheminfo.hook.appli.ActNemoWrapper");
		ProcessBuilder pb = new ProcessBuilder("java","-Xmx"+memory+"m","-classpath", pathToJar, "org.cheminfo.hook.appli.ActNemoWrapper");
		pb.start();

	}
}