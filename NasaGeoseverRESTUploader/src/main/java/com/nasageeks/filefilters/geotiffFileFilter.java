
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
public class geotiffFileFilter implements FileFilter{

	public boolean accept(File pathname) {
		if( (pathname.getName().endsWith(".tiff")) || (pathname.getName().endsWith(".tif")) )
			return true;
		else
			return false;
	}
}
