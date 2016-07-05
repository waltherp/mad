package osmi.todo.helper;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import osmi.todo.entities.TodoEntity;

/**
 * Created by patri on 28.06.2016.
 */
public class JsonIO {

    /**
     * ObjectMapper is able to read json objects from input streams and write json objects to output streams
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * JsonFactory is able to create json nodes and to provide json generators from output streams
     */
    private static final JsonFactory JSONFACTORY = new JsonFactory(MAPPER);

    public static JsonNode readJsonNodeFromInputStream(InputStream is)
            throws JsonParseException, JsonMappingException, IOException {

        // read a json node from the input stream
        return MAPPER.readValue(is, JsonNode.class);
    }

    public static void writeJsonNodeToOutputStream(JsonNode node,
                                                   OutputStream os) throws IOException {

        // obtain a json generator for the output stream
        JsonGenerator generator = JSONFACTORY.createJsonGenerator(os,
                JsonEncoding.UTF8);

        // write the object to the stream, using the generator
        generator.writeObject(node);
    }

    /**
     * this is for merely converting a json array node to a list of data items
     *
     * @param arrayNode
     * @return
     */
    public static List<TodoEntity> createDataItemListFromArrayNode(
            ArrayNode arrayNode) {

        List<TodoEntity> itemlist = new ArrayList<TodoEntity>();

        for (int i = 0; i < arrayNode.size(); i++) {
            itemlist.add(createDataItemFromObjectNode((ObjectNode) arrayNode
                    .get(i)));
        }

        return itemlist;
    }

    /**
     * this takes a json object nodes and created a TodoEntity using its attribute values
     *
     * @param objectNode
     * @return
     */
    public static TodoEntity createDataItemFromObjectNode(ObjectNode objectNode) {
        TodoEntity todoEntity = new TodoEntity();
        todoEntity.setId((int) objectNode.get("id").asLong());
        todoEntity.setName(objectNode.get("name").asText());
        todoEntity.setDesc(objectNode.get("description").asText());
        todoEntity.setFinalDate(new Date(objectNode.get("expiry").asLong()));
        todoEntity.setSolved(objectNode.get("done").asBoolean());
        todoEntity.setFav(objectNode.get("favourite").asBoolean());
        return todoEntity;
    }

    /**
     * this, reversely, takes a TodoEntity and creates a json object from it
     *
     * @param item
     * @return
     */
    public static ObjectNode createObjectNodeFromDataItem(TodoEntity item) {

        // JsonNodeFactory offers creation methods for each type of json node
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();

        objectNode.put("id", item.getId());
        objectNode.put("name", item.getName());
        objectNode.put("description", item.getDesc());
        objectNode.put("expiry", item.getFinalDate().getTime());
        objectNode.put("done", item.isSolved());
        objectNode.put("favourite", item.isFav());

        return objectNode;

    }
}
