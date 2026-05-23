package org.example.unishpere;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.io.File;

public class postController extends BaseController {
    @FXML
    private AnchorPane commentBox;
    @FXML
    private Label descriptions;
    @FXML
    ImageView mainImage;
    @FXML
    private Rectangle imageContainer;
    @FXML
    private Circle profile;
    @FXML
    private Label name;
    @FXML
    private Label time;

    public void setDescriptions(String des){
        descriptions.setText(des);
    }

    public void setUserDetails(String userName, String profilePhotoPath) {
        // Set user name
        name.setText(userName);

        // Set profile photo
        if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
            try {
                // Load image directly from the database URL
                Image profileImage = new Image(profilePhotoPath);
                profile.setFill(new ImagePattern(profileImage));
            } catch (Exception e) {
                System.out.println("Error loading profile photo: " + e.getMessage());
                setDefaultProfilePhoto();
            }
        } else {
            setDefaultProfilePhoto();
        }
    }

    private void setDefaultProfilePhoto() {
        try {
            String defaultPhotoPath = "src/main/resources/org/example/unishpere/images/default-profile.png";
            File defaultFile = new File(defaultPhotoPath);
            if (defaultFile.exists()) {
                Image defaultImage = new Image(defaultFile.toURI().toString());
                profile.setFill(new ImagePattern(defaultImage));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                File file = new File(imageUrl);
                if (file.exists()) {
                    // Load the image
                    Image image = new Image(file.toURI().toString());
                    imageContainer.setFill(new ImagePattern(image));

                } else {
                    System.out.println("Image file does not exist: " + imageUrl);
                    // Set a default image or handle the error
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle any other exceptions or set a default image
            }
        } else {
            System.out.println("Invalid image URL: " + imageUrl);
            // Set a default image or handle the error
        }
    }

    @FXML
    public void initialize() {
        String defaultPhotoPath = "src/main/resources/img/defaultPhoto.png";
        loadProfilePhoto(profile, defaultPhotoPath);
    }
}
