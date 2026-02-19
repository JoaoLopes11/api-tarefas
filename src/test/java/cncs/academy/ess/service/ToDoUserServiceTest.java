package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TodoUserServiceTest {
    private UserRepository userRepositoryMock;
    private TodoUserService todoUserService;

    @BeforeEach
    void setUp() {
        userRepositoryMock = mock(UserRepository.class);
        todoUserService = new TodoUserService(userRepositoryMock);
    }

    @Test
    void login_shouldReturnValidJWTTokenWhenCredentialsMatch() throws Exception {

        String username = "goncalo";
        String password = "correct_password";

        // password encriptada real (salt:hash)
        TodoUserService helper = new TodoUserService(userRepositoryMock);
        User tempUser = helper.addUser(username, password);

        // mockUser com a password segura
        User mockUser = new User(username, tempUser.getPassword());

        when(userRepositoryMock.findByUsername(username)).thenReturn(mockUser);


        String token = todoUserService.login(username, password);


        assertNotNull(token, "O login falhou.");

        // Verifica o prefixo "Bearer "
        assertTrue(token.startsWith("Bearer "), "O token deve começar com 'Bearer'");

        //Verifica a estrutura JWT
        String jwtContent = token.replace("Bearer ", "");
        String[] jwtParts = jwtContent.split("\\.");
        assertEquals(3, jwtParts.length, "O JWT deve ter exatamente 3 partes");
    }
}