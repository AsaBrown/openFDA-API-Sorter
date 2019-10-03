import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import javax.json.Json;
import javax.json.stream.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static javax.json.stream.JsonParser.Event.END_OBJECT;
import static javax.json.stream.JsonParser.Event.KEY_NAME;
import static javax.json.stream.JsonParser.Event.START_OBJECT;

public class ButtonHandlers {

    ButtonHandlers(){
    }

    /*This function runs the sorting functions on the data and outputs to the user-specified file. */
    public void handleSave(HashMap<RadioButton, String> rbmap, ToggleGroup toggle, String outputFileName, String sortCriteria, HashMap<String, CheckBox> cbmap){
        try {
            DownloadData dd = new DownloadData();
            SortFile sf = new SortFile();
            /*rbStr determines which API fields to pull data from*/
            String rbStr = rbmap.get(toggle.getSelectedToggle());
            /*Downloads the set of files for that particular API field*/
            dd.parseDownload(dd.parseString(rbStr));
            ArrayList<File> files = new ArrayList<>();
            /*Iterates over each URL in the addressList. Calls a function to download the file from that link. */
            for (String address : dd.getAddressList()) {
                files.add(dd.downloadZipFile(address));
            }
            /*Calls the main sorting function, which will output the user-specified file in sorted, readable format. */
            sf.sortDataObjectsDriver(sortCriteria, files, outputFileName, cbmap);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    /*This function will display the available data fields within the selected API field. */
    public void handleDataOptions(HashMap<String, CheckBox> cbmap, HashMap<RadioButton, String> rbmap, GridPane dataOptions, Label dataOptionsLabel, ComboBox<String> sortCriteriaComboBox, ToggleGroup toggle){
        try {
            String rbStr = rbmap.get(toggle.getSelectedToggle());
            getCheckboxMap(rbStr, cbmap);
            dataOptions.getChildren().clear();
            dataOptions.getChildren().add(0, dataOptionsLabel);
            addCheckboxes(dataOptions, cbmap, sortCriteriaComboBox);
        } catch(Exception ex){System.out.println(ex);}
    }

    /*This function creates and populates a HashMap containing String/CheckBox pairs. Each checkbox represents a data
    * field that can be selected and output. It pulls this info from a pre-written Json file. In the referenced Json file,
    * is a list of JsonObjects, each one corresponding to an API field. It searches for the user-selected api field/JsonObject. That
    * JsonObject is a dummy, as it contains an exact copy of all expected Keys in the KEY/VALUE pairs, but the value portion is not used. */
    public void getCheckboxMap(String dataType, HashMap<String, CheckBox> map) throws IOException {
        /*Open the pre-written Json file*/
        File file = new File("references\\dataTypes.json");
        FileInputStream fos = new FileInputStream(file);
        JsonParser jRead = Json.createParser(fos);
        JsonParser.Event e;
        jRead.next();
        e = jRead.next();
        /* Iterate over the JSON file until the end is reached or the requested parameter is found */
        while(e != JsonParser.Event.END_OBJECT){
            if(e == KEY_NAME && jRead.getString().equals(dataType)){
                e = jRead.next();
                /*
                If the dataType is found in the file, add each of its KEY_NAME's to the map, with a
                corresponding CheckBox
                 */
                while(e != END_OBJECT){
                    if(e == JsonParser.Event.KEY_NAME){
                        map.put(jRead.getString(), new CheckBox(jRead.getString()));
                    }
                    e = jRead.next();
                }
                return;
            }
            /*
            If START_OBJECT is reached, then the KEY_NAME of said object did not
            match the dataType parameter, meaning that JSON Object can be skipped.
             */
            if(e == START_OBJECT){
                jRead.skipObject();
            }
            e = jRead.next();
        }

        return;
    }

    /*Accesses the pre-written Json file, containing an example set of data fields for each API field that can be accessed.
    * Writes the API fields to a HashMap, which will allow the user to select which API field to use. */
    public HashMap<RadioButton, String> getDataTypes() throws IOException{
        HashMap<RadioButton, String> map = new HashMap<>();
        File file = new File("references\\dataTypes.json");
        FileInputStream fos = new FileInputStream(file);
        JsonParser jRead = Json.createParser(fos);
        JsonParser.Event e;
        jRead.next();
        e = jRead.next();
        while(e != JsonParser.Event.END_OBJECT){
            if(e == KEY_NAME ){
                map.put(new RadioButton(jRead.getString()), jRead.getString());
            }
            if(e == START_OBJECT){
                jRead.skipObject();
            }
            e = jRead.next();

        }
        return map;
    }

    /*Add the checkboxes to the GridPane. */
    public void addCheckboxes(GridPane gpane, Map<String, CheckBox> map, ComboBox<String> comboBox){
        int row = 1, rows = 25, col = 0;
        for(Map.Entry<String, CheckBox> pair : map.entrySet()){
            comboBox.getItems().add(pair.getKey());
            gpane.add(pair.getValue(), col, row);
            row = ++row % rows;
            col = row == 0 ? ++col : col;
        }
    }

    /*Add the radiobuttons to the GridPane*/
    public void addDataTypes (GridPane gpane, HashMap<RadioButton, String> map, ToggleGroup toggle){
        int row = 1, rows = 25, col = 0;
        for(Map.Entry<RadioButton, String> pair : map.entrySet()){
            gpane.add(pair.getKey(), col, row);
            pair.getKey().setToggleGroup(toggle);
            row = ++row % rows;
            col = row == 0 ? ++col : col;
        }
    }
}
