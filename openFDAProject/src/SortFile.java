import javafx.scene.control.CheckBox;

import javax.json.*;
import javax.json.stream.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static javax.json.stream.JsonParser.Event.*;

public class SortFile {

    SortFile(){
    }

    /*Iterates through a file of JSON Objects, adds each object to an ArrayList. Returns the ArrayList*/
    public ArrayList<JsonObject> getDataObjects(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        JsonParser jRead = Json.createParser(fis);
        ArrayList<JsonObject> jsonObjs = new ArrayList<>();

        JsonParser.Event e = jRead.next();
        /*Iterate until the start of the array*/
        while(e != START_ARRAY)
            e = jRead.next();
        e = jRead.next();
        /*Iterate through the list of JsonObjects, add each one to the ArrayList*/
        while(e == START_OBJECT){
            JsonObject jo = jRead.getObject();
            jsonObjs.add(jo);
            jRead.skipObject();
            e = jRead.next();
        }
        return jsonObjs;
    }

    /*A driver function for sorting Json data. It sorts each file and inputs the sorted items into an ArrayList.
     * The ArrayList is then written to a temporary file. Once each file has been sorted in this manner, all the temp
     * files are Externally Merge Sorted and printed in readable format to the final, user specified, output file. */
    public void sortDataObjectsDriver(String identifier, ArrayList<File> files, String outputFileName, HashMap<String, CheckBox> cbmap) {
        ArrayList<File> newFiles = new ArrayList<>();
        ArrayList<JsonObject> jsobs;
        PrintData pd = new PrintData();

        try {
            for (File file : files) {
                jsobs = sortJsonObjects(identifier, getDataObjects(file));
                File tempFile = File.createTempFile(file.getName().replaceFirst(".json$", ""), ".json");
                newFiles.add(tempFile);
                pd.writeTempDataToFile(jsobs, tempFile);
                mergeJsonObjectFiles(newFiles, identifier, outputFileName, cbmap);
                tempFile.deleteOnExit();
            }
        }catch(Exception ex){System.out.println(ex);}

    }

    /*This function is the merge portion of an external merge sort. It combines
    * the already sorted temp files into a single, readable output file */
    public File mergeJsonObjectFiles(ArrayList<File> files, String sortIdentifier, String outputFileName, HashMap<String, CheckBox> cbmap){
        PrintData pd = new PrintData();
        try {
            /*
            * ArrayList jReads: An array of JsonParsers, each parsing a different file within the same dataset. Objects
            * input from these parsers and put into their respective indexes within the jsobs Array.
            * ArrayList jsobs: An array of JSON Objects, it contains the current JsonObject to be compared from each of
            * the JsonParsers
            * ArrayList jEvents: An array of JsonParser.Event, it is used to determine if the end of the parser has been reached. */
            File outputFile = new File(outputFileName + ".txt");
            FileOutputStream fos = new FileOutputStream(outputFile);
            ArrayList<JsonObject> jsobs = new ArrayList<>();
            ArrayList<JsonParser> jReads = new ArrayList<>();
            ArrayList<JsonParser.Event> jEvents = new ArrayList<>();
            JsonParser.Event e;
            int size = 0;
            int minIndex = 0;

            /*Creates a JsonParser for each file, adds it to an array*/
            for (File file : files) {
                FileInputStream fis = new FileInputStream(file);
                JsonParser jRead = Json.createParser(fis);
                jReads.add(jRead);
                size++;
            }

            /*Initialize the JsonObject ArrayList*/
            for(JsonParser jRead : jReads){
                /*Iterate past the first START_OBJECT*/
                jRead.next();
                e = jRead.next();
                /*Iterate to the first object needed*/
                while(e != START_OBJECT)
                    e = jRead.next();
                /*Add the object to the array, iterate to the next object,
                * and add the returned Event from jRead.next()*/
                jsobs.add(jRead.getObject());
                jRead.skipObject();
                jEvents.add(jRead.next());
            }

            /*This portion of the function will iterate over all three arrays. It sets a min index representing which
            * position holds the least value. It iterates through the size of the arrays and compares the min index to
            * the new values. Should the new value be lower, it's index is set to the min index. Once the minimum value
            * is found in that iteration, that value is written to the output file and the JsonObject ArrayList it was taken
            * from is incremented. */
            /*Iterate until all arrays are emptied*/
            while(size >= 1){
                /*Iterate until the index for all arrays has been compared and the min index found*/
                for(int i = 0; i < size; i++){
                    /*If the next JsonEvent is END_ARRAY, that index for all three arrays(JsonParser, JsonEvents, and
                    * JsonObjects) is removed and the size variable is decremented by one. */
                    if(jEvents.get(i) == END_ARRAY){
                        jsobs.remove(i);
                        jReads.get(i).close();
                        jReads.remove(i);
                        jEvents.remove(i);
                        i = 0;
                        minIndex=0;
                        size--;
                    }
                    /*Else, compare the current JsonObject Array's current index to the min index. Should it be lower,
                     * Print it to file, and increment all array elements at that index.  */
                    else{
                        if((sortIdentifier.contains("/") ? findJsonDataWithinObject(sortIdentifier, jsobs.get(i)) : jsobs.get(i).getString(sortIdentifier)).compareTo(sortIdentifier.contains("/") ? findJsonDataWithinObject(sortIdentifier, jsobs.get(minIndex)) : jsobs.get(minIndex).getString(sortIdentifier)) < 0){
                            minIndex = i;
                        }
                        pd.writeFinalDataToFile(fos, jsobs.get(minIndex), cbmap );
                        //Increment Jsobs at index and jread at index
                        incrementLists(jsobs, jReads, jEvents, minIndex);
                        minIndex = 0;
                    }
                }
            }
            fos.close();
            return outputFile;
        } catch(Exception ex){System.out.println(ex);}
        return null;
    }

