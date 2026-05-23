package org.example.unishpere;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Start the chat server in a separate thread
        ChatServer chatServer = ChatServer.getInstance();
        new Thread(() -> chatServer.start()).start();
        
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

//        dbConnect db = new dbConnect();
//        db.getconnection();
    }

    @Override
    public void stop() {
        // Stop the chat server when the application closes
        ChatServer.getInstance().stop();
    }

    public static void main(String[] args) {
        launch();
    }
}