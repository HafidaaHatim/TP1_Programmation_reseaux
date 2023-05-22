package org.example.blocking;

import java.io.*;
import java.net.Socket;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ClientJavaFx extends Application {
    private TextArea messageArea = new TextArea();
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public static void main(String[] args) {

        launch(args);
    }
    public ClientJavaFx(){

        try{
            Socket socket=new Socket("localhost",1234);
            printWriter=new PrintWriter(socket.getOutputStream(),true);
            bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void ChatClient_JavaFx_connect(String requests){
        new Thread(()->{
            try {
                String request;
                while (true){
                    request=bufferedReader.readLine();
                    System.out.println(request);
                    messageArea.appendText(request + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        printWriter.println(requests);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();

        messageArea.setEditable(false);
        root.setCenter(messageArea);

        VBox inputBox = new VBox();
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setSpacing(10);
        inputBox.setPadding(new Insets(10));

        Label messageLabel = new Label("message:");
        TextField messageField = new TextField();
        inputBox.getChildren().addAll(messageLabel, messageField);


        Button connectButton = new Button("send");
        connectButton.setOnAction(e -> {
            String name = messageField.getText();
            if (!name.isEmpty()) {
                ChatClient_JavaFx_connect(name);
            }
        });
        inputBox.getChildren().add(connectButton);

        root.setBottom(inputBox);

        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();
    }
}