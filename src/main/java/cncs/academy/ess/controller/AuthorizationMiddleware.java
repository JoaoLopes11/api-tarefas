package cncs.academy.ess.controller;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationMiddleware implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationMiddleware.class);
    private final UserRepository userRepository;

    public AuthorizationMiddleware(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        
        if (ctx.method().name().equals("OPTIONS")) {
            return;
        }
        // Permitir pedidos não autenticados para registo e login
        if ((ctx.path().equals("/user") && ctx.method().name().equals("POST")) ||
                (ctx.path().equals("/login") && ctx.method().name().equals("POST"))) {
            return;
        }

        // Verificar se o cabeçalho Authorization existe
        String authorizationHeader = ctx.header("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.info("Cabeçalho de autorização em falta ou inválido para o caminho '{}'", ctx.path());
            throw new UnauthorizedResponse();
        }

        // Extrair e validar o token JWT
        String token = authorizationHeader.replace("Bearer", "").trim();
        int userId = validateTokenAndGetUserId(ctx, token);
        if (userId == -1) {
            logger.info("Token de autorização inválido");
            throw new UnauthorizedResponse();
        }

        //3 (jCasbin)

        //Obter o user da BD para saber o Username/Role
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UnauthorizedResponse();
        }

        //Iniciar o Enforcer com os files
        org.casbin.jcasbin.main.Enforcer enforcer = new org.casbin.jcasbin.main.Enforcer("model.conf", "policy.csv");

        //Definir as variáveis
        String sub = user.getUsername();
        String obj = ctx.path();
        String act = ctx.method().name();

        //Validar a permissão segundo o RBAC
        if (!enforcer.enforce(user.getUsername(), ctx.path(), ctx.method().name(), "allow")) {
            logger.warn("Acesso negado para o utilizador: {}", user.getUsername());
            throw new io.javalin.http.ForbiddenResponse("Acesso negado: permissões insuficientes.");
        }

        // Guardar o ID no contexto para uso posterior
        ctx.attribute("userId", userId);
    }

    private int validateTokenAndGetUserId(Context ctx, String token) {
        try {

            com.auth0.jwt.interfaces.DecodedJWT decodedJWT = com.auth0.jwt.JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256("segredo-grupo-4"))
                    .withIssuer("api-todo-list-manager")
                    .build()
                    .verify(token);


            String username = decodedJWT.getClaim("username").asString();


            User user = userRepository.findByUsername(username);

            if (user != null) {
                return user.getId();
            }
        } catch (Exception e) {
            logger.error("Erro ao validar token: {}", e.getMessage());
        }
        return -1;
    }
}

