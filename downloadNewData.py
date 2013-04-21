import os
from urllib2 import urlopen, URLError, HTTPError
from ftplib import FTP
from dataSources import DATA_FIELD

DST_DIR = "/home/olmo/tomcat/tomcat/webapps/nasa/"

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
    print "split = ", url.split('/')[:-1]
    if( protocol == "ftp" ):
        dlftp( url )
    elif( protocol == "http" or protocol == "https" ):
        dlhttp( url )
    else:
        exit( "ERROR: Don't recognize url protocol request\n" )

# DOWNLOAD VIA FTP
def dlftp( url ):
    """Download the file at the url using ftp"""

    ftp = FTP(url.split('/')[:-1])
    ftp.login()
    ftp.retrlines("LIST")

    ftp.cwd("folderOne")
    ftp.cwd("subFolder") # or ftp.cwd("folderOne/subFolder")

    listing = []
    ftp.retrlines("LIST", listing.append)
    words = listing[0].split(None, 8)
    filename = words[-1].lstrip()

# download the file
    local_filename = os.path.join(r"c:\myfolder", filename)
    lf = open(local_filename, "wb")
    ftp.retrbinary("RETR " + filename, lf.write, 8*1024)
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

    # Simulate a database query
    url = extractURL( field, dtype, sensor, collection, product )

    if( updated( url ) ):
        download( url )
    else:
        pass


if __name__ == '__main__':
    main()
