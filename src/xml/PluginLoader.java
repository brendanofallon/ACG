package xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * A class to load plugins from a list of potential sources. Sources are files, the list
 * of which is contained in the pluginPaths field. These files are not directories but should
 * all end in "jar" or "class", denoting actual files that may contain classes. 
 * @author brendan
 *
 */
public class PluginLoader {

	List<File> pluginPaths = new ArrayList<File>();
	
	public PluginLoader() {
		
	}
	
	public PluginLoader(String path) {
		addPluginPath(path);
	}
	
	/**
	 * Add the given file to the list of places to look for classes. If a directory,
	 * we add all files in the directory that end in .class or .jar to the list of places to
	 * look for classes.
	 * 
	 * @param file
	 */
	public void addPluginPath(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.endsWith("class") || name.endsWith("jar"))
							return true;
						else
							return false;
					}
				});
				
				for(int i=0; i<files.length; i++) {
					System.out.println("Adding plugin : " + files[i]);
					pluginPaths.add(files[i]);
				}
			}
			else {
				System.out.println("Added plugin : " + file);
				pluginPaths.add(file);
			}
		}
		else {
			System.out.println("File does not exist, cannot add file: " + file.getPath());
		}
	}
	
	/**
	 * Assumes path indicates either a directory or a file containing classes, then adds
	 * it to the list exactly as in addPluginPath(file)  
	 * @param path
	 */
	public void addPluginPath(String path) {
		File file = new File(path);
		addPluginPath(file);		
	}
	
	public Class getClassFromFile(File file, String name) throws Exception {
		URLClassLoader clazzLoader;
		Class clazz;
		String filePath = file.getAbsolutePath();
		if (filePath.endsWith("jar"))
			filePath = "jar:file://" + filePath + "!/";
		else
			filePath = "file://" + filePath + "!/";
		URL url = new URL(filePath);
		clazzLoader = new URLClassLoader(new URL[]{url});
		clazz = clazzLoader.loadClass(name);
		return clazz;
	}
	
	/**
	 * Search all pluginPaths for a class with the matching name. If found, load the class and return it.  
	 * 
	 * @param className Fully qualified name of class to find and load
	 * @return The first class encountered with the given name
	 */
	public Class getClassForName(String className) {
		for(File file : pluginPaths) {
			try {
				Class loadedClass = getClassFromFile(file, className);
				return loadedClass;
				
			} catch (Exception e) {
				//We don't care about this, there may be many paths to check and some won't have the
				//file we're looking for.
				System.out.println("Caught exception looking for class " + className + " in file : " + file.getPath() + ": \n" + e);
			}
		}
		
		//If we're here, we didn't find the specified class in any of the files listed in the pluginPaths
		// so we attempt to load it here. 
		try {
			Class clazz = ClassLoader.getSystemClassLoader().loadClass(className);
			return clazz;
		} catch (ClassNotFoundException e) {
			System.out.println("Could not find class " + className + " in any pluginPath or in the system loader.");
		}
		return null;
	}

	
	/**
	 * Return an object whose class is the given (fully qualified) name, created using the nullary constructor 
	 * @param className
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Object getObjForClass(String className) throws InstantiationException, IllegalAccessException {
		Object obj = null;

		try {
			for(File file : pluginPaths) {
				Class loadedClass = getClassFromFile(file, className);
				obj = loadedClass.newInstance();
			}
		} catch (Exception e) {

		}
		
		return obj;
	}
	
	/**
	 * A debugging method that just lists all of the classes found in jar files. 
	 */
	public void listClasses() {
		for(File file : pluginPaths) {
			try {
				JarInputStream jis =  new JarInputStream(new FileInputStream(file));
			
				JarEntry entry = jis.getNextJarEntry();
				while(entry != null) {
					System.out.println("Found : " + entry.getName() );
					entry = jis.getNextJarEntry();
				}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	}

}

