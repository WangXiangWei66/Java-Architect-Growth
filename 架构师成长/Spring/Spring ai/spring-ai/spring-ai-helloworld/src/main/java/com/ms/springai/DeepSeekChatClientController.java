package com.ms.springai;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;


@RequestMapping("/deepseek")
@RestController
public class DeepSeekChatClientController {

    private final DeepSeekChatModel chatModel;

    @Autowired
    public DeepSeekChatClientController(DeepSeekChatModel chatModel) {
        this.chatModel = chatModel;
    }

    // http://127.0.0.1:8080/deepseek/ai/generate?message=%E8%A7%A3%E9%87%8A%E4%B8%80%E4%B8%8B%E4%BB%80%E4%B9%88%E6%98%AFspringAI!
    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", chatModel.call(message));
    }
    // http://127.0.0.1:8080/deepseek/ai/generateStream?message=%E8%A7%A3%E9%87%8A%E4%B8%80%E4%B8%8B%E4%BB%80%E4%B9%88%E6%98%AFspringAI!
    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        var prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }
    // http://127.0.0.1:8080/deepseek/ai/generateJavaCode?message=%E8%AF%B7%E5%86%99%E4%B8%80%E4%B8%AA%E5%BF%AB%E9%80%9F%E6%8E%92%E5%BA%8F
    @GetMapping("/ai/generateJavaCode")
    public String generateJavaCode(@RequestParam(value = "message", defaultValue = "请写快速排序代码") String message) {
        UserMessage userMessage = new UserMessage(message);
        Message assistantMessage = DeepSeekAssistantMessage.prefixAssistantMessage("```java\\n");
        Prompt prompt = new Prompt(List.of(userMessage, assistantMessage), ChatOptions.builder().stopSequences(List.of("```")).build());
        ChatResponse response = chatModel.call(prompt);
        System.out.println(response.getResult().getOutput());
        return response.getResult().getOutput().getText();
    }
}
