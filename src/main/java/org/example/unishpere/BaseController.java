package org.example.unishpere;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseController {

    protected void loadProfilePhoto(Circle profile, String defaultPhotoPath) {
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
                    profile.setFill(new ImagePattern(new javafx.scene.image.Image(profilePhotoPath)));
                } else {
                    setDefaultPhoto(profile, defaultPhotoPath);
                }
            } else {
                System.out.println("User not found in the database.");
                setDefaultPhoto(profile, defaultPhotoPath);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setDefaultPhoto(profile, defaultPhotoPath);
        }
    }

    private void setDefaultPhoto(Circle profile, String defaultPhotoPath) {
        File file = new File(defaultPhotoPath);
        if (file.exists()) {
            profile.setFill(new ImagePattern(new javafx.scene.image.Image(file.toURI().toString())));
        } else {
            System.out.println("Default photo not found.");
        }
    }
}
