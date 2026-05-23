package org.example.unishpere;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class showTutReqController {
    @FXML
    private Circle profile;
    @FXML
    private FlowPane vBox;  // Changed from VBox to FlowPane

    // Rest of your controller code remains exactly the same
    @FXML
    public void initialize() {
        vBox.setHgap(10); // Set horizontal gap between cards
        vBox.setVgap(10); // Set vertical gap between cards
        loadProfilePhoto();
        loadTutoringRequests();
    }

    private void loadProfilePhoto() {
        String loggedInUserEmail = Session.getLoggedInUser();
        if (loggedInUserEmail == null) {
            System.out.println("No user is logged in.");
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
                    Image image = new Image(profilePhotoPath);
                    profile.setFill(new ImagePattern(image));
                } else {
                    setDefaultProfilePhoto();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setDefaultProfilePhoto();
        }
    }

    private void setDefaultProfilePhoto() {
        File file = new File("src/main/resources/img/defaultPhoto.png");
        if (file.exists()) {
            Image defaultImage = new Image(file.toURI().toString());
            profile.setFill(new ImagePattern(defaultImage));
        }
    }

    private void loadTutoringRequests() {
        String currentUserEmail = Session.getLoggedInUser();
        if (currentUserEmail == null) return;

        try (Connection connection = dbConnect.getconnection()) {
            String query = """
                SELECT pt.*, 
                       req.email as requester_email, req.first_name as requester_first_name, req.last_name as requester_last_name, req.profile_photo as requester_photo,
                       acc.email as accepter_email, acc.first_name as accepter_first_name, acc.last_name as accepter_last_name, acc.profile_photo as accepter_photo
                FROM peertutoring pt
                JOIN users req ON pt.requester_id = req.id
                LEFT JOIN users acc ON pt.accepter_id = acc.id
                WHERE pt.type = 'pending'
                   OR (pt.type = 'accepted' AND (req.email = ? OR acc.email = ?))
                ORDER BY pt.created_time DESC
            """;

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, currentUserEmail);
            statement.setString(2, currentUserEmail);
            ResultSet resultSet = statement.executeQuery();

            vBox.getChildren().clear();

            while (resultSet.next()) {
                String type = resultSet.getString("type");
                String requesterEmail = resultSet.getString("requester_email");
                String accepterEmail = resultSet.getString("accepter_email");

                String fxmlPath;
                if (type.equals("pending")) {
                    fxmlPath = "peerRequestCard.fxml";
                } else if (requesterEmail.equals(currentUserEmail)) {
                    fxmlPath = "acceptedRequestCardonRequesterSide.fxml";
                } else {
                    fxmlPath = "acceptedRequestCardonAccepterSide.fxml";
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Node cardNode = loader.load();

                if (type.equals("pending")) {
                    peerTutReqCardController controller = loader.getController();
                    controller.setRequesterEmail(requesterEmail);
                    controller.setRequestId(resultSet.getInt("id"));
                    controller.setRequesterDetails(
                            resultSet.getString("requester_first_name") + " " + resultSet.getString("requester_last_name"),
                            resultSet.getString("course_name"),
                            resultSet.getString("problem_topic"),
                            resultSet.getString("description")
                    );
                    controller.setProfilePhoto(resultSet.getString("requester_photo"));
                    
                } else if (requesterEmail.equals(currentUserEmail)) {
                    acceptedRequestCardonRequesterSideController controller = loader.getController();
                    controller.setRequestId(resultSet.getInt("id"));
                    controller.setRequesterEmail(requesterEmail);
                    controller.setRequesterDetails(
                            resultSet.getString("requester_first_name") + " " + resultSet.getString("requester_last_name"),
                            resultSet.getString("course_name"),
                            resultSet.getString("problem_topic"),
                            resultSet.getString("description")
                    );
                    controller.setProfilePhoto(resultSet.getString("requester_photo"));
                    controller.setAccepterName(resultSet.getString("accepter_first_name") + " " + resultSet.getString("accepter_last_name"));
                } else {
                    acceptedRequestCardonAccepterSideController controller = loader.getController();
                    controller.setRequestId(resultSet.getInt("id"));
                    controller.setRequesterEmail(requesterEmail);
                    controller.setRequesterDetails(
                            resultSet.getString("requester_first_name") + " " + resultSet.getString("requester_last_name"),
                            resultSet.getString("course_name"),
                            resultSet.getString("problem_topic"),
                            resultSet.getString("description")
                    );
                    controller.setProfilePhoto(resultSet.getString("requester_photo"));
                }

                vBox.getChildren().add(cardNode);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

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
    public void goBack(ActionEvent event) {
        Scene previousScene = Navigation.popScene();
        if (previousScene != null) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(previousScene);
            stage.show();
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
            popupStage.initOwner(parentStage);

            popupStage.setX(1367);
            popupStage.setY(106);
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}