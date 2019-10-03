import javafx.scene.control.CheckBox;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Character.toUpperCase;

public class PrintData {

    PrintData(){
    }

    /*This function is for finding a Json value within a secondary JsonObject. The function splits the passed string
     * at every '/' delimiter, denoting a path of JsonObjects to traverse. It assumes every passed string will be a
     * JsonObject, except for the last one which is the actual value being searched for.
     * Upon reaching the last delimiter, it searches for the Json value within the JsonObject at the end of the path. */
    public String findJsonDataWithinObject (String identifier, JsonObject jsob){
        String temp[] = identifier.split("/");
        JsonObject tempJsob = jsob;
        int i;
        for(i = 0; i < temp.length - 1; i++){
            tempJsob = jsob.getJsonObject(temp[i]);
        }
        if(tempJsob.size() == 0){
            return "";
        }
        else if(tempJsob.containsKey(temp[i])) {
            return tempJsob.getJsonArray(temp[i]).toString();
        }
        return "";
    }

    /*This function writes readable data to the final, user specified, output file. It is passed only a single JsonObject to
    * write at a time. It iterates over the map of checkboxes, if the user selected a checkbox, the corresponding Json Value
    * is printed. */
    public void writeFinalDataToFile(FileOutputStream fos, JsonObject jsob, HashMap<String, CheckBox> cbMap) throws IOException {
        String str, tempStr;
        /*Iterate over all entries in the map, containing checkboxes and strings that are the same as the Json Key/Value Keys*/
        for(Map.Entry<String, CheckBox> pair : cbMap.entrySet()){
            /*If a checkbox is selected, the corresponding JsonValue is formatted and output to file. */
            if(pair.getValue().isSelected()){
                if((pair.getKey().contains("/") ? findJsonDataWithinObject(pair.getKey(), jsob) : pair.getKey()) != null || jsob.containsKey(pair.getKey())) {
                    tempStr = (pair.getKey().contains("/") ? findJsonDataWithinObject(pair.getKey(), jsob) : jsob.getString(pair.getKey()));
                    fos.write(formatDataString(pair.getKey(), tempStr).getBytes());
                }
            }
        }
        fos.write("\n".getBytes());
    }

    /*This function writes sorted JsonObjects to a file, retaining Json format. These files are later used to be merged
    * into a single, readable output file. */
    public void writeTempDataToFile(ArrayList<JsonObject> jsobs, File file){
        try {
            FileOutputStream fos = new FileOutputStream(file);
            JsonGeneratorFactory jsonGeneratorFactory = Json.createGeneratorFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
            JsonGenerator jWrite = jsonGeneratorFactory.createGenerator(fos);
            jWrite.writeStartObject();
            jWrite.writeStartArray("results");
            /*For each object in the passed array*/
            for(JsonObject jsob : jsobs){
                jWrite.writeStartObject();
                /*For each value in the JsonObject, write it to the temp file. */
                jsob.forEach((key, value) -> {
                    jWrite.write(key, value);
                });
                jWrite.writeEnd();
            }
            /*Close the object, array, and finally the JsonGenerator*/
            jWrite.writeEnd();
            jWrite.writeEnd();
            jWrite.close();
        } catch (Exception ex) {System.out.println(ex);}
    }

    /*Formats the entire JSON key/value to be printed to text file*/
    public String formatDataString(String key, String value){
        return formatDataKey(key) + " : " + value + "\n";
    }

    /*Formats the key portion of a JSON value*/
    public String formatDataKey(String key){
        StringBuilder s = new StringBuilder();
        s.ensureCapacity(key.length());
        /*Initializing c ensure the first letter will be capitalized*/
        char c = '_';
        for(int i = 0; i < key.length(); i++) {
            /*If the previous character was a space, capitalize the next append*/
            if(c == '_' && key.charAt(i) != '_'){
                s.append(toUpperCase(key.charAt(i)));
            }
            /*Replace the '_' delimiters with a space*/
            else if(key.charAt(i) == '_') {
                s.append(' ');
            }
            else {
                s.append(key.charAt(i));
            }
            c = key.charAt(i);
        }
        return s.toString().trim();
    }
}
