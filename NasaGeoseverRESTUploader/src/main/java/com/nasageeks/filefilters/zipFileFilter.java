/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nasageeks.filefilters;

import java.io.File;
import java.io.FileFilter;
/**
 *
 * @author olmozavala
 */
public class zipFileFilter implements FileFilter{

	public boolean accept(File pathname) {
		if(pathname.getName().endsWith(".zip"))
			return true;
		else
			return false;
	}
}

