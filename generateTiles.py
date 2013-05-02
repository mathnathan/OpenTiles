import os
import sys
import subprocess as sp
import gdal
import math
import zipfile

def zipFldr( fldr ):

    dest = fldr
    zip_file = fldr+".zip"
    with zipfile.ZipFile(zip_file, 'w', compression=zipfile.ZIP_DEFLATED) as zipf:
        root_len = len(os.path.abspath(dest))
        for root, dirs, files in os.walk(dest):
            archive_root = os.path.abspath(root)[root_len:]
            for f in files:
                fullpath = os.path.join(root, f)
                archive_name = os.path.join(archive_root, f)
                print f
                zipf.write(fullpath, archive_name, zipfile.ZIP_DEFLATED)


def makeTiles( abs_path_filename, dst_filename=None ):

    print "Generating Tiles...\n"
    filename_handle = gdal.Open( abs_path_filename+".hdf" )
    sub_ds_list = filename_handle.GetSubDatasets()
    fldr_list = []

    for a, b, filenames in os.walk( abs_path_filename ):
        for i,filename in enumerate(filenames):
            sub_ds = gdal.Open( sub_ds_list[i][0] )

            nX = math.log(sub_ds.RasterXSize/4096.0, 2)
            nX = int(math.ceil(nX))
            nY = math.log(sub_ds.RasterYSize/4096.0, 2)
            nY = int(math.ceil(nY))
            if( nX > nY ):
                n = nX
            else:
                n = nY

            sub_ds = 0 # Close the dataset

            dirname = '.'.join(filename.split('.')[:-1] )
            full_path = os.path.join(abs_path_filename,dirname)
            if( not os.path.exists( full_path ) ):
                fldr_list.append( full_path )
                os.makedirs( full_path )

            cmd = "gdal_retile.py"
            #srs = "-s_srs"
            #srs_val = "EPSG:4326"
            ps = "-ps"
            dimx = "2048"
            dimy = "2048"
            levels = "-levels"
            n = str(n)
            tDir = "-targetDir"
            tDir_path = full_path
            shp = "-tileIndex"
            shp_name = "indexName"
            csv = "-csv"
            csv_name = "georefInfo"
            src = "".join( sub_ds_list[i][0].split("\"") )
            #sp.call([cmd, srs, srs_val, ps, dimx, dimy, levels, n, tDir, tDir_path, shp, shp_name, csv, csv_name, src])
            sp.call([cmd, ps, dimx, dimy, levels, n, tDir, tDir_path, shp, shp_name, csv, csv_name, src])

    print "Generating Pyramids...\n"
    sp.call(["zip", "-r", abs_path_filename+".zip", abs_path_filename])
    #for f in fldr_list:
        #zipFldr( f )

if __name__ == "__main__":

    makeTiles( sys.argv[1] )
