------------------------------------------------
# OpenTiles

------------------------------------------------

** This project is still in development **

This is a solution to the [2013](https://2013.spaceappschallenge.org/) International Space Apps Challenge ["Earth Tiles"](https://2013.spaceappschallenge.org/challenge/earthtiles/). 
The challenge is to create a service, for developers, which provides access to 
satellite derived tiles at various scales. These tiles would be used for various
projects similar to OpenLayers applications, some of which might resemble Google 
Maps or MapQuest.

#### Authors

[Nathan Crock]: http://mathnathan.com
[Olmo Zavala]: http://olmozavala.com
[Sam Rustan]: http://github.com/samrustan

[Nathan Crock][] - mathnathan@gmail.com <br />
[Olmo Zavala][] - osz09@fsu.edu <br />
[Sam Rustan][] - samrustan@gmail.com

#### Institutions and Affiliations
[Making Awesome]: http://makingawesome.org
[Florida State University]: http://fsu.edu
[Department of Scientific Computing]: http://sc.fsu.edu
[Center for Ocean-Atmospheric Prediction Studies]: http://coaps.fsu.edu
##### [Making Awesome][]
##### [Florida State University][]
##### [Department of Scientific Computing][]
##### [Center for Ocean-Atmospheric Prediction Studies][]

<br />

#### OS Support
* Ubuntu 12.04, 12.10, 13.04

#### Dependencies
[GDAL-1.9]: http://www.gdal.org/
[GDAL-1.9][] - Geospatial Data Abstraction Library

#### Project Description

Our solution OpenTiles is more of a service than a product. Currently it is a 
simple implementation intended as a proof of concept demonstrating the 5 main 
components of the project. We now explain how it would operate at a production scale. 

Let us take a very large storage system with no satellite data. This is the backend
of OpenTiles. Python scripts will take care of the first 3 components.

##### 1) Download

A script will constantly query the databases and servers where satellite data is 
stored. If the scripts find a data file that that is not on the local drive or has 
been updated, it will download it. This first component ensures all the data on 
the backend of OpenTiles is current.

##### 2) Convert 

Satellite observations are stored in various formats. While they all have their benefits
a collection of files in a more unified, GIS friendly format, would be more practical. 
After the data has been downloaded it is converted from its native type, most commonly 
NetCDF and HDF, into a Geotiff (Gtiff) file. We use the GDAL library for conversions.

##### 3) Crop and Scale

The magic of Google Earth and other earth observation data viewers is in the tiles and 
pyramids. One Gtiff could be laid over an earth model but it could only be viewed at 
that one scale. Tiling a Gtiff and creating a pyramid of different scales, allows the 
user to zoom in and out of the data in a more natural way. We use the GDAL to create
the various pyramids and tiles.

##### 4) Automatic Geoserver Integration
[Geoserver REST interface]: http://docs.geoserver.org/2.0.0/user/extensions/rest/index.html 
[Geoserver Manager]: https://github.com/geosolutions-it/geoserver-manager/wiki
Once the pyramids are obtained, we are using the [Geoserver REST interface][] to 
upload them into Geoserver. Specifically, we are using the [Geoserver Manager][] java 
library to read the contents of the pyramids and upload them into Geoserver automatically.
Once the pyramids are uploaded into Geoserver they can be served as 
images using the WMS standard (for OpenLayers, Leafleat, etc.), kml files (for
Google Earth) and GeoTiff files (for GIS).

##### 5) Demo - Tile Display
For visualizing the tiles that get uploaded into Geoserver, we are cofiguring
a web site that displays all the layers available in the server and it allows
the user to change the transparency of the layers, choose which layers to display,
download the layer as kml and Geotiff format and zooming in and out of the map
in a similar interface that Google maps does it. 
All these without any external intervention.
