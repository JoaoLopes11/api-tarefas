package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.Todo;
import cncs.academy.ess.repository.TodoRepository;
import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLTodoRepository implements TodoRepository {
    private final BasicDataSource dataSource;

    public SQLTodoRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int save(Todo todo) {
        String sql = "INSERT INTO todos (description, completed, list_id) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, todo.getDescription());
            stmt.setBoolean(2, todo.isCompleted());
            stmt.setInt(3, todo.getListId());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return 0;
    }

    @Override
    public List<Todo> findAllByListId(int listId) {
        List<Todo> items = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos WHERE list_id = ?")) {
            stmt.setInt(1, listId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new Todo(rs.getInt("id"), rs.getString("description"), rs.getBoolean("completed"), rs.getInt("list_id")));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return items;
    }

    @Override public Todo findById(int id) { return null; }
    @Override public List<Todo> findAll() { return new ArrayList<>(); }
    @Override public void update(Todo todo) {}
    @Override public boolean deleteById(int id) { return false; }
}