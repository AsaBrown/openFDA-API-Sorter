import java.util.HashMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*
TODO: Implement Multithreading in the section where it Externall Merge Sorts the Temporary files
TODO: Possibly add other options other than recall reports
TODO: Possibly Add data analogies between adverse reports and drug recalls
    TODO: Number of Adverse Events before recall
TODO: Improve sorting function
TODO: Explain that API is limited for results, so the need for downloading
 */

public class NewData extends Application {

    private AnchorPane pane = new AnchorPane();
    /*BtSave simply runs the sorting functions and outputs the file to the user.
    * btDefaultOptions has yet to be implemented, but it will allow the user to set
    * a default list of checkboxes to be checked by default.
    * btLoadDataOptions loads the available data fields that can be output/sorted, within
    * the given API field.*/
    Button btSave, btDefaultOptions, btLoadDataOptions;
    /*Data options contains the list of checkboxes for avaialble data fields
    * buttons contains the 3 buttons that the user interacts with to run the program. */
    GridPane dataOptions, buttons;
    /*A Hashmap containing all the checkboxes that represent the available data fields.*/
    HashMap<String, CheckBox> cbmap;
    /*A HashMap containing all the radiobuttons that represent the available API fields
    * that can be sorted/output. */
    HashMap<RadioButton, String> rbmap;
    /*A combobox, containing a list of the same fields available in the checkboxes, however
    * whichever one is selected here will be the sorting criteria. */
    ComboBox<String> sortCriteriaComboBox;
    ToggleGroup toggle = new ToggleGroup();
    /*TextField for the output file name. */
    TextField txOutputFile;
    Label dataOptionsLabel;
    VBox inputOptions;
    ButtonHandlers bh = new ButtonHandlers();

    public static void main (String[] args) {
        launch();
    }

    public void start (Stage primaryStage) {

        prepareStage(pane);

        btSave.setOnAction((e) -> {
            bh.handleSave(rbmap, toggle, txOutputFile.getText(), sortCriteriaComboBox.getValue(), cbmap);
        });

        btLoadDataOptions.setOnAction((e) -> {
            bh.handleDataOptions(cbmap, rbmap, dataOptions, dataOptionsLabel, sortCriteriaComboBox, toggle);
        });

        Scene scene = new Scene(pane, 900, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("OpenFDA Sorter");
        primaryStage.show();
    }

    public void prepareStage (AnchorPane pane){

        dataOptions = new GridPane();
        GridPane dataTypes = new GridPane();
        buttons = new GridPane();

        btSave = new Button("Save File");
        btDefaultOptions = new Button("Default Options");
        btLoadDataOptions = new Button("Load Data Options");

        txOutputFile = new TextField();
        sortCriteriaComboBox = new ComboBox<>();

        dataOptionsLabel = new Label("Section Data");
        Label outputFileNameLabel = new Label("Output File Name");
        Label sortingCriteriaLabel = new Label("Sorting Criteria");

        buttons.add(btDefaultOptions, 0, 1);
        buttons.add(btSave, 1, 1);
        buttons.add(btLoadDataOptions, 0, 0);

        dataOptions.add(dataOptionsLabel, 0, 0, 2, 1);
        inputOptions = new VBox();
        inputOptions.getChildren().addAll(outputFileNameLabel, txOutputFile, sortingCriteriaLabel, sortCriteriaComboBox);
        inputOptions.setMaxWidth(Double.MAX_VALUE);

        ButtonHandlers bt = new ButtonHandlers();
        try {
            rbmap = bt.getDataTypes();
            cbmap = new HashMap<>();
            bt.addDataTypes(dataTypes, rbmap, toggle);
        }catch(Exception ex){System.out.println(ex.toString());}

        inputOptions.setSpacing(5d);
        inputOptions.setPrefWidth(178d);

        btDefaultOptions.setMaxWidth(Double.MAX_VALUE);

        pane.getChildren().addAll(dataOptions, buttons, dataTypes, inputOptions);
        pane.setLeftAnchor(dataOptions, 20d);
        pane.setTopAnchor(dataOptions, 20d);
        pane.setLeftAnchor(buttons, 700d);
        pane.setTopAnchor(buttons, 20d);
        pane.setLeftAnchor(dataTypes, 550d);
        pane.setTopAnchor(dataTypes, 20d);
        pane.setTopAnchor(inputOptions, 85d);
        pane.setLeftAnchor(inputOptions, 700d);


    }
}


