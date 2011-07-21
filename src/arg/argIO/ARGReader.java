package arg.argIO;

import java.io.File;
import java.io.IOException;
import java.util.List;

import arg.ARG;
import arg.ARGNode;

public interface ARGReader {

	public ARG readARG(File file) throws IOException, ARGParseException;
	
}
