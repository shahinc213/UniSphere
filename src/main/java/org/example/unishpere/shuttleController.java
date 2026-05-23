package org.example.unishpere;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class shuttleController {
    @FXML
    private Circle profile;
    @FXML
    private AnchorPane groupChat;
    @FXML
    private Button plusButton;
    @FXML
    private Button minusButton;
    @FXML
    private Pane leftPane;
    @FXML
    private Pane rightPane;
    @FXML
    private Label availableSeatsLabel;
    @FXML
    private Label selectedSeatsLabel;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendButton;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox chatContainer;

    private List<Label> allSeats = new ArrayList<>();
    private Label currentlySelectedSeat = null;
    private static final int TOTAL_SEATS = 28;
    private static final String AVAILABLE_COLOR = "#69A2BD";
    private static final String SELECTED_COLOR = "Orange";
    private Timeline updateTimeline;
    private int lastMessageId = 0;

    @FXML
    public void initialize() {
        initializeSeats();
        initializeDatabase();
        updateSeatsDisplay();
        loadProfilePhoto();
        setupGroupChat();
        setupAutoRefresh();
    }

    private void setupGroupChat() {
        try {
            // Create chat table if not exists
            try (Connection connection = dbConnect.getconnection()) {
                String createTableQuery = """
                    CREATE TABLE IF NOT EXISTS shuttle_group_chat (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_email VARCHAR(100),
                        message TEXT,
                        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """;
                connection.createStatement().execute(createTableQuery);
            }

            // Load existing messages
            loadMessages();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT * FROM shuttle_group_chat ORDER BY id ASC";
            ResultSet rs = connection.createStatement().executeQuery(query);
            
            chatContainer.getChildren().clear();
            while (rs.next()) {
                String userEmail = rs.getString("user_email");
                String message = rs.getString("message");
                String userName = getUserName(userEmail);
                addMessageToUI(userEmail, userName, message);
                lastMessageId = rs.getInt("id");
            }
            
            scrollToBottom();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) return;

        try (Connection connection = dbConnect.getconnection()) {
            String userEmail = Session.getLoggedInUser();
            String userName = getUserName(userEmail);

            String insertQuery = "INSERT INTO shuttle_group_chat (user_email, message) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, userEmail);
            pstmt.setString(2, message);
            pstmt.executeUpdate();

            // Get the generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                lastMessageId = rs.getInt(1);
            }

            addMessageToUI(userEmail, userName, message);
            messageInput.clear();
            scrollToBottom();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getUserName(String email) {
        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT first_name, last_name FROM users WHERE email = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                return firstName + " " + lastName;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return email;
    }

    private void setupAutoRefresh() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            checkForUpdates();
            updateSeatsDisplay();
            checkForNewMessages();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void checkForNewMessages() {
        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT * FROM shuttle_group_chat WHERE id > ? ORDER BY id ASC";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, lastMessageId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String userEmail = rs.getString("user_email");
                String message = rs.getString("message");
                int currentId = rs.getInt("id");
                
                // Only process if it's a new message
                if (currentId > lastMessageId) {
                    String userName = getUserName(userEmail);
                    Platform.runLater(() -> {
                        addMessageToUI(userEmail, userName, message);
                        scrollToBottom();
                    });
                    lastMessageId = currentId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    private void addMessageToUI(String userEmail, String userName, String message) {
        HBox messageBox = new HBox(10);
        messageBox.setMaxWidth(700);
        
        Circle userPhoto = new Circle(20);
        try {
            String photoPath = null;
            try (Connection connection = dbConnect.getconnection()) {
                String query = "SELECT profile_photo FROM users WHERE email = ?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, userEmail);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    photoPath = rs.getString("profile_photo");
                }
            }

            if (photoPath != null && !photoPath.isEmpty()) {
                File file = new File(photoPath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    userPhoto.setFill(new ImagePattern(image));
                } else {
                    // Create gradient fill for default avatar
                    Stop[] stops = new Stop[] {
                        new Stop(0, Color.web("#FF6B6B")),
                        new Stop(1, Color.web("#4ECDC4"))
                    };
                    LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
                    userPhoto.setFill(gradient);
                }
            } else {
                // Create gradient fill for default avatar
                Stop[] stops = new Stop[] {
                    new Stop(0, Color.web("#FF6B6B")),
                    new Stop(1, Color.web("#4ECDC4"))
                };
                LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
                userPhoto.setFill(gradient);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback gradient
            Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#FF6B6B")),
                new Stop(1, Color.web("#4ECDC4"))
            };
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
            userPhoto.setFill(gradient);
        }
        userPhoto.setStroke(Color.WHITE);
        userPhoto.setStrokeWidth(2);
        userPhoto.setEffect(new javafx.scene.effect.DropShadow(5, Color.rgb(0, 0, 0, 0.1)));

        VBox messageContent = new VBox(5);
        Label nameLabel = new Label(userName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 14px;");
        
        // Add timestamp
        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666;");
        
        messageContent.getChildren().addAll(nameLabel, messageLabel, timeLabel);

        boolean isCurrentUser = Session.getLoggedInUser().equals(userEmail);
        if (isCurrentUser) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageBox.getChildren().addAll(messageContent, userPhoto);
            
            // Gradient background for sent messages
            Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#6C63FF")),  // Purple
                new Stop(1, Color.web("#4834DF"))   // Deep Purple
            };
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
            
            Region background = new Region();
            background.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gradient, new CornerRadii(20), Insets.EMPTY)));
            
            messageContent.setBackground(new javafx.scene.layout.Background(new BackgroundFill(gradient, new CornerRadii(20), Insets.EMPTY)));
            messageContent.setPadding(new Insets(10, 15, 10, 15));
            messageContent.setEffect(new javafx.scene.effect.DropShadow(10, Color.rgb(0, 0, 0, 0.2)));
            
            nameLabel.setStyle(nameLabel.getStyle() + "; -fx-text-fill: white;");
            messageLabel.setStyle(messageLabel.getStyle() + "; -fx-text-fill: white;");
            timeLabel.setStyle(timeLabel.getStyle() + "; -fx-text-fill: rgba(255,255,255,0.8);");
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageBox.getChildren().addAll(userPhoto, messageContent);
            
            // Gradient background for received messages
            Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#FF6B6B")),  // Coral
                new Stop(1, Color.web("#EE5253"))   // Red
            };
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
            
            messageContent.setBackground(new javafx.scene.layout.Background(new BackgroundFill(Color.WHITE, new CornerRadii(20), Insets.EMPTY)));
            messageContent.setPadding(new Insets(10, 15, 10, 15));
            messageContent.setEffect(new javafx.scene.effect.DropShadow(10, Color.rgb(0, 0, 0, 0.1)));
            
            nameLabel.setStyle(nameLabel.getStyle() + "; -fx-text-fill: #2C2C2C;");
            messageLabel.setStyle(messageLabel.getStyle() + "; -fx-text-fill: #2C2C2C;");
        }

        messageBox.setPadding(new Insets(5, 10, 5, 10));
        
        // Add hover effect
        messageContent.setOnMouseEntered(e -> {
            messageContent.setEffect(new javafx.scene.effect.DropShadow(15, Color.rgb(0, 0, 0, 0.2)));
        });
        messageContent.setOnMouseExited(e -> {
            messageContent.setEffect(new javafx.scene.effect.DropShadow(10, Color.rgb(0, 0, 0, isCurrentUser ? 0.2 : 0.1)));
        });

        Platform.runLater(() -> {
            chatContainer.getChildren().add(messageBox);
            scrollToBottom();
        });
    }

    private void checkForUpdates() {
        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT s.seat_number, s.user_email FROM seat_selections s";
            ResultSet rs = connection.createStatement().executeQuery(query);

            // Reset all seats to available
            for (Label seat : allSeats) {
                seat.setStyle("-fx-background-color: " + AVAILABLE_COLOR + "; -fx-background-radius: 5;");
            }
            currentlySelectedSeat = null;

            // Update seats based on database
            while (rs.next()) {
                String seatNumber = rs.getString("seat_number");
                String userEmail = rs.getString("user_email");

                // Find and update the seat
                for (Label seat : allSeats) {
                    if (seat.getText().equals(seatNumber)) {
                        seat.setStyle("-fx-background-color: " + SELECTED_COLOR + "; -fx-background-radius: 5;");
                        if (Session.getLoggedInUser().equals(userEmail)) {
                            currentlySelectedSeat = seat;
                        }
                        break;
                    }
                }
            }

            // Update the counts
            updateSeatsDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeSeats() {
        getAllSeats(leftPane);
        getAllSeats(rightPane);
    }

    private void getAllSeats(Pane pane) {
        if (pane != null) {
            pane.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    allSeats.add((Label) node);
                }
            });
        }
    }

    private void initializeDatabase() {
        try (Connection connection = dbConnect.getconnection()) {
            // Create seat_selections table if it doesn't exist
            String createTableQuery = """
                CREATE TABLE IF NOT EXISTS seat_selections (
                    seat_number VARCHAR(5),
                    user_email VARCHAR(100),
                    PRIMARY KEY (seat_number)
                )
            """;
            connection.createStatement().execute(createTableQuery);

            // Check if shuttle table data exists
            String checkQuery = "SELECT COUNT(*) FROM shuttle";
            ResultSet rs = connection.createStatement().executeQuery(checkQuery);
            rs.next();
            int count = rs.getInt(1);

            if (count == 0) {
                // Initialize the table with default values
                String insertQuery = "INSERT INTO shuttle (total_seat, available, selected) VALUES (?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(insertQuery);
                pstmt.setInt(1, TOTAL_SEATS);
                pstmt.setInt(2, TOTAL_SEATS);
                pstmt.setInt(3, 0);
                pstmt.executeUpdate();
            } else {
                // Reset the values
                String updateQuery = "UPDATE shuttle SET available = ?, selected = ?";
                PreparedStatement pstmt = connection.prepareStatement(updateQuery);
                pstmt.setInt(1, TOTAL_SEATS);
                pstmt.setInt(2, 0);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePlusButton(ActionEvent event) {
        if (currentlySelectedSeat != null) {
            System.out.println("Please deselect current seat before selecting another one");
            return;
        }

        try (Connection connection = dbConnect.getconnection()) {
            // Find first available seat
            for (Label seat : allSeats) {
                if (seat.getStyle().contains(AVAILABLE_COLOR)) {
                    // Try to insert the selection
                    String insertQuery = "INSERT INTO seat_selections (seat_number, user_email) VALUES (?, ?)";
                    PreparedStatement pstmt = connection.prepareStatement(insertQuery);
                    pstmt.setString(1, seat.getText());
                    pstmt.setString(2, Session.getLoggedInUser());

                    try {
                        pstmt.executeUpdate();
                        // Selection successful
                        seat.setStyle("-fx-background-color: " + SELECTED_COLOR + "; -fx-background-radius: 5;");
                        currentlySelectedSeat = seat;
                        updateDatabase(true);
                        break;
                    } catch (SQLException e) {
                        // Seat was taken by someone else
                        System.out.println("This seat was just taken by another user. Please try another seat.");
                        continue;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleMinusButton(ActionEvent event) {
        if (currentlySelectedSeat != null) {
            try (Connection connection = dbConnect.getconnection()) {
                // Remove the selection
                String deleteQuery = "DELETE FROM seat_selections WHERE seat_number = ? AND user_email = ?";
                PreparedStatement pstmt = connection.prepareStatement(deleteQuery);
                pstmt.setString(1, currentlySelectedSeat.getText());
                pstmt.setString(2, Session.getLoggedInUser());
                int affected = pstmt.executeUpdate();

                if (affected > 0) {
                    // Successfully removed
                    currentlySelectedSeat.setStyle("-fx-background-color: " + AVAILABLE_COLOR + "; -fx-background-radius: 5;");
                    currentlySelectedSeat = null;
                    updateDatabase(false);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateDatabase(boolean isSelecting) {
        try (Connection connection = dbConnect.getconnection()) {
            String updateQuery = "UPDATE shuttle SET available = available " + (isSelecting ? "- 1" : "+ 1") +
                    ", selected = selected " + (isSelecting ? "+ 1" : "- 1");
            connection.createStatement().executeUpdate(updateQuery);
            updateSeatsDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSeatsDisplay() {
        try (Connection connection = dbConnect.getconnection()) {
            String query = "SELECT available, selected FROM shuttle";
            ResultSet rs = connection.createStatement().executeQuery(query);
            if (rs.next()) {
                int available = rs.getInt("available");
                int selected = rs.getInt("selected");
                availableSeatsLabel.setText("Total Seat: " + available);
                selectedSeatsLabel.setText("Selected Seats: " + selected);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProfilePhoto() {
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
                    Image image = new Image(profilePhotoPath);
                    profile.setFill(new ImagePattern(image));
                } else {
                    File file = new File("src/main/resources/img/defaultPhoto.png");
                    if (file.exists()) {
                        String defaultPhotoPath = file.toURI().toString();
                        Image defaultImage = new Image(defaultPhotoPath);
                        profile.setFill(new ImagePattern(defaultImage));
                    } else {
                        System.out.println("Default photo not found.");
                    }
                }
            } else {
                System.out.println("User not found in the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openProfilePopup() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("profile.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @FXML
    public void goToHomePage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("homePage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void goToMessagePage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("message.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void goToServicePage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("services.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("services.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
