import subprocess as sp
from gdal import Open, GCP
import os
from gdalconst import *
from constants import *
import shlex

def defineProj( ds_name ):

    ds_proj = Open( ds_name, GA_ReadOnly ).GetProjection()
    if not ds_proj:
        return None
    params = ds_proj.split("PARAMETER")

    proj = "+proj=aea +lat_1="

    lat_1 = params[1].split(",")[1][:-1]

    proj = proj+lat_1+" +lat_2="

    lat_2 = params[2].split(",")[1][:-1]

    proj = proj+lat_2+" +lat_0="

    lat_0 = params[3].split(",")[1][:-1]

    proj = proj+lat_0+" +lon_0="

    lon_0 = params[4].split(",")[1][:-1]

    proj = proj+lon_0+" +x_0="

    x_0 = params[5].split(",")[1][:-1]

    proj = proj+x_0+" +y_0="

    y_0 = params[6].split(",")[1][:-1]

    proj = proj+y_0

    proj = "\""+proj+" +ellps=WGS84 +units=m +no_defs\""
    return proj

def convert( abs_path_filename, input_type ):
    """ This function is used to convert the native satellite data into a
    pyramid/tile friendly geotiff format

        Currently supports:
            HDF

    """

    print("Converting HDF to Gtiff...\n")

    print "abs_path_filename = ", abs_path_filename
    abs_path = '.'.join( abs_path_filename.split('.')[:-1] )
    print "abs_path = ", abs_path
    filename = abs_path_filename.split('/')[-1]
    print "filename = ", filename
    if( not os.path.exists( abs_path ) ):
        os.makedirs( abs_path )
    if( input_type.lower() == "hdf" ):
        src_ds = Open( abs_path_filename, GA_ReadOnly )
        src_ds_subsets = src_ds.GetSubDatasets()
        src_proj = "-s_srs"
        t_proj = "-t_srs"
        t_proj_value = "EPSG:4326"


    for subset in src_ds_subsets:
        var_name = subset[0].split(':')[-1]
        dst_filename = '.'.join(filename.split(".")[:-1])+"-"+var_name+".tiff"
        dst_abs_path_filename = os.path.join(abs_path, dst_filename )
        src_proj_value = defineProj( subset[0] )
        call = None
        if src_proj_value == None:
            sub = Open( subset[0] )
            #gcp1 = GCP( -180.0, 90.0, 0.0, 0, 0 )
            #gcp2 = GCP( 180.0, -90.0, 0.0, sub.RasterYSize, sub.RasterXSize )
            #sub.SetGCPs( [gcp1,gcp2], "EPSG:4326" )
            #proj = "-a_srs"
            #proj_val = "EPSG:4326"
            georef = "-a_ullr"
            georef_val = "-180.0 90.0 180.0 -90.0"

            #call = " ".join(["gdal_translate",proj,proj_val,georef,georef_val,subset[0],dst_abs_path_filename])
            call = " ".join(["gdal_translate",georef,georef_val,subset[0],dst_abs_path_filename])
            print "call = ", call
        else:
            call = " ".join(["gdalwarp",src_proj,src_proj_value,t_proj,t_proj_value,subset[0],dst_abs_path_filename])

        sp.call( shlex.split(call) )

    # Close the data set
    src_ds = 0

    return abs_path

if __name__ == "__main__":

    import sys

    argv = sys.argv

    convert( argv[0], argv[1] )
