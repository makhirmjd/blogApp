package com.freelancer_jprogrammer.blogapp.model;

/**
 * Created by Muhammad on 12/25/2016.
 */

public class Blog {
    private String title;
    private String description;
    private String image;
    private String userName;

    public Blog()
    {

    }

    public Blog(String title, String description, String image, String userName) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
