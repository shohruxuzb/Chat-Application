package by.shohruh.scenefx;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class SocketClient2 extends Application {

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Socket socket;

    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Scene2.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();

        // Set up socket connection
        socket = new Socket("localhost", 8082);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());

        // Connect the GUI components
        textArea = (TextArea) scene.lookup("#textArea");
        textField = (TextField) scene.lookup("#textField");
        Button sendButton = (Button) scene.lookup("#sendButton");

        // Handle server messages in a separate thread
        new Thread(() -> {
            try {
                while (true) {
                    String message = inputStream.readUTF();
                    textArea.appendText(message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Set up event handlers
        sendButton.setOnAction(event -> {
            String message = textField.getText();
            try {
                outputStream.writeUTF(message);
                textField.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
