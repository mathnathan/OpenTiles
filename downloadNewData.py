import os
from urllib2 import urlopen, URLError, HTTPError
from ftplib import FTP
from dataSources import DATA_FIELD

#DST_DIR = "/home/olmo/tomcat/tomcat/webapps/nasa/"
tomcat_dir = "tomcat/webapps/nasa/"
DST_DIR = os.path.join( os.path.dirname(__file__), tomcat_dir )

# CHECK FTP
def updated( url ):
    """Check if the file at this url has been updated"""

    files_updated = True
    if( files_updated ):
        return True
    else:
        return False

def download( url ):
    """Determine the file protocol and call the appropriate download function"""
    protocol = url.split(':')[0]
    print "PROTOCOL = ", protocol
    print "url = ", url
    print "split = ", url.split('/')
    print "downloading ", url.split('/')[-1]
    print "join = ", '/'.join(url.split('/')[2:-1])
    if( protocol == "ftp" ):
        dlftp( url )
    elif( protocol == "http" or protocol == "https" ):
        dlhttp( url )
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
    lf = open(DST_DIR+filename, "wb")
    ftp.retrbinary("RETR " + path2file, lf.write, 8*1024)
    lf.close()

# DOWNLOAD VIA HTTP
def dlhttp( url ):
    """Download the file at the url using http"""

    # Open the url
    try:
        f = urlopen(url)
        print "downloading " + url.split('/')[-1]

        # Open our local file for writing
        with open(os.path.basename(url), "wb") as local_file:
            local_file.write(f.read())

    #handle errors
    except HTTPError, e:
        print "HTTP Error:", e.code, url
    except URLError, e:
        print "URL Error:", e.reason, url

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
    collection = "AS41"
    product = 2

    if( not os.path.exists( tomcat_dir ) ):
        print "os path = ", os.path.exists( tomcat_dir )
        os.makedirs( tomcat_dir )
        print "os path = ", os.path.exists( tomcat_dir )

    # Simulate a database query
    url = extractURL( field, dtype, sensor, collection, product )

    if( updated( url ) ):
        download( url )
    else:
        pass


if __name__ == '__main__':
    main()
