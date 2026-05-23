package org.example.unishpere;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class acceptedRequestCardonAccepterSideController implements Initializable {
    @FXML
    private Circle photo;
    @FXML
    private Label name;
    @FXML
    private Label course;
    @FXML
    private Label topic;
    @FXML
    private Label description;
    @FXML
    private Button messageButton;

    private int requestId;
    private String requesterEmail;
    private dbConnect dbConnect;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnect = new dbConnect();
    }

    public void setRequestId(int id) {
        this.requestId = id;
    }

    public void setRequesterEmail(String email) {
        this.requesterEmail = email;
    }

    public void setRequesterDetails(String studentName, String courseName, String topicName, String desc) {
        name.setText(studentName);
        course.setText(courseName);
        topic.setText(topicName);
        description.setText(desc);
    }

    public void setProfilePhoto(String photoPath) {
        if (photoPath != null && !photoPath.isEmpty()) {
            Image image = new Image(photoPath);
            photo.setFill(new ImagePattern(image));
        } else {
            setDefaultPhoto();
        }
    }

    private void setDefaultPhoto() {
        Image defaultImage = new Image(getClass().getResource("/img/defaultPhoto.png").toExternalForm());
        photo.setFill(new ImagePattern(defaultImage));
    }

    private String getUsernameFromEmail(String email) {
        if (dbConnect == null) {
            dbConnect = new dbConnect();
        }
        
        String username = null;
        String query = "SELECT first_name, last_name FROM users WHERE email = ?";
        
        try (Connection connection = dbConnect.getconnection()) {
            if (connection == null) {
                return email.split("@")[0];
            }
            
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                username = firstName + " " + lastName;
            }
        } catch (SQLException e) {
            return email.split("@")[0];
        }
        
        return username != null ? username : email.split("@")[0];
    }

    @FXML
    private void handleMessageButton() {
        try {
            // Verify we have the requester's email
            if (requesterEmail == null || requesterEmail.isEmpty()) {
                showError("Could not start chat", "Requester email is not set");
                return;
            }

            // Verify the email exists in database
            try (Connection connection = dbConnect.getconnection()) {
                String query = "SELECT email FROM users WHERE email = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, requesterEmail);
                ResultSet rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    showError("Could not start chat", "Could not find user with email: " + requesterEmail);
                    return;
                }
                
                // Get current user's email
                String currentUserEmail = Session.getLoggedInUser();
                if (currentUserEmail == null || currentUserEmail.isEmpty()) {
                    showError("Could not start chat", "No user is logged in");
                    return;
                }

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("chatCorner.fxml"));
                Parent root = fxmlLoader.load();
                chatCornerController chatController = fxmlLoader.getController();
                
                chatController.setUsernames(currentUserEmail, requesterEmail);

                Stage stage = new Stage();
                stage.initModality(Modality.NONE);
                stage.setScene(new Scene(root));
                stage.setTitle("Chat with " + getUsernameFromEmail(requesterEmail));
                stage.setX(1421);
                stage.setY(395);
                stage.setOnCloseRequest(event -> chatController.closeConnection());
                stage.show();
            }
        } catch (SQLException e) {
            showError("Database Error", "Could not verify user details");
        } catch (IOException e) {
            showError("Could not open chat", "Error opening chat window");
        }
    }

    private void showError(String header, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
