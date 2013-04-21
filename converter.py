import subprocess as sp
from gdal import Open
import os
from gdalconst import *
from constants import *

def convert( abs_path_filename, input_type ):
    """ This function is used to convert the native satellite data into a
    pyramid/tile friendly geotiff format

        Currently supports:
            HDF

    """

    print("Converting HDF to Gtiff...\n")

    abs_path = '.'.join( abs_path_filename.split('.')[:-1] )
    filename = abs_path_filename.split('/')[-1]
    if( not os.path.exists( abs_path ) ):
        os.makedirs( abs_path )
    if( input_type.lower() == "hdf" ):
        src_ds = Open( abs_path_filename, GA_ReadOnly )
        src_ds_subsets = src_ds.GetSubDatasets()
        projection = "-s_srs"
        proj_value = "EPSG:4326"
        for subset in src_ds_subsets:
            var_name = subset[0].split(':')[-1]
            dst_filename = '.'.join(filename.split(".")[:-1])+"-"+var_name+".tif"
            dst_abs_path_filename = os.path.join(abs_path, dst_filename )
            sp.call(["gdalwarp", projection, proj_value, subset[0], dst_abs_path_filename])

    # Close the data set
    src_ds = 0

    return abs_path
