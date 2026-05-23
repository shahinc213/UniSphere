package org.example.unishpere;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class signUpController {
    @FXML
    private TextField firstName;
    @FXML
    private TextField LastName;
    @FXML
    private TextField email;
    @FXML
    private TextField sID;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField Cpassword;

    @FXML
    public void signIN(ActionEvent event) throws IOException {
        String fName = firstName.getText();
        String lName = LastName.getText();
        String emL = email.getText();
        String sId = sID.getText();
        String pass = password.getText();
        String cPass = Cpassword.getText();

        // Check if passwords match
        if (!pass.equals(cPass)) {
            System.out.println("Passwords do not match!");
            return;
        }

        // Save data to the database
        saveToDatabase(fName, lName, emL, sId, pass, cPass);

        // Navigate to login scene
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void saveToDatabase(String firstName, String lastName, String email, String studentID, String password, String cPassword) {
        String query = "INSERT INTO users (first_name, last_name, email, student_id, password, cPassword) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            dbConnect dbConnection = new dbConnect(); // Create an instance of dbConnect
            Connection conn = dbConnection.getconnection(); // Call the non-static method

            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, studentID);
            stmt.setString(5, password);
            stmt.setString(6, cPassword);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("User registered successfully.");
            } else {
                System.out.println("Failed to register user.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void goToLoginPage(ActionEvent event) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
