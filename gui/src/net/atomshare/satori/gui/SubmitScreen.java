//Jan Derbisz
package net.atomshare.satori.gui;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

public class SubmitScreen extends Application{
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Wysyłanie rozwiązań");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 4);

        final Text actiontarget2 = new Text();
        actiontarget2.setFill(Color.RED);
        actiontarget2.setText("Nie wybrano pliku");
        grid.add(actiontarget2, 1, 2);

        Button btn = new Button("Wyślij");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 2, 4);

        Button btn2 = new Button("Wybierz");
        HBox hbBtn2 = new HBox(10);
        hbBtn2.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn2.getChildren().add(btn2);
        grid.add(hbBtn2, 2, 2);

        ListView<String> list = new ListView<String>();
        ObservableList<String> items =FXCollections.observableArrayList (
                "A", "B", "C", "D");
        list.setItems(items);
        list.setPrefSize(210, 30);
        grid.add(list, 1, 1, 2, 1);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                actiontarget.setFill(Color.GREEN);
                actiontarget.setText("Wysłano");
            }
        });

        btn2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                actiontarget2.setFill(Color.BLUE);
                actiontarget2.setText("Nazwa_Pliku");
            }
        });

        Scene scene = new Scene(grid, 400, 225);
        primaryStage.setScene(scene);

        Text scenetitle = new Text("Wyślij rozwiązanie");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("Wybierz zadanie:");
        grid.add(userName, 0, 1);


        Label pw = new Label("Wybierz plik:");
        grid.add(pw, 0, 2);



        primaryStage.show();
    }
}
