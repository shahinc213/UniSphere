package org.example.unishpere;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class ChatUserCardController {
    @FXML
    private Circle userPhoto;
    @FXML
    private Label userName;

    private String userEmail;

    public void setUserDetails(String name, String email, String photoPath) {
        userName.setText(name);
        userEmail = email;

        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                Image image = new Image(photoPath);
                userPhoto.setFill(new ImagePattern(image));
            } catch (Exception e) {
                setDefaultPhoto();
            }
        } else {
            setDefaultPhoto();
        }
    }

    private void setDefaultPhoto() {
        Image defaultImage = new Image(getClass().getResourceAsStream("/img/default.png"));
        userPhoto.setFill(new ImagePattern(defaultImage));
    }

    public String getUserEmail() {
        return userEmail;
    }

    @FXML
    private void initialize() {
        // Add click handler to the entire card
        userPhoto.getParent().setOnMouseClicked(event -> {
            messagePageController.getInstance().openChat(userEmail);
        });
    }
}
