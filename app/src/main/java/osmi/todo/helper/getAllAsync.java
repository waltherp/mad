package osmi.todo.helper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.List;

import osmi.todo.entities.TodoEntity;

/**
 * Created by patri on 29.06.2016.
 */
public class getAllAsync extends AsyncTask<Void, Void, List<TodoEntity>> {

    public static final String HTTP_RESPONSE = "httpResponse";
    private Context context;
    private String action;

//    private static getAllAsync instance;
//    public static getAllAsync getInstance() {
//        if(instance == null) instance = new getAllAsync();
//        return instance;
//    }
//
//    private getAllAsync() {
//
//    }

    public getAllAsync(Context context, String action) {
        this.context = context;
        this.action = action;
    }

    @Override
    protected List<TodoEntity> doInBackground(Void... params) {
        return RemoteHelper.getInstance().readAllDataItems();
    }

    @Override
    protected void onPostExecute(List<TodoEntity> todoEntities) {
//        super.onPostExecute(todoEntities);
        Intent intent = new Intent(action);
        TodoPojo todoPojo = new TodoPojo();
        todoPojo.getAll = todoEntities;
        intent.putExtra(HTTP_RESPONSE, todoPojo);
//        intent.putParcelableExtra(HTTP_RESPONSE, todoEntities);

        // broadcast the completion
        context.sendBroadcast(intent);
    }
}
