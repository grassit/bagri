package com.bagri.common.util;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;


public class FileUtils {
	
	private final static Charset ENCODING = StandardCharsets.UTF_8;
	
	public static String readTextFile(String fileName) throws IOException {
	    Path path = Paths.get(fileName);
	    StringBuilder text = new StringBuilder();
	    try (Scanner scanner = new Scanner(path, ENCODING.name())){
	    	while (scanner.hasNextLine()) {
	    		text.append(scanner.nextLine()).append("\n");
	    	}      
	   	}
	    return text.toString();
	}

	public static Properties propsFromString(String properties) throws IOException {
		Properties props = new Properties();
		properties = properties.replaceAll(";", "\n\r");
		props.load(new StringReader(properties));
		return props;
	}
	
	public static String uri2Path(String uri) {
		if (uri.startsWith("file:/")) {
			return Paths.get(URI.create(uri)).toString();
		}
		return uri;
	}
	
	public static String path2Uri(String path) {
		if (!path.startsWith("file:/")) {
			return Paths.get(path).toUri().toString();
		}
		return path;
	}
	
}
