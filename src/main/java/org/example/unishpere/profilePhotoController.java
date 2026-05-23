package org.example.unishpere;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class profilePhotoController {

    @FXML
    private ImageView imagePreview;

    @FXML
    private Button uploadButton, saveButton;

    private String selectedImagePath;

    @FXML
    public void initialize() {
        uploadButton.setOnAction(event -> chooseImage());
        saveButton.setOnAction(this::saveProfilePhoto);
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            selectedImagePath = selectedFile.toURI().toString();
            Image image = new Image(selectedImagePath);
            imagePreview.setImage(image);
        }
    }

    @FXML
    private void saveProfilePhoto(ActionEvent event) {
        if (selectedImagePath != null) {
            String loggedInUserEmail = Session.getLoggedInUser();
            if (loggedInUserEmail == null) {
                System.out.println("No user is logged in.");
                return;
            }

            try (Connection connection = dbConnect.getconnection()) {
                // Retrieve the user_id using the logged-in user's email
                String getUserQuery = "SELECT id FROM users WHERE email = ?";
                PreparedStatement getUserStatement = connection.prepareStatement(getUserQuery);
                getUserStatement.setString(1, loggedInUserEmail);
                ResultSet resultSet = getUserStatement.executeQuery();

                if (resultSet.next()) {
                    int userId = resultSet.getInt("id");

                    // Update the profile photo for the retrieved user_id
                    String updateQuery = "UPDATE users SET profile_photo = ? WHERE id = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setString(1, selectedImagePath);
                    updateStatement.setInt(2, userId);

                    int rowsAffected = updateStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Profile photo updated successfully!");
                    } else {
                        System.out.println("Failed to update profile photo.");
                    }
                } else {
                    System.out.println("User not found for the logged-in email.");
                }

                // Close the current window
                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                stage.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No image selected!");
        }
    }


}
