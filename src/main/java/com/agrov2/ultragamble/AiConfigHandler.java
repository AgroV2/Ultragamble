package com.agrov2.ultragamble;

import com.agrov2.ultragamble.entities.BotUser;
import com.agrov2.ultragamble.entities.UserRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiConfigHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    public AiConfigHandler(TelegramClient telegramClient, UserRepository userRepository) {
        this.telegramClient = telegramClient;
        this.userRepository = userRepository;
    }

    public void handleCommand(Message msg) {
        long userId = msg.getFrom().getId();
        BotUser user = getOrCreate(userId, msg.getFrom().getFirstName());
        send(SendMessage.builder()
                .chatId(String.valueOf(msg.getChatId()))
                .text(menuText(user))
                .parseMode("HTML")
                .replyMarkup(menuKeyboard(userId))
                .build());
    }

    public void handleCallback(CallbackQuery cq) {
        String[] parts = cq.getData().split(":");
        String action = parts[1];
        long ownerId = Long.parseLong(parts[parts.length - 1]);

        if (cq.getFrom().getId() != ownerId) { answer(cq, "Это не твоё меню."); return; }

        BotUser user = getOrCreate(ownerId, cq.getFrom().getFirstName());
        Message src = (Message) cq.getMessage();
        long chatId = src.getChatId();
        int messageId = src.getMessageId();

        switch (action) {
            case "menu" -> edit(chatId, messageId, menuText(user), menuKeyboard(ownerId));
            case "apilist" -> edit(chatId, messageId, "Выбери API:", apiKeyboard(ownerId));
            case "modellist" -> {
                AiProvider p = AiProvider.fromName(user.getAiProvider());
                edit(chatId, messageId, "Модели для " + p.getDisplayName() + ":", modelKeyboard(p, ownerId));
            }
            case "setapi" -> {
                AiProvider p = AiProvider.values()[Integer.parseInt(parts[2])];
                user.setAiProvider(p.name());
                user.setAiModel(p.getDefaultModel());
                userRepository.save(user);
                edit(chatId, messageId, menuText(user), menuKeyboard(ownerId));
                answer(cq, "API: " + p.getDisplayName());
            }
            case "setmodel" -> {
                AiProvider p = AiProvider.fromName(user.getAiProvider());
                String chosen = p.getModels().get(Integer.parseInt(parts[2]));
                user.setAiModel(chosen);
                userRepository.save(user);
                edit(chatId, messageId, menuText(user), menuKeyboard(ownerId));
                answer(cq, "Модель: " + chosen);
            }
            default -> answer(cq, "");
        }
    }

    private String menuText(BotUser user) {
        AiProvider p = AiProvider.fromName(user.getAiProvider());
        return "<b>Настройки AI</b>\n\n" +
                "Текущий API: <b>" + p.getDisplayName() + "</b>\n" +
                "Текущая модель: <b>" + safeModel(user, p) + "</b>\n\nЧто меняем?";
    }

    private String safeModel(BotUser user, AiProvider p) {
        String m = user.getAiModel();
        return (m == null || !p.getModels().contains(m)) ? p.getDefaultModel() : m;
    }

    private InlineKeyboardMarkup menuKeyboard(long uid) {
        var api = InlineKeyboardButton.builder().text("🔌 API").callbackData("aicfg:apilist:" + uid).build();
        var model = InlineKeyboardButton.builder().text("🧠 Модель").callbackData("aicfg:modellist:" + uid).build();
        return InlineKeyboardMarkup.builder().keyboardRow(new InlineKeyboardRow(api, model)).build();
    }

    private InlineKeyboardMarkup apiKeyboard(long uid) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (AiProvider p : AiProvider.values()) {
            rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text(p.getDisplayName())
                    .callbackData("aicfg:setapi:" + p.ordinal() + ":" + uid).build()));
        }
        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text("⬅️ Назад").callbackData("aicfg:menu:" + uid).build()));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private InlineKeyboardMarkup modelKeyboard(AiProvider p, long uid) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        List<String> models = p.getModels();
        for (int i = 0; i < models.size(); i++) {
            rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text(models.get(i))
                    .callbackData("aicfg:setmodel:" + i + ":" + uid).build()));
        }
        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text("⬅️ Назад").callbackData("aicfg:menu:" + uid).build()));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private BotUser getOrCreate(long userId, String name) {
        return userRepository.findById(userId).orElseGet(() -> {
            BotUser u = new BotUser();
            u.setUserId(userId);
            u.setDisplayName(name);
            return userRepository.save(u);
        });
    }

    private void edit(long chatId, int messageId, String text, InlineKeyboardMarkup markup) {
        send(EditMessageText.builder().chatId(String.valueOf(chatId)).messageId(messageId)
                .text(text).parseMode("HTML").replyMarkup(markup).build());
    }

    private void answer(CallbackQuery cq, String text) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder().callbackQueryId(cq.getId()).text(text).build());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void send(SendMessage m) { try { telegramClient.execute(m); } catch (Exception e) { e.printStackTrace(); } }
    private void send(EditMessageText m) { try { telegramClient.execute(m); } catch (Exception e) { e.printStackTrace(); } }
}