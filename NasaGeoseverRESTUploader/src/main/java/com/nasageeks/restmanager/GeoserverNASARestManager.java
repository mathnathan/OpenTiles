/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nasageeks.restmanager;

import com.nasageeks.upload.BoundaryBox;
import com.nasageeks.upload.FileManager;
import gov.nasa.worldwind.formats.tiff.GeoCodec;
import gov.nasa.worldwind.formats.tiff.GeotiffReader;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTCoverage;
import it.geosolutions.geoserver.rest.decoder.RESTDimensionInfo;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * This file should contain the functions related with the upload of geotiff files into
 * geoserver
 *
 * @author olmozavala
 */
public class GeoserverNASARestManager {

	GeoServerRESTReader readerGeoserver;
	GeoServerRESTPublisher publisherGeoserver;
	String server;

	public GeoserverNASARestManager(String server, String user, String psw) throws MalformedURLException {
		readerGeoserver = new GeoServerRESTReader(server, user, psw);
		publisherGeoserver = new GeoServerRESTPublisher(server, user, psw);
		this.server = server;
	}

	public BoundaryBox publishGeoTiff(File geoTiffFile, String workspace, String store) throws IOException {

		String fileName = geoTiffFile.getName();
		System.out.println("FileName: " + fileName);
		String coverageName = fileName;

		String srs = "EPSG:4326";
		String def = "default";

		ProjectionPolicy proj = ProjectionPolicy.REPROJECT_TO_DECLARED;

		//Adding the nasa workspace into geoserver (only once should be done)
		boolean created = publisherGeoserver.createWorkspace(workspace);
		if (created) {
			System.out.println("The workspace " + workspace + " has been created, yeah!");
		}

		//Reads the Geotiff file and extracts the boundaries
		GeotiffReader reader = new GeotiffReader(geoTiffFile.getAbsoluteFile());
		if (reader.isGeotiff()) {
			GeoCodec geo = reader.getGeoCodec();

			//now get bounding box coordinates for entire image
			double[] bbox = geo.getBoundingBox(reader.getWidth(0), reader.getHeight(0));
			BoundaryBox BBOX = new BoundaryBox(bbox[0], bbox[2], bbox[3], bbox[1]);

			//---- Creates one new store for file

			System.out.println("------- Pusblishing Geotiff file name: " + fileName + " with BBOX: " + BBOX.toString() + " in store:" + store);
			created = publisherGeoserver.publishGeoTIFF(workspace, store, coverageName, geoTiffFile, srs, proj, def, bbox);
			if (created) {
				System.out.println("------------ Layer " + workspace + ":" + coverageName + " added SUCCESSFULLY!");
			} else {
				System.out.println("WARNING Adding layer " + workspace + ":" + coverageName + " (it may already exist in Geoserver)");
				//We should go to the next file here, but because the previous error
				// can occur if we try to upload one layer that is already in geoserver
				// in that case we should still modify the XML file for visualization
//						break;//Try with the next file
			}

			//--------- Read the newly uploaded layer and change its Meta data and boundary box
			RESTCoverage coverage = readerGeoserver.getCoverage(workspace, store, coverageName);
			System.out.println(coverage.getName() + " ..max:" + coverage.getMaxX());

			GSCoverageEncoder layerEncoder = new GSCoverageEncoder();
			layerEncoder.setNativeBoundingBox(bbox[0], bbox[3], bbox[2], bbox[1], srs);
			layerEncoder.setName(coverage.getName());
			boolean success = publisherGeoserver.configureCoverage(layerEncoder, workspace, store);
			if (success) {
				System.out.println("--------- BBOX has been updated for " + coverageName);
			} else {
				System.out.println("WARN BBOX FAIL to be updated for " + coverageName);
			}

			return BBOX;
		}
		return null;
	}

	public BoundaryBox publishMosaic(File mosaicFile, String workspace, String store) throws IOException {

		String fileName = mosaicFile.getName();
		System.out.println("FileName: " + fileName);
		String coverageName = fileName;
		
		String srs = "EPSG:4326";
		String def = "default";
		
		ProjectionPolicy proj = ProjectionPolicy.REPROJECT_TO_DECLARED;
		
		//Adding the nasa workspace into geoserver (only once should be done)
		boolean created = publisherGeoserver.createWorkspace(workspace);
		if (created) {
			System.out.println("The workspace " + workspace + " has been created, yeah!");
		}
		
		//---- Creates one new store for file
		System.out.println("------- Pusblishing mosaic file name: " + mosaicFile.getAbsoluteFile() + " in store:" + store+ " workspace: " + workspace);
		created = publisherGeoserver.publishImageMosaic(workspace, store, mosaicFile);
		if (created) {
			System.out.println("------------ Layer " + workspace + ":" + coverageName + " added SUCCESSFULLY!");
		} else {
			System.out.println("WARN adding layer " + workspace + ":" + coverageName + " (it may already exist in Geoserver)"); //We should go to the next file here, but because the previous error
			// can occur if we try to upload one layer that is already in geoserver
			// in that case we should still modify the XML file for visualization
			//						break;//Try with the next file
		}

		//--------- Read the newly uploaded layer and change its Meta data and boundary box
		RESTCoverage cov = readerGeoserver.getCoverage(workspace, store, FileManager.removeExt(fileName));
		BoundaryBox bbox = new BoundaryBox(cov.getMinX(), cov.getMaxX(), cov.getMinY(), cov.getMaxY());

		return bbox;
	}
}
