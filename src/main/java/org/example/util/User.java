package org.example.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    public int id;
    public String name;
    public String email;
    public String password;
    public boolean isAdm;

    // Construtor
    public User(String name, String email, String password, String type) {
        this.name = name;
        this.email = email;
        this.password = hashPasswordMD5(password);
        this.isAdm = "admin".equalsIgnoreCase(type); // Assume que o tipo "admin" indica administrador
    }

    public User(String name, String email, String password, String type, int userID) {
    }

    public User() {
    }

    // Getters e Setters
    public int getId() {
        return id;
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

    public boolean getIsAdm() {
        return isAdm;
    }

    public String hashPassword256(String password) {
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

    public String hashPasswordMD5(String password) {
        return DigestUtils.md5Hex(password).toUpperCase();
    }

    public boolean isPasswordCorrect(String password) {
        String hashedPassword = hashPasswordMD5(password);
        return this.password.equals(hashedPassword);
    }

    public String getType() {
        return isAdm ? "admin" : "user";
    }

    public String getID() {
        return String.valueOf(id);
    }
}