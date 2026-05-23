package org.example.unishpere;

public class Post {
    private int userId;
    private String caption;
    private String photoUrl;
    private String userEmail;
    private String userName;
    private String userProfilePhoto;

    public Post(int userId, String caption, String photoUrl, String userEmail, String userName, String userProfilePhoto) {
        this.userId = userId;
        this.caption = caption;
        this.photoUrl = photoUrl;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userProfilePhoto = userProfilePhoto;
    }

    public int getUserId() {
        return userId;
    }

    public String getCaption() {
        return caption;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserProfilePhoto() {
        return userProfilePhoto;
    }
}
