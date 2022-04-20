package Messages;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class MessageDeserializer implements JsonDeserializer<BaseMessage> {

    @Override
    public BaseMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        TextMessage textMessage = new TextMessage(object.get("messageID").getAsString(),object.get("conversationID").getAsString(),object.get("senderToken").getAsString(),object.get("messageType").getAsInt());
        textMessage.setQuotedMessageID(object.get("quotedMessageID").getAsString());
        textMessage.setReadTime(object.get("readTime").getAsString());
        textMessage.setSenderName(object.get("senderName").getAsString());
        textMessage.setArrivingTime("now");
        return textMessage;
    }
}
