package org.example.unishpere;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class cycleController implements Initializable {
    @FXML
    private Circle profile;
    
    @FXML
    private ComboBox<String> startLocation;
    
    @FXML
    private ComboBox<String> endLocation;
    
    @FXML
    private DatePicker pickupDate;
    
    @FXML
    private Button bookNowButton;
    
    @FXML
    private Button returnNowButton;

    private boolean hasActiveRental = false;
    private int activeRentalId = -1;
    private int activeCycleId = -1;
    private int currentUserId = -1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getCurrentUserId();
        checkActiveRental();
        updateUI();
        loadProfilePhoto();
    }

    private void getCurrentUserId() {
        try (Connection conn = dbConnect.getconnection()) {
            String query = "SELECT id FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, Session.getLoggedInUser());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUserId = rs.getInt("id");
            } else {
                showAlert("Error", "Could not find user information.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error occurred while getting user information.");
        }
    }

    private void checkActiveRental() {
        if (currentUserId == -1) return;
        
        try (Connection conn = dbConnect.getconnection()) {
            String query = "SELECT rental_id, cycle_id FROM cycle_rentals WHERE user_id = ? AND status = 'rented'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                hasActiveRental = true;
                activeRentalId = rs.getInt("rental_id");
                activeCycleId = rs.getInt("cycle_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error occurred while checking active rentals.");
        }
    }

    private void updateUI() {
        if (hasActiveRental) {
            startLocation.setDisable(true);
            endLocation.setDisable(true);
            pickupDate.setDisable(true);
            bookNowButton.setDisable(true);
            returnNowButton.setVisible(true);
        } else {
            startLocation.setDisable(false);
            endLocation.setDisable(false);
            pickupDate.setDisable(false);
            bookNowButton.setDisable(false);
            returnNowButton.setVisible(false);
        }
    }

    @FXML
    public void handleBookNow() {
        if (currentUserId == -1) {
            showAlert("Error", "Please log in to rent a cycle.");
            return;
        }

        if (hasActiveRental) {
            showAlert("Cannot Book", "You already have an active cycle rental. Please return it first.");
            return;
        }

        String start = startLocation.getValue();
        String end = endLocation.getValue();
        LocalDate date = pickupDate.getValue();

        if (start == null || end == null || date == null) {
            showAlert("Invalid Input", "Please select all required fields.");
            return;
        }

        if (start.equals(end)) {
            showAlert("Invalid Selection", "Start and end locations cannot be the same.");
            return;
        }

        try (Connection conn = dbConnect.getconnection()) {
            // Find available cycle at the start location
            String findCycleQuery = "SELECT cycle_id FROM cycles WHERE location = ? AND is_available = 1 LIMIT 1";
            PreparedStatement findCycleStmt = conn.prepareStatement(findCycleQuery);
            findCycleStmt.setString(1, start);
            
            ResultSet rs = findCycleStmt.executeQuery();
            if (!rs.next()) {
                showAlert("No Cycles Available", "Sorry, no cycles are available at the selected location.");
                return;
            }

            int cycleId = rs.getInt("cycle_id");

            // Begin transaction
            conn.setAutoCommit(false);
            try {
                // Update cycle availability
                String updateCycleQuery = "UPDATE cycles SET is_available = 0 WHERE cycle_id = ?";
                PreparedStatement updateCycleStmt = conn.prepareStatement(updateCycleQuery);
                updateCycleStmt.setInt(1, cycleId);
                updateCycleStmt.executeUpdate();

                // Create rental record
                String createRentalQuery = "INSERT INTO cycle_rentals (user_id, cycle_id, rental_start_time, status) VALUES (?, ?, NOW(), 'rented')";
                PreparedStatement createRentalStmt = conn.prepareStatement(createRentalQuery, Statement.RETURN_GENERATED_KEYS);
                createRentalStmt.setInt(1, currentUserId);
                createRentalStmt.setInt(2, cycleId);
                createRentalStmt.executeUpdate();

                ResultSet generatedKeys = createRentalStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    activeRentalId = generatedKeys.getInt(1);
                    activeCycleId = cycleId;
                    hasActiveRental = true;
                }

                conn.commit();
                showAlert("Success", "Cycle booked successfully!");
                updateUI();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to book the cycle. Please try again.");
        }
    }

    @FXML
    public void handleReturnNow() {
        if (!hasActiveRental) {
            showAlert("Error", "No active rental found.");
            return;
        }

        try (Connection conn = dbConnect.getconnection()) {
            conn.setAutoCommit(false);
            try {
                // Update rental status
                String updateRentalQuery = "UPDATE cycle_rentals SET status = 'returned', rental_end_time = NOW() WHERE rental_id = ?";
                PreparedStatement updateRentalStmt = conn.prepareStatement(updateRentalQuery);
                updateRentalStmt.setInt(1, activeRentalId);
                updateRentalStmt.executeUpdate();

                // Update cycle location and availability
                String updateCycleQuery = "UPDATE cycles SET location = ?, is_available = 1 WHERE cycle_id = ?";
                PreparedStatement updateCycleStmt = conn.prepareStatement(updateCycleQuery);
                updateCycleStmt.setString(1, endLocation.getValue());
                updateCycleStmt.setInt(2, activeCycleId);
                updateCycleStmt.executeUpdate();

                conn.commit();
                showAlert("Success", "Cycle returned successfully!");
                
                hasActiveRental = false;
                activeRentalId = -1;
                activeCycleId = -1;
                updateUI();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return the cycle. Please try again.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void goToHomePage(ActionEvent event) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("home.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void goToServicePage(ActionEvent event) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("services.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    public void goToMessagePage(ActionEvent event) throws IOException
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigation.pushScene(stage.getScene());

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("messagePage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void goBack(ActionEvent event) {
        // Get the previous scene from the stack
        Scene previousScene = Navigation.popScene();

        if (previousScene != null) {
            // Set the previous scene to the stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(previousScene);
            stage.show();
        } else {
            System.out.println("No previous page in history.");
        }
    }

    @FXML
    public void openProfilePopup() {
        try {
            // Load the FXML file for the popup content
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("profile.fxml"));
            AnchorPane popupContent = fxmlLoader.load();

            // Create a new stage for the popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with the main window
            popupStage.setTitle("Profile");
            Scene popupScene = new Scene(popupContent);
            popupStage.setScene(popupScene);

            // Get the main window (parent stage) and set it as the owner of the popup
            Stage parentStage = (Stage) profile.getScene().getWindow();
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

    private void loadProfilePhoto() {
        String loggedInUserEmail = Session.getLoggedInUser();

        if (loggedInUserEmail == null) {
            System.out.println("No user is logged in.");
            return;
        }

        try (Connection connection = dbConnect.getconnection()) {
            // Query to fetch the user data
            String query = "SELECT profile_photo FROM users WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loggedInUserEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
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
