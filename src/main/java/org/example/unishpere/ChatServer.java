package org.example.unishpere;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static HashSet<PrintWriter> writers = new HashSet<>();
    private static ServerSocket listener;
    private static boolean isRunning = false;
    private static Thread serverThread;
    private static ChatServer instance;

    private ChatServer() {}

    public static synchronized ChatServer getInstance() {
        if (instance == null) {
            instance = new ChatServer();
        }
        return instance;
    }

    public synchronized boolean isServerRunning() {
        if (isRunning && listener != null && !listener.isClosed()) {
            return true;
        }
        
        // Check if port is in use
        try (Socket socket = new Socket("localhost", PORT)) {
            // If we can connect, then a server is running
            return true;
        } catch (IOException e) {
            // If connection fails, no server is running
            return false;
        }
    }

    public synchronized void start() {
        if (isServerRunning()) {
            System.out.println("Chat Server is already running.");
            return;
        }

        if (serverThread != null && serverThread.isAlive()) {
            System.out.println("Server thread is still alive, stopping it...");
            stop();
        }

        serverThread = new Thread(() -> {
            try {
                System.out.println("Starting Chat Server on port " + PORT);
                listener = new ServerSocket(PORT);
                isRunning = true;

                while (!Thread.currentThread().isInterrupted() && !listener.isClosed()) {
                    try {
                        Socket socket = listener.accept();
                        new Handler(socket).start();
                    } catch (SocketException e) {
                        // Server socket was closed
                        break;
                    }
                }
            } catch (BindException e) {
                System.out.println("Port " + PORT + " is already in use. Server might be already running.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        });

        serverThread.start();
    }

    public synchronized void stop() {
        isRunning = false;
        if (listener != null && !listener.isClosed()) {
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Clear all writers
        writers.clear();
        
        if (serverThread != null) {
            serverThread.interrupt();
            try {
                serverThread.join(1000); // Wait up to 1 second for thread to stop
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (writers) {
                    writers.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    // Parse the message to extract sender information
                    String[] parts = message.split(":", 2);
                    if (parts.length == 2) {
                        String sender = parts[0];
                        String content = parts[1];

                        // Broadcast the message to all clients
                        synchronized (writers) {
                            for (PrintWriter writer : writers) {
                                writer.println(sender + ": " + content);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    synchronized (writers) {
                        writers.remove(out);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
