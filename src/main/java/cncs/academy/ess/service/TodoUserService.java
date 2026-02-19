package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import com.auth0.jwt.JWT; // Isto só funciona se o Maven carregar
import com.auth0.jwt.algorithms.Algorithm; // Isto resolve o erro da imagem 714fb2

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class TodoUserService {
    private final UserRepository repository;

    public TodoUserService(UserRepository userRepository) {
        this.repository = userRepository;
    }

    public User addUser(String username, String password) throws Exception {
        if (repository.findByUsername(username) != null) {
            throw new DuplicateUserException("Username already exists");
        }

        // 1. alinea a
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();

        String securePassword = Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);

        User user = new User(username, securePassword);
        int id = repository.save(user);
        user.setId(id);
        return user;
    }


    public boolean verifyPassword(String passwordDigitada, String passwordBD) throws Exception {
        String[] parts = passwordBD.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] storedHash = Base64.getDecoder().decode(parts[1]);

        KeySpec spec = new PBEKeySpec(passwordDigitada.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] testHash = factory.generateSecret(spec).getEncoded();

        return Arrays.equals(storedHash, testHash);
    }

    public String login(String username, String password) throws Exception {
        User user = repository.findByUsername(username);
        // alinea a
        if (user != null && verifyPassword(password, user.getPassword())) {
            // Adicionamos o prefixo conforme pedido no enunciado
            return "Bearer " + createAuthToken(user);
        }
        return null;
    }

    // 1. alinea b
    private String createAuthToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256("segredo-grupo-4");

        return JWT.create()
                .withIssuer("api-todo-list-manager")
                .withClaim("username", user.getUsername())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
                .sign(algorithm);
    }

    public User getUser(int id) {
        return repository.findById(id);
    }

    public void deleteUser(int id) {
        repository.deleteById(id);
    }
}