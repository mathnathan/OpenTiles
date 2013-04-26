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
import it.geosolutions.geoserver.rest.decoder.RESTCoverage;
import it.geosolutions.geoserver.rest.decoder.RESTCoverageList;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.RESTResource;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.util.logging.LoggerFactory;
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

			GeoServerRESTReader readerGeoserver = new GeoServerRESTReader(RESTURL, RESTUSER, RESTPW);
			GeoServerRESTPublisher publisherGeoserver = new GeoServerRESTPublisher(RESTURL, RESTUSER, RESTPW);

			for (int x = 0; x < geotTiffFiles.length; x++) {
				File geotiff = new File(geotTiffFiles[x]);
				String fileName = geotiff.getName();

				System.out.println("FileName: " + fileName);
				String coverageName = fileName;

				String srs = "EPSG:4326";
				String def = "default";

				ProjectionPolicy proj = ProjectionPolicy.REPROJECT_TO_DECLARED;

				//Adding the nasa workspace into geoserver (only once should be done)
				boolean created = publisherGeoserver.createWorkspace(workspace);
				if (created) {
					System.out.println("The workspace "+workspace+" has been created, yeah!");
				}

				//Reads the Geotiff file and extracts the boundaries
				GeotiffReader reader = new GeotiffReader(geotTiffFiles[x]);
				if (reader.isGeotiff()) {
					GeoCodec geo = reader.getGeoCodec();

					//now get bounding box coordinates for entire image
					@SuppressWarnings("MismatchedReadAndWriteOfArray")
					double[] bbox = geo.getBoundingBox(reader.getWidth(0), reader.getHeight(0));
					BoundaryBox BBOX = new BoundaryBox(bbox[0], bbox[2], bbox[3], bbox[1]);

					//---- Creates one new store for file
					String store = props.getProperty("store");
					store = store+"_"+x;

					System.out.println("------- Pusblishing Geotiff file name: " + geotiff.getName() + " with BBOX: " + BBOX.toString() + " in store:"+store);
					created = publisherGeoserver.publishGeoTIFF(workspace, store , coverageName, geotiff, srs, proj, def, bbox);
					if(created){
						System.out.println("------------ Layer "+workspace+":"+coverageName+" added SUCCESSFULLY!");
					}else{
						System.out.println("!!!!!!!!ERROR adding layer "+workspace+":"+coverageName+" (it may already exist in Geoserver)");
						//We should go to the next file here, but because the previous error
						// can occur if we try to upload one layer that is already in geoserver
						// in that case we should still modify the XML file for visualization
//						break;//Try with the next file
					}

					//--------- Read the newly uploaded layer and change its Meta data and boundary box
					RESTCoverage coverage = readerGeoserver.getCoverage(workspace,store,coverageName);
					System.out.println(coverage.getName()+" ..max:"+coverage.getMaxX());

					GSCoverageEncoder layerEncoder = new GSCoverageEncoder();
					layerEncoder.setNativeBoundingBox(bbox[0], bbox[3], bbox[2], bbox[1], srs);
					layerEncoder.setName(coverage.getName());
					boolean success = publisherGeoserver.configureCoverage(layerEncoder, workspace, store);
					if(success){
						System.out.println("--------- BBOX has been updated for "+coverageName);
					}else{
						System.out.println("!!!!!!!!ERROR BBOX FAIL to be updated for "+coverageName);
					}

					//---------- Modifying XML file for automatic visualization
					//---------- All this code should also go into a different Java file
					SAXBuilder builder = new SAXBuilder(); //used to read XML

					String layersPath = props.getProperty("layers");
					String layersFile = "";
					if (x == 0) {
						layersFile = layersPath+"Default/Layers.xml";
					} else {
						layersFile = layersPath+"NewLayers.xml";
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
							new FileWriter(layersPath+"NewLayers.xml");
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
	}

}