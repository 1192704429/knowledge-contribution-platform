package com.kp.mall.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 通用大模型聊天请求帮助类
 * - 支持同步完整返回
 * - 支持 SSE 流式返回
 * 配置通过 application.yml 的 ai-chat.* 注入
 */
@Component
@Slf4j
public class AiChatClient {

    private static final String CONTENT_TYPE = "application/json; charset=utf-8";

    private static final String DONE = "[DONE]";

    @Value("${ai-chat.api-url}")
    private String apiUrl;

    @Value("${ai-chat.api-key}")
    private String apiKey;

    @Value("${ai-chat.model}")
    private String model;

    /**
     * 同步请求：仅传一个 user 内容，内部构造 messages，等待完整回答
     */
    public String chat(String userContent) {
        return chat(null, userContent);
    }

    /**
     * 同步请求：可传 system + user，两条消息
     */
    public String chat(String systemContent, String userContent) {
        List<ChatMessage> messages = new ArrayList<>();
        if (systemContent != null && !systemContent.isEmpty()) {
            ChatMessage systemMsg = new ChatMessage();
            systemMsg.setRole("system");
            systemMsg.setContent(systemContent);
            messages.add(systemMsg);
        }
        ChatMessage userMsg = new ChatMessage();
        userMsg.setRole("user");
        userMsg.setContent(userContent);
        messages.add(userMsg);
        return chat(messages);
    }

    /**
     * 同步请求：自定义 messages
     */
    public String chat(List<ChatMessage> messages) {
        try {
            ChatRequest request = new ChatRequest();
            request.setModel(model);
            request.setMessages(messages);
            request.setStream(false);
            Map<String, Object> thinking = new HashMap<>();
            thinking.put("type", "disabled");
            request.setThinking(thinking);

            log.info("AiChat 同步请求, url:{}, model:{}, messages:{}", apiUrl, model, JSONUtil.toJsonStr(messages));

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", CONTENT_TYPE);
            headers.put("Authorization", "Bearer " + apiKey);

            HttpResponse response = HttpRequest.post(apiUrl)
                    .addHeaders(headers)
                    .body(JSONUtil.toJsonStr(request))
                    .timeout(60000)
                    .execute();

            String body = response.body();
            log.info("AiChat 同步请求, status:{}, body:{}", response.getStatus(), body);

            if (!response.isOk()) {
                log.error("AiChat 同步请求失败, status:{}, body:{}", response.getStatus(), body);
                return null;
            }

            ChatResponse chatResponse = JSONUtil.toBean(body, ChatResponse.class);
            if (chatResponse == null || CollectionUtils.isEmpty(chatResponse.getChoices())) {
                return null;
            }
            ChatChoice choice = chatResponse.getChoices().get(0);
            if (choice.getMessage() != null) {
                return choice.getMessage().getContent();
            }
            if (choice.getDelta() != null) {
                return choice.getDelta().getContent();
            }
            return null;
        } catch (Exception e) {
            log.error("AiChat 同步请求异常", e);
            return null;
        }
    }

    /**
     * SSE 流式请求：仅传一个 user 内容
     */
    public SseEmitter streamChat(String userContent) {
        return streamChat(null, userContent);
    }

    /**
     * SSE 流式请求：可传 system + user
     */
    public SseEmitter streamChat(String systemContent, String userContent) {
        SseEmitter emitter = new SseEmitter(60_000L);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                List<ChatMessage> messages = new ArrayList<>();
                if (systemContent != null && !systemContent.isEmpty()) {
                    ChatMessage systemMsg = new ChatMessage();
                    systemMsg.setRole("system");
                    systemMsg.setContent(systemContent);
                    messages.add(systemMsg);
                }
                ChatMessage userMsg = new ChatMessage();
                userMsg.setRole("user");
                userMsg.setContent(userContent);
                messages.add(userMsg);

                ChatRequest request = new ChatRequest();
                request.setModel(model);
                request.setMessages(messages);
                request.setStream(true);
                Map<String, Object> thinking = new HashMap<>();
                thinking.put("type", "disabled");
                request.setThinking(thinking);

                log.info("AiChat 流式请求, url:{}, model:{}, messages:{}", apiUrl, model, JSONUtil.toJsonStr(messages));

                OkHttpClient client = initOkHttpClient();
                EventSource.Factory factory = EventSources.createFactory(client);

                Request httpRequest = new Request.Builder()
                        .url(apiUrl)
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .addHeader("Content-Type", CONTENT_TYPE)
                        .post(okhttp3.RequestBody.create(
                                JSONUtil.toJsonStr(request),
                                okhttp3.MediaType.parse(CONTENT_TYPE)))
                        .build();

                factory.newEventSource(httpRequest, new EventSourceListener() {
                    @Override
                    public void onEvent(@NotNull EventSource eventSource,
                                        @Nullable String id,
                                        @Nullable String type,
                                        @NotNull String data) {
                        log.info("AiChat 流式响应: {}", data);
                        if (DONE.equals(data)) {
                            try {
                                emitter.send(SseEmitter.event()
                                        .data("")
                                        .id(String.valueOf(System.currentTimeMillis()))
                                        .name("close"));
                            } catch (IOException e) {
                                log.error("AiChat 流式关闭事件发送失败", e);
                            }
                            emitter.complete();
                            return;
                        }
                        try {
                            ChatResponse chatResponse = JSONUtil.toBean(data, ChatResponse.class);
                            if (chatResponse == null || CollectionUtils.isEmpty(chatResponse.getChoices())) {
                                return;
                            }
                            ChatChoice choice = chatResponse.getChoices().get(0);
                            if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                                String content = choice.getDelta().getContent();
                                emitter.send(SseEmitter.event()
                                        .data(content)
                                        .id(String.valueOf(System.currentTimeMillis()))
                                        .name("ai-message"));
                            }
                        } catch (Exception e) {
                            log.error("AiChat 流式响应解析异常", e);
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull EventSource eventSource,
                                          @Nullable Throwable t,
                                          @Nullable Response response) {
                        log.error("AiChat 流式请求失败", t);
                        emitter.completeWithError(t != null ? t : new RuntimeException("SSE failure"));
                    }
                });
            } catch (Exception e) {
                log.error("AiChat 流式请求异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private static OkHttpClient initOkHttpClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(12);
        return new OkHttpClient
                .Builder()
                .connectTimeout(1000, TimeUnit.MILLISECONDS)
                .writeTimeout(16000, TimeUnit.MILLISECONDS)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .dispatcher(dispatcher)
                .build();
    }

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }

    @Data
    public static class ChatRequest {
        private String model;
        private List<ChatMessage> messages;
        private boolean stream;
        /**
         * DeepSeek 扩展字段：关闭 thinking 模式
         * 请求体中会包含：
         * "thinking": { "type": "disabled" }
         */
        private Map<String, Object> thinking;
    }

    @Data
    public static class ChatResponse {
        private List<ChatChoice> choices;
    }

    @Data
    public static class ChatChoice {
        private ChatMessage delta;
        private ChatMessage message;
    }
}

