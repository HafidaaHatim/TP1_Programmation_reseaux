package org.example.nonblocking;

import javafx.application.Application;
import javafx.stage.Stage;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientFx extends Application {

    private TextArea chatTextArea = new TextArea();
    private TextField messageTextField = new TextField();
    private Button sendButton = new Button("Send");
    private Button connectButton = new Button("Connect");
    private Button disconnectButton = new Button("Disconnect");

    private SocketChannel socketChannel;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    private String host = "localhost";
    private int port = 1234;

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane root = new BorderPane();

        chatTextArea.setEditable(false);
        chatTextArea.setWrapText(true);
        chatTextArea.setFont(Font.font("Verdana", 12));

        HBox messageBox = new HBox();
        messageBox.setSpacing(10);
        messageBox.setPadding(new Insets(10, 10, 10, 10));
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.getChildren().addAll(messageTextField, sendButton);

        VBox buttonBox = new VBox();
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10, 10, 10, 10));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(connectButton, disconnectButton);

        root.setCenter(chatTextArea);
        root.setBottom(messageBox);
        root.setRight(buttonBox);

        sendButton.setOnAction(event -> sendMessage());
        messageTextField.setOnAction(event -> sendMessage());

        connectButton.setOnAction(event -> connect());
        disconnectButton.setOnAction(event -> disconnect());

        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    private void connect() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false);
            chatTextArea.appendText("Connected to server.\n");
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            sendButton.setDisable(false);
        } catch (IOException e) {
            e.printStackTrace();
            chatTextArea.appendText("Connection failed.\n");
        }
    }

    private void disconnect() {
        try {
            socketChannel.close();
            chatTextArea.appendText("Disconnected from server.\n");
            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
            sendButton.setDisable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageTextField.getText().trim();
        if (!message.isEmpty()) {
            try {
                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                socketChannel.write(buffer);
                messageTextField.clear();
            } catch (IOException e) {
                e.printStackTrace();
                chatTextArea.appendText("Failed to send message.\n");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
