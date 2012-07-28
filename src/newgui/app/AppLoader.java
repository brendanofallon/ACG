package newgui.app;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Small class with mostly static methods that looks in some standard locations
 * for a .jar file that we can load to actually begin the application
 * @author brendanofallon
 *
 */
public class AppLoader {

	public static String defaultDataDir = ".acgdata";
	
	public static File findLatestJar() {
		String baseDir = System.getProperty("user.dir");
		String fileSep = System.getProperty("file.separator");

		File latestJar = null;
		Integer latestVersion = 0;
		//Search a few standard places for acg jar files.. start with .acgData in user.dir
		File dataDir = new File(baseDir + fileSep + defaultDataDir);
		if (dataDir.exists() && dataDir.isDirectory()) {
			File[] files = dataDir.listFiles();
			for(int i=0; i<files.length; i++) {
				if (files[i].getName().endsWith(".jar")) {
					Integer version = parseVersion(files[i].getName());
					if (version != null && version > latestVersion) {
						latestJar = files[i];
						latestVersion = version;
					}
				}
			}
		}
		
		//Now try searching user.dir
		File appDir = new File(baseDir);
		if (appDir.exists() && appDir.isDirectory()) {
			File[] files = appDir.listFiles();
			for(int i=0; i<files.length; i++) {
				if (files[i].getName().endsWith(".jar")) {
					Integer version = parseVersion(files[i].getName());
					if (version != null && version > latestVersion) {
						latestJar = files[i];
						latestVersion = version;
					}
				}
			}
		}
		
		
		return latestJar;
	}
	
	/**
	 * Attempt to parse the version number from the given filename
	 * This is the integer parsed from the last token of the given string
	 * when separated by "-"
	 * thus filenames should look like "acgdata-2012-5-29-317.jar", 
	 * and 317 will be the version number
	 * @param filename
	 * @return
	 */
	private static Integer parseVersion(String filename) {
		String[] toks = filename.replace(".jar", "").split("-");
		try {
			Integer version = Integer.parseInt(toks[toks.length-1]);
			return version;
		}
		catch (NumberFormatException nfe) {
			
		}
		return null;
	}

	public static void runApplication(String[] args) {
		File latestJar = findLatestJar();
		
		if (latestJar == null) {
			System.err.println("Could not find application jar file, cannot run ACG");
			return;
		}
		
		Class<ACGApp> classApp = null;
		try {
			URL url = new URL("jar:file://" + latestJar.getAbsolutePath() + "!/" );
			ClassLoader clazzLoader = new URLClassLoader(new URL[]{url});
			classApp = (Class<ACGApp>) clazzLoader.loadClass(ACGApp.class.getCanonicalName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		if (classApp == null) {
			System.err.println("Could not create main application instance, aborting");
		}
		
		ACGApp app = null;
		try {
			app = classApp.newInstance();
			app.startup(args);
			System.err.println("Running ACG version " + app.getVersionNum());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {
		runApplication(args);
	}
}
