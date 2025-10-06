package com.ms.base;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

//使用okhttp原生调用DeepSeek
public class DeepSeekApiExample {
    private static final String API_KEY = "sk-98522d44c2a44d1298786af9a9711097"; // 替换为你的实际API密钥
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    public static void main(String[] args) {
        // 配置超时时间
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS) // 关键：调大读取超时
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        // 构建JSON请求体
        String jsonBody = "{\n" +
                "  \"model\": \"deepseek-chat\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"你是一位专业的Java开发，用简洁明了的方式回答问题\"},\n" +
                "    {\"role\": \"user\", \"content\": \"解释一下什么是springAI!\"}\n" +
                "  ],\n" +
                "  \"stream\": false\n" +
                "}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        // 构建请求
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 打印响应体
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
