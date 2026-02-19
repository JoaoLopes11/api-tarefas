package cncs.academy.ess.controller;

import cncs.academy.ess.controller.messages.ErrorMessage;
import cncs.academy.ess.controller.messages.UserAddRequest;
import cncs.academy.ess.controller.messages.UserLoginRequest;
import cncs.academy.ess.controller.messages.UserResponse;
import cncs.academy.ess.model.User;
import cncs.academy.ess.service.DuplicateUserException;
import cncs.academy.ess.service.TodoUserService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final TodoUserService userService;

    public UserController(TodoUserService userService) {
        this.userService = userService;
    }

    public void createUser(Context ctx) {
        UserAddRequest userRequest = ctx.bodyAsClass(UserAddRequest.class);
        try {
            // Alinea a: O addUser faz o hash e guarda na BD
            User user = userService.addUser(userRequest.username, userRequest.password);

            UserResponse response = new UserResponse(user.getId(), user.getUsername());
            ctx.status(201).json(response);

        } catch (DuplicateUserException e) {
            ctx.status(409).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Resolve o erro de compilação da imagem
            ctx.status(500).json(Map.of("error", "Erro de segurança interno"));
        }
    }

    public void getUser(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        User user = userService.getUser(userId);
        if (user != null) {
            UserResponse response = new UserResponse(user.getId(), user.getUsername());
            ctx.status(200).json(response);
        } else {
            ctx.status(404).json(new ErrorMessage("User not found"));
        }
    }

    public void deleteUser(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        userService.deleteUser(userId);
        ctx.status(204);
    }

    public void loginUser(Context ctx) {
        UserAddRequest loginRequest = ctx.bodyAsClass(UserAddRequest.class);
        try {
            // Alinea a e b: Verifica hash e gera o JWT assinado
            String token = userService.login(loginRequest.username, loginRequest.password);

            if (token != null) {
                ctx.status(200).result(token);
            } else {
                ctx.status(401).json(Map.of("error", "Credenciais inválidas"));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erro no processamento do login"));
        }
    }
}

