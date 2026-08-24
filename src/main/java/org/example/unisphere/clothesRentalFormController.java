package org.example.unishpere;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class clothesRentalFormController {
    @FXML
    private TextField type;
    @FXML
    private TextField size;
    @FXML
    private TextField color;
    @FXML
    private TextField gender;
    @FXML
    private TextField rentalPrice;

    private String selectedImagePath = null;

    @FXML
    public void onImageButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            System.out.println("Selected file: " + selectedImagePath);
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    @FXML
    public void onSubmitButtonClick(ActionEvent event) {
        if (validateInputs()) {
            try {
                String imagePath = copyImageToStorage();
                saveToDatabase(imagePath);
                // Close the current window
                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                stage.close();
                showAlert("Success", "Rental item posted successfully!", Alert.AlertType.INFORMATION);
                closeWindow(event);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to post rental item: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateInputs() {
        if (type.getText().trim().isEmpty() || 
            size.getText().trim().isEmpty() || 
            color.getText().trim().isEmpty() || 
            gender.getText().trim().isEmpty() || 
            rentalPrice.getText().trim().isEmpty() || 
            selectedImagePath == null) {
            
            showAlert("Validation Error", "All fields are required including an image!", Alert.AlertType.ERROR);
            return false;
        }

        try {
            Double.parseDouble(rentalPrice.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Rental price must be a valid number!", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private String copyImageToStorage() throws Exception {
        if (selectedImagePath == null) {
            throw new Exception("No image selected");
        }

        // Create images directory if it doesn't exist
        Path imagesDir = Paths.get("src/main/resources/img/cloths_rental");
        Files.createDirectories(imagesDir);

        // Generate unique filename
        String fileName = "cloths_" + System.currentTimeMillis() + "_" + new File(selectedImagePath).getName();
        Path targetPath = imagesDir.resolve(fileName);

        // Copy the file
        Files.copy(Paths.get(selectedImagePath), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Return the relative path for database storage
        return "img/cloths_rental/" + fileName;
    }

    private void saveToDatabase(String imagePath) throws SQLException {
        String sql = "INSERT INTO clothingrental (type, size, color, gender, rent_price, image_path, lessor, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = dbConnect.getconnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type.getText().trim());
            pstmt.setString(2, size.getText().trim());
            pstmt.setString(3, color.getText().trim());
            pstmt.setString(4, gender.getText().trim());
            pstmt.setDouble(5, Double.parseDouble(rentalPrice.getText().trim()));
            pstmt.setString(6, imagePath);
            pstmt.setInt(7, getUserId());
            
            pstmt.executeUpdate();
        }
    }

    private int getUserId() throws SQLException {
        String loggedInUserEmail = Session.getLoggedInUser();
        if (loggedInUserEmail == null) {
            throw new SQLException("No user is logged in");
        }

        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = dbConnect.getconnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, loggedInUserEmail);
            var rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("User not found");
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
