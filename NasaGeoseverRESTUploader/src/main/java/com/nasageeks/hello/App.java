package com.nasageeks.hello;

import com.nasageeks.filefilters.geotiffFileFilter;
import gov.nasa.worldwind.formats.tiff.GeoCodec;
import gov.nasa.worldwind.formats.tiff.GeotiffReader;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import com.nasageeks.hello.BoundaryBox;

import org.xml.sax.SAXException;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String[] args) throws FileNotFoundException {

		//TODO we should have 3 workspaces
		try {

			String RESTURL = "http://localhost:8080/geoserver";
			String RESTUSER = "admin";
			String RESTPW = "geoserver";

			// write 
			String workspace = "nasa";
			String store = "newstore";
			String server= "http://localhost:8080/geoserver/wms"; 
			String filePath = "/home/olmozavala/Dropbox/NewATLAS/GeoserverREST/exampleData/tiles/";

			String[] geotTiffFiles = FileManager.filesInFolder(filePath,new geotiffFileFilter());

			for(int x=0;x<geotTiffFiles.length; x++){

//				GeoServerRESTReader readerGeoserver = new GeoServerRESTReader(RESTURL, RESTUSER, RESTPW);
				GeoServerRESTPublisher publisherGeoserver = new GeoServerRESTPublisher(RESTURL, RESTUSER, RESTPW);

				File geotiff = new File(geotTiffFiles[x]);
				String fileName = geotiff.getName();

				System.out.println("FileName: "+fileName);
				String coverageName = fileName;

				String srs = "EPSG:4326";
				String def = "default";

				ProjectionPolicy proj = ProjectionPolicy.REPROJECT_TO_DECLARED;

				boolean created = publisherGeoserver.createWorkspace(workspace);
				if (created) {
					System.out.println("New workspace created yeah babe!");
				}

				GeotiffReader reader = new GeotiffReader(geotTiffFiles[x]);
				if (reader.isGeotiff()) {
					GeoCodec geo = reader.getGeoCodec();
					//now get bounding box coordinates for entire image

					@SuppressWarnings("MismatchedReadAndWriteOfArray")

					double[] bbox = geo.getBoundingBox(reader.getWidth(0), reader.getHeight(0));
					BoundaryBox BBOX = new BoundaryBox(bbox[0],bbox[1],bbox[2],bbox[3]);

					System.out.println("Pusblishing Geotiff file name: "+geotiff.getName()+" with BBOX: "+BBOX.toString());
					publisherGeoserver.publishGeoTIFF(workspace, store, coverageName, geotiff, srs, proj, def, bbox);

//					publishLayer(geotTiffFiles[x]);

					SAXBuilder builder = new SAXBuilder(); //used to read XML

					String layersFile;
					if(x==0){
						layersFile = "/home/olmozavala/Dropbox/NewATLAS/GeoserverREST/NASA_Template/web/layers/Default/Layers.xml";
					}else{
						layersFile = "/home/olmozavala/Dropbox/NewATLAS/GeoserverREST/NASA_Template/web/layers/NewLayers.xml";
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
							newElement.setAttribute("ID",fileName);
							newElement.setAttribute("EN",fileName);

							curr.addContent(newElement);
							curr.addContent("\n");
						}
						if (curr.getName().equals("MainLayers")) {
//							System.out.println(curr.getName());

							Element newElement = new Element("layer");
							newElement.setAttribute("Menu",fileName);
							newElement.setAttribute("EN",fileName);
							newElement.setAttribute("BBOX",BBOX.toString());
							newElement.setAttribute("server",server);
							newElement.setAttribute("tiled","true");
							newElement.setAttribute("format","image/jpeg");
							newElement.setAttribute("style","raster");

							curr.addContent(newElement);
							curr.addContent("\n");
						}
						if (curr.getName().equals("VectorLayers")) {
//							System.out.println(curr.getName());

							Element newElement = new Element("layer");
							newElement.setAttribute("Menu",fileName);
							newElement.setAttribute("EN",fileName);
							newElement.setAttribute("BBOX",BBOX.toString());
							newElement.setAttribute("selected","true");
							newElement.setAttribute("server",server);
							newElement.setAttribute("name",workspace+":"+fileName);
							newElement.setAttribute("tiled","true");
							newElement.setAttribute("format","image/jpeg");
							newElement.setAttribute("style","raster");

							curr.addContent(newElement);
							curr.addContent("\n");
						}

//						System.out.println(curr.getName());
					}

					XMLOutputter outputter = new XMLOutputter();
					FileWriter writer = 
							new FileWriter("/home/olmozavala/Dropbox/NewATLAS/GeoserverREST/NASA_Template/web/layers/NewLayers.xml");
					outputter.output(doc, writer);
					writer.close();

				}
			}
		} catch (Exception ex) {
			System.out.println("Bad 2 EXCEPTION:" + ex.getMessage());
		}
	}

	public static void publishLayer(String fileToPublish) throws IOException{
			String RESTURL = "http://localhost:8080/geoserver";
			String RESTUSER = "admin";
			String RESTPW = "geoserver";

			// write 
			String workspace = "nasa";
			String store = "newstore";
			String server= "http://localhost:8080/geoserver/wms"; 
			String filePath = "/home/olmozavala/Dropbox/NewATLAS/GeoserverREST/exampleData/tiles/";

				GeoServerRESTPublisher publisherGeoserver = new GeoServerRESTPublisher(RESTURL, RESTUSER, RESTPW);

				File geotiff = new File(fileToPublish);
				String fileName = geotiff.getName();

				System.out.println("FileName: "+fileName);
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
					BoundaryBox BBOX = new BoundaryBox(bbox[0],bbox[1],bbox[2],bbox[3]);

					System.out.println("Pusblishing Geotiff file name: "+geotiff.getName()+" with BBOX: "+BBOX.toString());
					publisherGeoserver.publishGeoTIFF(workspace, store, coverageName, geotiff, srs, proj, def, bbox);
				}

	}
}