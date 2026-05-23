package org.example.unishpere;

public class Session {
    private static String loggedInUserEmail;

    public static void setLoggedInUser(String email) {
        loggedInUserEmail = email;
    }

    public static String getLoggedInUser() {
        return loggedInUserEmail;
    }

    public static void clearSession() {
        loggedInUserEmail = null;
    }
}
