package org.example.unishpere;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import java.io.File;

public class clothsRentalCardController {
    @FXML
    private Rectangle imageContainer;
    @FXML
    private TextField name;
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

    public void setData(String name, String type, String size, String color, String gender, String rentalPrice, String photoPath) {
        this.name.setText(name);
        this.type.setText(type);
        this.size.setText(size);
        this.color.setText(color);
        this.gender.setText(gender);
        this.rentalPrice.setText(rentalPrice);

        if (photoPath != null && !photoPath.isEmpty()) {
            File file = new File(photoPath);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                imageContainer.setFill(new ImagePattern(image));
            }
        }
    }
}
