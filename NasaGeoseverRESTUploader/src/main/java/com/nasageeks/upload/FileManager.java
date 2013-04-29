/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nasageeks.upload;

import com.nasageeks.filefilters.xmlFileFilter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Date;

/**
 *This class searches the folders for files
 * @author olmozavala
 */
public class FileManager {

	/**
	 * Only used to display the current directory. Useful to find the properties file.
	 */
	public static void showCurrentDir() throws IOException{
			String current = new java.io.File(".").getCanonicalPath();
			System.out.println("Current dir:" + current);
			String currentDir = System.getProperty("user.dir");
			System.out.println("Current dir using System:" + currentDir);
	}

	/**
	 * It obtains the number of files for an specific folder. 
	 * @param {String} folder path
	 * @return int Number of files for that folder
	 */
	public static int numberOfFilesInFolder(String folder){
		File searchFolder = new File(folder);

		File[] files = searchFolder.listFiles(new xmlFileFilter());
		return files.length;
	}
	/**
	 * Searches the list of files inside a folder. 
	 * @param folder
	 * @return String[] List of files inside the folder. 
	 */
	public static String[] filesInFolder(String folder, FileFilter filter) {

		File searchFolder = new File(folder);

		File[] files = searchFolder.listFiles(filter);
		String[] xmlFiles = new String[files.length];
		int idx = 0;
		for (File currFile: files) {
			xmlFiles[idx] = currFile.getAbsolutePath();
			idx++;
		}

		if(idx == 0){//No file was found
			System.out.println("There is no XML files for the layers configuration" + " at: " + folder +" !!!");
		}
		return xmlFiles;
	}

	/**
	 * Retrieves the last date a file has been modified. 
	 * @param fileName
	 * @return Date Last modification.
	 */
	public static Date lastModification(String fileName){
		File file = new File(fileName);
		return new Date(file.lastModified());
	}

	/**
	 * It removes the extension of a file name.
	 * @param fileName
	 * @return {String} File name without extension
	 */
	public static String removeExt(String fileName){
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}
}
