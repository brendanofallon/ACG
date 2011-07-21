package arg.argIO;

import java.io.File;
import java.io.IOException;

import arg.ARG;

public interface ARGWriter {
	
	public void writeARG(ARG arg, File file) throws IOException;

}
