package org.example.unishpere;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {
    public List<Post> getPostsFromDatabase() {
        List<Post> posts = new ArrayList<>();
        String query = """
            SELECT p.*, u.email, u.first_name, u.last_name, u.profile_photo 
            FROM posts p 
            JOIN users u ON p.user_id = u.id 
            ORDER BY p.created_at DESC
            """;

        try (Connection connection = dbConnect.getconnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String caption = rs.getString("caption");
                String photoUrl = rs.getString("photo_url");
                String userEmail = rs.getString("email");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String userName = firstName + " " + lastName;
                String profilePhoto = rs.getString("profile_photo");

                Post post = new Post(userId, caption, photoUrl, userEmail, userName, profilePhoto);
                posts.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
}
