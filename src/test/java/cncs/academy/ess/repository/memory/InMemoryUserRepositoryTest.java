package cncs.academy.ess.repository.memory;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {
    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void saveAndFindById_ShouldWork() {
        User user = new User("jane", "password");
        int id = repository.save(user);
        User savedUser = repository.findById(id);
        assertNotNull(savedUser);
        assertEquals("jane", savedUser.getUsername());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        repository.save(new User("user1", "p1"));
        repository.save(new User("user2", "p2"));
        List<User> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        int id = repository.save(new User("deleteMe", "p"));
        repository.deleteById(id);
        assertNull(repository.findById(id));
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        repository.save(new User("target", "p"));
        User found = repository.findByUsername("target");
        assertNotNull(found);
        assertEquals("target", found.getUsername());
    }

    @Test
    void findByUsername_ShouldReturnNullIfNotFound() {
        User found = repository.findByUsername("nonexistent");
        assertNull(found);
    }
}