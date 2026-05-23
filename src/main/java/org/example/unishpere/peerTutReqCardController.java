package org.example.unishpere;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class peerTutReqCardController implements Initializable {

    @FXML
    private Circle photo;
    @FXML
    private Label name;
    @FXML
    private Label course;
    @FXML
    private Label topic;
    @FXML
    private Label description;
    @FXML
    private AnchorPane cardPane;
    @FXML
    private Button acceptButton;
    @FXML
    private Button messageButton;

    private String requesterEmail;  // Actual email from database
    private String requesterName;   // Display name
    private int requestId;
    private dbConnect dbConnect;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnect = new dbConnect();
    }

    public void setRequesterEmail(String email) {
        this.requesterEmail = email;
        
        // Show accept button only if current user is not the requester
        if (acceptButton != null && !email.equals(Session.getLoggedInUser())) {
            acceptButton.setVisible(true);
            acceptButton.setManaged(true);
        }
    }

    public void setRequesterDetails(String requesterName, String courseName, String problemTopic, String descriptionText) {
        this.requesterName = requesterName;
        name.setText(requesterName);
        course.setText(courseName);
        topic.setText(problemTopic);
        description.setText(descriptionText);
    }

    public void setRequestId(int id) {
        this.requestId = id;
    }

    void setProfilePhoto(String profilePhotoPath) {
        if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
            try {
                Image image = new Image(profilePhotoPath, false);
                photo.setFill(new ImagePattern(image));
            } catch (IllegalArgumentException e) {
                setDefaultProfilePhoto();
            }
        } else {
            setDefaultProfilePhoto();
        }
    }

    private void setDefaultProfilePhoto() {
        File file = new File("src/main/resources/img/defaultPhoto.png");
        if (file.exists()) {
            Image defaultImage = new Image(file.toURI().toString());
            photo.setFill(new ImagePattern(defaultImage));
        } else {
            System.out.println("Default photo not found.");
        }
    }

    private String getUsernameFromEmail(String email) {
        if (dbConnect == null) {
            dbConnect = new dbConnect();
        }

        String username = null;
        String query = "SELECT first_name, last_name FROM users WHERE email = ?";

        try (Connection connection = dbConnect.getconnection()) {
            if (connection == null) {
                return email.split("@")[0];
            }

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                username = firstName + " " + lastName;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return email.split("@")[0];
        }

        return username != null ? username : email.split("@")[0];
    }

    private String getEmailFromName(String fullName) {
        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT email FROM users WHERE CONCAT(first_name, ' ', last_name) = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, fullName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error getting email from name: " + e.getMessage());
        }
        return null;
    }

    @FXML
    public void handleMessageButton() {
        try {
            // Double check we have the correct email
            if (requesterEmail == null || requesterEmail.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not start chat");
                alert.setContentText("Requester email is not set");
                alert.showAndWait();
                return;
            }

            // Verify the email exists in the database
            try (Connection connection = dbConnect.getconnection()) {
                String query = "SELECT email, first_name, last_name FROM users WHERE email = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, requesterEmail);
                ResultSet rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could not start chat");
                    alert.setContentText("Could not find user with email: " + requesterEmail);
                    alert.showAndWait();
                    return;
                }
                
                // Get the current user's email
                String currentUserEmail = Session.getLoggedInUser();
                if (currentUserEmail == null || currentUserEmail.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could not start chat");
                    alert.setContentText("No user is logged in");
                    alert.showAndWait();
                    return;
                }
                
                // Load and setup the chat window
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("chatCorner.fxml"));
                Parent root = fxmlLoader.load();
                chatCornerController chatController = fxmlLoader.getController();
                
                // Pass the verified email addresses
                chatController.setUsernames(currentUserEmail, requesterEmail);
                
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Chat with " + requesterName);
                stage.setX(1421);
                stage.setY(395);
                stage.setOnCloseRequest(event -> chatController.closeConnection());
                stage.show();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database Error");
            alert.setContentText("Could not verify user details: " + e.getMessage());
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open chat");
            alert.setContentText("Error opening chat window: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleAcceptButton() {
        if (requesterEmail == null) {
            return;
        }

        try (Connection connection = dbConnect.getconnection()) {
            String accepterEmail = Session.getLoggedInUser();
            if (accepterEmail == null) {
                return;
            }

            // Update the request status and set accepter_id
            String updateQuery = "UPDATE peertutoring SET type = 'accepted', accepter_id = (SELECT id FROM users WHERE email = ?) WHERE id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, accepterEmail);
            updateStmt.setInt(2, requestId);
            updateStmt.executeUpdate();

            // Replace the current card with appropriate accepted request card
            replaceWithAcceptedCard(accepterEmail);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void replaceWithAcceptedCard(String accepterEmail) throws IOException, SQLException {
        if (requesterEmail == null) {
            return;
        }

        if (cardPane == null) {
            return;
        }

        try {
            // Load the accepted request card FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("acceptedRequestCardonAccepterSide.fxml"));
            Node acceptedCard = loader.load();

            acceptedRequestCardonAccepterSideController controller = loader.getController();
            // Set all details
            controller.setRequestId(requestId);
            controller.setRequesterEmail(requesterEmail);
            controller.setRequesterDetails(requesterName, course.getText(), topic.getText(), description.getText());
            if (photo.getFill() instanceof ImagePattern) {
                String photoUrl = ((ImagePattern)photo.getFill()).getImage().getUrl();
                controller.setProfilePhoto(photoUrl);
            }

            // Get the parent container and replace the current card
            Node parent = cardPane.getParent();
            if (parent == null) {
                return;
            }

            // Handle different types of parent containers
            if (parent instanceof FlowPane) {
                FlowPane parentFlow = (FlowPane) parent;
                int index = parentFlow.getChildren().indexOf(cardPane);
                if (index >= 0) {
                    parentFlow.getChildren().set(index, acceptedCard);
                }
            } else if (parent instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) parent;
                if (scrollPane.getContent() instanceof FlowPane) {
                    FlowPane flowPane = (FlowPane) scrollPane.getContent();
                    int index = flowPane.getChildren().indexOf(cardPane);
                    if (index >= 0) {
                        flowPane.getChildren().set(index, acceptedCard);
                    }
                }
            } else if (parent instanceof HBox) {
                HBox parentHBox = (HBox) parent;
                int index = parentHBox.getChildren().indexOf(cardPane);
                if (index >= 0) {
                    parentHBox.getChildren().set(index, acceptedCard);
                }
            } else if (parent instanceof VBox) {
                VBox parentVBox = (VBox) parent;
                int index = parentVBox.getChildren().indexOf(cardPane);
                if (index >= 0) {
                    parentVBox.getChildren().set(index, acceptedCard);
                }
            } else {
                // Try to find the FlowPane parent recursively
                Node currentParent = parent;
                while (currentParent != null) {
                    if (currentParent instanceof FlowPane) {
                        FlowPane flowPane = (FlowPane) currentParent;
                        int index = flowPane.getChildren().indexOf(cardPane);
                        if (index >= 0) {
                            flowPane.getChildren().set(index, acceptedCard);
                            return;
                        }
                    }
                    currentParent = currentParent.getParent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
