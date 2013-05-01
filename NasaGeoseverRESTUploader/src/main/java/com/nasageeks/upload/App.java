package com.nasageeks.upload;

import com.nasageeks.filefilters.geotiffFileFilter;
import com.nasageeks.filefilters.zipFileFilter;
import com.nasageeks.restmanager.GeoserverNASARestManager;
import com.nasageeks.viewer.ViewerManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Automatic application that upload NASA geotiff files into a Geoserver website
 *
 */
public class App {

	public static void main(String[] args) {

		String RESTURL = "http://localhost/geoserver";
		String RESTUSER = "admin";
		String RESTPW = "geoserver";
		FileInputStream file = null;

		try {
			Properties props = new Properties();
			file = new FileInputStream("./Params.properties");
			props.load(file);

			// write 
			String workspace = props.getProperty("workspace");
			String server = props.getProperty("server");
			String filePath = props.getProperty("filePath");
			String basestore = props.getProperty("store");
			String layersPath = props.getProperty("layers");
			System.out.println("Layers ---- "+layersPath);
			System.out.println("Files ---- "+filePath);

			GeoserverNASARestManager restMan = new GeoserverNASARestManager(RESTURL, RESTUSER, RESTPW);

			String layersOutputFile = layersPath + "NewLayers.xml";
			String layersInputFile = "";

			String store;

			String[] mosaicFiles = FileManager.filesInFolder(filePath, new zipFileFilter());

			//Iterates over every file
			basestore = basestore+ "_pyramid";

			System.out.println("Reading Pyramid (zip files)....");
			File pyramid = null;
			for (int fileNumber = 0; fileNumber < mosaicFiles.length; fileNumber++) {
				try{
					pyramid = new File(mosaicFiles[fileNumber]);

					System.out.println("DEBUG 1");

					store = basestore+ "_" + fileNumber;
					System.out.println("DEBUG 2");

					//Publishes the geotiff file
					BoundaryBox bbox = restMan.publishMosaic(pyramid, workspace, store);
					System.out.println("DEBUG 3");

					//Updates the XML of the map viewer.
					ViewerManager.addNewLayer(FileManager.removeExt(pyramid.getName()), server, workspace, bbox, layersInputFile, layersOutputFile);
					System.out.println("DEBUG 4");
				} catch (Exception ex) {
					System.out.println("ERROR! Fail to upload PYRAMID file "+pyramid.getName());
				}
			}

			String[] geotTiffFiles = FileManager.filesInFolder(filePath, new geotiffFileFilter());
			
			basestore = props.getProperty("store");

			//Iterates over every file
			System.out.println("Reading Geotiff files....");
			File geotiff = null;
			for (int fileNumber = 0; fileNumber < geotTiffFiles.length; fileNumber++) {
				try{
					geotiff = new File(geotTiffFiles[fileNumber]);

					store = basestore+ "_" + fileNumber;

					//Publishes the geotiff file
					BoundaryBox bbox = restMan.publishGeoTiff(geotiff, workspace, store);

					if (fileNumber == 0) {
						layersInputFile = layersPath + "Default/Layers.xml";
					} else {
						layersInputFile = layersOutputFile;
					}

					//Updates the XML of the map viewer.
					ViewerManager.addNewLayer(geotiff.getName(), server, workspace, bbox, layersInputFile, layersOutputFile);
				} catch (Exception ex) {
					System.out.println("ERROR! Fail to upload GeoTIFF file "+pyramid.getName());
				}
			}


		} catch (Exception ex) {
			System.out.println("Exception: "+ex.getMessage());
		} finally {
			try {
				file.close();
			} catch (IOException ex) {
				System.out.println("IOException: "+ex.getMessage());
			}
		}
	}
}
