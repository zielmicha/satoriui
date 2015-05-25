package net.atomshare.satori.gui;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
	String contest;
	String encoding;
	SubmitScreen()
	{
		contest=null;
		encoding=null;
	}
	SubmitScreen(String r, String s)
	{
		contest=r;
		encoding=s;
	}
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
        
        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 2, 2, 1);

      
        
        Button btn = new Button("Wyślij");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 2, 4);
      
        ContestList cl=new ContestList(encoding);
        ArrayList<String> l = cl.listProblems(cl.findContest(contest));
        final ListView<String> list = new ListView<String>();
        ObservableList<String> items =FXCollections.observableArrayList (l);
        list.setItems(items);
        list.setPrefSize(210, 30);
        grid.add(list, 1, 1, 2, 1);
        
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
            	if(list.getSelectionModel().getSelectedItem()==null)
            	{
            		actiontarget.setFill(Color.RED);
            		actiontarget.setText("Wybierz zadanie!");
            	}
            	else
            	{          	
            		actiontarget.setFill(Color.GREEN);
            		actiontarget.setText("Wysłano");
            	
            	}
            }
        });
        
 

        Scene scene = new Scene(grid, 400, 225);
        primaryStage.setScene(scene);

        Text scenetitle = new Text("Wyślij rozwiązanie");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("Wybierz zadanie:");
        grid.add(userName, 0, 1);


        Label pw = new Label("Podaj ścieżkę pliku:");
        grid.add(pw, 0, 2);

      

        primaryStage.show();
    }
}

