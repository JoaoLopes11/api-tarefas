package cncs.academy.ess.service;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.TodoListsRepository;

import java.util.Collection;

public class TodoListsService {
    TodoListsRepository todoListsRepository;

    public TodoListsService(TodoListsRepository todoListsRepository) {
        this.todoListsRepository = todoListsRepository;
    }

    public TodoList createTodoListItem(String listName, int ownerId) {
        TodoList list = new TodoList(listName, ownerId);
        int listId = todoListsRepository.save(list);
        list.setId(listId);
        return list;
    }
    public TodoList getTodoList(int listId) {
        return todoListsRepository.findById(listId);
    }
    public Collection<TodoList> getAllTodoLists(int userId) {
        return todoListsRepository.findAllByUserId(userId);
    }

    public void shareListWithUser(int listId, int currentUserId, int targetUserId) {
        TodoList list = todoListsRepository.findById(listId);
        if (list != null && list.getOwnerId() == currentUserId) {
            todoListsRepository.shareList(listId, targetUserId);
        } else {
            throw new RuntimeException("Apenas o dono pode partilhar esta lista");
        }
    }

    public boolean hasAccess(int listId, int userId) {
        return todoListsRepository.hasAccess(listId, userId);
    }
}
