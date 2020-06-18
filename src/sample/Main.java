package sample;

import UI.UI;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        UI ui=new UI(primaryStage);
        ui.displayUi();
    }
}
