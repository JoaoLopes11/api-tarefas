package cncs.academy.ess;

import cncs.academy.ess.repository.TodoListsRepository;
import cncs.academy.ess.repository.TodoRepository;
import cncs.academy.ess.repository.sql.SQLTodoListsRepository;
import cncs.academy.ess.repository.sql.SQLTodoRepository;
import org.apache.commons.dbcp2.BasicDataSource;
import cncs.academy.ess.controller.AuthorizationMiddleware;
import cncs.academy.ess.controller.TodoController;
import cncs.academy.ess.controller.TodoListController;
import cncs.academy.ess.controller.UserController;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.repository.sql.SQLUserRepository;
import cncs.academy.ess.service.DuplicateUserException;
import cncs.academy.ess.service.TodoListsService;
import cncs.academy.ess.service.TodoUserService;
import cncs.academy.ess.service.TodoService;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.Javalin;
import cncs.academy.ess.repository.memory.InMemoryUserRepository;
import cncs.academy.ess.repository.memory.InMemoryTodoListsRepository;
import cncs.academy.ess.repository.memory.InMemoryTodoRepository;

import java.security.NoSuchAlgorithmException;

public class App {
        public static void main(String[] args) throws NoSuchAlgorithmException, DuplicateUserException {
          /*  SslPlugin sslPlugin = new SslPlugin(ssl -> {
                ssl.host = "0.0.0.0";
                ssl.insecurePort = 7100;
                ssl.securePort = 8443;
                ssl.pemFromPath("cert.pem", "key.pem");
            });*/


            Javalin app = Javalin.create(config -> {
              //  config.registerPlugin(sslPlugin);
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(it -> it.anyHost());
                });
            }).start();

       BasicDataSource dbConfig = new BasicDataSource();
        dbConfig.setUrl("jdbc:postgresql://localhost:5432/postgres");
        dbConfig.setUsername("postgres");
        dbConfig.setPassword("changeit");

      /*  //para testes insql
        UserRepository userRepository = new SQLUserRepository(dbConfig);
        TodoListsRepository listsRepository = new SQLTodoListsRepository(dbConfig);
        TodoRepository todoRepository = new Sdocker tag todo-service o-teu-utilizador/todo-serviceQLTodoRepository(dbConfig);*/

         //para testes inmemory
        UserRepository userRepository = new InMemoryUserRepository();
        TodoListsRepository listsRepository = new InMemoryTodoListsRepository();
        TodoRepository todoRepository = new InMemoryTodoRepository();

        TodoUserService userService = new TodoUserService(userRepository);
        UserController userController = new UserController(userService);

        TodoListsService toDoListService = new TodoListsService(listsRepository);
        TodoListController todoListController = new TodoListController(toDoListService);

        TodoService todoService = new TodoService(todoRepository, listsRepository);
        TodoController todoController = new TodoController(todoService, toDoListService);

        AuthorizationMiddleware authMiddleware = new AuthorizationMiddleware(userRepository);

        // Authorization middleware
            app.before(ctx -> {
                String path = ctx.path();
                // Se for login ou criar utilizador, não corre o middleware
                if (path.equals("/login") || (path.equals("/user") && ctx.method().equals("POST"))) {
                    return;
                }
                // Para todas as outras rotas, corre a verificação
                authMiddleware.handle(ctx);
            });

        // User management
        app.post("/user", userController::createUser);
        app.get("/user/{userId}", userController::getUser);
        app.delete("/user/{userId}", userController::deleteUser);
        app.post("/login", userController::loginUser);

        app.post("/todolist", todoListController::createTodoList);
        app.get("/todolist", todoListController::getAllTodoLists);
        app.get("/todolist/{listId}", todoListController::getTodoList);

        app.post("/todolist/{id}/share", ctx -> {
            int listId = Integer.parseInt(ctx.pathParam("id"));
            int currentUserId = ctx.attribute("userId"); //
            int targetUserId = Integer.parseInt(ctx.body()); //

            toDoListService.shareListWithUser(listId, currentUserId, targetUserId);
            ctx.status(201).result("Lista partilhada com sucesso!");
        });


        app.post("/todo/item", todoController::createTodoItem);
        /* GET /todo/1/tasks */
        app.get("/todo/{listId}/tasks", todoController::getAllTodoItems);
        /* GET /todo/1/tasks/1 */
        app.get("/todo/{listId}/tasks/{taskId}", todoController::getTodoItem);
        /* DELETE /todo/1/tasks/1 */
        app.delete("/todo/{listId}/tasks/{taskId}", todoController::deleteTodoItem);

    }
}
