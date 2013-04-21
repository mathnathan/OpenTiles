import os
import sys
import subprocess as sp
import gdal
import math


def makeTiles( abs_path_filename, dst_filename=None ):

    print "Generating Tiles...\n"
    filename_handle = gdal.Open( abs_path_filename+".hdf" )
    sub_ds_list = filename_handle.GetSubDatasets()

    for a, b, filenames in os.walk( abs_path_filename ):
        for i,filename in enumerate(filenames):
            sub_ds = gdal.Open( sub_ds_list[i][0] )

            nX = math.log(sub_ds.RasterXSize/512.0, 2)
            nX = int(math.ceil(nX))
            nY = math.log(sub_ds.RasterYSize/512.0, 2)
            nY = int(math.ceil(nY))
            if( nX > nY ):
                n = nX
            else:
                n = nY

            sub_ds = 0


            dirname = '.'.join(filename.split('.')[:-1] )
            full_path = os.path.join(abs_path_filename,dirname)
            if( not os.path.exists( full_path ) ):
                os.makedirs( full_path )

            cmd = "gdal_retile.py"
            ps = "-ps"
            dimx = "256"
            dimy = "256"
            levels = "-levels"
            n = str(n)
            tDir = "-targetDir"
            tDir_path = full_path
            src = "".join( sub_ds_list[i][0].split("\"") )
            sp.call([cmd, ps, dimx, dimy, levels, n, tDir, tDir_path, src])


# DOWNLOAD VIA HTTP
def dlhttp( full_url ):
    """Download the file at the url using http"""

    url_list = full_url.split('/')
    http_url = url_list[2]
    filename = url_list[-1]
    print "Downloading" + filename + "...\n"
    path2file = '/'.join(url_list[3:])

    # Open the url
    try:
        f = urlopen(url)

        # Open our local file for writing
        dlDir = DST_DIR+filename
        with open(dlDir, "wb") as local_file:
            local_file.write( f.read() )

    #handle errors
    except HTTPError, e:
        print "HTTP Error:", e.code, url
    except URLError, e:
        print "URL Error:", e.reason, url

    return dlDir


