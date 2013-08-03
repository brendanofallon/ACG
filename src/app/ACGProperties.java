package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



/**
 * A singleton class to read and store sets of key-value pairs that are accessed
 * by many components to set aspects of the look and feel.
 * @author brendan
 *
 */
public class ACGProperties {
	
	public static final String FIRST_RUN_DATE = "first.run";
	public static final String JAVA_VENDOR = "java.vendor";
	public static final String JAVA_VERSION = "java.version";
	public static final String OS_ARCH = "os.arch";
	public static final String OS_NAME = "os.name";
	public static final String LAST_FILE_READ = "last.file.read";
	public static final String LAST_WRITTEN = "last.written";
	public static final String LAST_READ = "last.read";
	public static final String VERSION = "version";
	public static final String ID = "id";
	
	private Map<String, String> props = new HashMap<String, String>();
	private static ACGProperties properties = null;
	
	private ACGProperties(File propsFile) throws IOException {
		properties = this; //Must be here
		readPropertiesFromFile(propsFile);
	}
	
	private static ACGProperties getProperties() {
		return properties;
	}
	
	private Map<String, String> getPropsMap() {
		return props;
	}
	
	public static void initialize(File propertiesFile) throws IOException {
		properties = new ACGProperties(propertiesFile);
	}
	
	/**
	 * Returns true if there is a property associated with the given key
	 * @param key
	 * @return
	 */
	public static boolean hasProperty(String key) {
		//If there was an exception during creation then properties may be null
		if (getProperties()==null) {
			return false;
		}
		return getProperties().getPropsMap().containsKey(key);
	}
	
	/**
	 * Obtain the String-valued property associated with the given key
	 * @param key
	 * @return
	 */
	public static String getProperty(String key) {
		//If there was an exception during creation then properties may be null
		if (getProperties()==null) {
			return null;
		}
		return getProperties().getPropsMap().get(key);
	}
	
	
	/**
	 * Obtain an integer-valued property, or null if no property exists with the given key
	 */
	public static Integer getIntegerProperty(String key) {
		String val = getProperty(key);
		if (val == null)
			return null;
		else
			return Integer.parseInt(val);
	}
	
	/**
	 * Obtain a Double-valued property from the set of properties, or null if no
	 * property associated with the given key exists
	 * @param key
	 * @return
	 */
	public static Double getDoubleProperty(String key) {
		String val = getProperty(key);
		if (val == null)
			return null;
		else
			return Double.parseDouble(val);
	}
	
	/**
	 * Add the given key-value pair to the properties map
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean addProperty(String key, String value) {
		Map<String, String> props = getProperties().getPropsMap();
		boolean contains = props.containsKey(key);
		props.put(key, value);
		return contains;
	}
	
	/**
	 * Attempt to read sets of key-value pairs from the given file. Blank lines and lines
	 * starting with # are skipped. Properties are stored one per line, in key=value form
	 * @param propsFile
	 * @throws IOException
	 */
	public static void readPropertiesFromFile(File propsFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(propsFile));
		String line = reader.readLine();
		
		Map<String, String> props = getProperties().getPropsMap();
		while (line != null) {
			line = line.trim();
			//Skip blank and comment line
			if (line.length()==0 || line.startsWith("#")) {
				line = reader.readLine();
				continue;
			}
			
			String[] toks = line.split("=");
			if (toks.length==2) {
				String key = toks[0];
				String val = toks[1];
				//System.out.println("Adding " + key + "=" + val);
				props.put(key, val);
			}
			line = reader.readLine();
		}
		
		reader.close();
		Date now = new Date();
		addProperty(LAST_READ, now.toString());
		addProperty(LAST_FILE_READ, propsFile.getAbsolutePath());
		addProperty(ACGProperties.VERSION, ACGApp.VERSION);
		
		if ( getProperty(FIRST_RUN_DATE) == null) {
			addProperty(FIRST_RUN_DATE, now.toString());
		}
		
		addProperty(JAVA_VENDOR, System.getProperty("java.vendor"));
		addProperty(JAVA_VERSION, System.getProperty("java.version"));
		addProperty(OS_ARCH, System.getProperty("os.arch"));
		addProperty(OS_NAME, System.getProperty("os.name"));
	}
	
	/**
	 * Write the properties list to whatever the last.file.read property is set to
	 * @throws IOException
	 */
	public static void writeToLastFileRead() throws IOException {
		if (! hasProperty(LAST_FILE_READ)) {
			throw new IllegalArgumentException("Property last.file.read does has not been set");
		}
		
		String lastFileReadPath = getProperty(LAST_FILE_READ);
		File file = new File(lastFileReadPath);
		writePropertiesToFile(file);
	}
	
	/**
	 * Create a new properties file and write some basic properties to it. 
	 * @param fileToCreate
	 * @throws IOException
	 */
	public static void createPropertiesFile(File fileToCreate) throws IOException {
		if (fileToCreate.exists()) {
			throw new IllegalArgumentException("Properties file " + fileToCreate.getAbsolutePath() + " already exists");
		}
		fileToCreate.createNewFile();
		
		Date now = new Date();
		addProperty(LAST_READ, now.toString());
		addProperty(LAST_FILE_READ, fileToCreate.getAbsolutePath());
		addProperty(ACGProperties.VERSION, ACGApp.VERSION);
		
		if ( getProperty(FIRST_RUN_DATE) == null) {
			addProperty(FIRST_RUN_DATE, now.toString());
		}
		
		addProperty(JAVA_VENDOR, System.getProperty("java.vendor"));
		addProperty(JAVA_VERSION, System.getProperty("java.version"));
		addProperty(OS_ARCH, System.getProperty("os.arch"));
		addProperty(OS_NAME, System.getProperty("os.name"));
		writePropertiesToFile(fileToCreate);
	}
	
	/**
	 * Emit all properties to the given file
	 * @param props
	 * @throws IOException 
	 */
	public static void writePropertiesToFile(File propsFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(propsFile));
		
		Date now = new Date();
		addProperty(LAST_WRITTEN, now.toString());
		
		String lineSep = System.getProperty("line.separator");
		for(String key : getProperties().getKeySet()) {
			writer.write(key + "=" + getProperty(key) + lineSep);
		}
		writer.close();
	}
	
	/**
	 * Obtain the set of keys used to index all properties
	 * @return
	 */
	private Collection<String> getKeySet() {
		return props.keySet();
	}
	
	private void clearProperties() {
		props = new HashMap<String, String>();
	}
	
	/**
	 * Remove all properties from the given set
	 */
	public static void reset() {
		properties.clearProperties();
	}
	
}
