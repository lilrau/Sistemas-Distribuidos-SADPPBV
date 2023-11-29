package org.example.server;

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.database.DatabaseManager;
import org.example.session.SessionManager;
import org.example.session.TokenGenerator;
import org.example.util.Point;
import org.example.util.Segment;
import org.example.util.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class Server {

    private static final String secretKey = "AoT3QFTTEkj16rCby/TPVBWvfSQHL3GeEz3zVwEd6LDrQDT97sgDY8HJyxgnH79jupBWFOQ1+7fRPBLZfpuA2lwwHqTgk+NJcWQnDpHn31CVm63Or5c5gb4H7/eSIdd+7hf3v+0a5qVsnyxkHbcxXquqk9ezxrUe93cFppxH4/kF/kGBBamm3kuUVbdBUY39c4U3NRkzSO+XdGs69ssK5SPzshn01axCJoNXqqj+ytebuMwF8oI9+ZDqj/XsQ1CLnChbsL+HCl68ioTeoYU9PLrO4on+rNHGPI0Cx6HrVse7M3WQBPGzOd1TvRh9eWJrvQrP/hm6kOR7KrWKuyJzrQh7OoDxrweXFH8toXeQRD8=";

    public static void main(String[] args) throws IOException {

        System.out.println("IP do servidor: " + InetAddress.getLocalHost().getHostAddress());

        ServerSocket serverSocket = null;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Porta do servidor:");
        int serverPort = Integer.parseInt(stdIn.readLine());
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Servidor esperando por conexões...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("\nCliente conectado: " + socket.getInetAddress());

                // Tratar a solicitação do cliente em uma thread separada
                new Thread(() -> {
                    handleClientRequest(socket);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleClientRequest(Socket socket) {
        try {
            DatabaseManager databaseManager = new DatabaseManager();

            String jsonRequest = "";
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while ((line = inFromClient.readLine()) != null) {
                // Loop para continuar lendo as solicitações do cliente
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append(line);

                // Verifique se a linha contém uma solicitação completa (você pode usar uma
                // estratégia de delimitador)
                if (line.endsWith("}")) {
                    String receivedJson = jsonBuilder.toString();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(receivedJson);
                    String action = jsonNode.get("action").asText();

                    if (action.equals("cadastro-usuario")) {
                        databaseManager.openConnection();

                        JsonNode dataNode = jsonNode.get("data");
                        String name = dataNode.get("name").asText();
                        String email = dataNode.get("email").asText();
                        String password = dataNode.get("password").asText();
                        String role = dataNode.get("type").asText();

                        try {
                            // Check if a user with the same email already exists
                            if (databaseManager.getUserByEmail(email) != null) {
                                boolean error = true;
                                String message = "Erro: Já existe um usuário com o email fornecido.";

                                // Create the JSON response
                                Map<String, Object> jsonMapRegister = new HashMap<>();
                                jsonMapRegister.put("action", action);
                                jsonMapRegister.put("error", error);
                                jsonMapRegister.put("message", message);

                                // Create JSON confirmation of registration
                                jsonRequest = objectMapper.writeValueAsString(jsonMapRegister);

                                // Send an error response to the client
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Register the new user in the database
                                User newUser = new User(name, email, password, role);
                                databaseManager.addUser(newUser);

                                boolean error = false;
                                String message = "Usuário cadastrado com sucesso!";

                                // Create the JSON response
                                Map<String, Object> jsonMapRegister = new HashMap<>();
                                jsonMapRegister.put("action", action);
                                jsonMapRegister.put("error", error);
                                jsonMapRegister.put("message", message);

                                // Create JSON confirmation of registration
                                jsonRequest = objectMapper.writeValueAsString(jsonMapRegister);

                                // Send a confirmation response to the client
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace(); // Handle the exception appropriately
                        } finally {
                            // Close the database connection
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("autocadastro-usuario")) {
                        databaseManager.openConnection();

                        JsonNode dataNode = jsonNode.get("data");
                        String name = dataNode.get("name").asText();
                        String email = dataNode.get("email").asText();
                        String password = dataNode.get("password").asText();

                        try {
                            // Check if a user with the same email already exists in the database
                            if (databaseManager.getUserByEmail(email) != null) {
                                boolean error = true;
                                String message = "Erro: Já existe um usuário com o email fornecido.";

                                // Create the JSON response
                                Map<String, Object> jsonMapRegister = new HashMap<>();
                                jsonMapRegister.put("action", action);
                                jsonMapRegister.put("error", error);
                                jsonMapRegister.put("message", message);

                                // Create JSON confirmation of registration
                                jsonRequest = objectMapper.writeValueAsString(jsonMapRegister);

                                // Send an error response to the client
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Register the new user in the database
                                User newUser = new User(name, email, password, "user");
                                databaseManager.addUser(newUser);

                                boolean error = false;
                                String message = "Usuário cadastrado com sucesso!";

                                // Create the JSON response
                                Map<String, Object> jsonMapRegister = new HashMap<>();
                                jsonMapRegister.put("action", action);
                                jsonMapRegister.put("error", error);
                                jsonMapRegister.put("message", message);

                                // Create JSON confirmation of registration
                                jsonRequest = objectMapper.writeValueAsString(jsonMapRegister);

                                // Send a confirmation response to the client
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace(); // Handle the exception appropriately
                        } finally {
                            // Close the database connection
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("login")) {
                        databaseManager.openConnection();

                        JsonNode dataNode = jsonNode.get("data");
                        String email = dataNode.get("email").asText();
                        String password = dataNode.get("password").asText();

                        try {
                            // Buscar usuário pelo e-mail no banco de dados
                            User loggedInUser = databaseManager.getUserLogin(email, password);

                            if (loggedInUser != null) {
                                // Login bem-sucedido
                                boolean error = false;
                                String message = "Logado com sucesso!";

                                // Criar o token
                                TokenGenerator tokenGenerator = new TokenGenerator();
                                String token = tokenGenerator.generateToken((int) databaseManager.getUserIDByEmail(email), databaseManager.isAdmByEmail(email));

                                // Criar a sessão
                                SessionManager.createSession(token, loggedInUser);

                                // Criar o JSON de resposta
                                Map<String, Object> jsonMapLogin = new HashMap<>();
                                jsonMapLogin.put("action", action);
                                jsonMapLogin.put("error", error);
                                jsonMapLogin.put("message", message);

                                Map<String, String> dataMapLogin = new HashMap<>();
                                dataMapLogin.put("token", token);

                                jsonMapLogin.put("data", dataMapLogin);
                                jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                                // Enviar a resposta de confirmação para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Login falhou

                                boolean error = true;
                                String message = "Erro ao logar! Verifique as credenciais e tente novamente.";

                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonMapLogin = new HashMap<>();
                                jsonMapLogin.put("action", action);
                                jsonMapLogin.put("error", error);
                                jsonMapLogin.put("message", message);

                                jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("logout")) {
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();

                        SessionManager.removeSession(token);

                        boolean error = false;
                        String message = "Logout efetuado com sucesso!";

                        // Criar o JSON de resposta
                        Map<String, Object> jsonMapLogout = new HashMap<>();
                        jsonMapLogout.put("action", action);
                        jsonMapLogout.put("error", error);
                        jsonMapLogout.put("message", message);

                        jsonRequest = objectMapper.writeValueAsString(jsonMapLogout);

                        // Enviar a resposta de confirmação para o cliente
                        PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                        System.out.println("\njsonRequest enviado: " + jsonRequest);
                        outToClient.println(jsonRequest);
                        outToClient.flush();
                        System.out.println(message);
                    } else if (action.equals("listar-usuarios")) {
                        databaseManager.openConnection();

                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();

                        try {
                            // Obter informações de todos os usuários do banco de dados
                            List<Map<String, Object>> usersInfo = new ArrayList<>();

                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Sucesso";

                                Statement statement = null;
                                ResultSet resultSet = null;

                                try {
                                    statement = databaseManager.getConnection().createStatement();
                                    resultSet = statement.executeQuery("SELECT userID, name, type, email FROM user");

                                    while (resultSet.next()) {
                                        int id = resultSet.getInt("userID");
                                        String name = resultSet.getString("name");
                                        String type = resultSet.getString("type");
                                        String email = resultSet.getString("email");

                                        // Imprima os valores recuperados para verificar
                                        System.out.println("ID: " + id + ", Name: " + name + ", Type: " + type + ", Email: " + email);

                                        Map<String, Object> userInfo = new HashMap<>();
                                        userInfo.put("id", id);
                                        userInfo.put("name", name);
                                        userInfo.put("type", type.equalsIgnoreCase("admin") ? "admin" : "user");
                                        userInfo.put("email", email);
                                        usersInfo.add(userInfo);
                                    }

                                    // Criar o JSON de resposta
                                    Map<String, Object> jsonResponse = new HashMap<>();
                                    jsonResponse.put("action", action);
                                    jsonResponse.put("error", error);
                                    jsonResponse.put("message", message);

                                    Map<String, Object> dataMap = new HashMap<>();
                                    dataMap.put("users", usersInfo);
                                    jsonResponse.put("data", dataMap);

                                    // Converter o JSON de resposta em uma string
                                    jsonRequest = objectMapper.writeValueAsString(jsonResponse);

                                    // Enviar a resposta para o cliente
                                    PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                    System.out.println("\njsonRequest enviado: " + jsonRequest);
                                    outToClient.println(jsonRequest);
                                    outToClient.flush();
                                    System.out.println("Listagem de usuários enviada com sucesso.");
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } finally {
                                    // Fechar o statement e resultSet no bloco finally para garantir que sejam fechados, independentemente do resultado
                                    if (resultSet != null) {
                                        resultSet.close();
                                    }

                                    if (statement != null) {
                                        statement.close();
                                    }
                                }
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para listar usuários ou o token é inválido.";

                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);

                                // Converter o JSON de resposta de erro em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);

                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para listar usuários ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    }
                    else if (action.equals("pedido-proprio-usuario")) {
                        databaseManager.openConnection();

                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();

                        try {
                            // Obter informações do usuário a partir do banco de dados usando o token
                            User user = databaseManager.getUserByToken(token);

                            if (user != null) {
                                boolean error = false;
                                String message = "Dados do usuário recuperados com sucesso.";

                                // Obter dados do usuário diretamente do banco de dados
                                Map<String, Object> userData = databaseManager.getUserDataById(user.getId());

                                // Criar o JSON de resposta diretamente com os dados do banco de dados
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                                jsonResponse.put("data", userData);

                                // Converter o JSON de resposta em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);

                                // Enviar a resposta para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Token inválido ou não encontrado
                                boolean error = true;
                                String message = "Erro: Token inválido ou não encontrado.";

                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);

                                // Converter o JSON de resposta de erro em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);

                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    }
                    else if (action.equals("excluir-usuario")) {
                        databaseManager.openConnection();

                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();
                        int userIdToDelete = dataNode.get("user_id").asInt();

                        try {
                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Usuário excluído com sucesso.";

                                // Remover o usuário com o ID especificado
                                boolean userRemoved = databaseManager.deleteUser(userIdToDelete);

                                if (!userRemoved) {
                                    error = true;
                                    message = "Erro: Usuário com ID especificado não encontrado.";
                                }

                                // Criar o JSON de resposta
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);

                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);

                                // Enviar a resposta para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para excluir usuários ou o token é inválido.";

                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);

                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);

                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para excluir usuários ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    } else
                    // ep3 -------------------------------------------------------------------------------------------
                    if (action.equals("cadastro-ponto")) {
                        databaseManager.openConnection();
                    
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();
                        String pointName = dataNode.get("name").asText();
                        String pointObs = dataNode.get("obs").asText();
                    
                        try {
                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Ponto adicionado com sucesso.";
                    
                                // Adicionar o ponto com os dados especificados
                                boolean pointAdded = databaseManager.addPoint(pointName, pointObs);
                    
                                if (!pointAdded) {
                                    error = true;
                                    message = "Erro ao adicionar ponto. Verifique os dados fornecidos.";
                                }
                    
                                // Criar o JSON de resposta
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para adicionar pontos ou o token é inválido.";
                    
                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para adicionar pontos ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("listar-pontos")) {
                        databaseManager.openConnection();
                    
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();
                    
                        try {
                            // Obter informações de todos os pontos do banco de dados
                            List<Map<String, Object>> pointsInfo = new ArrayList<>();
                    
                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Sucesso";
                    
                                Statement statement = null;
                                ResultSet resultSet = null;
                    
                                try {
                                    statement = databaseManager.getConnection().createStatement();
                                    resultSet = statement.executeQuery("SELECT pointID, name, obs FROM point");
                    
                                    while (resultSet.next()) {
                                        int id = resultSet.getInt("pointID");
                                        String name = resultSet.getString("name");
                                        String obs = resultSet.getString("obs");
                    
                                        // Imprima os valores recuperados para verificar
                                        System.out.println("ID: " + id + ", Name: " + name + ", Obs: " + obs);
                    
                                        Map<String, Object> pointInfo = new HashMap<>();
                                        pointInfo.put("id", id);
                                        pointInfo.put("name", name);
                                        pointInfo.put("obs", obs);
                                        pointsInfo.add(pointInfo);
                                    }
                    
                                    // Criar o JSON de resposta
                                    Map<String, Object> jsonResponse = new HashMap<>();
                                    jsonResponse.put("action", action);
                                    jsonResponse.put("error", error);
                                    jsonResponse.put("message", message);
                    
                                    Map<String, Object> dataMap = new HashMap<>();
                                    dataMap.put("pontos", pointsInfo);
                                    jsonResponse.put("data", dataMap);
                    
                                    // Converter o JSON de resposta em uma string
                                    jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                    // Enviar a resposta para o cliente
                                    PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                    System.out.println("\njsonRequest enviado: " + jsonRequest);
                                    outToClient.println(jsonRequest);
                                    outToClient.flush();
                                    System.out.println("Listagem de pontos enviada com sucesso.");
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } finally {
                                    databaseManager.closeConnection();
                                }
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para listar pontos ou o token é inválido.";
                    
                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                // Converter o JSON de resposta de erro em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para listar pontos ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("pedido-edicao-ponto")) {
                        databaseManager.openConnection();
                    
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();
                        int pontoId = dataNode.get("ponto_id").asInt();
                    
                        try {
                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Sucesso";
                    
                                // Obter informações do ponto com o ID especificado
                                Point point = databaseManager.getPointById(pontoId);
                    
                                if (point != null) {
                                    // Criar o JSON de resposta
                                    Map<String, Object> jsonResponse = new HashMap<>();
                                    jsonResponse.put("action", action);
                                    jsonResponse.put("error", error);
                                    jsonResponse.put("message", message);
                    
                                    Map<String, Object> dataMap = new HashMap<>();
                                    Map<String, Object> pontoMap = new HashMap<>();
                                    pontoMap.put("id", point.getId());
                                    pontoMap.put("name", point.getName());
                                    pontoMap.put("obs", point.getObs());
                                    dataMap.put("ponto", pontoMap);
                                    jsonResponse.put("data", dataMap);
                    
                                    // Converter o JSON de resposta em uma string
                                    jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                    // Enviar a resposta para o cliente
                                    PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                    System.out.println("\njsonRequest enviado: " + jsonRequest);
                                    outToClient.println(jsonRequest);
                                    outToClient.flush();
                                    System.out.println("Informações do ponto enviadas com sucesso.");
                                } else {
                                    // Ponto não encontrado
                                    error = true;
                                    message = "Erro: Ponto com ID especificado não encontrado.";
                    
                                    // Criar o JSON de resposta de erro
                                    Map<String, Object> jsonResponse = new HashMap<>();
                                    jsonResponse.put("action", action);
                                    jsonResponse.put("error", error);
                                    jsonResponse.put("message", message);
                    
                                    // Converter o JSON de resposta de erro em uma string
                                    jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                    // Enviar a resposta de erro para o cliente
                                    PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                    System.out.println("\njsonRequest enviado: " + jsonRequest);
                                    outToClient.println(jsonRequest);
                                    outToClient.flush();
                                    System.out.println(message);
                                }
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para acessar informações do ponto ou o token é inválido.";
                    
                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                // Converter o JSON de resposta de erro em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para acessar informações do ponto ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("edicao-ponto")) {
                        databaseManager.openConnection();
                    
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();
                        int pontoId = dataNode.get("ponto_id").asInt();
                        String novoNome = dataNode.get("name").asText();
                        String novaObs = dataNode.get("obs").asText();
                    
                        try {
                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Ponto atualizado com sucesso!";
                    
                                // Atualizar os dados do ponto no banco de dados
                                boolean pontoAtualizado = databaseManager.updatePoint(pontoId, novoNome, novaObs);
                    
                                if (!pontoAtualizado) {
                                    error = true;
                                    message = "Erro: Ponto com ID especificado não encontrado.";
                                }
                    
                                // Criar o JSON de resposta
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                                jsonResponse.put("action", action);
                    
                                // Converter o JSON de resposta em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para editar pontos ou o token é inválido.";
                    
                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                                jsonResponse.put("action", action);
                    
                                // Converter o JSON de resposta de erro em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para editar pontos ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("excluir-ponto")) {
                        databaseManager.openConnection();
                    
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();
                        int pointIdToDelete = dataNode.get("ponto_id").asInt();
                    
                        try {
                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Ponto excluído com sucesso.";
                    
                                // Remover o ponto com o ID especificado
                                boolean pointRemoved = databaseManager.deletePoint(pointIdToDelete);
                    
                                if (!pointRemoved) {
                                    error = true;
                                    message = "Erro: Ponto com ID especificado não encontrado.";
                                }
                    
                                // Criar o JSON de resposta
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para excluir pontos ou o token é inválido.";
                    
                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para excluir pontos ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("cadastro-segmento")) {
                        databaseManager.openConnection();
                    
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();
                        
                        // Extrair informações do segmento
                        JsonNode segmentoNode = dataNode.get("segmento");
                        int pontoOrigemId = segmentoNode.get("ponto_origem").get("id").asInt();
                        int pontoDestinoId = segmentoNode.get("ponto_destino").get("id").asInt();
                        String direcao = segmentoNode.get("direcao").asText();
                        int distancia = segmentoNode.get("distancia").asInt();
                        String obs = segmentoNode.get("obs").asText();
                    
                        try {
                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Segmento cadastrado com sucesso!";
                    
                                // Criar instância do objeto Segment
                                Segment segment = new Segment(0, pontoOrigemId, pontoDestinoId, direcao, distancia, obs);
                    
                                // Adicionar o segmento ao banco de dados
                                boolean segmentoCadastrado = databaseManager.addSegment(segment);
                    
                                if (!segmentoCadastrado) {
                                    error = true;
                                    message = "Erro ao cadastrar o segmento.";
                                }
                    
                                // Criar o JSON de resposta
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                                jsonResponse.put("action", action);
                    
                                // Converter o JSON de resposta em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para cadastrar segmentos ou o token é inválido.";
                    
                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                                jsonResponse.put("action", action);
                    
                                // Converter o JSON de resposta de erro em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para cadastrar segmentos ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("listar-segmentos")) {
                        databaseManager.openConnection();
                    
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();
                    
                        try {
                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Sucesso";
                    
                                // Obter a lista de segmentos do banco de dados
                                List<Map<String, Object>> segmentList = databaseManager.getSegmentList();
                    
                                // Criar o JSON de resposta
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                Map<String, Object> dataMap = new HashMap<>();
                                dataMap.put("segmentos", segmentList);
                                jsonResponse.put("data", dataMap);
                    
                                // Converter o JSON de resposta em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Listagem de segmentos enviada com sucesso.");
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para listar segmentos ou o token é inválido.";
                    
                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                // Converter o JSON de resposta de erro em uma string
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para listar segmentos ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    } else if (action.equals("excluir-segmento")) {
                        databaseManager.openConnection();
                    
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();
                        int segmentIdToDelete = dataNode.get("segmento_id").asInt();
                    
                        try {
                            // Verificar se o token é de administrador
                            User adminUser = databaseManager.getUserByToken(token);
                            if (adminUser != null) {
                                boolean error = false;
                                String message = "Segmento excluído com sucesso.";
                    
                                // Remover o segmento com o ID especificado
                                boolean segmentRemoved = databaseManager.deleteSegment(segmentIdToDelete);
                    
                                if (!segmentRemoved) {
                                    error = true;
                                    message = "Erro: Segmento com ID especificado não encontrado.";
                                }
                    
                                // Criar o JSON de resposta
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println(message);
                            } else {
                                // Permissão negada ou token inválido
                                boolean error = true;
                                String message = "Você não tem permissão para excluir segmentos ou o token é inválido.";
                    
                                // Criar o JSON de resposta de erro
                                Map<String, Object> jsonResponse = new HashMap<>();
                                jsonResponse.put("action", action);
                                jsonResponse.put("error", error);
                                jsonResponse.put("message", message);
                    
                                jsonRequest = objectMapper.writeValueAsString(jsonResponse);
                    
                                // Enviar a resposta de erro para o cliente
                                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                                System.out.println("\njsonRequest enviado: " + jsonRequest);
                                outToClient.println(jsonRequest);
                                outToClient.flush();
                                System.out.println("Permissão negada para excluir segmentos ou token inválido.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Fechar a conexão com o banco de dados
                            databaseManager.closeConnection();
                        }
                    }

                    System.out.println("JSON recebido do cliente:");
                    System.out.println(receivedJson);

                    // Limpe o StringBuilder para a próxima solicitação
                    jsonBuilder.setLength(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTokenAdmin(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            return claims.get("admin", Boolean.class);
        } catch (Exception e) {
            // Se ocorrer uma exceção ao verificar o token, significa que não é um token
            // válido de administrador.
            return false;
        }
    }
}
