------------------------------------------------
# OpenTiles

------------------------------------------------

** This project is still under very heavy development **

This is a solution to the 2013 International Space Apps Challenge "Earth Tiles". 
The challenge is to create a service, for developers, which provides access to 
sattelite derived tiles at various scales. These tiles would be used for various
projects similar to OpenLayers applications, some of which might resemble Google 
Maps or MapQuest.

#### Authors

Nathan Crock - mathnathan@gmail.com <br />
[Olmo Zavala][http://olmozavala.com] - osz09@fsu.edu <br />
[Sam Rustan][http://github.com/samrustan] - samrustan@gmail.com

#### Institution
##### Making Awesome

<br />

#### OS Support
* Windows 7
* Mac OSX
* Ubuntu 12.04. 12.10

#### Dependencies
[GDAL]: http://www.gdal.org/
[GDAL][] - Geospatial Data Abstraction Library

#### Project Description

Our solution OpenTiles is more of a service than a product. Currently it is a 
simple implementation intended as a proof of concept demonstrating the 5 main 
components of the project. We now explain how would operate at a production scale. 

Let us take a very large storage system with no satellite data. This is the backend
of OpenTiles. Python scripts will take care of the first 3 components.

##### 1) Download

A script will constantly query the databases and servers where satellite data is 
stored. If the scripts find a data file that that is not on the local drive, it will 
download it. This first component ensures all the data on the backend of OpenTiles is
current.

##### 2) Convert 

Satellite observations are stored in various formats. While they all have their benefits
a collection of files in a more unified, GIS friendly format, would be more practical. 
After the data has been downloaded it is converted from its native type, most commonly 
NetCDF and HDF, into a Geotiff (Gtiff) file. We use the GDAL library for conversion.

##### 3) Crop and Scale

The magic of Google Earth and other earth observation data viewers is in the tiles and 
pyramids. One Gtiff could be laid over an earth model but it could only be viewed at 
that one scale. Tiling a Gtiff and creating a pyramid of different scales, allows the 
user to zoom in and out of the data in a more natural way. We use the GDAL to create
the various pyramids and tiles.

##### 4) Automatic Geoserver Integration

##### 5) Demo - Tile Display
