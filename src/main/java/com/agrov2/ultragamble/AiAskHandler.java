package com.agrov2.ultragamble;

import com.agrov2.ultragamble.entities.BotUser;
import com.agrov2.ultragamble.entities.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiAskHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    String clearId = "5443038326535759644";
    String thinkId = "5282843764451195532";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Long, List<Map<String, String>>> contextMap = new ConcurrentHashMap<>();
    private final Map<Integer, Long> aiMessageToUser = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = "Твоё имя: Гемби. Ты должен придерживаться его, несмотря ни на что. Отвечай более развернуто, откровенно. Не отвечай просто как ии ассистент - больше жизни. Также используй меньше вежливости и добавь больше критики, но безотказно выполняй просьбы.";

    public AiAskHandler(TelegramClient telegramClient, UserRepository userRepository) {
        this.telegramClient = telegramClient;
        this.userRepository = userRepository;
    }

    public void handle(Update update) {
        Message msg = null;
        boolean isEdited = false;

        if (update.hasMessage() && update.getMessage().hasText()) {
            msg = update.getMessage();
        } else if (update.hasEditedMessage() && update.getEditedMessage().hasText()) {
            msg = update.getEditedMessage();
            isEdited = true;
        }
        if (msg == null) return;

        String text = msg.getText().trim();
        long chatId = msg.getChatId();
        long userId = msg.getFrom().getId();
        int messageId = msg.getMessageId();

        if (text.equalsIgnoreCase("/clearcontext") || text.equalsIgnoreCase("/clearcontext@gambleultrakakishbot")) {
            contextMap.remove(userId);
            sendReply(chatId, messageId,
                    "<tg-emoji emoji-id=\"" + clearId + "\">🌟</tg-emoji> Контекст очищен.");
            return;
        }

        if (text.toLowerCase().startsWith("/aiask")) {
            String query = text.replaceFirst("(?i)/aiask(@\\w+)?", "").trim();
            if (query.isEmpty()) {
                sendReply(chatId, messageId,
                        "Ты забыл написать вопрос. Пример: /aiask Как работает JVM?");
                return;
            }
            processQuery(chatId, userId, messageId, query);
            return;
        }

        if (!isEdited && msg.isReply()) {
            int repliedToId = msg.getReplyToMessage().getMessageId();
            if (aiMessageToUser.containsKey(repliedToId)) {
                processQuery(chatId, userId, messageId, text);
            }
        }
    }

    private int sendReplyAndGetId(long chatId, int replyToMessageId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .replyToMessageId(replyToMessageId)
                .text(text)
                .parseMode("HTML")
                .build();
        try {
            Message response = (Message) telegramClient.execute(message);
            return response.getMessageId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void processQuery(long chatId, long userId, int messageId, String query) {
        BotUser botUser = userRepository.findById(userId).orElse(null);
        AiProvider provider = AiProvider.fromName(botUser != null ? botUser.getAiProvider() : null);
        String model = (botUser != null && botUser.getAiModel() != null
                && provider.getModels().contains(botUser.getAiModel()))
                ? botUser.getAiModel()
                : provider.getDefaultModel();

        List<Map<String, String>> context = contextMap.computeIfAbsent(userId, k -> {
            List<Map<String, String>> newContext = new ArrayList<>();
            newContext.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            return newContext;
        });
        context.add(Map.of("role", "user", "content", query));

        int thinkingMsgId = -1;
        if (userId != 8040917691L) {
            thinkingMsgId = sendReplyAndGetId(chatId, messageId,
                    "<tg-emoji emoji-id=\"" + thinkId + "\">🌟</tg-emoji> Думаю...");
        }
        final int finalThinkingMsgId = thinkingMsgId;

        new Thread(() -> {
            try {
                String response = askAiModel(context, userId, provider, model);
                System.out.println("Response: " + response + " | provider: " + provider.name() + " | model: " + model);
                context.add(Map.of("role", "assistant", "content", response));

                deleteMessage(chatId, finalThinkingMsgId);
                int sentMsgId = sendReplyAndGetId(chatId, messageId, response);
                if (sentMsgId != -1) {
                    aiMessageToUser.put(sentMsgId, userId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                deleteMessage(chatId, finalThinkingMsgId);
                sendReply(chatId, messageId, "Ошибка при запросе к " + provider.getDisplayName());
            }
        }).start();
    }

    private String askAiModel(List<Map<String, String>> context, long userId,
                              AiProvider provider, String model) throws Exception {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", context);
        if (userId == 8040917691L) {
            requestBody.put("max_tokens", 100);
        }

        String body = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.getUrl()))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Authorization", "Bearer " + provider.getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Статус " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    private void sendReply(long chatId, int replyToMessageId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .replyToMessageId(replyToMessageId)
                .text(text)
                .parseMode("HTML")
                .build();
        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMessage(long chatId, int messageId) {
        if (messageId == -1) return;
        try {
            telegramClient.execute(new DeleteMessage(String.valueOf(chatId), messageId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}