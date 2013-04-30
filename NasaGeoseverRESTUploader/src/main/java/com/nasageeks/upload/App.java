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

		String RESTURL = "http://localhost:8080/geoserver";
		String RESTUSER = "nasa";
		String RESTPW = "isac13";
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

			String[] geotTiffFiles = FileManager.filesInFolder(filePath, new geotiffFileFilter());

			GeoserverNASARestManager restMan = new GeoserverNASARestManager(RESTURL, RESTUSER, RESTPW);

			String layersOutputFile = layersPath + "NewLayers.xml";
			String layersInputFile = "";

			String store;
			//Iterates over every file
			for (int fileNumber = 0; fileNumber < geotTiffFiles.length; fileNumber++) {
				File geotiff = new File(geotTiffFiles[fileNumber]);

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
			}

			String[] mosaicFiles = FileManager.filesInFolder(filePath, new zipFileFilter());

			//Iterates over every file
			basestore = basestore+ "_pyramid";

			for (int fileNumber = 0; fileNumber < mosaicFiles.length; fileNumber++) {
				File pyramid = new File(mosaicFiles[fileNumber]);

				store = basestore+ "_" + fileNumber;

				//Publishes the geotiff file
				BoundaryBox bbox = restMan.publishMosaic(pyramid, workspace, store);

				//Updates the XML of the map viewer.
				ViewerManager.addNewLayer(FileManager.removeExt(pyramid.getName()), server, workspace, bbox, layersInputFile, layersOutputFile);
			}

		} catch (Exception ex) {
			Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				file.close();
			} catch (IOException ex) {
				Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}