package osmi.todo.helper;

import java.io.Serializable;
import java.util.List;

import osmi.todo.entities.TodoEntity;

/**
 * Created by patri on 29.06.2016.
 */
public class TodoPojoParams implements Serializable {
    public TodoEntity createTodoEntity;
    public TodoEntity deleteTodoEntity;
    public TodoEntity updateTodoEntity;
    public int getByIdId;


}
