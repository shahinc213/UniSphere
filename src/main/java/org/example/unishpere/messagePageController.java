package org.example.unishpere;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class messagePageController {
    private static messagePageController instance;

    @FXML
    private Circle profile;
    @FXML
    private VBox userListVBox;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox chatArea;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private TextField searchField;
    @FXML
    private Label chatUserName;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String currentChatUser;
    private dbConnect dbConnect;
    private Set<String> loadedUsers;

    public static messagePageController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        dbConnect = new dbConnect();
        loadedUsers = new HashSet<>();

        // Load profile photo
        loadProfilePhoto();

        // Load chat users
        loadChatUsers();

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });

        // Connect to chat server
        connectToChatServer();

        // Initialize send button
        sendButton.setOnAction(event -> sendMessage());
        messageField.setOnAction(event -> sendMessage());
    }

    private void loadProfilePhoto() {
        String loggedInUserEmail = Session.getLoggedInUser();
        if (loggedInUserEmail == null) {
            System.out.println("No user is logged in.");
            return;
        }

        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT profile_photo FROM users WHERE email = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, loggedInUserEmail);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String photoPath = rs.getString("profile_photo");
                if (photoPath != null && !photoPath.isEmpty()) {
                    Image image = new Image(photoPath);
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
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/img/defaultPhoto.png"));
            profile.setFill(new ImagePattern(defaultImage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onProfileClick() {
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

            // Position the popup
            popupStage.setX(1367);
            popupStage.setY(106);

            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatUsers() {
        String loggedInUser = Session.getLoggedInUser();
        try (Connection connection = dbConnect.getconnection()) {
            // Query to get all users except the logged-in user
            String query = "SELECT email, first_name, last_name, profile_photo " +
                          "FROM users " +
                          "WHERE email != ?";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, loggedInUser);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String email = rs.getString("email");
                if (!loadedUsers.contains(email)) {
                    addUserToList(
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        email,
                        rs.getString("profile_photo")
                    );
                    loadedUsers.add(email);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addUserToList(String name, String email, String photoPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatUserCard.fxml"));
            Parent userCard = loader.load();
            ChatUserCardController controller = loader.getController();
            controller.setUserDetails(name, email, photoPath);
            Platform.runLater(() -> userListVBox.getChildren().add(userCard));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterUsers(String searchText) {
        userListVBox.getChildren().clear();
        loadedUsers.clear();

        if (searchText.isEmpty()) {
            loadChatUsers();
            return;
        }

        String loggedInUser = Session.getLoggedInUser();
        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT DISTINCT u.email, u.first_name, u.last_name, u.profile_photo " +
                          "FROM users u " +
                          "WHERE (u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ?) " +
                          "AND u.email != ?";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            String searchPattern = "%" + searchText + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, loggedInUser);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String email = rs.getString("email");
                if (!loadedUsers.contains(email)) {
                    addUserToList(
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        email,
                        rs.getString("profile_photo")
                    );
                    loadedUsers.add(email);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void openChat(String userEmail) {
        currentChatUser = userEmail;
        
        // Get user's name for display
        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT first_name, last_name FROM users WHERE email = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                Platform.runLater(() -> chatUserName.setText(fullName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Clear previous chat
        Platform.runLater(() -> chatArea.getChildren().clear());

        // Load chat history
        loadChatHistory(userEmail);

        // Enable chat controls
        messageField.setDisable(false);
        sendButton.setDisable(false);
    }

    private void loadChatHistory(String userEmail) {
        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT sender_email, message_text, timestamp " +
                          "FROM chat_history " +
                          "WHERE (sender_email = ? AND receiver_email = ?) " +
                          "OR (sender_email = ? AND receiver_email = ?) " +
                          "ORDER BY timestamp";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, Session.getLoggedInUser());
            stmt.setString(2, userEmail);
            stmt.setString(3, userEmail);
            stmt.setString(4, Session.getLoggedInUser());
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String sender = rs.getString("sender_email");
                String message = rs.getString("message_text");
                boolean isSentByMe = sender.equals(Session.getLoggedInUser());
                Platform.runLater(() -> addMessage(message, isSentByMe));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connectToChatServer() {
        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start message receiving thread
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String[] parts = message.split(":", 2);
                        if (parts.length == 2) {
                            String sender = parts[0];
                            String content = parts[1];
                            
                            // Only show message if it's from the current chat user
                            if (sender.equals(currentChatUser)) {
                                Platform.runLater(() -> addMessage(content, false));
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        if (currentChatUser == null || messageField.getText().trim().isEmpty()) {
            return;
        }

        String message = messageField.getText().trim();
        String fullMessage = Session.getLoggedInUser() + ":" + message;
        out.println(fullMessage);

        // Save message to database
        saveChatMessage(message);

        // Add message to chat area
        addMessage(message, true);
        messageField.clear();
    }

    private void saveChatMessage(String message) {
        try (Connection connection = dbConnect.getconnection()) {
            String query = "INSERT INTO chat_history (sender_email, receiver_email, message_text) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, Session.getLoggedInUser());
            stmt.setString(2, currentChatUser);
            stmt.setString(3, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMessage(String message, boolean isSentByMe) {
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-background-color: " + (isSentByMe ? "#0084ff" : "#e9ecef") + ";" +
                            "-fx-text-fill: " + (isSentByMe ? "white" : "black") + ";" +
                            "-fx-padding: 10;" +
                            "-fx-background-radius: 15;");
        
        messageBox.getChildren().add(messageLabel);
        messageBox.setAlignment(isSentByMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        chatArea.getChildren().add(messageBox);
        chatScrollPane.setVvalue(1.0);
    }

    // Navigation methods
    @FXML
    public void goToHomePage(ActionEvent event) throws IOException {
        closeConnection();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("home.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void goToServicePage(ActionEvent event) throws IOException {
        closeConnection();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigation.pushScene(stage.getScene());
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("services.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
