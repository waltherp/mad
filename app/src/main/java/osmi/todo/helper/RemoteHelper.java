package osmi.todo.helper;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import osmi.todo.entities.TodoEntity;

/**
 * Created by patri on 28.06.2016.
 */
public class RemoteHelper {

    private static RemoteHelper instance;

    public static RemoteHelper getInstance() {
        if (instance == null) instance = new RemoteHelper();
        return instance;
    }

    private RemoteHelper() {

    }

    protected static String logger = TodoDbHelper.class
            .getSimpleName();

    private String baseUrl = "http://10.0.2.2:8080/api/todos";

    public List<TodoEntity> readAllDataItems() {

        Log.i(logger, "readAllItems(): baseUrl: " + baseUrl);

        HttpURLConnection con = null;
        InputStream is = null;
        try {
            // obtain a http url connection from the base url
            con = (HttpURLConnection) (new URL(baseUrl))
                    .openConnection();
            Log.d(logger, "readAllItems(): got connection: " + con);
            // set the request method (GET is default anyway...)
            con.setRequestMethod("GET");
            // then initiate sending the request...
            is = con.getInputStream();
            // check the response code
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // and create a json node from the input stream
                JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
                // then transform the node into a list of DataItem objects
                List<TodoEntity> items = JsonIO
                        .createDataItemListFromArrayNode((ArrayNode) json);
                Log.i(logger, "readAllItems(): " + items);

                return items;
            } else {
                Log.e(logger,
                        "readAllItems(): got response code: "
                                + con.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(logger, "readAllItems(): got exception: " + e);
        }
        finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(con != null) con.disconnect();
        }

        return new ArrayList<TodoEntity>();

    }

    public TodoEntity createDataItem(TodoEntity item) {
        Log.i(logger, "createItem(): " + item);

        try {
            // obtain a http url connection from the base url
            HttpURLConnection con = (HttpURLConnection) (new URL(baseUrl))
                    .openConnection();
            Log.d(logger, "createItem(): got connection: " + con);
            // set the request method
            con.setRequestMethod("POST");
            // indicate that we want to send a request body
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            // obtain the output stream and write the item as json object to it
            OutputStream os = con.getOutputStream();
            os.write(createJsonStringFromDataItem(item).getBytes());
            // then initiate sending the request...
            InputStream is = con.getInputStream();
            // check the response code
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // and create a json node from the input stream
                JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
                Log.d(logger, "createItem(): got json: " + json);
                // then transform the node into a DataItem object
                item = JsonIO.createDataItemFromObjectNode((ObjectNode) json);
                Log.i(logger, "createItem(): " + item);

                return item;
            } else {
                Log.e(logger,
                        "createItem(): got response code: "
                                + con.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(logger, "createItem(): got exception: " + e);
        }

        return null;
    }

    public boolean deleteDataItem(long itemId) {
        Log.i(logger, "deleteItem(): " + itemId);

        HttpURLConnection con = null;
        InputStream is = null;
        try {
            // obtain a http url connection from the base url
            con = (HttpURLConnection) (new URL(baseUrl + "/"
                    + itemId)).openConnection();
            Log.d(logger, "deleteItem(): got connection: " + con);
            // set the request method
            con.setRequestMethod("DELETE");
            con.setDoOutput(true);
            con.setRequestProperty(
                    "Content-Type", "application/json" );
            // then initiate sending the request...
            is = con.getInputStream();
            // check the response code
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // and create a json node from the input stream
                JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
                Log.d(logger, "deleteItem(): got json: " + json + " of class: "
                        + json.getClass());
                // then transform the node into a DataItem object

                return ((BooleanNode) json).asBoolean();
            } else {
                Log.e(logger,
                        "deleteItem(): got response code: "
                                + con.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(logger, "deleteItem(): got exception: " + e);
        }
        finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(con != null) con.disconnect();
        }

        return false;
    }

    /**
     * the only difference from create is the PUT method, i.e. the common
     * functionality could be factored out...
     */
    public TodoEntity updateDataItem(TodoEntity item) {
        Log.i(logger, "updateItem(): " + item);

        HttpURLConnection con = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            // obtain a http url connection from the base url
            con = (HttpURLConnection) (new URL(baseUrl))
                    .openConnection();
            Log.d(logger, "updateItem(): got connection: " + con);
            // set the request method
            con.setRequestMethod("PUT");
            // indicate that we want to send a request body
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            // obtain the output stream and write the item as json object to it
            os = con.getOutputStream();
            os.write(createJsonStringFromDataItem(item).getBytes());
            // then initiate sending the request...
            is = con.getInputStream();
            // check the response code
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // and create a json node from the input stream
                JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
                Log.d(logger, "updateItem(): got json: " + json);
                // then transform the node into a TodoEntity object
                item = JsonIO.createDataItemFromObjectNode((ObjectNode) json);
                Log.i(logger, "updateItem(): " + item);

                return item;
            } else {
                Log.e(logger,
                        "updateItem(): got response code: "
                                + con.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(logger, "updateItem(): got exception: " + e);
        }
        finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(con != null) con.disconnect();
        }

        return null;
    }

    /**
     * create a string from the data item's json representation
     *
     * @param item
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected String createJsonStringFromDataItem(TodoEntity item)
            throws UnsupportedEncodingException, IOException {

        // create a json node from the
        ObjectNode jsonNode = JsonIO.createObjectNodeFromDataItem(item);
        Log.i(logger, "created jsonNode: " + jsonNode + " from item: " + item);
        // serialise the object
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JsonIO.writeJsonNodeToOutputStream(jsonNode, os);

        // create a string entity from the output stream, using utf-8 character
        // encoding
        return os.toString();
    }

    public TodoEntity readDataItem(long dateItemId) {
        Log.i(logger, "readDataItem(): " + dateItemId);

        HttpURLConnection con = null;
        InputStream is = null;
        try {
            // obtain a http url connection from the base url
            con = (HttpURLConnection) (new URL(baseUrl + "/"
                    + dateItemId)).openConnection();
            Log.d(logger, "readDataItem(): got connection: " + con);
            // set the request method
            con.setRequestMethod("GET");
            // then initiate sending the request...
            is = con.getInputStream();
            // check the response code
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // and create a json node from the input stream
                JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
                Log.d(logger, "readDataItem(): got json: " + json);
                // then transform the node into a TodoEntity object
                TodoEntity item = JsonIO.createDataItemFromObjectNode((ObjectNode) json);
                Log.i(logger, "readDataItem(): " + item);

                return item;

//                JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
//                Log.d(logger, "deleteItem(): got json: " + json + " of class: "
//                        + json.getClass());
//                // then transform the node into a DataItem object
//
//                return ((BooleanNode) json).asBoolean();
            } else {
                Log.e(logger,
                        "deleteItem(): got response code: "
                                + con.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(logger, "deleteItem(): got exception: " + e);
        }
        finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(con != null) con.disconnect();
        }
        return null;
//        throw new UnsupportedOperationException(
//                "readDataItem() currently not supported by " + this.getClass());
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl + "dataitems";
    }
}
