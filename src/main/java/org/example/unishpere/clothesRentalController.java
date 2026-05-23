package org.example.unishpere;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.example.unishpere.dbConnect.connection;

public class clothesRentalController implements Initializable {
    @FXML
    private ScrollPane scrollPane; // Scroll pane to hold the rental cards
    @FXML
    private VBox vBox;
    @FXML
    private Button openPopupButton;
    @FXML
    private Circle profile;
    @FXML
    private Circle writePostPhoto;

    @FXML
    public void goToHomePage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("home.fxml"));
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
    public void goToServicePage(ActionEvent event) throws IOException
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigation.pushScene(stage.getScene());

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("services.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void openPopup(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("clothesRentalForm.fxml"));
            AnchorPane popupContent = fxmlLoader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Add Clothes for Rent");
            Scene popupScene = new Scene(popupContent);
            popupStage.setScene(popupScene);

            Stage parentStage = (Stage) openPopupButton.getScene().getWindow();
            popupStage.show();

            popupStage.setX(parentStage.getX() + (parentStage.getWidth() - popupStage.getWidth()) / 2);
            popupStage.setY(parentStage.getY() + (parentStage.getHeight() - popupStage.getHeight()) / 2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openProfilePopup() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("profile.fxml"));
            AnchorPane popupContent = fxmlLoader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Profile");
            Scene popupScene = new Scene(popupContent);
            popupStage.setScene(popupScene);

            Stage parentStage = (Stage) profile.getScene().getWindow();
            popupStage.show();

            popupStage.setX(1367);
            popupStage.setY(106);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            try {
                populateRentalCards();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        loadProfilePhoto();
        loadWriteCommentProfilePhoto();
    }

    private void populateRentalCards() {
        if (vBox == null || scrollPane == null) {
            return;
        }

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            conn = dbConnect.getconnection();
            if (conn == null) {
                return;
            }

            String query = "SELECT c.*, u.first_name, u.last_name, u.profile_photo " +
                           "FROM ClothingRental c " +
                           "JOIN users u ON c.lessor = u.id";

            statement = conn.prepareStatement(query);
            rs = statement.executeQuery();

            vBox.getChildren().clear();

            HBox currentRow = new HBox(20);
            currentRow.setAlignment(Pos.CENTER);
            int cardsInCurrentRow = 0;

            URL fxmlUrl = getClass().getResource("/org/example/unishpere/clothesRentalCard.fxml");
            
            if (fxmlUrl == null) {
                return;
            }

            while (rs.next()) {
                try {
                    FXMLLoader loader = new FXMLLoader(fxmlUrl);
                    
                    AnchorPane card = loader.load();
                    clothesRentalCardController cardController = loader.getController();

                    if (cardController == null) {
                        continue;
                    }

                    String fullName = rs.getString("first_name") + " " + rs.getString("last_name");

                    cardController.setName(fullName);
                    cardController.setType(rs.getString("type"));
                    cardController.setSize(rs.getString("size"));
                    cardController.setColor(rs.getString("color"));
                    cardController.setRentalPrice(rs.getDouble("rent_price"));

                    String profilePhotoPath = rs.getString("profile_photo");
                    if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
                        String resolvedProfilePath = resolvePath(profilePhotoPath);
                        if (resolvedProfilePath != null) {
                            cardController.setProfilePhoto(resolvedProfilePath);
                        }
                    }

                    String imagePath = rs.getString("image_path");
                    if (imagePath != null && !imagePath.isEmpty()) {
                        String resolvedImagePath = resolvePath(imagePath);
                        if (resolvedImagePath != null) {
                            cardController.setImageContainer(resolvedImagePath);
                        }
                    }

                    if (cardsInCurrentRow == 3) {
                        vBox.getChildren().add(currentRow);
                        currentRow = new HBox(20);
                        currentRow.setAlignment(Pos.CENTER);
                        cardsInCurrentRow = 0;
                    }

                    currentRow.getChildren().add(card);
                    cardsInCurrentRow++;

                } catch (IOException e) {
                    // Silent error handling
                }
            }

            if (cardsInCurrentRow > 0) {
                vBox.getChildren().add(currentRow);
            }

            scrollPane.setContent(vBox);
            scrollPane.setFitToWidth(true);

        } catch (SQLException e) {
            // Silent error handling
        } catch (Exception e) {
            // Silent error handling
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                // Silent error handling
            }
        }
    }

    private String resolvePath(String originalPath) {
        if (originalPath == null || originalPath.isEmpty()) {
            return null;
        }

        try {
            if (originalPath.startsWith("file:")) {
                originalPath = originalPath.substring(5);
            }

            originalPath = URLDecoder.decode(originalPath, StandardCharsets.UTF_8.toString());

            List<String> pathsToTry = new ArrayList<>(Arrays.asList(
                originalPath, 
                System.getProperty("user.dir") + File.separator + originalPath, 
                "src/main/resources/" + originalPath, 
                System.getProperty("user.home") + File.separator + originalPath
            ));

            String currentDir = System.getProperty("user.dir");
            pathsToTry.add(currentDir + File.separator + "uniShpere" + File.separator + originalPath);
            pathsToTry.add(currentDir + File.separator + "target" + File.separator + "classes" + File.separator + originalPath);

            for (String path : pathsToTry) {
                File file = new File(path);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }

            URL resourceUrl = getClass().getClassLoader().getResource(originalPath);
            if (resourceUrl != null) {
                File resourceFile = new File(resourceUrl.getFile());
                if (resourceFile.exists()) {
                    return resourceFile.getAbsolutePath();
                }
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    private void loadProfilePhoto() {
        String loggedInUserEmail = Session.getLoggedInUser();

        if (loggedInUserEmail == null) {
            return;
        }

        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT profile_photo FROM users WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loggedInUserEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String profilePhotoPath = resultSet.getString("profile_photo");
                if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
                    String resolvedProfilePath = resolvePath(profilePhotoPath);
                    if (resolvedProfilePath != null) {
                        Image image = new Image(resolvedProfilePath);
                        profile.setFill(new ImagePattern(image));
                    }
                } else {
                    File file = new File("src/main/resources/img/defaultPhoto.png");
                    if (file.exists()) {
                        String defaultPhotoPath = file.toURI().toString();
                        Image defaultImage = new Image(defaultPhotoPath);
                        profile.setFill(new ImagePattern(defaultImage)); 
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadWriteCommentProfilePhoto() {
        String loggedInUserEmail = Session.getLoggedInUser();

        if (loggedInUserEmail == null) {
            return;
        }

        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT profile_photo FROM users WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loggedInUserEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String profilePhotoPath = resultSet.getString("profile_photo");
                if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
                    String resolvedProfilePath = resolvePath(profilePhotoPath);
                    if (resolvedProfilePath != null) {
                        Image image = new Image(resolvedProfilePath);
                        writePostPhoto.setFill(new ImagePattern(image));
                    }
                } else {
                    File file = new File("src/main/resources/img/defaultPhoto.png");
                    if (file.exists()) {
                        String defaultPhotoPath = file.toURI().toString();
                        Image defaultImage = new Image(defaultPhotoPath);
                        writePostPhoto.setFill(new ImagePattern(defaultImage)); 
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
