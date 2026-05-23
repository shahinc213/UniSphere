package org.example.unishpere;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class tutoringFormController {

    @FXML
    private TextField courseName;
    @FXML
    private TextField problemTopic;
    @FXML
    private TextArea description;

    // Action to handle the request button click
    @FXML
    private void handleRequestButtonClick() {
        String course = courseName.getText();
        String topic = problemTopic.getText();
        String desc = description.getText();
        String loggedInUserEmail = Session.getLoggedInUser();

        if (loggedInUserEmail == null) {
            System.out.println("No user is logged in.");
            return;
        }

        if (course.isEmpty() || topic.isEmpty() || desc.isEmpty()) {
            System.out.println("All fields must be filled out.");
            return;
        }

        try (Connection connection = dbConnect.getconnection()) {
            if (connection == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            // Retrieve the user ID for the logged-in user
            String getUserQuery = "SELECT id FROM users WHERE email = ?";
            PreparedStatement getUserStatement = connection.prepareStatement(getUserQuery);
            getUserStatement.setString(1, loggedInUserEmail);
            ResultSet resultSet = getUserStatement.executeQuery();

            if (resultSet.next()) {
                int requesterId = resultSet.getInt("id");

                // Prepare the insert query
                String insertQuery = "INSERT INTO peertutoring (requester_id, course_name, problem_topic, description) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                insertStatement.setInt(1, requesterId);  // requester_id
                insertStatement.setString(2, course);    // course_name
                insertStatement.setString(3, topic);     // problem_topic
                insertStatement.setString(4, desc);      // description

                // Execute the query and check the number of rows affected
                int rowsAffected = insertStatement.executeUpdate();
                System.out.println("Rows affected: " + rowsAffected);

                if (rowsAffected > 0) {
                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int newRequestId = generatedKeys.getInt(1);
                        System.out.println("Inserted request ID: " + newRequestId);
                    }
                } else {
                    System.out.println("Failed to insert request.");
                }
            } else {
                System.out.println("User not found in the database.");
            }
        } catch (SQLException e) {
            System.out.println("Error executing SQL statement: " + e.getMessage());
            e.printStackTrace();
        }

        // Close the current window after submitting the request
        Stage stage = (Stage) courseName.getScene().getWindow();
        stage.close();
    }
}
