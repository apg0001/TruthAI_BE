package jpabasic.truthaiserver.dto.answer.openai;

import jpabasic.truthaiserver.dto.answer.Message;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ChatGptRequest {

    private String model;
    private List<Message> messages;
    List<Map<String,Object>> functions;
    Map<String,String> function_call;

    public ChatGptRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user",prompt));
    }

    public ChatGptRequest(String model,List<Message> messages) {
        this.model=model;
        this.messages = messages;
    }

    public ChatGptRequest(String model,List<Message> message,List<Map<String,Object>> functions,Map<String,String> function_call) {
        this.model=model;
        this.messages = message;
        this.functions = functions;
        this.function_call = function_call;
    }


}
