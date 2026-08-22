package org.example.unishpere;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class clothesRentalCardController {
    @FXML
    private Circle photo; // Profile photo
    @FXML
    private Rectangle imageContainer; // Product image
    @FXML
    private Label name;
    @FXML
    private Label type;
    @FXML
    private Label size;
    @FXML
    private Label color;
    @FXML
    private VBox cardVBox;
    @FXML
    private Label rentalPrice;

    // Helper method to resolve file paths
    private File resolveImageFile(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        try {
            // Try different path resolution strategies
            File file;

            // 1. Direct file path
            file = new File(imagePath);
            if (file.exists()) {
                return file;
            }

            // 2. URL-encoded path
            try {
                URI uri = new URI(imagePath);
                file = new File(uri);
                if (file.exists()) {
                    return file;
                }
            } catch (URISyntaxException e) {
                // Ignore if not a valid URI
            }

            // 3. Relative to project root
            file = new File(System.getProperty("user.dir"), imagePath);
            if (file.exists()) {
                return file;
            }

            // 4. Relative to src/main/resources
            URL resourceUrl = getClass().getClassLoader().getResource(imagePath);
            if (resourceUrl != null) {
                file = new File(resourceUrl.getFile());
                if (file.exists()) {
                    return file;
                }
            }

            // 5. Relative to current project structure
            file = new File("src/main/resources/" + imagePath);
            if (file.exists()) {
                return file;
            }

            System.err.println("Could not resolve image path: " + imagePath);
            return null;

        } catch (Exception e) {
            System.err.println("Error resolving image path: " + e.getMessage());
            return null;
        }
    }

    public void setProfilePhoto(String photoPath) {
        if (photo == null) {
            return;
        }

        // Default photo path
        File defaultPhotoFile = new File("src/main/resources/img/defaultPhoto.png");

        try {
            // If no photo path or path is empty, use default photo
            if (photoPath == null || photoPath.isEmpty()) {
                if (defaultPhotoFile.exists()) {
                    Image defaultImage = new Image(defaultPhotoFile.toURI().toString());
                    photo.setFill(new ImagePattern(defaultImage));
                    return;
                }
            }

            // Try to use the provided photo path
            File file = new File(photoPath);
            if (!file.exists()) {
                // If provided photo doesn't exist, use default photo
                if (defaultPhotoFile.exists()) {
                    file = defaultPhotoFile;
                } else {
                    return;
                }
            }

            Image image = new Image(file.toURI().toString());
            
            // Check if image is loaded successfully
            if (image.isError()) {
                // If image loading fails, use default photo
                if (defaultPhotoFile.exists()) {
                    image = new Image(defaultPhotoFile.toURI().toString());
                } else {
                    return;
                }
            }

            photo.setFill(new ImagePattern(image));
        } catch (Exception e) {
            // If any exception occurs, try to use default photo
            if (defaultPhotoFile.exists()) {
                Image defaultImage = new Image(defaultPhotoFile.toURI().toString());
                photo.setFill(new ImagePattern(defaultImage));
            }
        }
    }

    public void setImageContainer(String imagePath) {
        if (imageContainer == null) {
            System.err.println("ERROR: Image container Rectangle is null!");
            return;
        }

        if (imagePath == null || imagePath.isEmpty()) {
            System.err.println("ERROR: Product image path is null or empty!");
            return;
        }

        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                System.err.println("ERROR: Product image file does not exist: " + imagePath);
                
                // Fallback to default image
                file = new File("src/main/resources/img/defaultImage.png");
                if (!file.exists()) {
                    System.err.println("ERROR: Default image not found!");
                    return;
                }
            }

            Image image = new Image(file.toURI().toString());
            
            // Check if image is loaded successfully
            if (image.isError()) {
                System.err.println("ERROR: Failed to load product image: " + imagePath);
                return;
            }

            imageContainer.setFill(new ImagePattern(image));
            System.out.println("DEBUG: Successfully set product image: " + imagePath);
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR setting product image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setName(String userName) {
        if (name != null) {
            name.setText(userName != null ? userName : "Unknown");
        }
    }

    public void setType(String clothType) {
        if (type != null) {
            type.setText(clothType != null ? clothType : "Unknown Type");
        }
    }

    public void setSize(String clothSize) {
        if (size != null) {
            size.setText(clothSize != null ? clothSize : "Unknown Size");
        }
    }

    public void setColor(String clothColor) {
        if (color != null) {
            color.setText(clothColor != null ? clothColor : "Unknown Color");
        }
    }

    public void setRentalPrice(double price) {
        if (rentalPrice != null) {
            rentalPrice.setText(String.format("$%.2f", price));
        }
    }
}
