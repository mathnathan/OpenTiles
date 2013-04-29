/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nasageeks.viewer;

import com.nasageeks.upload.BoundaryBox;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author olmozavala
 */
public class ViewerManager {
	
	public static void addNewLayer(String layerName, String server, String workspace, BoundaryBox bbox,
							String inputFile, String outputFile) throws IOException, JDOMException{

				//---------- Modifying XML file for automatic visualization
				//---------- All this code should also go into a different Java file
				SAXBuilder builder = new SAXBuilder(); //used to read XML

				Document doc = builder.build(inputFile);

				// Obtains the root element of the current XML file
				Element root = doc.getRootElement();
				List children = root.getChildren();

				//Obtains the menu entries or the layers
				for (Iterator it = children.iterator(); it.hasNext();) {
					Element curr = (Element) it.next();
					if (curr.getName().equals("MenuEntries")) {
//							System.out.println(curr.getName());

						Element newElement = new Element("MenuEntry");
						newElement.setAttribute("ID", layerName);
						newElement.setAttribute("EN", layerName);

						curr.addContent(newElement);
						curr.addContent("\n");
					}
					if (curr.getName().equals("MainLayers") || curr.getName().equals("VectorLayers")) {
//							System.out.println(curr.getName());

						Element newElement = new Element("layer");
						newElement.setAttribute("EN", layerName);
						newElement.setAttribute("BBOX", bbox.toString());
						newElement.setAttribute("server", server);
						newElement.setAttribute("tiled", "true");
						newElement.setAttribute("format", "image/jpeg");
						newElement.setAttribute("name", workspace + ":" + layerName);

						if (curr.getName().equals("VectorLayers")) {
							newElement.setAttribute("Menu", layerName);
							newElement.setAttribute("selected", "true");
						} else {
							newElement.setAttribute("Menu", "nasa," + layerName);
							newElement.setAttribute("style", "raster");
						}

						curr.addContent(newElement);
						curr.addContent("\n");
					}
				}

				XMLOutputter outputter = new XMLOutputter();
				FileWriter writer =
						new FileWriter(outputFile);
				outputter.output(doc, writer);
				writer.close();


	}
}
