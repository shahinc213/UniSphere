package org.example.unishpere;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class writeCmntController {

    @FXML
    private TextField writeComment; // Text field for writing caption
    @FXML
    private Circle profile;
    @FXML
    private Label name;

    private String selectedImagePath = null; // Path of the selected image

    // Action for Image Button
    public void onImageButtonClick() {
        // Create a FileChooser instance
        FileChooser fileChooser = new FileChooser();

        // Set extension filters for image files
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Show the open file dialog
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        // Check if a file is selected
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            System.out.println("Selected file: " + selectedImagePath);
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    // Action for Post Button
    @FXML
    public void onPostButtonClick(ActionEvent event) {
        String caption = writeComment.getText();

        // Check if caption is empty
        if (caption.isEmpty()) {
            System.out.println("Caption cannot be empty.");
            return;
        }

        // Get the logged-in user's email
        String loggedInUserEmail = Session.getLoggedInUser();

        // Check if user is logged in
        if (loggedInUserEmail == null) {
            System.out.println("No user is logged in. Cannot save the post.");
            return;
        }

        // Ensure image path is set before attempting to save to the database
        if (selectedImagePath == null) {
            System.out.println("No image selected. Please select an image.");
            return;
        }

        try (Connection conn = dbConnect.getconnection()) { // Try-with-resources for connection
            // Query to fetch user_id based on the logged-in user's email
            String userQuery = "SELECT id FROM users WHERE email = ?";
            PreparedStatement userStatement = conn.prepareStatement(userQuery);
            userStatement.setString(1, loggedInUserEmail);
            ResultSet userResultSet = userStatement.executeQuery();

            Integer userId = null;
            if (userResultSet.next()) {
                userId = userResultSet.getInt("id");
            }

            // If user ID is not found
            if (userId == null) {
                System.out.println("Could not find user ID for the logged-in user.");
                return;
            }

            // SQL query to insert post data into the posts table
            String query = "INSERT INTO posts (user_id, caption, photo_url) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, userId); // Use the fetched user_id
            preparedStatement.setString(2, caption);
            preparedStatement.setString(3, selectedImagePath); // Use selectedImagePath

            // Execute the insert operation
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Post saved successfully!");
            }

            // Close resources
            preparedStatement.close();
            userStatement.close();

            // Close the current window
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error saving post to the database.");
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


                // Retrieve and set the user's profile photo
                String profilePhotoPath = resultSet.getString("profile_photo");
                if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
                    Image image = new Image(profilePhotoPath);
//                    profilePhoto.setImage(image);
                    profile.setFill(new ImagePattern(image));
                } else {
                    File file = new File("src/main/resources/img/defaultPhoto.png");
                    if (file.exists()) {
                        String defaultPhotoPath = file.toURI().toString();
                        Image defaultImage = new Image(defaultPhotoPath);
                        profile.setFill(new ImagePattern(defaultImage)); // Assuming Circle
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
