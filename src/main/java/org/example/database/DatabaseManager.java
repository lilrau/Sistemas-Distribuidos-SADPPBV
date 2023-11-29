package org.example.database;

import org.example.session.TokenDecoder;
import org.example.util.Point;
import org.example.util.Segment;
import org.example.util.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- USER CRUD --------------------------------------------------------------------------------
    public void addUser(User user) {
        Statement statement;
        try {
            statement = this.connection.createStatement();
            statement.executeUpdate("INSERT INTO user (name, email, password, type) VALUES ('" + user.getName() + "', '" + user.getEmail() + "', '" + user.getPassword() + "', '" + user.getType() + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(User user) {
        Statement statement;
        try {
            statement = this.connection.createStatement();
            statement.executeUpdate("UPDATE user SET name = '" + user.getName() + "', email = '" + user.getEmail() + "', password = '" + user.getPassword() + "', type = '" + user.getType() + "' WHERE userID = '" + user.getID() + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteUser(int userID) {
        Statement statement;
        try {
            statement = this.connection.createStatement();
            statement.executeUpdate("DELETE FROM user WHERE userID = '" + userID + "'");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUserByID(long userID) throws SQLException {
        Statement statement;
        ResultSet resultSet = null;
        statement = this.connection.createStatement();
        resultSet = statement.executeQuery("SELECT * FROM user WHERE userID = '" + userID + "'");
        if (resultSet == null || !resultSet.isBeforeFirst()) {
            return null;
        }
        return getUserFromResultSet(resultSet);
    }

    public User getUserByToken(String token) throws SQLException {
        return getUserByID(TokenDecoder.getUserIdFromToken(token));
    }

    public User getUserLogin(String email, String password) throws SQLException {
        Statement statement;
        ResultSet resultSet = null;
        statement = this.connection.createStatement();
        resultSet = statement.executeQuery("SELECT * FROM user WHERE email = '" + email + "' AND password = '" + password + "'");
        if (resultSet == null || !resultSet.isBeforeFirst()) {
            return null;
        }
        return getUserFromResultSet(resultSet);
    }

    public User getUserFromResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return null;
        }
        User user = null;
        try {
            user = new User(resultSet.getString("name"), resultSet.getString("email"), resultSet.getString("password"), resultSet.getString("type"), resultSet.getInt("userID"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User getUserByEmail(String email) throws SQLException {
        Statement statement;
        ResultSet resultSet = null;
        statement = this.connection.createStatement();
        resultSet = statement.executeQuery("SELECT * FROM user WHERE email = '" + email + "'");
        if (resultSet == null || !resultSet.isBeforeFirst()) {
            return null;
        }
        return getUserFromResultSet(resultSet);
    }

    public long getUserIDByEmail(String email) throws SQLException {
        Statement statement;
        ResultSet resultSet = null;
        statement = this.connection.createStatement();
        resultSet = statement.executeQuery("SELECT userID FROM user WHERE email = '" + email + "'");
        if (resultSet == null || !resultSet.isBeforeFirst()) {
            return -1;
        }
        return resultSet.getLong("userID");
    }

    public String getTypeByEmail(String email) throws SQLException {
        Statement statement;
        ResultSet resultSet = null;
        statement = this.connection.createStatement();
        resultSet = statement.executeQuery("SELECT type FROM user WHERE email = '" + email + "'");
        if (resultSet == null || !resultSet.isBeforeFirst()) {
            return null;
        }
        return resultSet.getString("type");
    }

    public boolean isAdmByEmail(String email) throws SQLException {
        String userType = getTypeByEmail(email);
        return "admin".equalsIgnoreCase(userType);
    }

    public Map<String, Object> getUserDataById(int userId) throws SQLException {
        Statement statement;
        ResultSet resultSet = null;
        statement = this.connection.createStatement();
        resultSet = statement.executeQuery("SELECT userID, name, type, email FROM user WHERE userID = '" + userId + "'");

        if (resultSet == null || !resultSet.isBeforeFirst()) {
            return null;
        }

        resultSet.next();

        // Retrieve user data directly from the database
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", resultSet.getInt("userID"));
        userData.put("name", resultSet.getString("name"));
        userData.put("type", resultSet.getString("type"));
        userData.put("email", resultSet.getString("email"));

        return userData;
    }
    
    // ---------------- POINT CRUD -------------------------------------------------------------------------------
    public boolean addPoint(String pointName, String pointObs) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = this.connection.prepareStatement("INSERT INTO point (name, obs) VALUES (?, ?)");
            preparedStatement.setString(1, pointName);
            preparedStatement.setString(2, pointObs);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Point getPointById(int pointId) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            preparedStatement = this.connection.prepareStatement("SELECT * FROM point WHERE pointID = ?");
            preparedStatement.setInt(1, pointId);
    
            resultSet = preparedStatement.executeQuery();
    
            if (resultSet.next()) {
                int id = resultSet.getInt("pointID");
                String name = resultSet.getString("name");
                String obs = resultSet.getString("obs");
    
                // Criar e retornar um objeto Point
                return new Point(id, name, obs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Fechar o preparedStatement e resultSet no bloco finally para garantir que sejam fechados, independentemente do resultado
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
    
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        // Retornar null se o ponto não for encontrado
        return null;
    }

    public boolean updatePoint(int pointId, String newName, String newObs) {
        PreparedStatement preparedStatement;
    
        try {
            preparedStatement = this.connection.prepareStatement("UPDATE point SET name = ?, obs = ? WHERE pointID = ?");
            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, newObs);
            preparedStatement.setInt(3, pointId);
    
            int rowsUpdated = preparedStatement.executeUpdate();
    
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deletePoint(int pointID) {
        Statement statement;
        try {
            statement = this.connection.createStatement();
            statement.executeUpdate("DELETE FROM point WHERE pointID = '" + pointID + "'");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- SEGMENT CRUD -----------------------------------------------------------------------------
    public boolean addSegment(Segment segment) {
        PreparedStatement preparedStatement;

        try {
            preparedStatement = this.connection.prepareStatement("INSERT INTO segment (startPointID, endPointID, direction, distance, obs) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, segment.getStartPointId());
            preparedStatement.setInt(2, segment.getEndPointId());
            preparedStatement.setString(3, segment.getDirection());
            preparedStatement.setInt(4, segment.getDistance());
            preparedStatement.setString(5, segment.getObs());

            int rowsInserted = preparedStatement.executeUpdate();

            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getSegmentList() {
        List<Map<String, Object>> segmentList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    
        try {
            preparedStatement = this.connection.prepareStatement("SELECT * FROM segment");
            resultSet = preparedStatement.executeQuery();
    
            while (resultSet.next()) {
                int id = resultSet.getInt("segmentID");
                int startPointId = resultSet.getInt("startPointID");
                int endPointId = resultSet.getInt("endPointID");
                String direction = resultSet.getString("direction");
                int distance = resultSet.getInt("distance");
                String obs = resultSet.getString("obs");
    
                // Obter informações dos pontos de origem e destino usando o método getPointById
                Point pontoOrigem = getPointById(startPointId);
                Point pontoDestino = getPointById(endPointId);
    
                // Criar o mapa para representar o segmento
                Map<String, Object> segmentInfo = new HashMap<>();
                segmentInfo.put("id", id);
                segmentInfo.put("ponto_origem", pontoOrigem != null ? pontoOrigem.toMap() : null);
                segmentInfo.put("ponto_destino", pontoDestino != null ? pontoDestino.toMap() : null);
                segmentInfo.put("direcao", direction);
                segmentInfo.put("distancia", distance);
                segmentInfo.put("obs", obs);
    
                segmentList.add(segmentInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Fechar o resultSet e preparedStatement no bloco finally para garantir que sejam fechados, independentemente do resultado
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
    
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    
        return segmentList;
    }    
    

    public boolean deleteSegment(int segmentID) {
        Statement statement;
        try {
            statement = this.connection.createStatement();
            statement.executeUpdate("DELETE FROM segment WHERE segmentID = '" + segmentID + "'");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- CONNECTION -------------------------------------------------------------------------------
    public void openConnection() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return this.connection;
    }
}
