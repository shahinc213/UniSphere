package org.example.unishpere;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class profileController {
    @FXML
    private AnchorPane profileScene;
    @FXML
    private Hyperlink logout;
    @FXML
    private Button addPhoto;
    @FXML
    private ImageView profilePhoto;
    @FXML
    private Label name;
    @FXML
    private Label email;
    @FXML
    private Circle photo;

    @FXML
    public void logout(ActionEvent event) throws IOException {
        // Clear the user session
        System.out.println("User logged out: " + Session.getLoggedInUser());
        Session.clearSession();

        // Close the popup window
        Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        popupStage.close();

        // Close the main application window
        Stage mainStage = (Stage) popupStage.getOwner(); // Fetch the owner (main stage)
        if (mainStage != null) {
            mainStage.close();
        }

        // Open the login page in a new stage
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage loginStage = new Stage();
        loginStage.setScene(scene);
        loginStage.show();
    }

    @FXML
    public void openProfilePhotoUpload(ActionEvent event) {
        try {
            // Load the FXML file for the popup content
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("addProfilePhoto.fxml"));
            AnchorPane popupContent = fxmlLoader.load();

            // Create a new stage for the popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with the main window
            popupStage.setTitle("Add Photo");
            Scene popupScene = new Scene(popupContent);
            popupStage.setScene(popupScene);

            // Get the main window (parent stage) and set it as the owner of the popup
            Stage parentStage = (Stage) addPhoto.getScene().getWindow();
            popupStage.initOwner(parentStage); // Set parent stage as the owner

            // Center the popup relative to the screen (adjust position as needed)
            popupStage.setX(1367);
            popupStage.setY(106);

            // Show the popup
            popupStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        loadProfileData();
    }

    private void loadProfileData() {
        String loggedInUserEmail = Session.getLoggedInUser();

        if (loggedInUserEmail == null) {
            System.out.println("No user is logged in.");
            return;
        }

        try (Connection connection = dbConnect.getconnection()) {
            // Query to fetch the user data
            String query = "SELECT first_name, last_name, email, profile_photo FROM users WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loggedInUserEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Retrieve and set the user's name
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                name.setText(firstName + " " + lastName);

                // Retrieve and set the user's email
                String userEmail = resultSet.getString("email");
                email.setText(userEmail);

                // Retrieve and set the user's profile photo
                String profilePhotoPath = resultSet.getString("profile_photo");
                if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
                    Image image = new Image(profilePhotoPath);
//                    profilePhoto.setImage(image);
                    photo.setFill(new ImagePattern(image));
                } else {
                    File file = new File("src/main/resources/img/defaultPhoto.png");
                    if (file.exists()) {
                        String defaultPhotoPath = file.toURI().toString();
                        Image defaultImage = new Image(defaultPhotoPath);
                        photo.setFill(new ImagePattern(defaultImage)); // Assuming Circle
                    } else {
                        System.out.println("Default photo not found.");
                    }
                }
            } else {
                System.out.println("User not found in the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
