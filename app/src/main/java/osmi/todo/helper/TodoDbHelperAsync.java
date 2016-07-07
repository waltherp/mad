package osmi.todo.helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import osmi.todo.entities.TodoEntity;

/**
 * Created by patri on 12.06.2016.
 */
public class TodoDbHelperAsync extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Todo.db";
    public static final String TABLE_NAME = "todo_table";
    public static final String COL_ID = "ID";
    public static final String COL_NAME = "NAME";
    public static final String COL_DESC = "DESC";
    public static final String COL_SOLVED = "SOLVED";
    public static final String COL_FAV = "FAV";
    public static final String COL_FINALDATE = "FINALDATE";

    public static final String ACTION_CREATE = "create";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_GET_ALL = "getAll";
    public static final String ACTION_GET_BY_ID = "getById";
    public static final String HTTP_RESPONSE = "httpResponse";

    private RemoteHelper remoteHelper;
    private Context context;
    private boolean isOnline;

    public TodoDbHelperAsync(Context context, boolean isOnline) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
        this.isOnline = isOnline;
        remoteHelper = RemoteHelper.getInstance();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ("+COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COL_NAME+" TEXT, "+COL_DESC+" TEXT, "+COL_SOLVED+" INTEGER, "+COL_FAV+" INTEGER, "+COL_FINALDATE+" INTEGER) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }

    public TodoEntity create(TodoEntity todoEntity) {
        if (validateEntry(todoEntity)) return null;
        if (todoEntity.getId() != 0) {
            return update(todoEntity);
        }
        TodoEntity todoEntityCreate = createLocal(todoEntity);
        if (todoEntityCreate == null) return null;

        if(isOnline) {
            new AsyncTask<TodoEntity, Void, TodoEntity>() {

                @Override
                protected TodoEntity doInBackground(TodoEntity... params) {
                    return remoteHelper.createDataItem(params[0]);
                }
            }.execute(todoEntity);
        }

        return todoEntity;
    }

    private TodoEntity createLocal(TodoEntity todoEntity) {
        if (validateEntry(todoEntity)) return null;
        if (todoEntity.getId() != 0) {
            return updateLocal(todoEntity);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = getContentValues(todoEntity);
        long id = db.insert(TABLE_NAME, null, contentValues);
        if(id == -1) return null;
        todoEntity.setId((int) id);
        return todoEntity;
    }

    @NonNull
    private ContentValues getContentValues(TodoEntity todoEntity) {
        ContentValues contentValues = new ContentValues();
        String name = todoEntity.getName();
        contentValues.put(COL_NAME, name);
        String desc = todoEntity.getDesc();
        contentValues.put(COL_DESC, desc);
        boolean solved = todoEntity.isSolved();
        contentValues.put(COL_SOLVED, solved);
        boolean fav = todoEntity.isFav();
        contentValues.put(COL_FAV, fav);
        Date finalDate = todoEntity.getFinalDate();
        long time = 0;
        if(finalDate != null ) time = finalDate.getTime();
        contentValues.put(COL_FINALDATE, time);
        return contentValues;
    }

    public boolean delete(TodoEntity todoEntity) {
        boolean delete = deleteLocal(todoEntity);

        if(isOnline) {
            if (validateEntry(todoEntity)) return false;
            int id = todoEntity.getId();
            if (validateId(id)) return false;
            new AsyncTask<Integer, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Integer... params) {
                    return remoteHelper.deleteDataItem(params[0]);
                }

            }.execute(id);
        }

        return delete;
    }

    private boolean deleteLocal(TodoEntity todoEntity) {
        if (validateEntry(todoEntity)) return false;
        int id = todoEntity.getId();
        if (validateId(id)) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        int delete = db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
        return delete > 0;
    }

    public TodoEntity update(TodoEntity todoEntity) {
        TodoEntity todoEntityUpdate = updateLocal(todoEntity);
        if (todoEntityUpdate == null) return null;

        if(isOnline) {
            new AsyncTask<TodoEntity, Void, TodoEntity>() {

                @Override
                protected TodoEntity doInBackground(TodoEntity... params) {
                    return remoteHelper.updateDataItem(params[0]);
                }
            }.execute(todoEntity);
        }

        return todoEntity;
    }

    private TodoEntity updateLocal(TodoEntity todoEntity) {
        if (validateEntry(todoEntity)) return null;
        int id = todoEntity.getId();
        if (validateId(id)) return null;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = getContentValues(todoEntity);
        int update = db.update(TABLE_NAME, contentValues, COL_ID + "=?", new String[]{String.valueOf(id)});
        if(update <= 0) return null;
        return todoEntity;
    }

    private boolean validateEntry(TodoEntity todoEntity) {
        return todoEntity == null;
    }

    public TodoEntity getById(int id) {
        if (validateId(id)) return null;
        if(isOnline) {
            new AsyncTask<Integer, Void, TodoEntity>() {

                @Override
                protected TodoEntity doInBackground(Integer... params) {
                    return remoteHelper.readDataItem(params[0]);
                }

                @Override
                protected void onPostExecute(TodoEntity todoEntity) {
                    createLocal(todoEntity);
                }
            }.execute(id);
        }

        return getByIdLocal(id);
    }

    private TodoEntity getByIdLocal(int id) {
        if (validateId(id)) return null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {COL_ID, COL_NAME, COL_DESC, COL_SOLVED, COL_FAV, COL_FINALDATE}, COL_ID+ "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        TodoEntity todoEntity = new TodoEntity();
        todoEntity.setId(cursor.getInt(0));
        todoEntity.setName(cursor.getString(1));
        todoEntity.setDesc(cursor.getString(2));
        todoEntity.setSolved(cursor.getInt(3) > 0);
        todoEntity.setFav(cursor.getInt(4) > 0);
        todoEntity.setFinalDate(new Date(cursor.getInt(5)));

        return todoEntity;
    }

    private boolean validateId(int id) {
        return id == 0;
    }

    public List<TodoEntity> getAll() {
        if(isOnline) {
            new AsyncTask<Void, Void, List<TodoEntity>>() {

                @Override
                protected List<TodoEntity> doInBackground(Void... params) {
                    return remoteHelper.readAllDataItems();
                }

                @Override
                protected void onPostExecute(List<TodoEntity> todoEntities) {
                    for (TodoEntity todoEntity:
                            todoEntities) {
                        createLocal(todoEntity);
                    }
                }
            }.execute();
        }
        return getAllLocal();
    }

    public List<TodoEntity> getAllLocal() {
        String sortedColumn = COL_FAV;
        return getAllLocalWithSortBy(sortedColumn);
    }

    @NonNull
    public List<TodoEntity> getAllLocalWithSortBy(String sortedColumn) {
        List<TodoEntity> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String oppositeColumn = sortedColumn.equals(COL_FAV) ? COL_FINALDATE : COL_FAV;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_SOLVED + ", " + sortedColumn + ", " + oppositeColumn;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                TodoEntity todoEntity = new TodoEntity();
                todoEntity.setId(cursor.getInt(0));
                todoEntity.setName(cursor.getString(1));
                todoEntity.setDesc(cursor.getString(2));
                todoEntity.setSolved(cursor.getInt(3) > 0);
                todoEntity.setFav(cursor.getInt(4) > 0);
                todoEntity.setFinalDate(new Date(cursor.getInt(5)));
                list.add(todoEntity);
            } while (cursor.moveToNext());
        }

        return list;
    }

    public void sync() {

        if(isOnline) {
            new AsyncTask<Void, Void, TodoPojo>() {

                @Override
                protected TodoPojo doInBackground(Void... params) {
                    List<TodoEntity> result = new ArrayList<>();
                    List<TodoEntity> allLocal = getAllLocal();
                    List<TodoEntity> todoEntities = remoteHelper.readAllDataItems();
                    if(allLocal != null && allLocal.size() > 0) {
                        for (TodoEntity todoEntity:
                                todoEntities) {
                            remoteHelper.deleteDataItem(todoEntity.getId());
                        }
                        for (TodoEntity todoEntity:
                                allLocal) {
                            TodoEntity dataItem = remoteHelper.createDataItem(todoEntity);
                            if(dataItem != null) {
                                result.add(dataItem);
                            } else {
                                result.add(todoEntity);
                            }
                        }
                    } else {
                        for (TodoEntity todoEntity:
                                todoEntities) {
                            TodoEntity local = createLocal(todoEntity);
                            if(local == null) {
                                int todoEntityId = todoEntity.getId();
                                if(todoEntityId != 0) {
                                    todoEntity.setId(0);
                                }
                                local = createLocal(todoEntity);
                            }
                            if(local == null) continue;
                            result.add(local);
                        }
                    }
                    TodoPojo todoPojo = new TodoPojo();
                    todoPojo.getAll = result;
                    return todoPojo;
                }

                @Override
                protected void onPostExecute(TodoPojo todoPojo) {
                    Intent intent = new Intent(ACTION_GET_ALL);
                    intent.putExtra(HTTP_RESPONSE, todoPojo);
                    context.sendBroadcast(intent);
                }
            }.execute();
        }
    }

}
