package org.example.unishpere;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.io.*;
import java.net.Socket;
import java.net.ConnectException;

public class chatCornerController {
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox messageArea;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String username;
    private String recipientName;
    private static boolean serverStarted = false;
    private dbConnect dbConnect;

    public void initialize() {
        dbConnect = new dbConnect();
        messageArea.setSpacing(10);
        messageArea.setStyle("-fx-padding: 10px;");
        setupMessageField();
        setupSendButton();
    }

    public void setUsernames(String senderEmail, String receiverEmail) {
        this.username = senderEmail;  // Current user's email
        this.recipientName = receiverEmail;  // Recipient's email
        
        Platform.runLater(() -> {
            loadChatHistory();
            startChatServer();
            connectToServer();
        });
    }

    private void loadChatHistory() {
        try (Connection connection = dbConnect.getconnection()) {
            Platform.runLater(() -> messageArea.getChildren().clear());
            
            String query = "SELECT sender_email, receiver_email, message_text, timestamp " +
                          "FROM chat_history " +
                          "WHERE (sender_email = ? AND receiver_email = ?) " +
                          "OR (sender_email = ? AND receiver_email = ?) " +
                          "ORDER BY timestamp";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, recipientName);
            stmt.setString(3, recipientName);
            stmt.setString(4, username);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String sender = rs.getString("sender_email");
                String message = rs.getString("message_text");
                java.sql.Timestamp timestamp = rs.getTimestamp("timestamp");
                
                boolean isSentByMe = sender.equals(username);
                final String finalMessage = message;
                
                Platform.runLater(() -> {
                    addMessageToUI(finalMessage, isSentByMe, timestamp);
                });
            }
            
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
            
        } catch (SQLException e) {
            Platform.runLater(() -> {
                addErrorMessageToUI("Failed to load chat history");
            });
        }
    }

    private void addMessageToUI(String message, boolean isSentByMe, Timestamp timestamp) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        VBox messageContainer = new VBox(5); // 5px spacing between elements
        messageContainer.setMaxWidth(300);
        
        // Create message bubble
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        
        Label messageLabel = new Label(message.trim());
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);
        
        String bubbleColor = isSentByMe ? "#DCF8C6" : "#E8E8E8";
        messageLabel.setStyle(String.format(
            "-fx-background-color: %s;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 10px;" +
            "-fx-font-size: 14px;",
            bubbleColor
        ));

        messageBox.getChildren().add(messageLabel);
        messageBox.setAlignment(isSentByMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // Create time label
        Label timeLabel = new Label(formatTimestamp(timestamp));
        timeLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        timeLabel.setAlignment(isSentByMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        // Add components to container
        messageContainer.getChildren().addAll(messageBox, timeLabel);
        messageContainer.setAlignment(isSentByMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        // Add to message area
        messageArea.getChildren().add(messageContainer);
        scrollPane.setVvalue(1.0);
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new java.text.SimpleDateFormat("MMM dd, HH:mm").format(timestamp);
    }

    private void saveChatMessage(String message) {
        try (Connection connection = dbConnect.getconnection()) {
            String query = "INSERT INTO chat_history (sender_email, receiver_email, message_text) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            
            stmt.setString(1, username);
            stmt.setString(2, recipientName);
            stmt.setString(3, message);
            
            int result = stmt.executeUpdate();
            if (result <= 0) {
                Platform.runLater(() -> {
                    addErrorMessageToUI("Failed to save message");
                });
            }
        } catch (SQLException e) {
            Platform.runLater(() -> {
                addErrorMessageToUI("Failed to save message");
            });
        }
    }

    private void addErrorMessageToUI(String errorMessage) {
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        messageBox.setMaxWidth(300);
        messageBox.setAlignment(Pos.CENTER);

        Label messageLabel = new Label(errorMessage);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-background-color: #ffebee;" +
                            "-fx-text-fill: #c62828;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-padding: 10px;" +
                            "-fx-font-size: 12px;");

        messageBox.getChildren().add(messageLabel);
        messageArea.getChildren().add(messageBox);
    }

    private void setupMessageField() {
        messageField.setOnAction(event -> sendMessage());
    }

    private void setupSendButton() {
        sendButton.setOnAction(event -> sendMessage());
    }

    @FXML
    public void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            saveChatMessage(message);
            
            addMessageToUI(message, true, new Timestamp(System.currentTimeMillis()));
            
            if (out != null) {
                String fullMessage = username + ":" + message;
                out.println(fullMessage);
            }
            
            messageField.clear();
            
            scrollPane.setVvalue(1.0);
        }
    }

    private void startChatServer() {
        ChatServer server = ChatServer.getInstance();
        if (!server.isServerRunning()) {
            new Thread(() -> {
                server.start();
            }).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectToServer() {
        int maxRetries = 3;
        int retryCount = 0;
        boolean connected = false;

        while (retryCount < maxRetries && !connected) {
            try {
                socket = new Socket("localhost", 5000);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected = true;

                new Thread(() -> {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            String[] parts = message.split(":", 2);
                            if (parts.length == 2) {
                                String sender = parts[0];
                                String content = parts[1];
                                
                                Platform.runLater(() -> {
                                    if (sender.equals(recipientName)) {
                                        addMessageToUI(content, false, new Timestamp(System.currentTimeMillis()));
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            addMessageToUI("Lost connection to chat server", false, new Timestamp(System.currentTimeMillis()));
                            messageField.setDisable(true);
                            sendButton.setDisable(true);
                        });
                    }
                }).start();

            } catch (ConnectException e) {
                retryCount++;
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                } else {
                    Platform.runLater(() -> {
                        addMessageToUI("Could not connect to chat server", false, new Timestamp(System.currentTimeMillis()));
                        messageField.setDisable(true);
                        sendButton.setDisable(true);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    addMessageToUI("Error connecting to chat server", false, new Timestamp(System.currentTimeMillis()));
                    messageField.setDisable(true);
                    sendButton.setDisable(true);
                });
                break;
            }
        }
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}