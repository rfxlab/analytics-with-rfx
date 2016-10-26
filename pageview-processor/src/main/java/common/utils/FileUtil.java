package common.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	public static void listFilesForFolder(final File folder, List<String> files) {
		File[] fList = folder.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				files.add(file.getAbsolutePath());
			} else if (file.isDirectory()) {
				listFilesForFolder(file, files);
			}
		}
	}
	
	public static String createDirectory(String directoryPath) {
	    File dir = new File(directoryPath);
	    if (!dir.isDirectory()) {
	        dir.mkdir();
        }
	    return dir.getAbsolutePath();
	}

	public static void main(String[] args) {
		final File folder = new File("/home/dungth5/Downloads/day=2014-10-24");
		List<String> files = new ArrayList<String>();
		listFilesForFolder(folder, files);
		System.out.println(files);
	}
	
	public static void checkAndCreateDirectories(String directoryName){
		File theDir = new File(directoryName);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    System.out.println("creating directory: " + directoryName);
		    boolean result = false;

		    try{
		        theDir.mkdirs();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}
	}
}
