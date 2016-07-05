package osmi.todo.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import osmi.todo.entities.TodoEntity;

/**
 * Created by patri on 12.06.2016.
 */
public class TodoDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Todo.db";
    public static final String TABLE_NAME = "todo_table";
    public static final String COL_ID = "ID";
    public static final String COL_NAME = "NAME";
    public static final String COL_DESC = "DESC";
    public static final String COL_SOLVED = "SOLVED";
    public static final String COL_FAV = "FAV";
    public static final String COL_FINALDATE = "FINALDATE";

    private RemoteHelper remoteHelper;

    public TodoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        remoteHelper = RemoteHelper.getInstance();
//        SQLiteDatabase db = this.getWritableDatabase();
//        onUpgrade(db, 0,0);
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
        if (todoEntity == null) {
            return null;
        }
        if (todoEntity.getId() != 0) {
            return update(todoEntity);
        }
        SQLiteDatabase db = this.getWritableDatabase();
//        onUpgrade(db, 0,0);
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
        if (todoEntity == null) {
            return false;
        }
        int id = todoEntity.getId();
        if (id == 0) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        int delete = db.delete(TABLE_NAME, COL_ID+"=?", new String[]{String.valueOf(id)});
        return delete > 0;
    }

    public TodoEntity update(TodoEntity todoEntity) {
        if (todoEntity == null) {
            return null;
        }
        int id = todoEntity.getId();
        if (id == 0) {
            return null;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = getContentValues(todoEntity);
        int update = db.update(TABLE_NAME, contentValues, COL_ID + "=?", new String[]{String.valueOf(id)});
        if(update <= 0) return null;
        return todoEntity;
    }

    public TodoEntity getById(int id) {
        if (id == 0) {
            return null;
        }
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

    public List<TodoEntity> getAll(Context context, String action) {
        new getAllAsync(context, action).execute();
//        remoteHelper.execute("readAllDataItems");
//        return remoteHelper.readAllDataItems;

        List<TodoEntity> list = new ArrayList<>();
//        SQLiteDatabase db = this.getWritableDatabase();
//        String selectQuery = "SELECT * FROM " + TABLE_NAME;
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        if (cursor.moveToFirst()) {
//            do {
//                TodoEntity todoEntity = new TodoEntity();
//                todoEntity.setId(cursor.getInt(0));
//                todoEntity.setName(cursor.getString(1));
//                todoEntity.setDesc(cursor.getString(2));
//                todoEntity.setSolved(cursor.getInt(3) > 0);
//                todoEntity.setFav(cursor.getInt(4) > 0);
//                todoEntity.setFinalDate(new Date(cursor.getInt(5)));
//                list.add(todoEntity);
//            } while (cursor.moveToNext());
//        }
//
        return list;
    }

}
