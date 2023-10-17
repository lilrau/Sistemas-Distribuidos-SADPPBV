package org.example;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server {
        private static List<User> allUsers = new ArrayList<>(); //Lista para armazenar os usuários registrados

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
                System.out.println("Cliente conectado: " + socket.getInetAddress());

                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println("Servidor conectado: " + socket.getInetAddress() + ":" + serverPort);
        
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
            String jsonRequest = "";
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String line;
            while ((line = inFromClient.readLine()) != null) {
                // Loop para continuar lendo as solicitações do cliente
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append(line);
    
                // Verifique se a linha contém uma solicitação completa (você pode usar uma estratégia de delimitador)
                if (line.endsWith("}")) {
                    String receivedJson = jsonBuilder.toString();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(receivedJson);
                    String action = jsonNode.get("action").asText();

                    if (action.equals("cadastro-usuario")) {

                        JsonNode dataNode = jsonNode.get("data");
                        String name = dataNode.get("nome").asText();
                        String email = dataNode.get("email").asText();
                        String password = dataNode.get("senha").asText();
                        String role = dataNode.get("tipo").asText();

                        // Crie um novo usuário com os dados recebidos e adicione-o à lista
                        User newUser = new User();
                        newUser.register(name, email, password, role);
                        allUsers.add(newUser);

                        String error = "false";
                        String message = "Usuário cadastrado com sucesso!";

                        // Criar o JSON
                        Map<String, Object> jsonMapRegister = new HashMap<>();
                        jsonMapRegister.put("action", action);

                        Map<String, String> dataMapRegister = new HashMap<>();
                        dataMapRegister.put("error", error);
                        dataMapRegister.put("message", message);

                        jsonMapRegister.put("data", dataMapRegister);
                        jsonRequest = objectMapper.writeValueAsString(jsonMapRegister);

                        // Enviar uma resposta de confirmação para o cliente, se necessário
                        PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                        outToClient.flush();
                        System.out.println(message);
                    } else if (action.equals("login")) {
                        JsonNode dataNode = jsonNode.get("data");
                        String email = dataNode.get("email").asText();
                        String password = dataNode.get("senha").asText();
                    
                        User loggedInUser = getUserByEmail(email);
                    
                        if (loggedInUser != null && loggedInUser.isPasswordCorrect(password)) {
                            // Login bem-sucedido
                            String error = "false";
                            String message = "Logado com sucesso!";

                            // Criar o token
                            TokenGenerator tokenGenerator = new TokenGenerator();
                            String token = tokenGenerator.generateToken(1, loggedInUser.getRole());

                            // Criar a sessão
                            SessionManager.createSession(token, loggedInUser);

                            // Criar o JSON
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);
                            jsonMapLogin.put("error", error);
                            jsonMapLogin.put("message", message);

                            Map<String, String> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", token);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar uma resposta de confirmação para o cliente, se necessário
                            PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                            outToClient.flush();
                            System.out.println(message);
                        } else {
                            // Login falhou
                            String error = "true";
                            String message = "Erro ao logar! Verifique as credenciais e tente novamente.";
                            // Criar o JSON
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);
                            jsonMapLogin.put("error", error);
                            jsonMapLogin.put("message", message);

                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar uma resposta de confirmação para o cliente, se necessário
                            PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                            outToClient.flush();
                            System.out.println(message);
                        }
                    } else if (action.equals("logout")) {
                        JsonNode dataNode = jsonNode.get("data");
                        String token = dataNode.get("token").asText();

                        SessionManager.removeSession(token);

                        String error = "true";
                        String message = "Logout efetuado com sucesso!";

                        // Criar o JSON
                        Map<String, Object> jsonMapLogin = new HashMap<>();
                        jsonMapLogin.put("action", action);
                        jsonMapLogin.put("error", error);
                        jsonMapLogin.put("message", message);
                    }
                    
        
                    System.out.println("JSON recebido do cliente:");
                    System.out.println(receivedJson);
                            
                    // Limpe o StringBuilder para a próxima solicitação
                    jsonBuilder.setLength(0);

                    // Enviar o JSON para o cliente
                    PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                    outToClient.println(jsonRequest);
                    outToClient.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static User getUserByEmail(String email) {
        for (User user : allUsers) {
            if (user.getEmail().equals(email)) {
                return user; // Retorna o usuário encontrado
            }
        }
        return null; // Retorna null se nenhum usuário com o email fornecido for encontrado
    }    
    
}