    /*A helper function for mergeJsonObjectFiles, this function increments each index in the given array to the next elements,
     * such as the next JsonObject, JsonParser iteration, or JsonParser.Event */
    public void incrementLists(ArrayList<JsonObject> jsobs, ArrayList<JsonParser> jread, ArrayList<JsonParser.Event> jEvents, int index){
        jsobs.set(index, jread.get(index).getObject());
        jread.get(index).skipObject();
        jEvents.set(index, jread.get(index).next());
    }

    /*A simple merge sort for a single json file. It is passed an identifier so it knows what criteria to sort by, and
    * also the Json ArrayList to be sorted. */
    public ArrayList<JsonObject> sortJsonObjects(String identifier, ArrayList<JsonObject> both){
        ArrayList<JsonObject> left = new ArrayList<>();
        ArrayList<JsonObject> right = new ArrayList<>();
        int size = both.size();
        int midpoint = size / 2;

        if (size == 1) {
            return both;
        }
        else {
            for(int i = 0; i < midpoint; i++){
                left.add(both.get(i));
            }
            for (int i = midpoint; i < size; i++){
                right.add(both.get(i));
            }

            sortJsonObjects(identifier, left);
            sortJsonObjects(identifier, right);

            mergeJsonObjects(identifier, left, right, both);
            return both;
        }
    }

    /*A merge function to be used by the sortJsonObjects function*/
    public void mergeJsonObjects(String identifier, ArrayList<JsonObject> left, ArrayList<JsonObject> right, ArrayList<JsonObject> both){
        int leftIndex = 0;
        int rightIndex = 0;
        int bothIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            if ((identifier.contains("/") ? findJsonDataWithinObject(identifier, left.get(leftIndex)) : left.get(leftIndex).getString(identifier)).compareTo((identifier.contains("/") ? findJsonDataWithinObject(identifier, right.get(rightIndex)) : right.get(rightIndex).getString(identifier))) < 0) {
                both.set(bothIndex, left.get(leftIndex));
                leftIndex++;
            } else {
                both.set(bothIndex, right.get(rightIndex));
                rightIndex++;
            }
            bothIndex++;
        }

        if (leftIndex >= left.size()) {
            // The left ArrayList has been used up...
            for (int i = rightIndex; i < right.size(); i++) {
                both.set(bothIndex, right.get(i));
                bothIndex++;
            }
        } else {
            for (int i = leftIndex; i < left.size(); i++) {
                both.set(bothIndex, left.get(i));
                bothIndex++;
            }
        }

    }

    /*This function is for finding a Json value within a secondary JsonObject. The function splits the passed string
     * at every '/' delimiter, denoting a path of JsonObjects to traverse. It assumes every passed string will be a
     * JsonObject, except for the last one which is the actual value being searched for.
     * Upon reaching the last delimiter, it searches for the Json value within the JsonObject at the end of the path. */
    public String findJsonDataWithinObject (String identifier, JsonObject jsob){
        String temp[] = identifier.split("/");
        JsonObject tempJsob = jsob;
        int i;
        /*Iterate through the path of JsonObjects until the final JsonObject in the path is opened/retrieved */
        for(i = 0; i < temp.length - 1; i++){
            tempJsob = jsob.getJsonObject(temp[i]);
        }
        /*If the JsonObject is empty, return a "N/A" string*/
        if(tempJsob.size() == 0){
            return "N/A";
        }
        /*If the JsonObject contains a Key that matches the identifier, return the value*/
        else if(tempJsob.containsKey(temp[i])) {
            return tempJsob.getJsonArray(temp[i]).toString();
        }
        return "";
    }

}
