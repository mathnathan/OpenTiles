import subprocess as sp
from gdal import Open
import os
from gdalconst import *
from constants import *

def convert( abs_filename, input_type ):
    """ This function is used to convert the native satellite data into a
    pyramid/tile friendly geotiff format

        Currently supports:
            HDF

    """

    folder = '.'.join( abs_filename.split('.')[:-1] )
    filename = abs_filename.split('/')[-1]
    print "folder = ", folder
    print "filename = ", filename
    if( not os.path.exists( folder ) ):
        os.makedirs( folder )
    dst_filename = os.path.join(folder,'.'.join(filename.split(".")[:-1])+".tif")
    if( input_type.lower() == "hdf" ):
        src_ds = Open( abs_filename, GA_ReadOnly )
        src_ds_subsets = src_ds.GetSubDatasets()
        for subset in src_ds_subsets:
            print "command = gdalwarp", subset[0], dst_filename
            sp.call(["gdalwarp", subset[0], dst_filename])

    # Close the data sets
    src_ds = 0
