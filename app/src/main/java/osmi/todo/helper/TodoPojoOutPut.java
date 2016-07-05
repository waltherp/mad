package osmi.todo.helper;

import java.io.Serializable;
import java.util.List;

import osmi.todo.entities.TodoEntity;

/**
 * Created by patri on 29.06.2016.
 */
public class TodoPojoOutPut implements Serializable {
    public List<TodoEntity> getAll;
    public TodoEntity create;
    public boolean delete;
    public TodoEntity update;
    public TodoEntity getById;


}
