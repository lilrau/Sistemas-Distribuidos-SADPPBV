package org.example;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

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

            boolean running = true;

            while (running) {
                try {
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String serverResponse = inFromServer.readLine();
                System.out.println(serverResponse);

                    System.out.println("Digite o Número de Operação");
                    System.out.println("1 - Cadastro");
                    System.out.println("2 - Login");
                    System.out.println("0 - Encerrar");

                    User user = new User();
                    int menu = Integer.parseInt(stdIn.readLine());
                    String action = "";

                    switch (menu) {
                        case 0:
                            System.out.println("Encerrando");
                            running = false;
                            break;

                        case 1: {
                            TokenGenerator tokenGenerator = new TokenGenerator();
                            String name = "";
                            String email = "";
                            String password = "";
                            String role = "";
                            int ra = 0;
                            action = "cadastro-usuario";
                            System.out.println(" **************** - CADASTRAR - ****************");
                            System.out.println("Registro de Aluno (RA): ");
                            ra = Integer.parseInt(stdIn.readLine());
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
                            String token = tokenGenerator.generateToken(ra, role);
                            user.register(token, name, email, password);

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();

                            Map<String, Object> jsonMapRegister = new HashMap<>();
                            jsonMapRegister.put("action", action);

                            Map<String, String> dataMap = new HashMap<>();
                            dataMap.put("token", user.getToken());
                            dataMap.put("nome", user.getName());
                            dataMap.put("email", user.getEmail());
                            dataMap.put("senha", user.getPassword());

                            jsonMapRegister.put("data", dataMap);

                            jsonRequest = objectMapper.writeValueAsString(jsonMapRegister);
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
                            break;
                        }


                        default: {
                            System.out.println("Opção inválida");
                            break;
                        }
                    }

                    // Enviar o JSON para o servidor
                    PrintWriter outToServer = new PrintWriter(socket.getOutputStream(), true);
                    outToServer.println(jsonRequest);
                    outToServer.flush();

                } catch (IOException e) {
                    System.err.println("Couldn't get I/O: " + e.getMessage());
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
}
