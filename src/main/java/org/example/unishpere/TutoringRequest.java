package org.example.unishpere;

public class TutoringRequest {
    private String requesterName;
    private String courseName;
    private String problemTopic;
    private String description;

    // Constructor
    public TutoringRequest(String requesterName, String courseName, String problemTopic, String description) {
        this.requesterName = requesterName;
        this.courseName = courseName;
        this.problemTopic = problemTopic;
        this.description = description;
    }

    // Getters and setters
    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getProblemTopic() {
        return problemTopic;
    }

    public void setProblemTopic(String problemTopic) {
        this.problemTopic = problemTopic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

