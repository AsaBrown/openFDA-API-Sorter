import javafx.application.Application;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.json.Json;
import javax.json.stream.*;
import javax.json.stream.JsonParser;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Test extends Application {

    public void start (Stage primaryStage) {

    }

    public static void main(String[] args) throws IOException {
        /*
        FileInputStream fis = new FileInputStream("references\\dataTypes.json");
        JsonParser jRead = Json.createParser(fis);
        JsonParser.Event e = jRead.next();
        System.out.println(e);
        e = jRead.next();
        System.out.println(e);
        e = jRead.next();
        System.out.println(e);
        jRead.skipObject();
        e = jRead.next();
        System.out.println(jRead.getString());
        */

    }
}
