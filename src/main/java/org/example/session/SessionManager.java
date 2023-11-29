package org.example.session;

import java.util.HashMap;
import java.util.Map;

import org.example.util.User;

public class SessionManager {
    private static Map<String, User> sessions = new HashMap<>();

    public static void createSession(String token, User user) {
        sessions.put(token, user);
    }

    public static User getSessionUser(String token) {
        return sessions.get(token);
    }

    public static void removeSession(String token) {
        sessions.remove(token);
    }
}

