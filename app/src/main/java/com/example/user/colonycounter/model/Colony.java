package com.example.user.colonycounter.model;

/**
 * Created by user on 24-Jun-19.
 */

public class Colony {
    private int id;
    private String number;
    private byte[] image;

    public Colony(String number, byte[] image, int id) {
        this.id = id;
        this.number = number;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}