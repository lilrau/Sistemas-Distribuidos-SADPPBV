package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private String token;
    private String name;
    private String email;
    private String password;

    public User(String name, String email, String password) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.password = hashPassword(password);
    }

    public User() {

    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void register(String token, String name, String email, String password) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.password = hashPassword(password);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isPasswordCorrect(String password) {
        String hashedPassword = hashPassword(password);
        return this.password.equals(hashedPassword);
    }

}