package org.example;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Client {
    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("IP do servidor:");
        String serverHostname = stdIn.readLine();
        System.out.println("Porta do servidor:");
        int serverPort = Integer.parseInt(stdIn.readLine());

        try (Socket socket = new Socket(serverHostname, serverPort)) {
            String jsonRequest = "";
            PrintWriter outToServer = new PrintWriter(socket.getOutputStream(), true);
            String sessionToken = "";

            boolean running = true;

            while (running) {
                try {
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String serverResponse = inFromServer.readLine();
                System.out.println(serverResponse);

                    System.out.println("Digite o Número de Operação");
                    System.out.println("1 - Cadastro");
                    System.out.println("2 - Login");
                    System.out.println("3 - Logout");
                    System.out.println("0 - Encerrar");

                    User user = new User();
                    int menu = Integer.parseInt(stdIn.readLine());
                    String action = "";

                    switch (menu) {
                        case 0:
                            System.out.println("Encerrando...");
                            running = false;
                            break;

                        case 1: {
                            String name = "";
                            String email = "";
                            String password = "";
                            String role = "";
                            action = "cadastro-usuario";
                            System.out.println(" **************** - CADASTRAR - ****************");
                            System.out.println("Nome: ");
                            name = stdIn.readLine();
                            System.out.println("Email: ");
                            email = stdIn.readLine();
                            System.out.println("Senha: ");
                            password = stdIn.readLine();
                            System.out.println("Tipo de usuário: ");
                            System.out.println("0 - para Usuário Comum");
                            System.out.println("1 - para Administrador");
                            role = stdIn.readLine();
                            if (role.equals("0")) {
                                role = "user";
                            } else if (role.equals("1")) {
                                role = "admin";
                            } else {
                                while (!role.equals("0") && !role.equals("1")) {
                                    switch (role) {
                                    case "0":
                                        role = "user";
                                        break;
                                    case "1":
                                        role = "admin";
                                        break;
                                    default:
                                        System.out.println("Opção inválida. Tente novamente.");
                                        role = stdIn.readLine();
                                        break;
                                    }
                                }
                            }
                            user.register(name, email, password, role);

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();

                            Map<String, Object> jsonMapRegister = new HashMap<>();
                            jsonMapRegister.put("action", action);

                            Map<String, String> dataMap = new HashMap<>();
                            dataMap.put("nome", user.getName());
                            dataMap.put("email", user.getEmail());
                            dataMap.put("senha", user.getPassword());
                            dataMap.put("tipo", user.getRole());

                            jsonMapRegister.put("data", dataMap);

                            jsonRequest = objectMapper.writeValueAsString(jsonMapRegister);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Receber a resposta do servidor
                            System.out.println(getResponse(inFromServer));

                            break;
                        }

                        case 2: {
                            action = "login";
                            System.out.println(" **************** - LOGIN - ****************");
                            System.out.println("Email: ");
                            String loginEmail = stdIn.readLine();
                            System.out.println("Senha: ");
                            String loginPassword = stdIn.readLine();
                            loginPassword = user.hashPassword(loginPassword);
                        
                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                        
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);
                        
                            Map<String, String> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("email", loginEmail);
                            dataMapLogin.put("senha", loginPassword);
                        
                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);
                            
                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            System.out.println("teste0");
                            System.out.println(getResponse(inFromServer));
                            System.out.println("a");

                            // Receber resposta do servidor
                            String receivedJson = inFromServer.toString();
                            System.out.println("b");
                            objectMapper = new ObjectMapper();
                            System.out.println("c");
                            System.out.println("teste1 " + receivedJson);
                            JsonNode jsonNode = objectMapper.readTree(receivedJson);
                            JsonNode errorNode = jsonNode.get("error");
                            System.out.println(errorNode);

                            break;
                        }
                        

                        case 3: {
                            action = "logout";
                            System.out.println(" **************** - LOGOUT - ****************");

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            Map<String, String> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", sessionToken);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Receber a resposta do servidor
                            System.out.println(getResponse(inFromServer));
                            break;
                        }


                        default: {
                            System.out.println("Opção inválida");
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Couldn't get I/O: " + e.getMessage());
                    running = false;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + serverHostname);
            System.exit(1);
        }
    }

    private static String getResponse(BufferedReader inFromServer) {
        try {
            String serverResponse = inFromServer.readLine();
            return serverResponse;
        } catch (IOException e) {
            System.err.println("Error reading server response: " + e.getMessage());
            return "";
        }
    }

    private static void sendRequest(PrintWriter outToServer, String jsonRequest) {
        outToServer.println(jsonRequest);
        outToServer.flush();
    }
}
