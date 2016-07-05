package osmi.todo.helper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.List;

import osmi.todo.entities.TodoEntity;

/**
 * Created by patri on 29.06.2016.
 */
public class TodoAsyncHelper extends AsyncTask<TodoPojoParams, Void, TodoPojoOutPut> {

    public static final String HTTP_RESPONSE = "httpResponse";
    private Context context;
    private String action;
    private boolean isOnline;

    public TodoAsyncHelper(Context context, String action, boolean isOnline) {
        this.context = context;
        this.action = action;
        this.isOnline = isOnline;
    }

    @Override
    protected TodoPojoOutPut doInBackground(TodoPojoParams... params) {
        TodoPojoParams param = params[0];
        TodoPojoOutPut todoPojoOutPut= new TodoPojoOutPut();
        TodoDbHelperAsync todoDbHelperAsync= new TodoDbHelperAsync(context, isOnline);
        if(action.equals(TodoDbHelperAsync.ACTION_CREATE)){
            todoPojoOutPut.create = todoDbHelperAsync.create(param.createTodoEntity);
        } else if(action.equals(TodoDbHelperAsync.ACTION_UPDATE)){
            todoPojoOutPut.update = todoDbHelperAsync.update(param.updateTodoEntity);
        } else if(action.equals(TodoDbHelperAsync.ACTION_DELETE)){
            todoPojoOutPut.delete = todoDbHelperAsync.delete(param.deleteTodoEntity);
        } else if(action.equals(TodoDbHelperAsync.ACTION_GET_ALL)){
            todoPojoOutPut.getAll = todoDbHelperAsync.getAll();
        } else if(action.equals(TodoDbHelperAsync.ACTION_GET_BY_ID)){
            todoPojoOutPut.getById = todoDbHelperAsync.getById(param.getByIdId);
        }
        return todoPojoOutPut;
    }

    @Override
    protected void onPostExecute(TodoPojoOutPut todoPojoOutPut) {
        Intent intent = new Intent(action);
        intent.putExtra(HTTP_RESPONSE, todoPojoOutPut);
        context.sendBroadcast(intent);
    }
}
