import javax.json.Json;
import javax.json.stream.JsonParser;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.Double.parseDouble;

public class DownloadData {
    private int totalRecords, totalSize;
    private ArrayList<String> addressList;

    DownloadData(){
        /*Total size is the total mb across all downloaded files for a given process.
        * Total records is the number of JsonObject entries across all downloaded files.
        * addressList is a list of links, each link being a different file to be downloaded
        * within the same API field. */
        totalSize = 0;
        totalRecords = 0;
        addressList = new ArrayList<>();
        /*
        Upon creating this object, it automatically downloads a new copy of the list of available download links.
        This is used to download the individual data files. It overwrites the existing file because this list of downloads
        is updated weekly by openFDA.
        */
        try {
            downloadFile("https://api.fda.gov/download.json");
        }catch(Exception e){System.out.println(e.getMessage());}
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public ArrayList<String> getAddressList() {
        return addressList;
    }

    /*This function downloads a ZipFile containing a list of JsonObjects. It is passed the URL to download said file from */
    public File downloadZipFile(String srcURLName) throws IOException {
        File destDir = new File("references");
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new URL(srcURLName).openStream());
        ZipEntry zipEntry = zis.getNextEntry();
        if (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zis.closeEntry();
            zis.close();
            return newFile;
        }
        zis.closeEntry();
        zis.close();
        return null;
    }

    /*This function downloads the JsonFile that lists the download links for all files in each API field. */
    private void downloadFile(String srcURLName) throws IOException{
        byte[] buffer = new byte[1024];
        InputStream is = new URL(srcURLName).openStream();
        File newFile = new File("references\\download.json");
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = is.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();
    }

    /*This file creates a new file to be used in other functions. */
    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    /*
    This function searches the download file for a specific field, and
    returns the links to download the files for the specific field. */
    public ArrayList<String> parseDownload(String[] dataType) throws IOException  {
        File downloadsFile = new File("references\\download.json");
        FileInputStream fis = new FileInputStream(downloadsFile);
        JsonParser jRead = Json.createParser(fis);

        while (jRead.hasNext()){
            JsonParser.Event event = jRead.next();
            if(event == JsonParser.Event.KEY_NAME && jRead.getString().equals(dataType[0])) {
                while (jRead.hasNext()) {
                    event = jRead.next();
                    if(event == JsonParser.Event.KEY_NAME && jRead.getString().equals(dataType[1])) {
                        while (event != JsonParser.Event.END_ARRAY) {
                            event = jRead.next();
                            if (event == JsonParser.Event.KEY_NAME) {
                                if (jRead.getString().equals("file")) {
                                    jRead.next();
                                    this.addressList.add(jRead.getString());
                                } else if (jRead.getString().equals("size_mb")) {
                                    jRead.next();
                                    this.totalSize += parseDouble(jRead.getString());
                                } else if (jRead.getString().equals("records")) {
                                    jRead.next();
                                    this.totalRecords += jRead.getLong();
                                }
                            }
                        }
                    }
                }
                return this.addressList;
            }
        }
        return this.addressList;
    }

    public String[] parseString(String str){
        String [] strArray = str.split("-");
        System.out.println(strArray.toString());
        return strArray;

    }
}
