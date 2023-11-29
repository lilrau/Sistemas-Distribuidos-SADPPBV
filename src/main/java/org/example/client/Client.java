package org.example.client;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import org.example.util.User;
import org.example.util.Validator;

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

                    String serverResponse = "";

                    System.out.println("Digite o Número de Operação");
                    System.out.println("1 - Cadastro");
                    System.out.println("2 - Login");
                    System.out.println("3 - Logout");
                    System.out.println("4 - Listar Usuários");
                    System.out.println("5 - Dados do Usuário");
                    System.out.println("6 - Editar Usuário");
                    System.out.println("7 - Excluir Usuário");
                    System.out.println("8 - Cadastrar Ponto");
                    System.out.println("9 - Editar Ponto");
                    System.out.println("10 - Listar Pontos");
                    System.out.println("11 - Excluir Ponto");
                    System.out.println("12 - Cadastrar Segmento");
                    System.out.println("13 - Editar Segmento");
                    System.out.println("14 - Listar Segmentos");
                    System.out.println("15 - Excluir Segmento");
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
                            if (sessionToken.equals("")) {
                                String name = "";
                                String email = "";
                                String password = "";
                                action = "autocadastro-usuario";
                                System.out.println(" **************** - AUTO-CADASTRAR - ****************");
                                System.out.println("Nome: ");
                                name = stdIn.readLine();
                                while (!Validator.isNameValid(name)) {
                                System.out.println("Nome inválido. Tente novamente.");
                                name = stdIn.readLine();
                                }
                                System.out.println("Email: ");
                                email = stdIn.readLine();
                                while (!Validator.isEmailValid(email)) {
                                    System.out.println("Email inválido. Tente novamente.");
                                    email = stdIn.readLine();
                                }
                                System.out.println("Senha: ");
                                password = stdIn.readLine();
                                while (!Validator.isPasswordValid(password)) {
                                    System.out.println("Senha inválida. Tente novamente.");
                                    password = stdIn.readLine();
                                }
                                password = user.hashPasswordMD5(password);

                                // Criar o JSON
                                ObjectMapper objectMapper = new ObjectMapper();

                                Map<String, Object> jsonMapRegister = new HashMap<>();
                                jsonMapRegister.put("action", action);

                                Map<String, String> dataMap = new HashMap<>();
                                dataMap.put("name", name);
                                dataMap.put("email", email);
                                dataMap.put("password", password);
                                if (!sessionToken.equals("")) {
                                    dataMap.put("token", sessionToken);
                                }

                                jsonMapRegister.put("data", dataMap);

                                jsonRequest = objectMapper.writeValueAsString(jsonMapRegister);

                                // Enviar o JSON para o servidor
                                sendRequest(outToServer, jsonRequest);

                                // Limpar o buffer
                                while (inFromServer.ready()) {
                                    inFromServer.readLine();
                                }

                                // Receber resposta do servidor
                                serverResponse = getResponse(inFromServer);
                                System.out.println("serverResponse:" + serverResponse); 
                                break;
                            }

                            String name = "";
                            String email = "";
                            String password = "";
                            String type = "";
                            action = "cadastro-usuario";
                            System.out.println(" **************** - CADASTRAR - ****************");
                            System.out.println("Nome: ");
                            name = stdIn.readLine();
                            while (!Validator.isNameValid(name)) {
                                System.out.println("Nome inválido. Tente novamente.");
                                name = stdIn.readLine();
                            }
                            System.out.println("Email: ");
                            email = stdIn.readLine();
                            while (!Validator.isEmailValid(email)) {
                                System.out.println("Email inválido. Tente novamente.");
                                email = stdIn.readLine();
                            }
                            System.out.println("Senha: ");
                            password = stdIn.readLine();
                            while (!Validator.isPasswordValid(password)) {
                                System.out.println("Senha inválida. Tente novamente.");
                                password = stdIn.readLine();
                            }
                            System.out.println("Tipo de usuário: ");
                            System.out.println("0 - para Usuário Comum");
                            System.out.println("1 - para Administrador");
                            type = stdIn.readLine();
                            if (type.equals("0")) {
                                type = "user";
                            } else if (type.equals("1")) {
                                type = "admin";
                            } else {
                                while (!type.equals("0") && !type.equals("1")) {
                                    switch (type) {
                                        case "0":
                                            type = "user";
                                            break;
                                        case "1":
                                            type = "admin";
                                            break;
                                        default:
                                            System.out.println("Opção inválida. Tente novamente.");
                                            type = stdIn.readLine();
                                            break;
                                    }
                                }
                            }

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();

                            Map<String, Object> jsonMapRegister = new HashMap<>();
                            jsonMapRegister.put("action", action);

                            Map<String, String> dataMap = new HashMap<>();
                            dataMap.put("name", name);
                            dataMap.put("email", email);
                            dataMap.put("password", password);
                            if (!sessionToken.equals("")) {
                                dataMap.put("token", sessionToken);
                                dataMap.put("type", type);
                            } else {
                                dataMap.put("type", "user");
                            }

                            jsonMapRegister.put("data", dataMap);

                            jsonRequest = objectMapper.writeValueAsString(jsonMapRegister);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);
                            break;
                        }

                        case 2: {
                            action = "login";
                            System.out.println(" **************** - LOGIN - ****************");
                            System.out.println("Email: ");
                            String loginEmail = stdIn.readLine();
                            System.out.println("Senha: ");
                            String loginPassword = stdIn.readLine();
                            loginPassword = user.hashPasswordMD5(loginPassword);

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();

                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            Map<String, String> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("email", loginEmail);
                            dataMapLogin.put("password", loginPassword);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);

                            StringBuilder jsonBuilder = new StringBuilder();
                            jsonBuilder.append(serverResponse);
                            String receivedJson = jsonBuilder.toString();
                            objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(receivedJson);
                            String error = jsonNode.get("error").asText();

                            if (error.equals("false")) {
                                JsonNode dataNode = jsonNode.get("data");
                                sessionToken = dataNode.get("token").asText();
                            }
                            break;
                        }

                        case 3: {
                            action = "logout";
                            System.out.println(" **************** - LOGOUT - ****************");

                            if (sessionToken.equals("")) {
                                System.out.println("Você não está logado.");
                                break;
                            }

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
                            sessionToken = "";

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);
                            break;
                        }

                        case 4: {
                            if (!Validator.isTokenAdmin(sessionToken)) {
                                System.out.println("Você não tem permissão para listar usuários.");
                                break;
                            }

                            action = "listar-usuarios";
                            System.out.println(" **************** - LISTAR USUÁRIOS - ****************");

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

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);

                            // Analisar o JSON
                            objectMapper = new ObjectMapper();
                            JsonNode responseNode = objectMapper.readTree(serverResponse);

                            // Verificar se "error" é false e definir "action" com base nisso
                            if (responseNode.has("error") && !responseNode.get("error").asBoolean()) {
                                  // Verificar se o campo "data" contém a lista de usuários
                                if (responseNode.has("data") && responseNode.get("data").has("users")) {
                                    JsonNode usersNode = responseNode.get("data").get("users");

                                    if (usersNode.isArray()) {
                                        System.out.println("Usuários recebidos:");
                                        for (JsonNode userNode : usersNode) {
                                            int id = userNode.get("id").asInt();
                                            String name = userNode.get("name").asText();
                                            String type = userNode.get("type").asText();
                                            String email = userNode.get("email").asText();

                                            System.out.println("ID: " + id);
                                            System.out.println("Nome: " + name);
                                            System.out.println("Tipo: " + type);
                                            System.out.println("Email: " + email);
                                            System.out.println();
                                        }
                                    } else {
                                        System.out.println("Nenhum usuário encontrado na resposta do servidor.");
                                    }
                                } else {
                                    System.out.println("Nenhum campo 'users' encontrado na resposta do servidor.");
                                }
                            }
                            break;
                        }

                        case 5: {
                            action = "pedido-proprio-usuario";
                            System.out.println(" **************** - DADOS DO USUÁRIO - ****************");

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

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);
                            break;
                        }

                        case 6: {
                            int altId = 0;
                            if (!Validator.isTokenAdmin(sessionToken)) {
                                action = "autoedicao-usuario";
                                System.out.println(" **************** - EDITAR USUARIO - ****************");

                                System.out.println("- caso queira manter o mesmo, digite 0 -");

                                System.out.println("Novo nome: ");
                                String name = stdIn.readLine();
                                if (!name.equals("0")) {
                                    while (!Validator.isNameValid(name)) {
                                        System.out.println("Nome inválido. Tente novamente.");
                                        name = stdIn.readLine();
                                    }
                                }

                                System.out.println("Novo email: ");
                                String email = stdIn.readLine();
                                if (!email.equals("0")) {
                                    while (!Validator.isEmailValid(email)) {
                                        System.out.println("Email inválido. Tente novamente.");
                                        email = stdIn.readLine();
                                    }
                                }

                                System.out.println("Nova senha: ");
                                String password = stdIn.readLine();
                                if (!password.equals("0")) {
                                    while (!Validator.isPasswordValid(password)) {
                                        System.out.println("Senha inválida. Tente novamente.");
                                        password = stdIn.readLine();
                                    }
                                }

                                password = user.hashPasswordMD5(password);

                                // Criar o JSON
                                ObjectMapper objectMapper = new ObjectMapper();
                                Map<String, Object> jsonMapLogin = new HashMap<>();
                                jsonMapLogin.put("action", action);

                                Map<String, Object> dataMapLogin = new HashMap<>();
                                dataMapLogin.put("token", sessionToken);

                                if (!name.equals("0")) {
                                    dataMapLogin.put("name", name);
                                } else {
                                    dataMapLogin.put("name", null);
                                }

                                if (!email.equals("0")) {
                                    dataMapLogin.put("email", email);
                                } else {
                                    dataMapLogin.put("email", null);
                                }

                                if (!password.equals("0")) {
                                    dataMapLogin.put("password", password);
                                } else {
                                    dataMapLogin.put("password", null);
                                }

                                dataMapLogin.put("id", Validator.getId(sessionToken));

                                jsonMapLogin.put("data", dataMapLogin);
                                jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                                // Enviar o JSON para o servidor
                                sendRequest(outToServer, jsonRequest);

                                // Limpar o buffer
                                while (inFromServer.ready()) {
                                    inFromServer.readLine();
                                }

                                // Receber resposta do servidor
                                serverResponse = getResponse(inFromServer);
                                System.out.println("serverResponse:" + serverResponse);
                                break;
                            }
                            action = "pedido-edicao-usuario";
                            System.out.println(" **************** - EDITAR USUÁRIO - ****************");

                            System.out.println("ID do usuário a ser editado: ");
                            int id = Integer.parseInt(stdIn.readLine());

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            Map<String, Object> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", sessionToken);
                            dataMapLogin.put("user_id", id);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);

                            // Analisar o JSON
                            objectMapper = new ObjectMapper();
                            JsonNode responseNode = objectMapper.readTree(serverResponse);

                            // Verificar se "error" é false e definir "action" com base nisso
                            if (responseNode.has("error") && !responseNode.get("error").asBoolean()) {
                                action = "edicao-usuario";
                                altId = id;

                                System.out.println("- caso queira manter o mesmo, digite 0 -");

                                System.out.println("Novo nome: ");
                                String name = stdIn.readLine();
                                if (!name.equals("0")) {
                                    while (!Validator.isNameValid(name)) {
                                        System.out.println("Nome inválido. Tente novamente.");
                                        name = stdIn.readLine();
                                    }
                                }

                                System.out.println("Novo email: ");
                                String email = stdIn.readLine();
                                if (!email.equals("0")) {
                                    while (!Validator.isEmailValid(email)) {
                                        System.out.println("Email inválido. Tente novamente.");
                                        email = stdIn.readLine();
                                    }
                                }

                                System.out.println("Nova senha: ");
                                String password = stdIn.readLine();
                                if (!password.equals("0")) {
                                    while (!Validator.isPasswordValid(password)) {
                                        System.out.println("Senha inválida. Tente novamente.");
                                        password = stdIn.readLine();
                                    }
                                }
                                password = user.hashPasswordMD5(password);

                                System.out.println("Tipo de usuário: ");
                                System.out.println("(0 - usuário comum)");
                                System.out.println("(1 - administrador)");

                                String type = stdIn.readLine();

                                if (type.equals("0")) {
                                    type = "user";
                                } else if (type.equals("1")) {
                                    type = "admin";
                                } else {
                                    while (!type.equals("0") && !type.equals("1")) {
                                        switch (type) {
                                            case "0":
                                                type = "user";
                                                break;
                                            case "1":
                                                type = "admin";
                                                break;
                                            default:
                                                System.out.println("Opção inválida. Tente novamente.");
                                                type = stdIn.readLine();
                                                break;
                                        }
                                    }
                                }

                                // Criar o JSON
                                objectMapper = new ObjectMapper();
                                jsonMapLogin = new HashMap<>();
                                jsonMapLogin.put("action", action);

                                dataMapLogin = new HashMap<>();
                                dataMapLogin.put("token", sessionToken);

                                if (!name.equals("0")) {
                                    dataMapLogin.put("name", name);
                                } else {
                                    dataMapLogin.put("name", null);
                                }

                                if (!email.equals("0")) {
                                    dataMapLogin.put("email", email);
                                } else {
                                    dataMapLogin.put("email", null);
                                }

                                if (!password.equals("0")) {
                                    dataMapLogin.put("password", password);
                                } else {
                                    dataMapLogin.put("password", null);
                                }

                                dataMapLogin.put("user_id", altId);
                                dataMapLogin.put("type", type);

                                jsonMapLogin.put("data", dataMapLogin);
                                jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                                // Enviar o JSON para o servidor
                                sendRequest(outToServer, jsonRequest);

                                // Limpar o buffer
                                while (inFromServer.ready()) {
                                    inFromServer.readLine();
                                }

                                // Receber resposta do servidor
                                serverResponse = getResponse(inFromServer);
                                System.out.println("serverResponse:" + serverResponse);

                            } else {
                                break;
                            }

                            break;
                        }

                        case 7: {
                            if (!Validator.isTokenAdmin(sessionToken)) {
                                System.out.println("**************** - EXCLUIR USUÁRIO - ****************");
                                action = "excluir-proprio-usuario";

                                System.out.println("Email: ");
                                String email = stdIn.readLine();
                                System.out.println("Senha: ");
                                String password = stdIn.readLine();
                                password = user.hashPasswordMD5(password);

                                // Criar o JSON
                                ObjectMapper objectMapper = new ObjectMapper();
                                Map<String, Object> jsonMapLogin = new HashMap<>();
                                jsonMapLogin.put("action", action);

                                Map<String, String> dataMapLogin = new HashMap<>();
                                dataMapLogin.put("token", sessionToken);
                                dataMapLogin.put("email", email);
                                dataMapLogin.put("password", password);

                                jsonMapLogin.put("data", dataMapLogin);
                                jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                                // Enviar o JSON para o servidor
                                sendRequest(outToServer, jsonRequest);

                                // Limpar o buffer
                                while (inFromServer.ready()) {
                                    inFromServer.readLine();
                                }

                                // Receber resposta do servidor
                                serverResponse = getResponse(inFromServer);
                                System.out.println("serverResponse:" + serverResponse);
                                break;
                            }

                            action = "excluir-usuario";
                            System.out.println("**************** - EXCLUIR USUÁRIO - ****************");

                            System.out.println("ID do usuário a ser apagado: ");
                            int id = Integer.parseInt(stdIn.readLine());

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            Map<String, Object> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", sessionToken);
                            dataMapLogin.put("user_id", id);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);
                            break;
                        }

                        case 8: {
                            action = "cadastro-ponto";
                            System.out.println(" **************** - CADASTRAR PONTO - ****************");

                            System.out.println("Nome do ponto: ");
                            String name = stdIn.readLine();
                            System.out.println("Descrição do ponto: ");
                            System.out.println("Entre 0 para deixar em branco: ");
                            String obs = stdIn.readLine();
                            if (obs.equals("1")) {
                                obs = "";
                            }
                            if (obs.equals("0")) {
                                obs = null;
                            }

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            Map<String, String> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", sessionToken);
                            dataMapLogin.put("name", name);
                            dataMapLogin.put("obs", obs);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);
                            break;
                        }

                        case 9: {
                            action = "pedido-edicao-ponto";
                            System.out.println(" **************** - EDITAR PONTO - ****************");

                            System.out.println("ID do ponto: ");
                            int id = Integer.parseInt(stdIn.readLine());

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            Map<String, Object> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", sessionToken);
                            dataMapLogin.put("ponto_id", id);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);

                            JsonNode responseNode = objectMapper.readTree(serverResponse);

                            // Verificar se "error" é false e definir "action" com base nisso
                            if (responseNode.has("error") && !responseNode.get("error").asBoolean()) {
                                // Verificar se o campo "data" contém informações do ponto
                                if (responseNode.has("data") && responseNode.get("data").has("ponto")) {
                                    JsonNode pontoNode = responseNode.get("data").get("ponto");

                                    // Extrair informações do ponto
                                    id = pontoNode.get("id").asInt();
                                    String name = pontoNode.get("name").asText();
                                    String obs = pontoNode.get("obs").asText();

                                    System.out.println("ID: " + id);
                                    System.out.println("Nome: " + name);
                                    System.out.println("Observação: " + obs);
                                } else {
                                    System.out.println("Nenhum campo 'ponto' encontrado na resposta do servidor.");
                                }
                            } else {
                                // Lidar com a resposta de erro, se necessário
                                String errorMessage = responseNode.get("message").asText();
                                System.out.println("Erro: " + errorMessage);
                            }

                            action = "edicao-ponto";

                            System.out.println("Novo nome: ");
                            String name = stdIn.readLine();
                            System.out.println("Nova descrição: ");
                            String obs = stdIn.readLine();

                            // Criar o JSON
                            objectMapper = new ObjectMapper();
                            jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", sessionToken);
                            dataMapLogin.put("ponto_id", id);
                            dataMapLogin.put("name", name);
                            dataMapLogin.put("obs", obs);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            break;
                        }

                        case 10: {
                            action = "listar-pontos";
                            System.out.println(" **************** - LISTAR PONTOS - ****************");

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

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);

                            JsonNode responseNode = objectMapper.readTree(serverResponse);

                            // Verificar se "error" é false e definir "action" com base nisso
                            if (responseNode.has("error") && !responseNode.get("error").asBoolean()) {
                                // Verificar se o campo "data" contém a lista de pontos
                                if (responseNode.has("data") && responseNode.get("data").has("pontos")) {
                                    JsonNode pointsNode = responseNode.get("data").get("pontos");

                                    if (pointsNode.isArray()) {
                                        System.out.println("Pontos recebidos:");
                                        for (JsonNode pointNode : pointsNode) {
                                            int id = pointNode.get("id").asInt();
                                            String name = pointNode.get("name").asText();
                                            String obs = pointNode.get("obs").asText();

                                            System.out.println("ID: " + id);
                                            System.out.println("Nome: " + name);
                                            System.out.println("Observação: " + obs);
                                            System.out.println();
                                        }
                                    } else {
                                        System.out.println("Nenhum ponto encontrado na resposta do servidor.");
                                    }
                                } else {
                                    System.out.println("Nenhum campo 'points' encontrado na resposta do servidor.");
                                }
                            }
                            break;
                        }

                        case 11: {
                            action = "excluir-ponto";
                            System.out.println(" **************** - EXCLUIR PONTO - ****************");

                            System.out.println("ID do ponto a ser apagado: ");
                            int id = Integer.parseInt(stdIn.readLine());

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            Map<String, Object> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", sessionToken);
                            dataMapLogin.put("ponto_id", id);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);
                            break;
                        }

                        case 12: {
                            action = "cadastro-segmento";
                            System.out.println(" **************** - CADASTRAR SEGMENTO - ****************");

                            System.out.println("ID do ponto de origem: ");
                            int id_origem = Integer.parseInt(stdIn.readLine());
                            System.out.println("Nome do ponto de origem: ");
                            String name_origem = stdIn.readLine();
                            System.out.println("Obs do ponto de origem: ");
                            String obs_origem = stdIn.readLine();

                            System.out.println("ID do ponto destino: ");
                            int id_destino = Integer.parseInt(stdIn.readLine());
                            System.out.println("Nome do ponto destino: ");
                            String name_destino = stdIn.readLine();
                            System.out.println("Obs do ponto destino: ");
                            String obs_destino = stdIn.readLine();

                            System.out.println("Direcao: ");
                            String direcao = stdIn.readLine();
                            System.out.println("Distancia: ");
                            int distancia = Integer.parseInt(stdIn.readLine());
                            System.out.println("Obs do segmento: ");
                            System.out.println("Entre 0 para nulo: ");
                            String obs_segmento = stdIn.readLine();
                            if (obs_segmento.equals("1")) {
                                obs_segmento = "";
                            }
                            if (obs_segmento.equals("0")) {
                                obs_segmento = null;
                            }
                        

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            Map<String, Object> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", sessionToken);

                            Map<String, Object> OrigemMap = new HashMap<>();
                            OrigemMap.put("id", id_origem);
                            OrigemMap.put("name", name_origem);
                            OrigemMap.put("obs", obs_origem);

                            Map<String, Object> DestinoMap = new HashMap<>();
                            DestinoMap.put("id", id_destino);
                            DestinoMap.put("name", name_destino);
                            DestinoMap.put("obs", obs_destino);

                            Map<String, Object> SegmentoMap = new HashMap<>();
                            SegmentoMap.put("ponto_origem", OrigemMap);
                            SegmentoMap.put("ponto_destino", DestinoMap);
                            SegmentoMap.put("direcao", direcao);
                            SegmentoMap.put("distancia", distancia);
                            SegmentoMap.put("obs", obs_segmento);

                            dataMapLogin.put("segmento", SegmentoMap);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);
                            break;
                        }

                        case 14: {
                            action = "listar-segmentos";
                            System.out.println(" **************** - LISTAR SEGMENTOS - ****************");

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

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);

                            JsonNode responseNode = objectMapper.readTree(serverResponse);

                            // Verificar se "error" é false e definir "action" com base nisso
                            if (responseNode.has("error") && !responseNode.get("error").asBoolean()) {
                                // Verificar se o campo "data" contém a lista de segmentos
                                if (responseNode.has("data") && responseNode.get("data").has("segmentos")) {
                                    JsonNode segmentosNode = responseNode.get("data").get("segmentos");

                                    if (segmentosNode.isArray()) {
                                        System.out.println("Segmentos recebidos:");
                                        for (JsonNode segmentoNode : segmentosNode) {
                                            int id = segmentoNode.get("id").asInt();
                                            
                                            // Ponto Origem
                                            JsonNode pontoOrigemNode = segmentoNode.get("ponto_origem");
                                            int pontoOrigemId = pontoOrigemNode.get("id").asInt();
                                            String pontoOrigemName = pontoOrigemNode.get("name").asText();
                                            String pontoOrigemObs = pontoOrigemNode.get("obs").asText();

                                            // Ponto Destino
                                            JsonNode pontoDestinoNode = segmentoNode.get("ponto_destino");
                                            int pontoDestinoId = pontoDestinoNode.get("id").asInt();
                                            String pontoDestinoName = pontoDestinoNode.get("name").asText();
                                            String pontoDestinoObs = pontoDestinoNode.get("obs").asText();

                                            String direcao = segmentoNode.get("direcao").asText();
                                            String distancia = segmentoNode.get("distancia").asText();
                                            String obs = segmentoNode.get("obs").asText();

                                            System.out.println("ID: " + id);
                                            System.out.println("Ponto Origem - ID: " + pontoOrigemId + ", Nome: " + pontoOrigemName + ", Obs: " + pontoOrigemObs);
                                            System.out.println("Ponto Destino - ID: " + pontoDestinoId + ", Nome: " + pontoDestinoName + ", Obs: " + pontoDestinoObs);
                                            System.out.println("Direção: " + direcao);
                                            System.out.println("Distância: " + distancia);
                                            System.out.println("Observação: " + obs);
                                            System.out.println();
                                        }
                                    } else {
                                        System.out.println("Nenhum segmento encontrado na resposta do servidor.");
                                    }
                                } else {
                                    System.out.println("Nenhum campo 'segmentos' encontrado na resposta do servidor.");
                                }
                            } else {
                                // Lidar com a resposta de erro, se necessário
                                String errorMessage = responseNode.get("message").asText();
                                System.out.println("Erro: " + errorMessage);
                            }
                            break;
                        }

                        case 15: {
                            action = "excluir-segmento";
                            System.out.println(" **************** - EXCLUIR SEGMENTO - ****************");

                            System.out.println("ID do segmento a ser apagado: ");
                            int id = Integer.parseInt(stdIn.readLine());

                            // Criar o JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> jsonMapLogin = new HashMap<>();
                            jsonMapLogin.put("action", action);

                            Map<String, Object> dataMapLogin = new HashMap<>();
                            dataMapLogin.put("token", sessionToken);
                            dataMapLogin.put("segmento_id", id);

                            jsonMapLogin.put("data", dataMapLogin);
                            jsonRequest = objectMapper.writeValueAsString(jsonMapLogin);

                            // Enviar o JSON para o servidor
                            sendRequest(outToServer, jsonRequest);

                            // Limpar o buffer
                            while (inFromServer.ready()) {
                                inFromServer.readLine();
                            }

                            // Receber resposta do servidor
                            serverResponse = getResponse(inFromServer);
                            System.out.println("serverResponse:" + serverResponse);
                            break;
                        }

                        default: {
                            System.out.println("Opção inválida");
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Couldn't get I/O: " + e.getMessage());
                    System.exit(1);
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
