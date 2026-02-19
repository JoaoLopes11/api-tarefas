package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.TodoListsRepository;
import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLTodoListsRepository implements TodoListsRepository {
    private final BasicDataSource dataSource;

    public SQLTodoListsRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int save(TodoList todoList) {
        String sql = "INSERT INTO lists (name, owner_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, todoList.getName());
            stmt.setInt(2, todoList.getOwnerId());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return 0;
    }

    @Override
    public TodoList findById(int listId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM lists WHERE id = ?")) {
            stmt.setInt(1, listId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return new TodoList(rs.getInt("id"), rs.getString("name"), rs.getInt("owner_id"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public List<TodoList> findAllByUserId(int userId) {
        List<TodoList> result = new ArrayList<>();
        String sql = "SELECT * FROM lists WHERE owner_id = ? " +
                "UNION " +
                "SELECT l.* FROM lists l " +
                "JOIN list_shares ls ON l.id = ls.list_id " +
                "WHERE ls.user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            //  uma para o owner_id e outra para o list_shares
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new TodoList(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("owner_id")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    // v. Grava a partilha na nova tabela
    @Override
    public void shareList(int listId, int userId) {
        String sql = "INSERT INTO list_shares (list_id, user_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao partilhar lista", e);
        }
    }

    // v. Verifica se o utilizador tem acesso (é dono OU está na tabela de partilha)
    @Override
    public boolean hasAccess(int listId, int userId) {
        String sql = "SELECT 1 FROM lists WHERE id = ? AND owner_id = ? " +
                "UNION " +
                "SELECT 1 FROM list_shares WHERE list_id = ? AND user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            stmt.setInt(2, userId);
            stmt.setInt(3, listId);
            stmt.setInt(4, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override public List<TodoList> findAll() { return new ArrayList<>(); }
    @Override public void update(TodoList todoList) {}
    @Override public boolean deleteById(int listId) { return false; }
}