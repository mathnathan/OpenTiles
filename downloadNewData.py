import os
from constants import *
from urllib2 import urlopen, URLError, HTTPError
from ftplib import FTP
from dataSources import DATA_FIELD
from converter import convert

# CHECK FTP
def updated( url ):
    """Check if the file at this url has been updated"""

    files_updated = True
    if( files_updated ):
        return True
    else:
        return False

def download( url, protocol=None ):
    """Determine the file protocol and call the appropriate download function"""

    if( protocol == None ):
        protocol = url.split(':')[0]
    print "PROTOCOL = ", protocol
    print "url = ", url
    print "split = ", url.split('/')
    print "downloading ", url.split('/')[-1]
    print "join = ", '/'.join(url.split('/')[2:-1])
    if( protocol.lower() == "ftp" ):
        return dlftp( url )
    elif( protocol.lower() == "http" or protocol.lower() == "https" ):
        return dlhttp( url )
    else:
        exit( "ERROR: Don't recognize url protocol request\n" )

# DOWNLOAD VIA FTP
def dlftp( full_url ):
    """Download the file at the url using ftp"""

    url_list = full_url.split('/')
    ftp_url = url_list[2]
    filename = url_list[-1]
    path2file = '/'.join(url_list[3:])
    print "\nurl_list = ", url_list
    print "ftp_url = ", ftp_url
    print "filename = ", filename
    print "path2file = ", path2file, "\n"
    ftp = FTP( ftp_url )
    ftp.login()

    listing = []
    ftp.retrlines("LIST", listing.append)

# download the file

    print "dst_dir = ", DST_DIR
    dlDir = DST_DIR+filename
    lf = open(dlDir, "wb")
    ftp.retrbinary("RETR " + path2file, lf.write, 8*1024)
    lf.close()

    return dlDir

# DOWNLOAD VIA HTTP
def dlhttp( full_url ):
    """Download the file at the url using http"""

    url_list = full_url.split('/')
    http_url = url_list[2]
    filename = url_list[-1]
    path2file = '/'.join(url_list[3:])

    # Open the url
    try:
        f = urlopen(url)
        print "downloading " + filename

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

def extractURL( dField=None, dType=None, sensor=None, collection=None,
        product=None ):
    """Find the link associated with the request criteria"""
    # A poor simulation of a database access using relationships
    url = DATA_FIELD[dField][dType][sensor][collection][product]

    return url


def main():

    field = "Land"
    dtype = "Temperature"
    sensor = "MODIS"
    collection = "AS3"
    product = 2

    if( not os.path.exists( tomcat_dir ) ):
        print "os path = ", os.path.exists( tomcat_dir )
        os.makedirs( tomcat_dir )
        print "os path = ", os.path.exists( tomcat_dir )

    # Simulate a database query
    url = extractURL( field, dtype, sensor, collection, product )

    if( updated( url ) ):
        dlFile = download( url, "FTP" )
        convert( dlFile, "HDF" )
    else:
        pass

if __name__ == '__main__':
    main()
