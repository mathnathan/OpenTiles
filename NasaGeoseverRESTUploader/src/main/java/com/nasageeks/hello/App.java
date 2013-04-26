package com.nasageeks.hello;

import com.nasageeks.filefilters.geotiffFileFilter;
import gov.nasa.worldwind.formats.tiff.GeoCodec;
import gov.nasa.worldwind.formats.tiff.GeotiffReader;
import it.geosolutions.geoserver.rest.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import com.nasageeks.hello.BoundaryBox;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.JDOMException;

/**
 * Automatic application that upload NASA geotiff files into a Geoserver website
 *
 */
public class App {

	/**
	 * Only used to display the current directory. Useful to find the properties file.
	 */
	public static void showCurrentDir() throws IOException{
			String current = new java.io.File(".").getCanonicalPath();
			System.out.println("Current dir:" + current);
			String currentDir = System.getProperty("user.dir");
			System.out.println("Current dir using System:" + currentDir);
	}

	public static void main(String[] args){

		//TODO we should have 3 workspaces
		try {

			String RESTURL = "http://localhost:8080/geoserver";
			String RESTUSER = "nasa";
			String RESTPW = "isac13";

			Properties props = new Properties();
			FileInputStream file = new FileInputStream("./Params.properties");
			props.load(file);

			// write 
			String workspace = props.getProperty("workspace");
			String server = props.getProperty("server");
			String filePath = props.getProperty("filePath");

			String[] geotTiffFiles = FileManager.filesInFolder(filePath, new geotiffFileFilter());

			for (int x = 0; x < geotTiffFiles.length; x++) {

				GeoServerRESTReader readerGeoserver = new GeoServerRESTReader(RESTURL, RESTUSER, RESTPW);
				GeoServerRESTPublisher publisherGeoserver = new GeoServerRESTPublisher(RESTURL, RESTUSER, RESTPW);

				File geotiff = new File(geotTiffFiles[x]);
				String fileName = geotiff.getName();

				System.out.println("FileName: " + fileName);
				String coverageName = fileName;

				String srs = "EPSG:4326";
				String def = "default";

				ProjectionPolicy proj = ProjectionPolicy.REPROJECT_TO_DECLARED;

				boolean created = publisherGeoserver.createWorkspace(workspace);
				if (created) {
					System.out.println("The workspace "+workspace+" has been created, yeah!");
				}

				GeotiffReader reader = new GeotiffReader(geotTiffFiles[x]);
				if (reader.isGeotiff()) {
					GeoCodec geo = reader.getGeoCodec();
					//now get bounding box coordinates for entire image

					@SuppressWarnings("MismatchedReadAndWriteOfArray")
					double[] bbox = geo.getBoundingBox(reader.getWidth(0), reader.getHeight(0));
					BoundaryBox BBOX = new BoundaryBox(bbox[0], bbox[1], bbox[2], bbox[3]);

					String store = props.getProperty("store");
					store = store+"_"+x;
					System.out.println("Pusblishing Geotiff file name: " + geotiff.getName() + " with BBOX: " + BBOX.toString() + " in store:"+store);
					publisherGeoserver.publishGeoTIFF(workspace, store , coverageName, geotiff, srs, proj, def, bbox);

					SAXBuilder builder = new SAXBuilder(); //used to read XML

					String layersFile = props.getProperty("layers");
					if (x == 0) {
						layersFile = layersFile+"Default/Layers.xml";
					} else {
						layersFile = layersFile+"NewLayers.xml";
					}

					Document doc = builder.build(layersFile);

					// Obtains the root element of the current XML file
					Element root = doc.getRootElement();
					List children = root.getChildren();

					//Obtains the menu entries or the layers
					for (Iterator it = children.iterator(); it.hasNext();) {
						Element curr = (Element) it.next();
						if (curr.getName().equals("MenuEntries")) {
//							System.out.println(curr.getName());

							Element newElement = new Element("MenuEntry");
							newElement.setAttribute("ID", fileName);
							newElement.setAttribute("EN", fileName);

							curr.addContent(newElement);
							curr.addContent("\n");
						}
						if (curr.getName().equals("MainLayers") || curr.getName().equals("VectorLayers")) {
//							System.out.println(curr.getName());

							Element newElement = new Element("layer");
							newElement.setAttribute("EN", fileName);
							newElement.setAttribute("BBOX", BBOX.toString());
							newElement.setAttribute("server", server);
							newElement.setAttribute("tiled", "true");
							newElement.setAttribute("format", "image/jpeg");
							newElement.setAttribute("name", workspace + ":" + fileName);

							if (curr.getName().equals("VectorLayers")) {
								newElement.setAttribute("Menu", fileName);
								newElement.setAttribute("selected", "true");
							}else{
								newElement.setAttribute("Menu", "nasa,"+fileName);
								newElement.setAttribute("style", "raster");
							}

							curr.addContent(newElement);
							curr.addContent("\n");
						}
					}

					XMLOutputter outputter = new XMLOutputter();
					FileWriter writer =
							new FileWriter(layersFile);
					outputter.output(doc, writer);
					writer.close();

				}
			}
		} catch (JDOMException ex) {
			Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			System.out.println("IOException:" + ex.getMessage());
		}
	}

	public static void publishLayer(String fileToPublish) throws IOException {
		String RESTURL = "http://localhost:8080/geoserver";
		String RESTUSER = "admin";
		String RESTPW = "geoserver";

		// write 
		String workspace = "nasa";
		String store = "newstore";
		String server = "http://localhost:8080/geoserver/wms";
		String filePath = "/home/olmozavala/Dropbox/NewATLAS/GeoserverREST/exampleData/tiles/";

		GeoServerRESTPublisher publisherGeoserver = new GeoServerRESTPublisher(RESTURL, RESTUSER, RESTPW);

		File geotiff = new File(fileToPublish);
		String fileName = geotiff.getName();

		System.out.println("FileName: " + fileName);
		String coverageName = fileName;

		String srs = "EPSG:4326";
		String def = "default";

		ProjectionPolicy proj = ProjectionPolicy.REPROJECT_TO_DECLARED;

		boolean created = publisherGeoserver.createWorkspace(workspace);
		if (created) {
			System.out.println("New workspace created yeah babe!");
		}

		GeotiffReader reader = new GeotiffReader(fileToPublish);
		if (reader.isGeotiff()) {
			GeoCodec geo = reader.getGeoCodec();
			//now get bounding box coordinates for entire image

			double[] bbox = geo.getBoundingBox(reader.getWidth(0), reader.getHeight(0));
			BoundaryBox BBOX = new BoundaryBox(bbox[0], bbox[1], bbox[2], bbox[3]);

			System.out.println("Pusblishing Geotiff file name: " + geotiff.getName() + " with BBOX: " + BBOX.toString());
			publisherGeoserver.publishGeoTIFF(workspace, store, coverageName, geotiff, srs, proj, def, bbox);
		}

	}
}