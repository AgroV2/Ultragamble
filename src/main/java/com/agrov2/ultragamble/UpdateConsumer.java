package com.agrov2.ultragamble;

import com.agrov2.ultragamble.entities.BotUser;
import com.agrov2.ultragamble.entities.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    String completeEmojiId = "5206607081334906820";
    String incompleteEmojiId = "5210952531676504517";
    String warningEmojiId = "5447644880824181073";
    String superwarningEmojiId = "5420323339723881652";
    String diamondEmojiId = "5427168083074628963";
    String starEmojiId = "5438496463044752972";
    String restrictionEmojiId = "5260293700088511294";
    String dollarEmojiId = "5409048419211682843";
    String plusEmojiId = "5397916757333654639";
    String star1EmojiId = "5287285615333763802";
    String star2EmojiId = "5287613613396225173";
    String star3EmojiId = "5289708110032694075";
    String star4EmojiId = "5289537672845487622";
    String star5EmojiId = "5289932526368876363";
    String star6EmojiId = "5289537771629736374";
    String star7EmojiId = "5289623941558592680";
    String wow1EmojiId = "5231492190567427455";
    String wow2EmojiId = "5233596853391484977";
    String wow3EmojiId = "5233332382190304339";
    String wow4EmojiId = "5233267768702299887";
    String wow5EmojiId = "5231251599384407947";
    String star8EmojiId = "5287316685127181297";
    private final java.util.Set<String> successfulCatches = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private final UserRepository userRepository;
    private final TelegramClient telegramClient;
    private final AiAskHandler aiAskHandler;
    private final AiConfigHandler aiConfigHandler;


    public UpdateConsumer(
            UserRepository userRepository,
            TelegramClient telegramClient,
            AiAskHandler aiAskHandler,
            AiConfigHandler aiConfigHandler
    ) {
        this.userRepository = userRepository;
        this.telegramClient = telegramClient;
        this.aiAskHandler = aiAskHandler;
        this.aiConfigHandler = aiConfigHandler;
    }






    @SneakyThrows
    @Override
    public void consume(Update update) {
       


        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Long blockId = update.getMessage().getFrom().getId();
            if (blockId.equals(1259088578L)) {
                return;
            }
            if (messageText.startsWith("/aiconfig")) {
                aiConfigHandler.handleCommand(update.getMessage());
                return;
            }



            if (messageText.equals("/gamblehelp" ) || messageText.equals("/gamblehelp@gambleultrakakishbot")) {
                sendMessage(chatId, "Приветствую! Чтобы начать, напиши команду /gamble. " +
                        "ВНИМАНИЕ: Данный бот НЕ использует реальные денежные средства для обработки " +
                        "команд. По всем вопросам и фидбекам пишите @AgroV2");
            } else if (messageText.equals("/gamble") || messageText.equals("/gamble@gambleultrakakishbot")) {
                Long userId = update.getMessage().getFrom().getId();
                String userName = update.getMessage().getFrom().getFirstName();


                SendMessage sm = SendMessage.builder()
                        .chatId(chatId)
                        .text("Загрузка меню...")
                        .build();

                var sentMessage = telegramClient.execute(sm);

                sendGambleMenu(chatId, sentMessage.getMessageId(), userId, userName);
            } else if (messageText.equals("/gambletop") || messageText.equals("/gambletop@gambleultrakakishbot")) {
                sendLeaderboard(chatId);
            } else if (messageText.startsWith("/setbalance")) {
                Long adminId = update.getMessage().getFrom().getId();



                if (!adminId.equals(5019071424L)) {
                    sendMessage(chatId, "<tg-emoji emoji-id=\"" + incompleteEmojiId + "\">❌</tg-emoji> У вас нет прав администратора для этой команды.");
                    return;
                }


                try {
                    String[] parts = messageText.split(" ");
                    if (parts.length != 3) {
                        sendMessage(chatId, "⚠️ Формат: `/setbalance [ID_пользователя] [сумма]`");
                        return;
                    }

                    Long targetUserId = Long.parseLong(parts[1]);
                    long newBalance = Long.parseLong(parts[2]);


                    userRepository.findById(targetUserId).ifPresentOrElse(user -> {
                        user.setBalance(newBalance);
                        userRepository.save(user);
                        sendMessage(chatId, "✅ Баланс пользователя " + targetUserId + " изменен на " + newBalance + " UC.");
                    }, () -> sendMessage(chatId, "❌ Пользователь с таким ID не найден в базе."));

                } catch (NumberFormatException e) {
                    sendMessage(chatId, "❌ Ошибка: ID и сумма должны быть числами.");
                }
            } else if (messageText.equals("/pve") || messageText.equals("/pve@gambleultrakakishbot")) {
                Long userId = update.getMessage().getFrom().getId();
                String userName = update.getMessage().getFrom().getFirstName();


                SendMessage sm = SendMessage.builder()
                        .chatId(chatId)
                        .text("Загрузка PVE меню...")
                        .build();


                var sentMessage = telegramClient.execute(sm);


                sendPveMenu(chatId, sentMessage.getMessageId(), userId, userName);
            } else if (messageText.equals("/yourstats") || messageText.equals("/yourstats@gambleultrakakishbot")) {
                Long userId = update.getMessage().getFrom().getId();


                BotUser user = userRepository.findById(userId).orElseGet(() -> {
                    BotUser newUser = new BotUser();
                    newUser.setUserId(userId);
                    newUser.setBalance(1000);
                    return userRepository.save(newUser);
                });

                sendStats(chatId, user);
            } else if (messageText.equals("/shop") || messageText.equals("/shop@gambleultrakakishbot")) {
                Long userId = update.getMessage().getFrom().getId();
                String userName = update.getMessage().getFrom().getFirstName();


                SendMessage sm = SendMessage.builder()
                        .chatId(chatId)
                        .text("Загрузка магазина...")
                        .build();

                var sentMessage = telegramClient.execute(sm);

                sendGambleShop(chatId, sentMessage.getMessageId(), userId);
            }
            else if (messageText.equals("/inventory") || messageText.equals("/inventory@gambleultrakakishbot")) {
                Long userId = update.getMessage().getFrom().getId();
                String userName = update.getMessage().getFrom().getFirstName();


                SendMessage sm = SendMessage.builder()
                        .chatId(chatId)
                        .text("🎒 Открываем инвентарь...")
                        .build();

                var sent = telegramClient.execute(sm);
                sendInventoryMenu(chatId, sent.getMessageId(), userId, userName);
            } else if (messageText.equals("/resetcd")) {
                Long adminId = update.getMessage().getFrom().getId();


                if (!adminId.equals(5019071424L)) {
                    sendMessage(chatId, "<tg-emoji emoji-id=\"" + incompleteEmojiId + "\">❌</tg-emoji> У вас нет прав администратора для этой команды.");
                    return;
                }



                BotUser user = userRepository.findById(adminId).orElseThrow();


                user.setLastFarmTime(0L);
                user.setLastFishingTime(0L);
                user.setLastBossfightTime(0L);
                user.setLastGambleTime(0L);
                user.setLastFiftyFiftyTime(0L);
                user.setLastPokerTime(0L);

                userRepository.save(user);

                sendMessage(chatId, "✅ Все ваши кулдауны успешно сброшены!");
            } else if (messageText.startsWith("/setmaxhp")) {
                Long adminId = update.getMessage().getFrom().getId();



                if (!adminId.equals(5019071424L)) {
                    sendMessage(chatId, "<tg-emoji emoji-id=\"" + incompleteEmojiId + "\">❌</tg-emoji> У вас нет прав администратора для этой команды.");
                    return;
                }


                try {
                    String[] parts = messageText.split(" ");
                    if (parts.length != 3) {
                        sendMessage(chatId, "⚠️ Формат: `/setmaxhp [ID_пользователя] [значение макс хп]`");
                        return;
                    }

                    Long targetUserId = Long.parseLong(parts[1]);
                    int newMaxHp = Integer.parseInt(parts[2]);


                    userRepository.findById(targetUserId).ifPresentOrElse(user -> {
                        user.setMaxHp(newMaxHp);
                        userRepository.save(user);
                        sendMessage(chatId, "✅ Макс ХП пользователя " + targetUserId + " изменено на " + newMaxHp + " ХП.");
                    }, () -> sendMessage(chatId, "❌ Пользователь с таким ID не найден в базе."));

                } catch (NumberFormatException e) {
                    sendMessage(chatId, "❌ Ошибка: ID и новое макс хп должны быть числами.");
                }
            } else if (messageText.startsWith("/say")) {
                if (update.getMessage().getFrom().getId().equals(5019071424L)) {
                    try {

                        String[] parts = messageText.split("\\s+", 3);

                        if (parts.length < 3) {
                            sendMessage(chatId, "⚠️ Формат: /say <ID> <текст>");
                            return;
                        }

                        String targetChatId = parts[1].trim();
                        String messageToDeliver = parts[2];

                        SendMessage msg = SendMessage.builder()
                                .chatId(targetChatId)
                                .text(messageToDeliver)
                                .build();

                        telegramClient.execute(msg);
                        sendMessage(chatId, "✅ Отправлено в: " + targetChatId);
                    } catch (Exception e) {
                        sendMessage(chatId, "❌ Ошибка: " + e.toString());
                    }
                }
            }



           else    if (update.hasEditedMessage() && update.getEditedMessage().hasText()) {
                String editedText = update.getEditedMessage().getText();
                if (editedText.trim().toLowerCase().startsWith("/aiask")
                        || update.getEditedMessage().isReply()) {
                    aiAskHandler.handle(update);
                }
                return;
            }

            if (!update.hasMessage() || !update.getMessage().hasText()) return;
            if (messageText.startsWith("/aiask")
                    || messageText.equalsIgnoreCase("/clearcontext")
                    || messageText.equalsIgnoreCase("/clearcontext@gambleultrakakishbot")
                    || update.getMessage().isReply()) {
                aiAskHandler.handle(update);
            }






        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        Long clickingUserId = callbackQuery.getFrom().getId();
        String clickingUserName = (callbackQuery.getFrom().getUserName() != null)
                ? callbackQuery.getFrom().getUserName()
                : callbackQuery.getFrom().getFirstName();
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long userId = callbackQuery.getFrom().getId();

        String rawData = callbackQuery.getData();


        String[] parts = rawData.split(":");
        String action = parts[0];

        if (parts.length > 1) {

            Long ownerId = Long.parseLong(parts[parts.length - 1]);

            if (!clickingUserId.equals(ownerId)) {
                sendAlert(callbackQuery.getId(), clickingUserName + ", это не ваше меню!");
                return;
            }
        }





        String userMention = (callbackQuery.getFrom().getUserName() != null)
                ? callbackQuery.getFrom().getUserName()
                : callbackQuery.getFrom().getFirstName();





        long currentTime = Instant.now().getEpochSecond();


        BotUser user = userRepository.findById(userId).orElseGet(() -> {
            BotUser newUser = new BotUser();
            newUser.setUserId(userId);
            newUser.setBalance(1000);
            return userRepository.save(newUser);
        });
        user.setDisplayName(userMention);
        userRepository.save(user);
        if (data.startsWith("upx:")) {

            String[] partss = data.split(":");
            int count = Integer.parseInt(partss[1]);
            Long targetUserId = Long.parseLong(partss[2]);


            buyFarmUpgrade(user, chatId, messageId, count, callbackQuery.getId());
        }
        switch (action) {
            case "gamble" -> {
                if (currentTime - user.getLastGambleTime() < 30) {
                    sendAlert(callbackQuery.getId(), "Кулдаун: " + (30 - (currentTime - user.getLastGambleTime())) + " сек.");
                } else {
                    processGamble(user, chatId, userMention, callbackQuery.getId(), messageId);
                }
            }
            case "farm" -> {
                if (currentTime - user.getLastFarmTime() < 4 * 3600) {
                    sendAlert(callbackQuery.getId(), "Работать можно раз в 4 часа!");
                } else {
                    processFarm(user, chatId, userMention, callbackQuery.getId(), messageId);
                }
            }
            case "50/50" -> {
                if (currentTime - user.getLastFiftyFiftyTime() < 60) {
                    sendAlert(callbackQuery.getId(), "Кулдаун: " + (60 - (currentTime - user.getLastFiftyFiftyTime())) + " сек.");
                } else {
                    processFiftyFifty(user, chatId, userMention, callbackQuery.getId(), messageId);
                }
            }
            case "mult_menu" -> {
                Long lastTime = user.getLastMultiplyTime();


                if (lastTime != null && (currentTime - lastTime < 60)) {
                    sendAlert(callbackQuery.getId(), "Кулдаун: " + (60 - (currentTime - lastTime)) + " сек.");
                } else {
                    sendMultiplierMenu(user, chatId, messageId, clickingUserId, clickingUserName);
                }
            }


            case "ps" -> {
                handlePokerSwap(chatId, messageId, parts[1], parts[2], parts[3], clickingUserId);
            }
            case "pf" -> {
                handlePokerFinal(user, chatId, messageId, parts[1], callbackQuery.getId());
            }
            case "ai_menu" -> sendAiBattleMenu(chatId, messageId, clickingUserId, user);
            case "ai_start" -> handleAiBattleStart(chatId, messageId, clickingUserId, user);
            case "ai_hit" -> handleAiBattleHit(chatId, messageId, clickingUserId, user);
            case "ai_ignore" -> {} 
            case "p_start" -> {
                if (currentTime - user.getLastPokerTime() < 80) {
                    sendAlert(callbackQuery.getId(), "Кулдаун: " + (80 - (currentTime - user.getLastPokerTime())) + " сек.");
                } else {
                    processPoker(user, chatId, callbackQuery.getId(), messageId);
                }
            }
            case "shop_p" -> sendPotionShop(chatId, messageId, clickingUserId);

            case "buy_p" -> {
                int level = Integer.parseInt(parts[1]);
                handleBuy(user, chatId, messageId, "p", level, callbackQuery.getId(), false);
            }
            case "prg" ->{
                sendPrestigeMenu(chatId, messageId, userId);
            }
            case "pru" -> {
                PrestigeUpgrade(user, callbackQuery.getId());
            }


            case "back" -> {
                sendGambleMenu(chatId, messageId, clickingUserId, clickingUserName);
                answerCallback(callbackQuery.getId());
            }
            case "nextgm" -> {
                sendGambleMenu2(chatId, messageId, clickingUserId, clickingUserName);
                answerCallback(callbackQuery.getId());
            }
            case "back3" -> {
                sendGambleShop(chatId, messageId, userId);
                answerCallback(callbackQuery.getId());
            }
            case "f_start" -> {
                if (currentTime - user.getLastFishingTime() < 1800) {
                    sendAlert(callbackQuery.getId(), "Кулдаун: " + (1800 - (currentTime - user.getLastFishingTime())) + " сек.");
                } else {
                    processFishing(user, chatId, messageId, clickingUserId);
                }
            }
            case "b_items" -> {


                sendCombatInventory(user, chatId, messageId, parts[1], parts[2], parts[3], parts[4], parts[5], callbackQuery.getId(), parts[6]);
            }

            case "use_potion" -> {

                handleUsePotion(user, chatId, messageId, parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], callbackQuery.getId());
            }

            case "b_resume" -> {

                renderBattleFrame(user, chatId, messageId, parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], "Возвращение в бой...");
            }
            case "f_pull" -> {

                handleFishingPull(user, chatId, messageId);
            }
            case "shop_menu" -> sendShopMenu(chatId, messageId, clickingUserId);
            case "boss_menu" -> sendBossSelectionMenu(user, chatId, messageId);

            case "b_start" -> {


                long lastFight = (user.getLastBossfightTime() == null) ? 0L : user.getLastBossfightTime();

                long cooldown = 900;


                if (currentTime - lastFight < cooldown) {
                    long remaining = cooldown - (currentTime - lastFight);
                    sendAlert(callbackQuery.getId(), "⏳ Кулдаун: " + remaining + " сек.");
                } else {

                    int bossId = Integer.parseInt(parts[1]);
                    startBossFight(user, chatId, messageId, bossId);
                }
            }


            case "alert_lock" -> {
                sendAlert(callbackQuery.getId(), "Этот босс закрыт! Сначала победите предыдущего.");
            }
            case "b_hit" -> {









                handleBossHit(user, chatId, messageId, parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
            }
            case "ai_items" -> sendAiCombatInventory(chatId, messageId, clickingUserId, user, callbackQuery.getId());

            case "ai_items_back" -> {
                AiBattleState st = activeAiBattles.get(clickingUserId);
                if (st != null) renderAiBattleFrame(chatId, messageId, clickingUserId, user, st, "⬅️ Возврат к бою.");
            }

            case "ai_use_potion" -> handleAiUsePotion(chatId, messageId, clickingUserId, user);

            case "shop_w" -> sendWeaponShop(chatId, messageId, clickingUserId);
            case "shop_w2" -> sendWeaponShop2(chatId, messageId, clickingUserId);
            case "shop_a" -> sendArmorShop(chatId, messageId, clickingUserId);

            case "buy_w" -> {
                int level = Integer.parseInt(parts[1]);
                handleBuy(user, chatId, messageId, "w", level, callbackQuery.getId(), false);
            }
            case "buy_a" -> {
                int level = Integer.parseInt(parts[1]);
                handleBuy(user, chatId, messageId, "a", level, callbackQuery.getId(), false);
            }
            case "back2" -> sendPveMenu(chatId, messageId, clickingUserId, clickingUserName);
            case "inv_w" -> sendInventoryCategory(chatId, messageId, clickingUserId, "w");
            case "inv_a" -> sendInventoryCategory(chatId, messageId, clickingUserId, "a");
            case "inv_back" -> sendInventoryMenu(chatId, messageId, userId, clickingUserName);
            case "mines_menu" -> sendMinesMenu(chatId, messageId, clickingUserId, user);

            case "mines_start" -> {
                int size = Integer.parseInt(parts[1]);
                handleMinesStart(chatId, messageId, clickingUserId, user, size);
            }

            case "mines_click" -> {
                int cellIndex = Integer.parseInt(parts[1]);
                handleMinesClick(chatId, messageId, clickingUserId, user, cellIndex);
            }

            case "mines_stop" -> handleMinesStop(chatId, messageId, clickingUserId, user);

            case "mines_ignore" -> {}

            case "equip" -> {

                String type = parts[1];


                int level = Integer.parseInt(parts[2]);


                handleBuy(user, chatId, messageId, type, level, callbackQuery.getId(), true);
            }
            case "upfm" -> sendUpgradeFarmMenu(user, chatId, messageId, userId);
            case "bj_menu" -> sendBlackjackMenu(chatId, messageId, clickingUserId, user);

            case "bj_start" -> {
                long bet = Long.parseLong(parts[1]);
                handleBlackjackStart(chatId, messageId, clickingUserId, user, bet);
            }

            case "bj_hit" -> handleBlackjackHit(chatId, messageId, clickingUserId, user);

            case "bj_stand" -> handleBlackjackStand(chatId, messageId, clickingUserId, user, null);





            default -> {
                if (action.startsWith("mult_bet_")) {

                    String betValue = action.replace("mult_bet_", "");
                    long bet = Long.parseLong(betValue);

                    processMultiplier(user, chatId, userMention, callbackQuery.getId(), messageId, bet);
                }
            }
        }
    }

    @PostConstruct
    public void clearOldUpdates() {
        try {

            GetUpdates getUpdates = new GetUpdates();

            getUpdates.setOffset(-1);

            List<Update> updates = telegramClient.execute(getUpdates);

            if (!updates.isEmpty()) {


                int lastUpdateId = updates.get(updates.size() - 1).getUpdateId();

                GetUpdates confirmUpdates = new GetUpdates();
                confirmUpdates.setOffset(lastUpdateId + 1);
                telegramClient.execute(confirmUpdates);

                System.out.println("Старые сообщения очищены.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при очистке очереди: " + e.getMessage());
        }
    }


    @SneakyThrows
    private void answerCallback(String callbackQueryId) {
        telegramClient.execute(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build());
    }

    @SneakyThrows
    private void sendAlert(String callbackQueryId, String text) {
        telegramClient.execute(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .showAlert(true)
                .build());
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();
        telegramClient.execute(message);
    }
    @SneakyThrows
    private void editMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup markup) {
        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();
        try {
            telegramClient.execute(edit);
        } catch (Exception e) {

            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
    }

    @SneakyThrows
    private void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .build();
        telegramClient.execute(deleteMessage);
    }

    private void processFarm(BotUser user, Long chatId, String mention, String queryId, Integer msgId) {

        long bonus = (user.getFarmUpgrade() == null) ? 0L : user.getFarmUpgrade();

        long reward = 500 + (bonus * 500);
        user.setBalance(user.getBalance() + reward);
        user.setLastFarmTime(Instant.now().getEpochSecond());
        userRepository.save(user);

        String text = "Вы поработали! 💰 +" + reward + " UC (бонус: " + bonus + ")\nБаланс: " + user.getBalance() + " UC";

        var backBtn = InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + user.getUserId()).build();
        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(backBtn))));
        answerCallback(queryId);
    }
    private void buyFarmUpgrade(BotUser user, Long chatId, Integer msgId, int count, String queryId) {

        Long nextPriceObj = user.getNextUpgradePrice();
        long currentPrice = (nextPriceObj == null || nextPriceObj == 0) ? 1000L : nextPriceObj;

        Long currentUpgradeObj = user.getFarmUpgrade();
        long currentUpgrade = (currentUpgradeObj == null) ? 0L : currentUpgradeObj;

        long totalCost = 0;
        long tempPrice = currentPrice;


        for (int i = 0; i < count; i++) {
            totalCost += tempPrice;
            tempPrice += 250;
        }

        if (user.getBalance() < totalCost) {

            String errorText = "❌ Недостаточно UC!\nСтоимость x" + count + ": " + totalCost + " UC\nВаш баланс: " + user.getBalance();
            var backBtn = InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("upfm:" + user.getUserId()).build();
            editMessage(chatId, msgId, errorText, new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(backBtn))));
            answerCallback(queryId);
            return;
        }



        user.setBalance(user.getBalance() - totalCost);
        user.setFarmUpgrade(currentUpgrade + count);
        user.setNextUpgradePrice(tempPrice);


        userRepository.save(user);

        sendUpgradeFarmMenu(user, chatId, msgId, user.getUserId());
        answerCallback(queryId);
    }
    private String evaluateHand(List<String> hand) {
        List<Integer> values = hand.stream()
                .map(c -> {

                    String rankStr = c.replaceAll("[hdcs♥️♦️♣️♠️♥♦♣♠]", "");

                    return switch (rankStr) {
                        case "J" -> 11;
                        case "Q" -> 12;
                        case "K" -> 13;
                        case "A" -> 14;
                        default -> Integer.parseInt(rankStr);
                    };
                })
                .sorted()
                .toList();


        List<String> suits = hand.stream()
                .map(c -> {
                    String s = c.substring(c.length() - 1);

                    if (s.equals("♥️") || s.equals("♥")) return "h";
                    if (s.equals("♦️") || s.equals("♦")) return "d";
                    if (s.equals("♣️") || s.equals("♣")) return "c";
                    if (s.equals("♠️") || s.equals("♠")) return "s";
                    return s;
                })
                .toList();


        boolean isFlush = suits.stream().distinct().count() == 1;


        boolean isStraight = true;
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i + 1) - values.get(i) != 1) {
                isStraight = false;
                break;
            }
        }


        var groups = values.stream().collect(java.util.stream.Collectors.groupingBy(v -> v, java.util.stream.Collectors.counting()));
        var counts = groups.values().stream().sorted(java.util.Comparator.reverseOrder()).toList();


        if (isFlush && isStraight && values.get(0) == 10) return "РОЯЛЬ-ФЛЕШ (x50)";
        if (isFlush && isStraight) return "СТРИТ-ФЛЕШ (x25)";
        if (counts.get(0) == 4) return "КАРЕ (x10)";
        if (counts.get(0) == 3 && counts.get(1) == 2) return "ФУЛЛ-ХАУС (x5)";
        if (isFlush) return "ФЛЕШ (x3)";
        if (isStraight) return "СТРИТ (x2)";
        if (counts.get(0) == 3) return "СЕТ (x1.7)";
        if (counts.get(0) == 2 && counts.get(1) == 2) return "ДВЕ ПАРЫ (x1.5)";
        if (counts.get(0) == 2) return "ПАРА (x1.2)";

        return "НИЧЕГО (x0)";
    }
    @SneakyThrows
    private void handlePokerSwap(Long chatId, Integer messageId, String indexStr, String handData, String swapCountStr, Long userId) {
        int swapsDone = Integer.parseInt(swapCountStr);
        int index = Integer.parseInt(indexStr);


        if (swapsDone >= 2) {


            return;
        }

        int newSwapsCount = swapsDone + 1;
        List<String> hand = new ArrayList<>(List.of(handData.split(",")));


        String[] suits = {"♥️", "♦️", "♣️", "♠️"};
        String[] ranks = {"7", "8", "9", "10", "J", "Q", "K", "A"};
        hand.set(index, ranks[ThreadLocalRandom.current().nextInt(ranks.length)] + suits[ThreadLocalRandom.current().nextInt(suits.length)]);


        String newHandData = String.join(",", hand);
        List<InlineKeyboardRow> rows = new ArrayList<>();


        if (newSwapsCount < 2) {
            List<InlineKeyboardButton> cardButtons = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                cardButtons.add(InlineKeyboardButton.builder()
                        .text("♻️ " + (i + 1))
                        .callbackData("ps:" + i + ":" + newHandData + ":" + newSwapsCount + ":" + userId)
                        .build());
            }
            rows.add(new InlineKeyboardRow(cardButtons));
        }

        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text("🃏 ВСКРЫВАЕМСЯ")
                .callbackData("pf:" + newHandData + ":" + userId)
                .build()));

        String limitText = (newSwapsCount == 2) ? "\n\n🚫 *Лимит замен исчерпан!*" : "\n\nИспользовано замен: *" + newSwapsCount + "/2*";

        EditMessageText edit = EditMessageText.builder()

                .chatId(chatId.toString())
                .messageId(messageId)
                .text("🃏 *ПОКЕР*\n\nВаши карты:\n`[" + String.join("] [", hand) + "]`" + limitText)
                .parseMode("Markdown")
                .replyMarkup(new InlineKeyboardMarkup(rows))
                .build();
        String visualHand = handData.replace("h", "♥️").replace("d", "♦️").replace("c", "♣️").replace("s", "♠️");

        telegramClient.execute(edit);
    }
    @SneakyThrows
    private void handlePokerFinal(BotUser user, Long chatId, Integer msgId, String handData, String queryId) {
        List<String> hand = List.of(handData.split(","));
        String combo = evaluateHand(hand);
        int cost;
        if (user.getGambleUpgrade() == 0) {
            cost = 500;
        } else {
            cost = 5000;
        }

        double multiplier = 0;
        if (combo.contains("x50")) multiplier = 50;
        else if (combo.contains("x25")) multiplier = 25;
        else if (combo.contains("x10")) multiplier = 10;
        else if (combo.contains("x5")) multiplier = 5;
        else if (combo.contains("x3")) multiplier = 3;
        else if (combo.contains("x2")) multiplier = 2;
        else if (combo.contains("x1.7")) multiplier = 1.7;
        else if (combo.contains("x1.5")) multiplier = 1.5;
        else if (combo.contains("x1.2")) multiplier = 1.2;

        long win = (long) (cost * multiplier);
        user.setBalance(user.getBalance() + win);
        userRepository.save(user);


        String visualHand = "[" + String.join("] [", hand)
                .replace("h", "♥️").replace("d", "♦️").replace("c", "♣️").replace("s", "♠️") + "]";

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text("🏁 *ИТОГ ПОКЕРА*\n\nРука: `" + visualHand + "`\nРезультат: *" + combo + "*\n\nВыплата: *" + win + " UC*")
                .parseMode("Markdown")
                .replyMarkup(new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + user.getUserId()).build()
                ))))
                .build();
        telegramClient.execute(edit);
    }




    private List<String> generateHand(int count) {
        String[] suits = {"h", "d", "c", "s"};
        String[] ranks = {"7", "8", "9", "10", "J", "Q", "K", "A"};
        List<String> hand = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            hand.add(ranks[ThreadLocalRandom.current().nextInt(ranks.length)] + suits[ThreadLocalRandom.current().nextInt(suits.length)]);
        }
        return hand;
    }

    @SneakyThrows
    private void processPoker(BotUser user, Long chatId, String queryId, Integer msgId) {
        user.setLastPokerTime(Instant.now().getEpochSecond());
        int price;
        if (user.getGambleUpgrade() == 0) {
            price = 500;
        } else {
            price = 5000;
        }
        if (user.getBalance() < price) {
            sendAlert(queryId, "Нужно " + price + " UC!");
            return;
        }
        user.setBalance(user.getBalance() - price);
        userRepository.save(user);


        List<String> hand = generateHand(5);
        String handStr = String.join(" ", hand);


        List<InlineKeyboardRow> rows = new ArrayList<>();
        List<InlineKeyboardButton> cardButtons = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cardButtons.add(InlineKeyboardButton.builder()
                    .text("♻️ " + (i + 1))
                    .callbackData("ps:" + i + ":" + String.join(",", hand) + ":0:" + user.getUserId())
                    .build());
        }
        rows.add(new InlineKeyboardRow(cardButtons));
        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text("🃏 ВСКРЫВАЕМСЯ")
                .callbackData("pf:" + String.join(",", hand) + ":" + user.getUserId())
                .build()));

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text("🃏 *ПОКЕР* (Ставка: " + price + ")\n\nВаши карты:\n`" + handStr + "`\n\n" +
                        "h - ♥, d - ♦️, c - ♣️, s - ♠️" +
                        "Нажмите на номер карты, чтобы пометить её на замену, затем нажмите кнопку ниже.")
                .parseMode("Markdown")
                .replyMarkup(new InlineKeyboardMarkup(rows))
                .build();
        telegramClient.execute(edit);
    }
    private void sendStats(Long chatId, BotUser user) {

        int hp = (user.getHp() == null) ? 100 : user.getHp();
        int maxHp = (user.getMaxHp() == null) ? 100 : user.getMaxHp();
        int atk = (user.getAttack() == null) ? 10 : user.getAttack();
        int def = (user.getDefense() == null) ? 5 : user.getDefense();
        String weapon = (user.getWeapon() == null) ? "Кулаки" : user.getWeapon();
        String armor = (user.getArmor() == null) ? "Рубаха" : user.getArmor();

        String statsText = "👤 ПРОФИЛЬ ИГРОКА: " + user.getDisplayName() + "\n\n" +
                "💰 Баланс: " + user.getBalance() + " UC\n" +
                "❤️ Здоровье: " + hp + "/" + maxHp + "\n" +
                "⚔️ Атака: " + atk + "\n" +
                "🛡 Защита: " + def + "\n\n" +
                "💫Престиж: " + user.getGambleUpgrade() + "\n" +
                "🛠 СНАРЯЖЕНИЕ:\n" +
                "🗡 Оружие: " + weapon + "\n" +
                "🛡 Броня: " + armor + "\n";




        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(statsText)
                .parseMode("HTML")
                .build();

        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SneakyThrows
    private void sendMultiplierMenu(BotUser user, Long chatId, Integer messageId, Long userId, String userName) {


        String text = "Игрок " + userName + ", выберите ставку для игры в Множитель:";

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .build();


        List<Long> bets = List.of(50L, 100L, 300L, 500L, 1000L, 2500L);
        if (user.getGambleUpgrade() == 0) {
            bets = List.of(50L, 100L, 300L, 500L, 1000L, 2500L);
        } else {
            bets = List.of(500L, 1000L, 3000L, 5000L, 10000L, 25000L);
        }

        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (Long bet : bets) {
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(bet + " UC")

                            .callbackData("mult_bet_" + bet + ":" + userId)
                            .build()
            ));
        }


        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("⬅️ Назад")
                        .callbackData("back:" + userId)
                        .build()
        ));

        edit.setReplyMarkup(new InlineKeyboardMarkup(rows));
        telegramClient.execute(edit);
    }
    private void processMultiplier(BotUser user, Long chatId, String mention, String queryId, Integer msgId, long bet) {
        user.setLastMultiplyTime(Instant.now().getEpochSecond());
        if (user.getBalance() < bet) {
            sendAlert(queryId, "Недостаточно средств! Ваш баланс: " + user.getBalance() + " UC");
            return;
        }

        int roll = ThreadLocalRandom.current().nextInt(1, 101);
        long winAmount = 0;
        String resultEmoji;
        String message;
        System.out.println("Умножалка у " + user + " со счетом " + winAmount);
        user.setBalance(user.getBalance() - bet);

        if (roll <= 65) {
            winAmount = 0;
            resultEmoji = "<tg-emoji emoji-id=\"" + incompleteEmojiId + "\">💰</tg-emoji>";
            message = "Выпало " + roll + ". Проигрыш!";
        } else if (roll <= 80) {
            winAmount = bet * 2;
            resultEmoji = "<tg-emoji emoji-id=\"" + dollarEmojiId + "\">💰</tg-emoji>";
            message = "Выпало " + roll + ". x2!";
        } else if (roll <= 99) {
            winAmount = bet * 3;
            resultEmoji = "<tg-emoji emoji-id=\"" + starEmojiId + "\">💰</tg-emoji>";
            message = "Выпало " + roll + ". x3!!";
        } else {
            winAmount = bet * 5;
            resultEmoji = "<tg-emoji emoji-id=\"" + diamondEmojiId + "\">💰</tg-emoji>";
            message = "ВЫПАЛО 100! x5!!!";
        }

        user.setBalance(user.getBalance() + winAmount);
        userRepository.save(user);

        answerCallback(queryId);


        String finalMessage = resultEmoji + " " + mention + ", " + message +
                "\n💸 Результат: " + (winAmount >= 0 ? "+" : "") + winAmount + " UC" +
                "\n💰 Баланс: " + user.getBalance() + " UC";

        InlineKeyboardMarkup backMarkup = new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + user.getUserId()).build()
        )));
        editMessage(chatId, msgId, finalMessage, backMarkup);
        answerCallback(queryId);
    }
    @SneakyThrows
    private void sendBossSelectionMenu(BotUser user, Long chatId, Integer msgId) {
        int level = (user.getBossLevel() == null) ? 0 : user.getBossLevel();
        String text = "👹 *ВЫБОР БОССА*\n\nВаш текущий ранг: *" + level + "*\nПобеждайте боссов, чтобы открыть новых!";


        var boss1 = InlineKeyboardButton.builder().text("Крабулон").callbackData("b_start:1:" + user.getUserId()).build();


        String boss2Text = (level >= 1) ? "Глаз Ктулху" : "🔒 Убейте Крабулона";
        var boss2 = InlineKeyboardButton.builder()
                .text(boss2Text)
                .callbackData(level >= 1 ? "b_start:2:" + user.getUserId() : "alert_lock:1")
                .build();


        String boss3Text = (level >= 2) ? "Скелетрон" : "🔒 Убейте Глаза Ктулху";
        var boss3 = InlineKeyboardButton.builder()
                .text(boss3Text)
                .callbackData(level >= 2 ? "b_start:3:" + user.getUserId() : "alert_lock:2").build();


        String boss4Text = (level >= 3) ? "Девиантт" : "🔒 Убейте Скелетрона";
        var boss4 = InlineKeyboardButton.builder()
                .text(boss4Text)
                .callbackData(level >= 3 ? "b_start:4:" + user.getUserId() : "alert_lock:3").build();


        String boss5Text = (level >= 4) ? "Акватический Бич" : "🔒 Убейте Девиантт";
        var boss5 = InlineKeyboardButton.builder()
                .text(boss5Text)
                .callbackData(level >= 4 ? "b_start:5:" + user.getUserId() : "alert_lock:4").build();
        String boss6Text = (level >= 5) ? "Анахита и Левиафан" : "🔒 Убейте Акватического Бича";
        var boss6 = InlineKeyboardButton.builder()
                .text(boss6Text)
                .callbackData(level >= 5 ? "b_start:6:" + user.getUserId() : "alert_lock:5")
                .build();


        String boss7Text = (level >= 6) ? "Клон Каламитас" : "🔒 Убейте Анахиту и Левиафана";
        var boss7 = InlineKeyboardButton.builder()
                .text(boss7Text)
                .callbackData(level >= 6 ? "b_start:7:" + user.getUserId() : "alert_lock:6")
                .build();


        String boss8Text = (level >= 7) ? "Голиаф" : "🔒 Убейте Клон Каламитас";
        var boss8 = InlineKeyboardButton.builder()
                .text(boss8Text)
                .callbackData(level >= 7 ? "b_start:8:" + user.getUserId() : "alert_lock:7")
                .build();


        var backBtn = InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("back2:" + user.getUserId()).build();

        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(boss1),
                new InlineKeyboardRow(boss2),
                new InlineKeyboardRow(boss3),
                new InlineKeyboardRow(boss4),
                new InlineKeyboardRow(boss5),
                new InlineKeyboardRow(boss6),
                new InlineKeyboardRow(boss7),
                new InlineKeyboardRow(boss8),
                new InlineKeyboardRow(backBtn)
        )));
    }
    @SneakyThrows

    private void startBossFight(BotUser user, Long chatId, Integer msgId, int bossId) {
        int bossHp;
        int bossAtk;
        String bossName;

        switch (bossId) {
            case 1 -> { bossName = "Крабулон"; bossHp = 500; bossAtk = 15; }
            case 2 -> { bossName = "Глаз Ктулху"; bossHp = 1500; bossAtk = 40; }
            case 3 -> { bossName = "Скелетрон"; bossHp = 5000; bossAtk = 100; }
            case 4 -> { bossName = "Девиантт"; bossHp = 10000; bossAtk = 200; }
            case 5 -> { bossName = "Акватический Бич"; bossHp = 20000; bossAtk = 300; }
            case 6 -> { bossName = "Анахита и Левиафан"; bossHp = 10000; bossAtk = 400; }
            case 7 -> { bossName = "Клон Каламитас"; bossHp = 25000; bossAtk = 500; }
            case 8 -> { bossName = "Голиаф"; bossHp = 100000; bossAtk = 100; }
            default -> { bossName = "Слизняк"; bossHp = 100; bossAtk = 5; }
        }


        String text = "⚔️ *БОЙ С БОССОМ: " + bossName + "*\n\n❤️ ХП Босса: " + bossHp + "\n👤 Ваше ХП: " + user.getHp();

        var hitBtn = InlineKeyboardButton.builder()
                .text("⚔️ УДАРИТЬ!")

                .callbackData("b_hit:" + bossId + ":" + bossHp + ":" + user.getHp() + ":1:" + user.getUserId())
                .build();

        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(hitBtn))));

        renderBattleFrame(user, chatId, msgId, String.valueOf(bossId), String.valueOf(bossHp),
                String.valueOf(user.getHp()), "1", "0", "0", "⚔️ Бой начался!");

    }
    private String getRandomFish(BotUser user) {
        int chance = ThreadLocalRandom.current().nextInt(100);
        if (user.getGambleUpgrade() == 0) {
            if (chance < 5) return "Золотая рыбка:5000";
            if (chance < 20) return "Рыба-фугу:1200";
            if (chance < 30) return "Лосось:500";
            if (chance < 50) return "Карась:300";
            if (chance < 80) return "Малек:200";
            return "Старый башмак:50";
        } else {
            if (chance < 5) return "Сокровище бездны:50000";
            if (chance < 20) return "Левиафан:20000";
            if (chance < 30) return "Кит:12000";
            if (chance < 50) return "Акула:10000";
            if (chance < 80) return "Золотая рыбка:5000";
            return "Рыба-фугу:1200";
        }
    }
    private final java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newScheduledThreadPool(4);
    @SneakyThrows
    private void processFishing(BotUser user, Long chatId, Integer msgId, Long userId) {


        String catchKey = chatId + ":" + msgId;
        successfulCatches.remove(catchKey);


        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text("🌊 Вы закинули удочку... Ждем клева...")
                .replyMarkup(new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("🚫 Отмена").callbackData("back:" + userId).build()
                ))))
                .build();
        telegramClient.execute(edit);

        int delay = ThreadLocalRandom.current().nextInt(5, 31);

        scheduler.schedule(() -> {
            try {

                EditMessageText strike = EditMessageText.builder()
                        .chatId(chatId.toString())
                        .messageId(msgId)
                        .text( "<tg-emoji emoji-id=\"" + superwarningEmojiId + "\">⚠️</tg-emoji>" + "*ОЙ! КЛЮЕТ! У ТЕБЯ 2 СЕКУНДЫ!*")
                        .parseMode("HTML")
                        .replyMarkup(new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                                InlineKeyboardButton.builder().text("🎣 ТЯНИ!!!").callbackData("f_pull:" + userId).build()
                        ))))
                        .build();
                telegramClient.execute(strike);


                scheduler.schedule(() -> {

                    if (!successfulCatches.contains(catchKey)) {
                        try {
                            EditMessageText missed = EditMessageText.builder()
                                    .chatId(chatId.toString())
                                    .messageId(msgId)
                                    .text("💨 Эх... Рыба сорвалась. Попробуйте позже!")
                                    .replyMarkup(new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                                            InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + userId).build()
                                    ))))
                                    .build();
                            telegramClient.execute(missed);
                        } catch (Exception ignored) {}
                    }
                    successfulCatches.remove(catchKey);
                }, 2, java.util.concurrent.TimeUnit.SECONDS);

            } catch (Exception e) { e.printStackTrace(); }
        }, delay, java.util.concurrent.TimeUnit.SECONDS);
    }
    @SneakyThrows
    private void handleFishingPull(BotUser user, Long chatId, Integer msgId) {
        String catchKey = chatId + ":" + msgId;
        successfulCatches.add(catchKey);

        String fishData = getRandomFish(user);
        String[] parts = fishData.split(":");
        String name = parts[0];
        long price = Long.parseLong(parts[1]);
        user.setLastFishingTime(Instant.now().getEpochSecond());
        user.setBalance(user.getBalance() + price);
        userRepository.save(user);

        EditMessageText success = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text("✅ *УДАЧА!* \n\nВы поймали: " + name + "!\nПродано за: *" + price + " UC*")
                .parseMode("Markdown")
                .replyMarkup(new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + user.getUserId()).build()
                ))))
                .build();
        telegramClient.execute(success);
    }
    private final Map<Long, MinesGameState> activeMinesGames = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    private static class MinesGameState {
        int size;
        Set<Integer> mines;
        Set<Integer> opened;
        int reward;
        int penalty;
    }
    @SneakyThrows
    private void sendMinesMenu(Long chatId, Integer msgId, Long userId, BotUser user) {
        int mult = (user.getGambleUpgrade() != null && user.getGambleUpgrade() == 1) ? 10 : 1;

        String text = "💣 <b>Сапёр</b>\n\nВыберите размер поля:\n\n"
                + "• 2×2 — 1 мина | +" + (500 * mult) + " UC за клетку | -" + (3000 * mult) + " UC за мину\n"
                + "• 3×3 — 2 мины | +" + (500 * mult) + " UC за клетку | -" + (3000 * mult) + " UC за мину\n"
                + "• 4×4 — 3 мины | +" + (500 * mult) + " UC за клетку | -" + (3000 * mult) + " UC за мину";

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("2×2 (1 мина)").callbackData("mines_start:2:" + userId).build()
                ),
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("3×3 (2 мины)").callbackData("mines_start:3:" + userId).build()
                ),
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("4×4 (3 мины)").callbackData("mines_start:4:" + userId).build()
                ),
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + userId).build()
                )
        ));

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();
        telegramClient.execute(edit);
    }

    @SneakyThrows
    private void handleMinesStart(Long chatId, Integer msgId, Long userId, BotUser user, int size) {
        int mineCount = size == 2 ? 1 : size == 3 ? 2 : 3;
        int totalCells = size * size;
        int mult = (user.getGambleUpgrade() != null && user.getGambleUpgrade() == 1) ? 10 : 1;

        
        Set<Integer> mines = new HashSet<>();
        Random random = new Random();
        while (mines.size() < mineCount) {
            mines.add(random.nextInt(totalCells));
        }

        MinesGameState state = new MinesGameState(size, mines, new HashSet<>(), 500 * mult, 3000 * mult);
        activeMinesGames.put(userId, state);

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text("💣 <b>Сапёр</b> | Поле " + size + "×" + size + "\n\nНажимайте на клетки. Удачи!")
                .parseMode("HTML")
                .replyMarkup(buildMinesMarkup(state, userId, false))
                .build();
        telegramClient.execute(edit);
    }

    @SneakyThrows
    private void handleMinesClick(Long chatId, Integer msgId, Long userId, BotUser user, int cellIndex) {
        MinesGameState state = activeMinesGames.get(userId);
        if (state == null) {
            
            sendMinesMenu(chatId, msgId, userId, user);
            return;
        }

        if (state.getOpened().contains(cellIndex)) {
            
            return;
        }

        if (state.getMines().contains(cellIndex)) {
            
            user.setBalance(user.getBalance() - state.getPenalty());
            userRepository.save(user);
            activeMinesGames.remove(userId);

            state.getOpened().add(cellIndex);

            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(msgId)
                    .text("💥 <b>БУМ!</b> Вы попали на мину!\n\n<b>-" + state.getPenalty() + " UC</b>\n\nБаланс: " + user.getBalance() + " UC")
                    .parseMode("HTML")
                    .replyMarkup(buildMinesMarkup(state, userId, true))
                    .build();
            telegramClient.execute(edit);

        } else {
            
            state.getOpened().add(cellIndex);
            user.setBalance(user.getBalance() + state.getReward());
            userRepository.save(user);

            int totalCells = state.getSize() * state.getSize();
            int safeCells = totalCells - state.getMines().size();
            boolean allSafeOpened = state.getOpened().stream()
                    .filter(i -> !state.getMines().contains(i))
                    .count() == safeCells;

            if (allSafeOpened) {
                
                activeMinesGames.remove(userId);
                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId.toString())
                        .messageId(msgId)
                        .text("🏆 <b>Вы открыли всё поле!</b>\n\nБаланс: " + user.getBalance() + " UC")
                        .parseMode("HTML")
                        .replyMarkup(new InlineKeyboardMarkup(List.of(
                                new InlineKeyboardRow(
                                        InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + userId).build()
                                )
                        )))
                        .build();
                telegramClient.execute(edit);
            } else {
                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId.toString())
                        .messageId(msgId)
                        .text("💣 <b>Сапёр</b> | Поле " + state.getSize() + "×" + state.getSize()
                                + "\n\n✅ +" + state.getReward() + " UC\nБаланс: " + user.getBalance() + " UC")
                        .parseMode("HTML")
                        .replyMarkup(buildMinesMarkup(state, userId, false))
                        .build();
                telegramClient.execute(edit);
            }
        }
    }

    private InlineKeyboardMarkup buildMinesMarkup(MinesGameState state, Long userId, boolean revealMines) {
        int size = state.getSize();
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (int row = 0; row < size; row++) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                String btnText;
                String callbackData;

                if (state.getOpened().contains(index)) {
                    if (state.getMines().contains(index)) {
                        btnText = "💥";
                    } else {
                        btnText = "✅";
                    }
                    callbackData = "mines_ignore:" + userId;
                } else if (revealMines && state.getMines().contains(index)) {
                    btnText = "💣";
                    callbackData = "mines_ignore:" + userId;
                } else {
                    btnText = "⬜";
                    callbackData = "mines_click:" + index + ":" + userId;
                }

                keyboardRow.add(InlineKeyboardButton.builder()
                        .text(btnText)
                        .callbackData(callbackData)
                        .build());
            }
            rows.add(keyboardRow);
        }

        
        if (!revealMines) {
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("🏳️ Завершить игру")
                            .callbackData("mines_stop:" + userId)
                            .build()
            ));
        } else {
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("⬅️ В меню")
                            .callbackData("back:" + userId)
                            .build()
            ));
        }

        return new InlineKeyboardMarkup(rows);
    }

    @SneakyThrows
    private void handleMinesStop(Long chatId, Integer msgId, Long userId, BotUser user) {
        activeMinesGames.remove(userId);

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text("🏳️ <b>Игра завершена.</b>\n\nБаланс: " + user.getBalance() + " UC")
                .parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(List.of(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("⬅️ В меню")
                                        .callbackData("back:" + userId)
                                        .build()
                        )
                )))
                .build();
        telegramClient.execute(edit);
    }
    private final Map<Long, BlackjackGameState> activeBlackjackGames = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    private static class BlackjackGameState {
        List<Integer> playerCards;
        List<Integer> dealerCards;
        long bet;
    }



    private int getCardValue(int card) {
        
        if (card == 1) return 11; 
        if (card >= 10) return 10;
        return card;
    }

    private int calculateHandValue(List<Integer> cards) {
        int total = 0;
        int aces = 0;
        for (int card : cards) {
            if (card == 1) {
                aces++;
                total += 11;
            } else if (card >= 10) {
                total += 10;
            } else {
                total += card;
            }
        }
        
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    private String cardToEmoji(int card) {
        return switch (card) {
            case 1 -> "🂡 A";
            case 2 -> "2";
            case 3 -> "3";
            case 4 -> "4";
            case 5 -> "5";
            case 6 -> "6";
            case 7 -> "7";
            case 8 -> "8";
            case 9 -> "9";
            case 10 -> "10";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> "?";
        };
    }

    private String formatHand(List<Integer> cards) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            sb.append("[").append(cardToEmoji(cards.get(i))).append("]");
            if (i < cards.size() - 1) sb.append(" ");
        }
        return sb.toString();
    }

    private int drawCard() {
        return new Random().nextInt(13) + 1;
    }



    @SneakyThrows
    private void sendBlackjackMenu(Long chatId, Integer msgId, Long userId, BotUser user) {
        int mult = (user.getGambleUpgrade() != null && user.getGambleUpgrade() == 1) ? 10 : 1;

        long s1 = 500L * mult;
        long s2 = 1000L * mult;
        long s3 = 2500L * mult;
        long s4 = 5000L * mult;

        String text = "🃏 <b>Блекджек</b>\n\n"
                + "Выберите ставку.\n"
                + "Победа = ставка ×2\n\n"
                + "Ваш баланс: <b>" + user.getBalance() + " UC</b>";

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(s1 + " UC").callbackData("bj_start:" + s1 + ":" + userId).build(),
                        InlineKeyboardButton.builder().text(s2 + " UC").callbackData("bj_start:" + s2 + ":" + userId).build()
                ),
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text(s3 + " UC").callbackData("bj_start:" + s3 + ":" + userId).build(),
                        InlineKeyboardButton.builder().text(s4 + " UC").callbackData("bj_start:" + s4 + ":" + userId).build()
                ),
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + userId).build()
                )
        ));

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();
        telegramClient.execute(edit);
    }

    @SneakyThrows
    private void handleBlackjackStart(Long chatId, Integer msgId, Long userId, BotUser user, long bet) {
        if (user.getBalance() < bet) {
            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(msgId)
                    .text("❌ <b>Недостаточно средств.</b>\n\nВаш баланс: " + user.getBalance() + " UC")
                    .parseMode("HTML")
                    .replyMarkup(new InlineKeyboardMarkup(List.of( new InlineKeyboardRow(
                                    InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("bj_menu:" + userId).build()
                            )
                    )))
                    .build();
            telegramClient.execute(edit);
            return;
        }

        
        user.setBalance(user.getBalance() - bet);
        userRepository.save(user);

        
        List<Integer> playerCards = new ArrayList<>(List.of(drawCard(), drawCard()));
        List<Integer> dealerCards = new ArrayList<>(List.of(drawCard(), drawCard()));

        BlackjackGameState state = new BlackjackGameState(playerCards, dealerCards, bet);
        activeBlackjackGames.put(userId, state);

        int playerValue = calculateHandValue(playerCards);

        
        if (playerValue == 21) {
            handleBlackjackStand(chatId, msgId, userId, user, state);
            return;
        }

        String text = buildBlackjackText(state, user, false);

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(buildBlackjackMarkup(userId))
                .build();
        telegramClient.execute(edit);
    }

    @SneakyThrows
    private void handleBlackjackHit(Long chatId, Integer msgId, Long userId, BotUser user) {
        BlackjackGameState state = activeBlackjackGames.get(userId);
        if (state == null) {
            sendBlackjackMenu(chatId, msgId, userId, user);
            return;
        }

        state.getPlayerCards().add(drawCard());
        int playerValue = calculateHandValue(state.getPlayerCards());

        if (playerValue > 21) {
            
            activeBlackjackGames.remove(userId);
            

            String text = "🃏 <b>Блекджек</b>\n\n"
                    + "Ваши карты: " + formatHand(state.getPlayerCards()) + " = <b>" + playerValue + "</b>\n"
                    + "Карты дилера: " + formatHand(state.getDealerCards()) + " = <b>" + calculateHandValue(state.getDealerCards()) + "</b>\n\n"
                    + "💥 <b>Перебор! Вы проиграли.</b>\n"
                    + "-" + state.getBet() + " UC\n"
                    + "Баланс: " + user.getBalance() + " UC";

            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(msgId)
                    .text(text)
                    .parseMode("HTML")
                    .replyMarkup(new InlineKeyboardMarkup(List.of(
                            new InlineKeyboardRow(
                                    InlineKeyboardButton.builder().text("🔄 Сыграть ещё").callbackData("bj_menu:" + userId).build(),
                                    InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + userId).build()
                            )
                    )))
                    .build();
            telegramClient.execute(edit);

        } else {
            
            String text2 = buildBlackjackText(state, user, false);

            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(msgId)
                    .text(text2)
                    .parseMode("HTML")
                    .replyMarkup(buildBlackjackMarkup(userId))
                    .build();
            telegramClient.execute(edit);
        }
    }

    @SneakyThrows
    private void handleBlackjackStand(Long chatId, Integer msgId, Long userId, BotUser user, BlackjackGameState state) {
        if (state == null) state = activeBlackjackGames.get(userId);
        if (state == null) {
            sendBlackjackMenu(chatId, msgId, userId, user);
            return;
        }
        activeBlackjackGames.remove(userId);

        
        while (calculateHandValue(state.getDealerCards()) < 17) {
            state.getDealerCards().add(drawCard());
        }

        int playerValue = calculateHandValue(state.getPlayerCards());
        int dealerValue = calculateHandValue(state.getDealerCards());

        String resultText;
        if (dealerValue > 21 || playerValue > dealerValue) {
            
            long winAmount = state.getBet() * 2;
            user.setBalance(user.getBalance() + winAmount);
            userRepository.save(user);
            resultText = "🏆 <b>Вы победили!</b>\n+"  + winAmount + " UC";
        } else if (playerValue == dealerValue) {
            
            user.setBalance(user.getBalance() + state.getBet());
            userRepository.save(user);
            resultText = "🤝 <b>Ничья!</b>\nСтавка возвращена.";
        } else {
            
            resultText = "😔 <b>Вы проиграли.</b>\n-" + state.getBet() + " UC";
        }

        String text = "🃏 <b>Блекджек</b>\n\n"
                + "Ваши карты: " + formatHand(state.getPlayerCards()) + " = <b>" + playerValue + "</b>\n"
                + "Карты дилера: " + formatHand(state.getDealerCards()) + " = <b>" + dealerValue + "</b>\n\n"
                + resultText + "\n"
                + "Баланс: " + user.getBalance() + " UC";

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(List.of(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder().text("🔄 Сыграть ещё").callbackData("bj_menu:" + userId).build(),
                                InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + userId).build()
                        )
                )))
                .build();
        telegramClient.execute(edit);
    }

    private String buildBlackjackText(BlackjackGameState state, BotUser user, boolean reveal) {
        int playerValue = calculateHandValue(state.getPlayerCards());
        String dealerDisplay = reveal
                ? formatHand(state.getDealerCards()) + " = <b>" + calculateHandValue(state.getDealerCards()) + "</b>"
                : "[" + cardToEmoji(state.getDealerCards().get(0)) + "] [?]";

        return "🃏 <b>Блекджек</b>\n\n"
                + "Карты дилера: " + dealerDisplay + "\n\n"
                + "Ваши карты: " + formatHand(state.getPlayerCards()) + " = <b>" + playerValue + "</b>\n\n"
                + "Ставка: " + state.getBet() + " UC\n"
                + "Баланс: " + user.getBalance() + " UC";
    }

    private InlineKeyboardMarkup buildBlackjackMarkup(Long userId) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("➕ Ещё карту").callbackData("bj_hit:" + userId).build(),
                        InlineKeyboardButton.builder().text("✋ Стоп").callbackData("bj_stand:" + userId).build()
                )
        ));
    }
    @SneakyThrows
    private void processFiftyFifty(BotUser user, Long chatId, String mention, String queryId, Integer msgId) {
        double chance = ThreadLocalRandom.current().nextDouble();
        long change = 0;
        String result;
        System.out.println("50/50- " + chance + " у" + chatId);
        if (user.getGambleUpgrade() == 0) {
            if (chance < 0.5) {
                result = "Повезло! +500UC";
                change = 500;
            } else {
                result = "Не повезло... -500UC";
                change = -500;
            }
        } else {
            if (chance < 0.5) {
                result = "Повезло! +5000UC";
                change = 500;
            } else {
                result = "Не повезло... -5000UC";
                change = -500;
            }
        }
        user.setBalance(user.getBalance() + change);
        user.setLastFiftyFiftyTime(Instant.now().getEpochSecond());

        
        userRepository.save(user);

        


        String finalMessage = mention + ", " + result + "\n💰 Ваш баланс: " + user.getBalance() + " UC";
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + user.getUserId()).build()
        )));

        editMessage(chatId, msgId, finalMessage, markup);
        answerCallback(queryId);
    }

    private void processGamble(BotUser user, Long chatId, String mention, String queryId, Integer msgId) {
        double chance = ThreadLocalRandom.current().nextDouble();
        long change = 0;
        String result = "";
        System.out.println("гембл- " + chance + "у" + chatId);
        
        if (user.getGambleUpgrade() == 0) {
            if (chance < 0.0005) {
                result = "Колесо крутится и... ДЖЕКПОТ!!!!!! +1000000UC";
                change = 1000000;
            } else if (chance < 0.005) {
                result = "Колесо крутится и... +25000UC";
                change = 25000;
            } else if (chance < 0.01) {
                result = "Колесо крутится и... +10000UC";
                change = 10000;
            } else if (chance < 0.02) {
                result = "Колесо крутится и... +5000UC";
                change = 5000;
            } else if (chance < 0.03) {
                result = "Колесо крутится и... +3000UC";
                change = 3000;
            } else if (chance < 0.05) {
                result = "Колесо крутится и... +1000UC";
                change = 1000;
            } else if (chance < 0.08) {
                result = "Колесо крутится и... +500UC";
                change = 500;
            } else if (chance < 0.1) {
                result = "Колесо крутится и... +200UC";
                change = 200;
            } else if (chance < 0.3) {
                result = "Колесо крутится и... +100UC";
                change = 100;
            } else if (chance < 0.4) {
                result = "Колесо крутится и... 0UC";
                change = 0;
            } else if (chance < 0.5) {
                result = "Колесо крутится и... -50UC";
                change = -50;
            } else if (chance < 0.6) {
                result = "Колесо крутится и... -150UC";
                change = -150;
            } else if (chance < 0.7) {
                result = "Колесо крутится и... -300UC";
                change = -300;
            } else if (chance < 0.8) {
                result = "Колесо крутится и... -500UC";
                change = -500;
            } else {
                result = "Колесо крутится и... -1000UC";
                change = -1000;
            }
        }
        else {
            if (chance < 0.0005) {
                result = "Колесо крутится и... ДЖЕКПОТ!!!!!! +2500000UC";
                change = 2500000;
            } else if (chance < 0.005) {
                result = "Колесо крутится и... +125000UC";
                change = 125000;
            } else if (chance < 0.01) {
                result = "Колесо крутится и... +100000UC";
                change = 100000;
            } else if (chance < 0.02) {
                result = "Колесо крутится и... +50000UC";
                change = 50000;
            } else if (chance < 0.03) {
                result = "Колесо крутится и... +30000UC";
                change = 30000;
            } else if (chance < 0.05) {
                result = "Колесо крутится и... +10000UC";
                change = 10000;
            } else if (chance < 0.08) {
                result = "Колесо крутится и... +5000UC";
                change = 5000;
            } else if (chance < 0.1) {
                result = "Колесо крутится и... +2000UC";
                change = 2000;
            } else if (chance < 0.3) {
                result = "Колесо крутится и... +1000UC";
                change = 1000;
            } else if (chance < 0.4) {
                result = "Колесо крутится и... 0UC";
                change = 0;
            } else if (chance < 0.5) {
                result = "Колесо крутится и... -500UC";
                change = -500;
            } else if (chance < 0.6) {
                result = "Колесо крутится и... -1500UC";
                change = -1500;
            } else if (chance < 0.7) {
                result = "Колесо крутится и... -3000UC";
                change = -3000;
            } else if (chance < 0.8) {
                result = "Колесо крутится и... -5000UC";
                change = -5000;
            }
        }

        
        user.setBalance(user.getBalance() + change);
        user.setLastGambleTime(Instant.now().getEpochSecond());

        
        userRepository.save(user);

        


        String finalMessage = mention + ", " + result + "\n💰 Ваш баланс: " + user.getBalance() + " UC";

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("⬅️ В меню").callbackData("back:" + user.getUserId()).build()
        )));

        editMessage(chatId, msgId, finalMessage, markup);
        answerCallback(queryId);
    }

    @SneakyThrows
    private void sendInventoryMenu(Long chatId, Integer msgId, Long userId, String userName) {
        BotUser user = userRepository.findById(userId).orElseThrow();
        String inv = (user.getInventory() == null) ? "Кулаки,Рубаха" : user.getInventory();

        String text = "🎒 ИНВЕНТАРЬ: " + userName + "\n\n" +
                "🗡 Оружие сейчас: " + user.getWeapon() + "\n" +
                "🛡 Броня сейчас: " + user.getArmor() + "\n\n" +
                "Выбери категорию для смены снаряжения:";

        var weaponsBtn = InlineKeyboardButton.builder().text("⚔️ Оружие").callbackData("inv_w:" + userId).build();
        var armorBtn = InlineKeyboardButton.builder().text("🛡 Броня").callbackData("inv_a:" + userId).build();


        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(weaponsBtn, armorBtn)
        )));
    }
    @SneakyThrows
    private void sendInventoryCategory(Long chatId, Integer msgId, Long userId, String type) {
        BotUser user = userRepository.findById(userId).orElseThrow();
        String inv = user.getInventory();
        List<InlineKeyboardRow> rows = new ArrayList<>();

        String title = type.equals("w") ? "⚔️ ТВОЁ ОРУЖИЕ:" : "🛡 ТВОЯ БРОНЯ:";

        
        List<String> allItems = type.equals("w")
                ? List.of("Нож", "Топор", "Меч", "Лук", "Пистолет", "Оружие Мастерской Розеспаннер",
                "Оружие Мастерской Зелькова", "Оружие Мастерской Аляс", "Оружие Мастерской Стигма",
                "Оружие Мастерской Мук", "Оружие Кристального Ателье", "Оружие Ателье Логики", "Оружие Колёсной Промышленности", "Кулаки")
                : List.of("Кожаный жилет", "Кольчуга", "Доспехи Рыцаря", "Броня Джунглей", "Литая Броня",
                "Мифриловая Броня", "Титановая броня", "Святая броня", "Рубаха");

        for (String item : allItems) {
            if (inv.contains(item)) {
                
                int level = getLevelByName(item);
                rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text(item + (item.equals(user.getWeapon()) || item.equals(user.getArmor()) ? " ✅" : ""))
                        .callbackData("equip:" + type + ":" + level + ":" + userId)
                        .build()));
            }
        }

        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("inv_back:" + userId).build()));
        editMessage(chatId, msgId, title, new InlineKeyboardMarkup(rows));
    }

    
    private int getLevelByName(String name) {
        return switch (name) {
            case "Нож", "Кожаный жилет" -> 1;
            case "Топор", "Кольчуга" -> 2;
            case "Меч", "Доспехи Рыцаря" -> 3;
            case "Лук", "Броня Джунглей" -> 4;
            case "Пистолет", "Литая Броня" -> 5;
            case "Оружие Мастерской Розеспаннер", "Мифриловая Броня" -> 6;
            case "Оружие Мастерской Зелькова", "Титановая броня" -> 7;
            case "Оружие Мастерской Аляс", "Святая броня" -> 8;
            case "Оружие Мастерской Стигма" -> 9;
            case "Оружие Мастерской Мук" -> 10;
            case "Оружие Кристального Ателье" -> 11;
            case "Оружие Ателье Логики" -> 12;
            case "Оружие Колёсной Промышленности" -> 13;
            default -> 0; 
        };
    }
    @SneakyThrows
    private void sendPrestigeMenu(Long chatId, Integer msgId, Long userId) {

        String text = "ПРЕСТИЖ ГЕМБЛА";
        var row1 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("Улучшить престиж").callbackData("pru:" + userId).build()
        );
        var row2 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("back3:" + userId).build()
        );

        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(row1, row2)));
    }
    @SneakyThrows
    private void PrestigeUpgrade(BotUser user, String queryId) {
        int currentPrestige = (user.getGambleUpgrade() != null) ? user.getGambleUpgrade() : 0;
        int price = 1000000;

        if (currentPrestige >= 1) {
            sendAlert(queryId, "❌ Ваш престиж на максимальном значении!");
            return;
        }

        if (user.getBalance() < price) {
            sendAlert(queryId, "❌ Недостаточно денег! Нужно: " + price + " UC");
        } else {
            user.setBalance(user.getBalance() - price);
            user.setGambleUpgrade(1); 
            userRepository.save(user); 

            sendAlert(queryId, "✅ Поздравляем! Престиж Гембла успешно улучшен!");
        }



    }
    @SneakyThrows
    private void sendLeaderboard(Long chatId) {
        List<BotUser> topUsers = userRepository.findTop10ByOrderByBalanceDesc();

        if (topUsers.isEmpty()) {
            sendMessage(chatId, "Таблица лидеров пока пуста!");
            return;
        }

        StringBuilder sb = new StringBuilder("🏆 ТОП-10 Богачей:\n\n");
        for (int i = 0; i < topUsers.size(); i++) {
            BotUser u = topUsers.get(i);
            
            String name = (u.getDisplayName() != null) ? u.getDisplayName() : "Игрок " + u.getUserId();

            
            sb.append(i + 1).append(". `")
                    .append(name).append("` — ")
                    .append(u.getBalance()).append(" UC\n");
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .parseMode("Markdown") 
                .build();

        telegramClient.execute(message);
    }

    @SneakyThrows
    private void sendPveMenu(Long chatId, Integer messageId, Long userId, String userName) {
        String text = "⚔️ PVE ЦЕНТР\n\nИгрок: " + userName + "\nЗдесь вы можете сразиться с боссами или закупиться снаряжением.";

        var shopBtn = InlineKeyboardButton.builder()
                .text("🛒 Магазин")
                .callbackData("shop_menu:" + userId)
                .build();
        var vsaiBtn = InlineKeyboardButton.builder()
                .text("🦾 VS Гемби")
                .callbackData("ai_menu:" + userId)
                .build();

        var bossBtn = InlineKeyboardButton.builder()
                .text("👹 Боссы")
                .callbackData("boss_menu:" + userId)
                .build();


        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(new InlineKeyboardMarkup(List.of(
                        new InlineKeyboardRow(shopBtn, bossBtn),
                        new InlineKeyboardRow(vsaiBtn)

                )))
                .build();

        telegramClient.execute(edit);
    }
    @SneakyThrows
    private void sendGambleShop(Long chatId, Integer msgId, Long userId) {
        String text = "<tg-emoji emoji-id=\"" + dollarEmojiId + "\">🌟</tg-emoji>" + " Gamble МАГАЗИН\nВыбери категорию товаров:";

        var upgradefarm = InlineKeyboardButton.builder().text("Улучшение фармы").callbackData("upfm:" + userId).build();
        var upgradegamble = InlineKeyboardButton.builder().text("Престиж гембла").callbackData("prg:" + userId).build();
        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(upgradefarm),
                new InlineKeyboardRow(upgradegamble)
        )));
    }
    private void sendUpgradeFarmMenu(BotUser user, Long chatId, Integer msgId, Long userId) {
        long price1 = user.getNextUpgradePriceSafe();
        long currentLevel = user.getFarmUpgradeSafe();

        
        long price3 = price1 + (price1 + 250) + (price1 + 500);
        long price5 = price3 + (price1 + 750) + (price1 + 1000);
        long price10 = price5 + (price1 + 1500) + (price1 + 1500);


        String text = "🛒 Улучшение фармы\n" +
                "Текущий уровень: " + currentLevel + "\n" +
                "Бонус: +" + (currentLevel * 500) + " UC\n" +
                "Баланс: " + user.getBalance() + " UC";

        
        var row1 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("x1 (" + price1 + ")").callbackData("upx:1:" + userId).build(),
                InlineKeyboardButton.builder().text("x3 (" + price3 + ")").callbackData("upx:3:" + userId).build()
        );
        var row2 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("x5 (" + price5 + ")").callbackData("upx:5:" + userId).build(),
                InlineKeyboardButton.builder().text("x10 (" + price10 + ")").callbackData("upx:10:" + userId).build()
        );
        var row3 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("back3:" + userId).build()
        );

        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(row1, row2, row3)));
    }
    @SneakyThrows
    private void sendShopMenu(Long chatId, Integer msgId, Long userId) {
        String text = "🛒 PVE МАГАЗИН\nВыбери категорию товаров:";

        var weapons = InlineKeyboardButton.builder().text("⚔️ Оружие").callbackData("shop_w:" + userId).build();
        var armors = InlineKeyboardButton.builder().text("🛡 Броня").callbackData("shop_a:" + userId).build();
        var potions = InlineKeyboardButton.builder().text("💠 Предметы").callbackData("shop_p:" + userId).build();
        var back = InlineKeyboardButton.builder().text("⬅️ В PVE меню").callbackData("back2:" + userId).build();

        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(weapons, armors),
                new InlineKeyboardRow(potions),
                new InlineKeyboardRow(back)
        )));
    }
    private void handleBuy(BotUser user, Long chatId, Integer msgId, String type, int level, String queryId, boolean isFromInventory) {
        int price;
        int bonus;
        String name;
        if (type.equals("p")) {
            int pricepot = switch(level) {
                case 1 -> 1000; case 2 -> 5000; case 3 -> 7500; case 4 -> 10000; case 5 -> 15000;
                default -> 0;
            };
            String pName = switch(level) {
                case 1 -> "Зелье ХП";
                default -> "";
            };

            if (user.getBalance() < pricepot) {
                sendAlert(queryId, "❌ Недостаточно UC!");
                return;
            }

            user.setBalance(user.getBalance() - pricepot);
            
            String currentCons = (user.getConsumables() == null) ? "" : user.getConsumables();
            user.setConsumables(currentCons + pName + ",");

            userRepository.save(user);
            sendAlert(queryId, "✅ Вы купили " + pName);
            sendPotionShop(chatId, msgId, user.getUserId());
            return;
        }

        
        if (type.equals("w")) {
            switch (level) {
                case 1 -> { name = "Нож"; price = 2500; bonus = 15; }
                case 2 -> { name = "Топор"; price = 10000; bonus = 40; }
                case 3 -> { name = "Меч"; price = 50000; bonus = 120; }
                case 4 -> { name = "Лук"; price = 100000; bonus = 200; }
                case 5 -> { name = "Пистолет"; price = 200000; bonus = 300; }
                case 6 -> { name = "Оружие Мастерской Розеспаннер"; price = 500000; bonus = 500; }
                case 7 -> { name = "Оружие Мастерской Зелькова"; price = 1000000; bonus = 750; }
                case 8 -> { name = "Оружие Мастерской Аляс"; price = 2000000; bonus = 1000; }
                case 9 -> { name = "Оружие Мастерской Стигма"; price = 3000000; bonus = 1500; }
                case 10 -> { name = "Оружие Мастерской Мук"; price = 5000000; bonus = 2000; }
                case 11 -> { name = "Оружие Кристального Ателье"; price = 7500000; bonus = 3000; }
                case 12 -> { name = "Оружие Ателье Логики"; price = 10000000; bonus = 4500; }
                case 13 -> { name = "Оружие Колёсной промышленности"; price = 12500000; bonus = 5500; }
                default -> {
                    return;
                }
            }
        } else {
            switch (level) {
                case 1 -> { name = "Кожаный жилет"; price = 2000; bonus = 10; }
                case 2 -> { name = "Кольчуга"; price = 8000; bonus = 30; }
                case 3 -> { name = "Доспехи Рыцаря"; price = 40000; bonus = 100; }
                case 4 -> { name = "Броня Джунглей"; price = 100000; bonus = 150; }
                case 5 -> { name = "Литая Броня"; price = 500000; bonus = 300; }
                case 6 -> { name = "Мифриловая Броня"; price = 1000000; bonus = 500; }
                case 7 -> { name = "Титановая броня"; price = 3000000; bonus = 750; }
                case 8 -> { name = "Святая броня"; price = 10000000; bonus = 1000; }
                default -> {
                    return;
                }
            }
        }

        
        String inv = (user.getInventory() == null) ? "" : user.getInventory();
        boolean alreadyOwned = inv.contains(name);

        if (name.equals(user.getWeapon()) || name.equals(user.getArmor())) {
            sendAlert(queryId, "🔹 Этот предмет уже на вас!");
            return;
        }

        
        if (alreadyOwned) {
            equipItem(user, type, name, bonus);
            userRepository.save(user);
            sendAlert(queryId, "✅ Вы снова надели " + name);
        } else {
            
            if (user.getBalance() < price) {
                sendAlert(queryId, "❌ Недостаточно UC!");
                return;
            }
            user.setBalance(user.getBalance() - price);
            user.setInventory(inv + "," + name); 
            equipItem(user, type, name, bonus);
            userRepository.save(user);
            sendAlert(queryId, "💰 Куплено и экипировано: " + name);
        }

        
        if (type.equals("w")) sendWeaponShop(chatId, msgId, user.getUserId());
        else sendArmorShop(chatId, msgId, user.getUserId());
        if (isFromInventory) {
            
            sendInventoryCategory(chatId, msgId, user.getUserId(), type);
        } else {
            
            if (type.equals("w")) sendWeaponShop(chatId, msgId, user.getUserId());
            else sendArmorShop(chatId, msgId, user.getUserId());
        }
    }

    
    private void equipItem(BotUser user, String type, String name, int bonus) {
        if (type.equals("w")) {
            user.setAttack(10 + bonus);
            user.setWeapon(name);
        } else {
            user.setDefense(5 + bonus);
            user.setArmor(name);
        }
    }
    @SneakyThrows
    private void sendWeaponShop(Long chatId, Integer msgId, Long userId) {
        BotUser user = userRepository.findById(userId).orElseThrow();
        String text = "⚔️ МАГАЗИН ОРУЖИЯ\n💰 Ваш баланс: " + user.getBalance() + " UC\n\n" +
                "1. Нож (+15 атк) — 2500 UC\n" +
                "2. Топор викинга (+40 атк) — 10000 UC\n" +
                "3. Меч (+120 атк) — 50000 UC\n" +
                "4. Лук (+200 атк) — 100000 UC\n" +
                "5. Пистолет (+300 атк) — 200000 UC\n" +
                "6. Оружие Мастерской Розеспаннер (+500 атк) — 500000 UC\n" +
                "7. Оружие Мастерской Зелькова (+750 атк) — 1000000 UC\n" +
                "8. Оружие Мастерской Аляс (+1000 атк) — 2000000 UC"
                ;

        var w1 = InlineKeyboardButton.builder().text("Купить Нож (2500)").callbackData("buy_w:1:" + userId).build();
        var w2 = InlineKeyboardButton.builder().text("Купить Топор (10000)").callbackData("buy_w:2:" + userId).build();
        var w3 = InlineKeyboardButton.builder().text("Купить Меч (50000)").callbackData("buy_w:3:" + userId).build();
        var w4 = InlineKeyboardButton.builder().text("Купить Лук (100000)").callbackData("buy_w:4:" + userId).build();
        var w5 = InlineKeyboardButton.builder().text("Купить Пистолет (200000)").callbackData("buy_w:5:" + userId).build();
        var w6 = InlineKeyboardButton.builder().text("Купить Оружие Мастерской Розеспаннер (500000)").callbackData("buy_w:6:" + userId).build();
        var w7 = InlineKeyboardButton.builder().text("Купить Оружие Мастерской Зелькова (1000000)").callbackData("buy_w:7:" + userId).build();
        var w8 = InlineKeyboardButton.builder().text("Купить Оружие Мастерской Аляс (2000000)").callbackData("buy_w:8:" + userId).build();
        var back = InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("shop_menu:" + userId).build();
        var next = InlineKeyboardButton.builder().text("⏩ Дальше").callbackData("shop_w2:" + userId).build();

        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(w1),
                new InlineKeyboardRow(w2),
                new InlineKeyboardRow(w3),
                new InlineKeyboardRow(w4),
                new InlineKeyboardRow(w5),
                new InlineKeyboardRow(w6),
                new InlineKeyboardRow(w7),
                new InlineKeyboardRow(w8),
                new InlineKeyboardRow(back, next)
        )));
    }
    @SneakyThrows
    private void sendWeaponShop2(Long chatId, Integer msgId, Long userId) {
        BotUser user = userRepository.findById(userId).orElseThrow();
        String text = "⚔️ МАГАЗИН ОРУЖИЯ\n💰 Ваш баланс: " + user.getBalance() + " UC\n\n" +
                "9. Оружие Мастерской Стигма (+1500 атк) — 3000000 UC\n" +
                "10. Оружие Мастерской Мук (+2000 атк) — 5000000 UC\n" +
                "11. Оружие Кристального Ателье (+3000 атк) — 7500000 UC\n" +
                "12. Оружие Ателье Логики (+4500 атк) — 10000000 UC\n" +
                "13. Оружие Колёсной Промышленности (+5500 атк) — 12500000 UC\n"
                ;

        var w8 = InlineKeyboardButton.builder().text("Купить Оружие Мастерской Стигма").callbackData("buy_w:9:" + userId).build();
        var w9 = InlineKeyboardButton.builder().text("Купить Оружие Мастерской Мук").callbackData("buy_w:10:" + userId).build();
        var w10 = InlineKeyboardButton.builder().text("Купить Оружие Кристального Ателье").callbackData("buy_w:11:" + userId).build();
        var w11 = InlineKeyboardButton.builder().text("Купить Оружие Ателье Логики").callbackData("buy_w:12:" + userId).build();
        var w12 = InlineKeyboardButton.builder().text("Купить Оружие Колёсной Промышленности").callbackData("buy_w:13:" + userId).build();
        var back = InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("shop_w:" + userId).build();

        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(w8),
                new InlineKeyboardRow(w9),
                new InlineKeyboardRow(w10),
                new InlineKeyboardRow(w11),
                new InlineKeyboardRow(w12),
                new InlineKeyboardRow(back)
        )));
    }

    @SneakyThrows
    private void sendArmorShop(Long chatId, Integer msgId, Long userId) {
        String text = "🛡 МАГАЗИН БРОНИ\n\n" +
                "1. Кожаный жилет (+10 защ) — 2000 UC\n" +
                "2. Кольчуга (+30 защ) — 8000 UC\n" +
                "3. Доспехи Рыцаря (+100 защ) — 40000 UC\n" +
                "4. Броня Джунглей (+150 защ) — 40000 UC\n" +
                "5. Литая Броня (+300 защ) — 40000 UC\n" +
                "6. Мифриловая Броня (+500 защ) — 40000 UC\n" +
                "7. Титановая Броня (+750 защ) — 40000 UC\n" +
                "8. Святая Броня (+1000 защ) — 40000 UC\n";

        var a1 = InlineKeyboardButton.builder().text("Купить Жилет (2000)").callbackData("buy_a:1:" + userId).build();
        var a2 = InlineKeyboardButton.builder().text("Купить Кольчугу (8000)").callbackData("buy_a:2:" + userId).build();
        var a3 = InlineKeyboardButton.builder().text("Купить Доспехи (40000)").callbackData("buy_a:3:" + userId).build();
        var a4 = InlineKeyboardButton.builder().text("Купить Броню Джунглей (100000)").callbackData("buy_a:4" + userId).build();
        var a5 = InlineKeyboardButton.builder().text("Купить Литую Броня (500000)").callbackData("buy_a:5:" + userId).build();
        var a6 = InlineKeyboardButton.builder().text("Купить Мифриловую Броню (1000000)").callbackData("buy_a:6:" + userId).build();
        var a7 = InlineKeyboardButton.builder().text("Купить Титановую Броню (3000000)").callbackData("buy_a:7:" + userId).build();
        var a8 = InlineKeyboardButton.builder().text("Купить Святую Броню (10000000)").callbackData("buy_a:8:" + userId).build();
        var back = InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("shop_menu:" + userId).build();

        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(a1),
                new InlineKeyboardRow(a2),
                new InlineKeyboardRow(a3),
                new InlineKeyboardRow(a4),
                new InlineKeyboardRow(a5),
                new InlineKeyboardRow(a6),
                new InlineKeyboardRow(a7),
                new InlineKeyboardRow(a8),
                new InlineKeyboardRow(back)
        )));
    }
    @SneakyThrows
    private void sendPotionShop(Long chatId, Integer msgId, Long userId) {
        BotUser user = userRepository.findById(userId).orElseThrow();
        String text = "МАГАЗИН ПРЕДМЕТОВ\n💰 Ваш баланс: " + user.getBalance() + " UC\n\n" +
                "1. Зелье ХП — 1000 UC\n" +
                "Восстанавливает 100 HP в бою";





        var p1 = InlineKeyboardButton.builder().text("Купить Зелье ХП(1000)").callbackData("buy_p:1:" + userId).build();




        var back = InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("shop_menu:" + userId).build();

        editMessage(chatId, msgId, text, new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(p1),


                new InlineKeyboardRow(back)
        )));
    }


    @SneakyThrows
    private void handleBossHit(BotUser user, Long chatId, Integer msgId,
                               String bossIdStr, String bHpStr, String uHpStr,
                               String turnStr, String itemsUsed, String phaseStr) {
        int bossId = Integer.parseInt(bossIdStr);
        int bHp = Integer.parseInt(bHpStr);
        int uHp = Integer.parseInt(uHpStr);
        int turn = Integer.parseInt(turnStr);
        int phase = Integer.parseInt(phaseStr);

        
        int userAtk = (user.getAttack() == null) ? 10 : user.getAttack();
        int damageToBoss = userAtk + ThreadLocalRandom.current().nextInt(0, 6);
        bHp -= damageToBoss;

        
        if (bHp <= 0) {

            int currentMaxLevel = (user.getBossLevel() == null) ? 0 : user.getBossLevel();
            String bonusText = "";

            
            if (bossId > currentMaxLevel) {
                user.setBossLevel(bossId); 

                
                int oldMaxHp = (user.getMaxHp() == null) ? 100 : user.getMaxHp();
                int newMaxHp = oldMaxHp + 100;
                user.setMaxHp(newMaxHp);

                bonusText = "\n🌟 ПЕРВАЯ ПОБЕДА! Макс. HP увеличено: " + oldMaxHp + " ➔ " + newMaxHp;
            }

            
            user.setHp(user.getMaxHp());

            long reward = bossId * 5000L;
            user.setBalance(user.getBalance() + reward);
            user.setLastBossfightTime(Instant.now().getEpochSecond());
            userRepository.save(user);

            String winText = "🎉 *ПОБЕДА НАД БОССОМ #" + bossId + "\n" +
                    "💰 Награда: " + reward + " UC" + bonusText;

            var backBtn = InlineKeyboardButton.builder()
                    .text("⬅️ К списку боссов")
                    .callbackData("boss_menu:" + user.getUserId())
                    .build();

            editMessage(chatId, msgId, winText, new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(backBtn))));
            return;
        }



        
        int bossAtk = getBossBaseAttack(bossId); 
        String bossAction = "";
        if (bossId == 1 && turn % 5 == 0) {
            bossAtk += 50;
            bossAction =  "<tg-emoji emoji-id=\"" + star1EmojiId + "\">🌟</tg-emoji>" + "Крабулон использует грибные копья!";
        }
        else if (bossId == 2 && turn % 3 == 0) {
            bossAtk += 30;
            bossAction =  "<tg-emoji emoji-id=\"" + star2EmojiId + "\">🌟</tg-emoji>" + "Глаз ктулху использует рывок!";
        }
        else if (bossId == 3 && turn % 5 == 0) {
            bossAtk = 0;
            bossAction = "<tg-emoji emoji-id=\"" + star1EmojiId + "\">🌟</tg-emoji>" + "Скелетрон начинает вращаться...";
        } else if (bossId == 3 && (turn - 1) % 5 == 0 && turn > 1) {
            bossAtk += 150;
            bossAction = "<tg-emoji emoji-id=\"" + star3EmojiId + "\">🌟</tg-emoji>" + "Скелетрон РЕЗКО АТАКУЕТ!";
        } else if (bossId == 4 && turn % 2 == 0) {
            bossAtk += 50;
            bossAction = "<tg-emoji emoji-id=\"" + star1EmojiId + "\">🌟</tg-emoji>" + "Девиантт стреляет сердцами!";
            if (bHp == 5000) {
                bossAtk += 100;
                bossAction = "<tg-emoji emoji-id=\"" + star4EmojiId + "\">🌟</tg-emoji>" + "Девиантт стреляет разьяренными сердцами!";
            }
        }
        else if (bossId == 5 && turn % 3 == 0) {
            bossAtk += 200;
            bossAction = "<tg-emoji emoji-id=\"" + star2EmojiId + "\">🌟</tg-emoji>" + "Акватический Бич пронзает вас!";
        }
        else if (bossId == 5 && turn % 10 == 0) {
            bossAtk += 300;
            bossAction = "<tg-emoji emoji-id=\"" + star3EmojiId + "\">🌟</tg-emoji>" + "Акватический Бич чуть не поглащает вас!";
        }


        if (bossId == 6) {
            
            if (bHp <= 2000 && phase == 0) {
                phase = 1; 
                bHp += 10000;
                bossAction = "<tg-emoji emoji-id=\"" + star4EmojiId + "\">🌟</tg-emoji>" + "Левиафан прибывает на помощь Анахите! ХП восстановлено!" +
                        "<tg-emoji emoji-id=\"" + star4EmojiId + "\">🌟</tg-emoji>";
                bossAtk += 100; 
            }

            
            if (phase == 0) {
                if (turn % 5 == 0) {
                    bossAtk += 200;
                    bossAction = "<tg-emoji emoji-id=\"" + star2EmojiId + "\">🌟</tg-emoji>" + "Анахита поет, и ваше сознание поддается!";
                }
            } else {
                if (turn % 6 == 0) {
                    bossAtk += 400;
                    bossAction = "<tg-emoji emoji-id=\"" + star3EmojiId + "\">🌟</tg-emoji>" + "Левиафан пронзает вас!";
                }
            }
        }
        else if (bossId == 7 && turn % 2 == 0) {
            bossAtk += 100;
            bossAction = "<tg-emoji emoji-id=\"" + star1EmojiId + "\">🌟</tg-emoji>" + "Клон Каламитас стреляет в вас серным снарядом!";
        }
        else if (bossId == 7 && turn % 5 == 0) {
            bossAtk += 500;
            bossAction += "<tg-emoji emoji-id=\"" + star3EmojiId + "\">🌟</tg-emoji>" + " И пронзает вас!";
        }
        else if (bossId == 7 && bHp <= 12500 && phase == 0) {
            phase += 1;
            bossAtk += 1000;
            bossAction = "<tg-emoji emoji-id=\"" + star4EmojiId + "\">🌟</tg-emoji>" + "Клон Каламитас жгет вас серным лучом!";
        }
        else if (bossId == 8 && turn % 2 == 0) {
            bossAtk += 200;
            bossAction = "<tg-emoji emoji-id=\"" + star2EmojiId + "\">🌟</tg-emoji>" + "Голиаф совершает рывок!";
        }else if (bossId == 8 && bHp <= 2000) {
            bossAtk += 1000;
            bossAction += "<tg-emoji emoji-id=\"" + star4EmojiId + "\">🌟</tg-emoji>" + "ПРОТОКОЛ ИСТРЕБЛЕНИЯ!" + "<tg-emoji emoji-id=\"" + star4EmojiId + "\">🌟</tg-emoji>\n";
        } else if (bossId == 8 && phase == 0) {
            phase += 1;
            bossAtk += 2000;
            bossAction += "<tg-emoji emoji-id=\"" + star3EmojiId + "\">🌟</tg-emoji>" + "!!Запуск протокола истребления!!";
        }


        int userDef = (user.getDefense() == null) ? 5 : user.getDefense();
        int damageToUser = Math.max(1, bossAtk - userDef);
        uHp -= damageToUser; 

        
        if (uHp <= 0) { user.setHp(user.getMaxHp());
            userRepository.save(user);
            String loseText = "<tg-emoji emoji-id=\"" + star7EmojiId + "\">🌟</tg-emoji>" + " ВЫ ПОГИБЛИ!\nБосс #" + bossId + " прикончил вас на " + turn + "-м ходу.";
            var backBtn = InlineKeyboardButton.builder().text("⬅️ Назад").callbackData("boss_menu:" + user.getUserId()).build();
            editMessage(chatId, msgId, loseText, new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(backBtn))));
             return; }
        if (turn % 5 == 0) {
            itemsUsed = "0";
            
            bossAction += "\n<tg-emoji emoji-id=\"" + wow2EmojiId + "\">🌟</tg-emoji>" + " ВАШИ ЛИМИТЫ ПРЕДМЕТОВ СБРОШЕНЫ!";
        }
        
        String battleLog = "<tg-emoji emoji-id=\"" + wow3EmojiId + "\">🌟</tg-emoji>" + " Вы нанесли: -" + damageToBoss + " HP\n" +
                (bossAction.isEmpty() ? "<tg-emoji emoji-id=\"" + wow5EmojiId + "\">🌟</tg-emoji>" + " Босс ударил на: -" + damageToUser + " HP" : bossAction);

        
        
        renderBattleFrame(user, chatId, msgId, bossIdStr,
                String.valueOf(bHp),
                String.valueOf(uHp),
                String.valueOf(turn + 1),
                itemsUsed,
                String.valueOf(phase), 
                battleLog);
        userRepository.save(user);
    }
    private int getBossBaseAttack(int bossId) {
        return switch (bossId) {
            case 1 -> 15;
            case 2 -> 40;
            case 3 -> 100;
            case 4 -> 200;
            case 5 -> 300;
            case 6 -> 400;
            case 7 -> 500;
            case 8 -> 100;
            default -> 5;
        };
    }
    @SneakyThrows
    private void sendCombatInventory(BotUser user, Long chatId, Integer msgId, String bossId, String bHp, String uHp, String turn, String itemsUsed, String queryId, String phase) {
        int used = Integer.parseInt(itemsUsed);

        
        if (used >= 2) {
            sendAlert(queryId, "❌ Лимит предметов (2/2) исчерпан!");
            return;
        }

        String cons = (user.getConsumables() == null) ? "" : user.getConsumables();
        long potionCount = java.util.Arrays.stream(cons.split(","))
                .filter(s -> s.trim().equals("Зелье ХП"))
                .count();

        
        if (potionCount <= 0) {
            sendAlert(queryId, "🎒 У вас нет подходящих предметов!");
            return;
        }

        
        try {
            answerCallback(queryId);
        } catch (Exception ignored) {}

        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text("🧪 Зелье ХП (у вас: " + potionCount + " шт.)")
                
                .callbackData("use_potion:" + bossId + ":" + bHp + ":" + uHp + ":" + turn + ":" + itemsUsed + ":" + phase + ":" + user.getUserId())
                .build()));

        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text("⬅️ Назад к бою")
                .callbackData("b_resume:" + bossId + ":" + bHp + ":" + uHp + ":" + turn + ":" + itemsUsed + ":" + phase + ":" + user.getUserId())
                .build()));

        editMessage(chatId, msgId, "🎒 ИНВЕНТАРЬ БОЯ\nИспользовано: " + used + "/2", new InlineKeyboardMarkup(rows));
    }
    @SneakyThrows
    private void handleUsePotion(BotUser user, Long chatId, Integer msgId, String bId, String bHp, String uHp, String turn, String itemsUsed, String phase, String qId) {
        String cons = user.getConsumables();
        if (cons == null || !cons.contains("Зелье ХП")) return;

        
        try {
            answerCallback(qId);
        } catch (Exception ignored) {}

        
        user.setConsumables(cons.replaceFirst("Зелье ХП,?", ""));
        int maxHp = (user.getMaxHp() == null) ? 100 : user.getMaxHp();
        int newHp = Math.min(maxHp, Integer.parseInt(uHp) + 100);
        int newUsed = Integer.parseInt(itemsUsed) + 1;
        userRepository.save(user);

        
        String currentPhase = (phase == null || phase.isEmpty()) ? "0" : phase;

        renderBattleFrame(user, chatId, msgId, bId, bHp, String.valueOf(newHp), turn,
                String.valueOf(newUsed), currentPhase, "<tg-emoji emoji-id=\"" + star7EmojiId + "\">🌟</tg-emoji>" + " Вы использовали зелье (+100 HP)");
    }
    @SneakyThrows
    private void renderBattleFrame(BotUser user, Long chatId, Integer msgId, String bossId, String bHp, String uHp, String turn, String itemsUsed, String phase, String battleLog) {
        int maxHp = (user.getMaxHp() == null) ? 100 : user.getMaxHp();

        String status = "<tg-emoji emoji-id=\"" + star5EmojiId + "\">🌟</tg-emoji>" + " БИТВА С БОССОМ #" + bossId + "\n\n" +
                battleLog + "\n\n" +
                "<tg-emoji emoji-id=\"" + star6EmojiId + "\">🌟</tg-emoji>" + " ХП Босса: " + bHp + "\n" +
                "<tg-emoji emoji-id=\"" + star6EmojiId + "\">🌟</tg-emoji>" + " Ваше ХП: " + uHp + "/" + maxHp + "\n" +
                "<tg-emoji emoji-id=\"" + star8EmojiId + "\">🌟</tg-emoji>" + " Ход: " + turn + "\n" +
                "<tg-emoji emoji-id=\"" + star8EmojiId + "\">🌟</tg-emoji>" + " Предметы: " + itemsUsed + "/2";

        
        
        var hitBtn = InlineKeyboardButton.builder()
                .text("⚔️ УДАРИТЬ")
                .callbackData("b_hit:" + bossId + ":" + bHp + ":" + uHp + ":" + turn + ":" + itemsUsed + ":" + phase + ":" + user.getUserId())
                .build();

        
        var itemBtn = InlineKeyboardButton.builder()
                .text("🎒 ПРЕДМЕТЫ")
                .callbackData("b_items:" + bossId + ":" + bHp + ":" + uHp + ":" + turn + ":" + itemsUsed + ":" + phase + ":" + user.getUserId())
                .build();

        var markup = new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(hitBtn, itemBtn)));
        editMessage(chatId, msgId, status, markup);
    }

    @SneakyThrows
    private void sendGambleMenu(Long chatId, Integer messageId, Long userId, String userName) {

        String text = "Игрок " + userName + ", выбери действие:";
        EditMessageText message = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .parseMode("HTML")
                .build();


        
        var gambleButton = InlineKeyboardButton.builder().text("🎰 Гембл").callbackData("gamble:" + userId).build();
        var farmButton = InlineKeyboardButton.builder().text("🧑‍🌾 Фарма").callbackData("farm:" + userId).build();
        var fiftyfiftyButton = InlineKeyboardButton.builder().text("🔀 50/50").callbackData("50/50:" + userId).build();
        var multiplierButton = InlineKeyboardButton.builder().text("✖️ Множитель").callbackData("mult_menu:" + userId).build();
        var pokerButton = InlineKeyboardButton.builder()
                .text("🃏 Покер")
                .callbackData("p_start:" + userId) 
                .build();
        var fishButton = InlineKeyboardButton.builder()
                .text("🎣 Рыбалка")
                .callbackData("f_start:" + userId) 
                .build();
        var nextButton = InlineKeyboardButton.builder()
                .text("⏩ Далее")
                .callbackData("nextgm:" + userId)
                .build();





        message.setReplyMarkup(new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(gambleButton, fiftyfiftyButton),
                new InlineKeyboardRow(farmButton, multiplierButton),
                new InlineKeyboardRow(pokerButton, fishButton),
                new InlineKeyboardRow(nextButton)
        )));

        telegramClient.execute(message);
    }

    @SneakyThrows
    private void sendGambleMenu2(Long chatId, Integer messageId, Long userId, String userName) {

        String text = "Игрок " + userName + ", выбери действие:";
        EditMessageText message = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .parseMode("HTML")
                .build();

        var minesButton = InlineKeyboardButton.builder()
                .text("💥 Мины")
                .callbackData("mines_menu:" + userId)
                .build();
        var blackjackButton = InlineKeyboardButton.builder()
                .text("🎭 Блекджек")
                .callbackData("bj_menu:" + userId)
                .build();
        var backButton = InlineKeyboardButton.builder()
                .text("⏪ Назад")
                .callbackData("back:" + userId)
                .build();

        message.setReplyMarkup(new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(minesButton, blackjackButton),
                new InlineKeyboardRow(backButton)
        )));

        telegramClient.execute(message);
    }
    private final Map<Long, AiBattleState> activeAiBattles = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    private static class AiBattleState {
        int playerHp;
        int aiHp;
        int turn;
        int aiHealsUsed;
        int aiAbility1Cooldown;
        int aiAbility2Cooldown;
        int aiHealWarnings;
        boolean waitingForAi;
        int itemsUsed;
    }



    @SneakyThrows
    private void sendAiBattleMenu(Long chatId, Integer msgId, Long userId, BotUser user) {
        String text = " <b>Бой с ИИ</b>\n\n"
                + "Противник: <b>Гемби</b>\n"
                + "HP противника: <b>45000</b>\n"
                + "Вход: <b>2000 UC</b>\n"
                + "Победа: <b>5000 UC</b>\n\n"
                + "Ваш баланс: <b>" + user.getBalance() + " UC</b>";

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("⚔️ Начать бой")
                                .callbackData("ai_start:" + userId)
                                .build()
                ),
                new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("⬅️ Назад")
                                .callbackData("back2:" + userId)
                                .build()
                )
        ));

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();
        telegramClient.execute(edit);
    }



    @SneakyThrows
    private void handleAiBattleStart(Long chatId, Integer msgId, Long userId, BotUser user) {
        if (user.getBalance() < 2000) {
            editMessage(chatId, msgId,
                    "❌ <b>Недостаточно средств.</b>\n\nНужно 2000 UC. Ваш баланс: " + user.getBalance() + " UC",
                    new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text("⬅️ Назад")
                                    .callbackData("ai_menu:" + userId)
                                    .build()
                    ))));
            return;
        }

        long now = Instant.now().getEpochSecond();
        if (user.getLastBossfightTime() != null && now - user.getLastBossfightTime() < 3600) {
            long remaining = 3600 - (now - user.getLastBossfightTime());
            long mins = remaining / 60;
            long secs = remaining % 60;
            editMessage(chatId, msgId,
                    "⏳ <b>Кулдаун!</b>\n\nПодождите ещё " + mins + "м " + secs + "с",
                    new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text("⬅️ Назад")
                                    .callbackData("ai_menu:" + userId)
                                    .build()
                    ))));
            return;
        }

        user.setBalance(user.getBalance() - 2000);
        userRepository.save(user);

        int maxHp = (user.getMaxHp() == null) ? 100 : user.getMaxHp();
        AiBattleState state = new AiBattleState(maxHp, 45000, 1, 0, 0, 0, 0, false, 0);
        activeAiBattles.put(userId, state);

        renderAiBattleFrame(chatId, msgId, userId, user, state, "⚔️ Бой начался! Ваш ход.");
    }
    @SneakyThrows
    private void sendAiCombatInventory(Long chatId, Integer msgId, Long userId, BotUser user, String queryId) {
        AiBattleState state = activeAiBattles.get(userId);
        if (state == null) return;

        if (state.getItemsUsed() >= 2) {
            sendAlert(queryId, "❌ Лимит предметов (2/2) исчерпан!");
            return;
        }

        String cons = (user.getConsumables() == null) ? "" : user.getConsumables();
        long potionCount = Arrays.stream(cons.split(","))
                .filter(s -> s.trim().equals("Зелье ХП"))
                .count();

        if (potionCount <= 0) {
            sendAlert(queryId, "🎒 У вас нет подходящих предметов!");
            return;
        }

        try { answerCallback(queryId); } catch (Exception ignored) {}

        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("🧪 Зелье ХП (у вас: " + potionCount + " шт.)")
                        .callbackData("ai_use_potion:" + userId)
                        .build()
        ));
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("⬅️ Назад к бою")
                        .callbackData("ai_items_back:" + userId)
                        .build()
        ));

        editMessage(chatId, msgId,
                "🎒 <b>ИНВЕНТАРЬ БОЯ</b>\nИспользовано: " + state.getItemsUsed() + "/2",
                new InlineKeyboardMarkup(rows));
    }

    @SneakyThrows
    private void handleAiUsePotion(Long chatId, Integer msgId, Long userId, BotUser user) {
        AiBattleState state = activeAiBattles.get(userId);
        if (state == null) return;

        String cons = user.getConsumables();
        if (cons == null || !cons.contains("Зелье ХП")) {
            renderAiBattleFrame(chatId, msgId, userId, user, state, "❌ Зелье не найдено.");
            return;
        }

        user.setConsumables(cons.replaceFirst("Зелье ХП,?", ""));
        int maxHp = (user.getMaxHp() == null) ? 100 : user.getMaxHp();
        state.setPlayerHp(Math.min(maxHp, state.getPlayerHp() + 100));
        state.setItemsUsed(state.getItemsUsed() + 1);
        userRepository.save(user);

        renderAiBattleFrame(chatId, msgId, userId, user, state,
                "🧪 Вы использовали зелье! Восстановлено <b>100 HP</b>.");
    }



    @SneakyThrows
    private void renderAiBattleFrame(Long chatId, Integer msgId, Long userId,
                                     BotUser user, AiBattleState state, String log) {
        int maxHp = (user.getMaxHp() == null) ? 100 : user.getMaxHp();

        String text = "<tg-emoji emoji-id=\"" + star5EmojiId + "\">🌟</tg-emoji>" + " <b>БОЙ С ИИ — Гемби</b>\n\n"
                + log + "\n\n"
                + "<tg-emoji emoji-id=\"" + star6EmojiId + "\">🌟</tg-emoji>" + "Ваше HP: <b>" + state.getPlayerHp() + "/" + maxHp + "</b>\n"
                + "<tg-emoji emoji-id=\"" + star6EmojiId + "\">🌟</tg-emoji>" + "HP Гемби: <b>" + state.getAiHp() + "/45000</b>\n"
                + "<tg-emoji emoji-id=\"" + star8EmojiId + "\">🌟</tg-emoji>" + "Ход: <b>" + state.getTurn() + "</b>\n"
                + "<tg-emoji emoji-id=\"" + star8EmojiId + "\">🌟</tg-emoji>" + "Предметы: <b>" + state.getItemsUsed() + "/2</b>";

        InlineKeyboardMarkup markup;

        if (state.isWaitingForAi()) {
            markup = new InlineKeyboardMarkup(List.of(
                    new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text("⏳ Гемби думает...")
                                    .callbackData("ai_ignore:" + userId)
                                    .build()
                    )
            ));
        } else {
            var hitBtn = InlineKeyboardButton.builder()
                    .text("⚔️ УДАРИТЬ")
                    .callbackData("ai_hit:" + userId)
                    .build();
            var itemBtn = InlineKeyboardButton.builder()
                    .text("🎒 ПРЕДМЕТЫ")
                    .callbackData("ai_items:" + userId)
                    .build();
            markup = new InlineKeyboardMarkup(List.of(
                    new InlineKeyboardRow(hitBtn, itemBtn)
            ));
        }

        editMessage(chatId, msgId, text, markup);
    }




    @SneakyThrows
    private void handleAiBattleHit(Long chatId, Integer msgId, Long userId, BotUser user) {
        AiBattleState state = activeAiBattles.get(userId);
        if (state == null) {
            sendAiBattleMenu(chatId, msgId, userId, user);
            return;
        }

        if (state.isWaitingForAi()) return;

        int userAtk = (user.getAttack() == null) ? 10 : user.getAttack();
        int damage = userAtk + ThreadLocalRandom.current().nextInt(0, 6);
        state.setAiHp(state.getAiHp() - damage);

        
        if (state.getAiHp() <= 0) {
            activeAiBattles.remove(userId);
            user.setBalance(user.getBalance() + 5000);
            user.setLastBossfightTime(Instant.now().getEpochSecond());
            userRepository.save(user);

            editMessage(chatId, msgId,
                    "🏆 <b>ПОБЕДА!</b>\n\nВы победили Гемби!\n+5000 UC\nБаланс: " + user.getBalance() + " UC",
                    new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text("⬅️ В меню")
                                    .callbackData("back2:" + userId)
                                    .build()
                    ))));
            return;
        }

        
        state.setWaitingForAi(true);
        String playerLog =  "<tg-emoji emoji-id=\"" + wow3EmojiId + "\">🌟</tg-emoji>" + " Вы нанесли <b>" + damage + "</b> урона Гемби.";
        renderAiBattleFrame(chatId, msgId, userId, user, state, playerLog + "\n⏳ Гемби думает над ответом...");

        
        new Thread(() -> {
            try {
                String aiDecision = askAiForAction(state);
                String aiLog = processAiAction(state, aiDecision);
                String action = aiDecision.trim().toUpperCase();

                
                if (state.getTurn() % 5 == 0) {
                    state.setAiHealsUsed(0);
                }

                
                if (state.getAiAbility1Cooldown() > 0) state.setAiAbility1Cooldown(state.getAiAbility1Cooldown() - 1);
                if (state.getAiAbility2Cooldown() > 0) state.setAiAbility2Cooldown(state.getAiAbility2Cooldown() - 1);

                state.setTurn(state.getTurn() + 1);

                
                if (action.startsWith("ATTACK")) {
                    int userDef = (user.getDefense() == null) ? 5 : user.getDefense();
                    int aiDamage = Math.max(1, 400 - userDef);
                    state.setPlayerHp(state.getPlayerHp() - aiDamage);
                    aiLog +=  "\n <tg-emoji emoji-id=\"" + wow4EmojiId + "\">🌟</tg-emoji>" + "Гемби атакует! Вы получаете <b>" + aiDamage + "</b> урона.";
                } else if (action.startsWith("USE1") && state.getAiAbility1Cooldown() == 0) {
                    int userDef = (user.getDefense() == null) ? 5 : user.getDefense();
                    int aiDamage = Math.max(1, 500 - userDef);
                    state.setPlayerHp(state.getPlayerHp() - aiDamage);
                    aiLog += "\n <tg-emoji emoji-id=\"" + star3EmojiId + "\">🌟</tg-emoji>" + "ДЕНЕЖНЫЙ ЗАЛП! Вы получаете <b>" + aiDamage + "</b> урона!";
                }

                state.setWaitingForAi(false);

                
                if (state.getPlayerHp() <= 0) {
                    activeAiBattles.remove(userId);
                    int maxHp = (user.getMaxHp() == null) ? 100 : user.getMaxHp();
                    user.setHp(maxHp);
                    user.setLastBossfightTime(Instant.now().getEpochSecond());
                    userRepository.save(user);
                    editMessage(chatId, msgId,
                            " <b>ВЫ ПОГИБЛИ!</b>\n\nГемби прикончил вас на " + state.getTurn() + "-м ходу.\n-2000 UC (вход не возвращается)",
                            new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                                    InlineKeyboardButton.builder()
                                            .text("⬅️ В меню")
                                            .callbackData("back:" + userId)
                                            .build()
                            ))));
                    return;
                }

                String fullLog = playerLog + "\n" + aiLog;
                renderAiBattleFrame(chatId, msgId, userId, user, state, fullLog);

            } catch (Exception e) {
                e.printStackTrace();
                state.setWaitingForAi(false);
                renderAiBattleFrame(chatId, msgId, userId, user, state,
                        "⚠️ Ошибка связи с Гемби. Ваш ход.");
            }
        }).start();
    }


private final HttpClient httpClient = HttpClient.newHttpClient();
    private String askAiForAction(AiBattleState state) throws Exception {
        String healAvailable = (state.getAiHealsUsed() < 2) ? "да" : "нет";
        String use1Available = (state.getAiAbility1Cooldown() == 0) ? "да" : "нет";
        String model = "llama-3.3-70b-versatile";
        String groqApiKey = System.getenv("GROQ_TOKEN");
        String use2Available = (state.getAiAbility2Cooldown() == 0) ? "да" : "нет";

        double aiHpPercent = (double) state.getAiHp() / 45000.0 * 100;

        
        
        String forcedDecision = null;

        
        if (aiHpPercent < 30) {
            if (state.getAiAbility2Cooldown() == 0) forcedDecision = "USE2";
            else if (state.getAiHealsUsed() < 2) forcedDecision = "HEAL";
        }
        
        else if (aiHpPercent < 60 && state.getTurn() % 3 == 0) {
            if (state.getAiHealsUsed() < 2) forcedDecision = "HEAL";
        }
        
        if (forcedDecision == null && state.getAiAbility1Cooldown() == 0 && state.getTurn() % 10 == 0) {
            forcedDecision = "USE1";
        }

        
        if (forcedDecision != null) {
            System.out.println("AI decision (forced): " + forcedDecision);
            return forcedDecision;
        }

        
        String prompt = "Ты — боевой ИИ Гемби. Выбери одно действие.\n\n"
                + "Правила выбора:\n"
                + "- Если твоё HP ниже 30% и HEAL доступен → HEAL\n"
                + "- Если твоё HP ниже 50% и USE2 доступна → USE2\n"
                + "- Если USE1 доступна → USE1\n"
                + "- Иначе → ATTACK\n\n"
                + "Твоё HP: " + state.getAiHp() + "/45000 (" + String.format("%.0f", aiHpPercent) + "%)\n"
                + "HP игрока: " + state.getPlayerHp() + "\n"
                + "HEAL доступен: " + healAvailable + "\n"
                + "USE1 доступна: " + use1Available + "\n"
                + "USE2 доступна: " + use2Available + "\n"
                + "Ход: " + state.getTurn() + "\n\n"
                + "Ответь ТОЛЬКО одним словом: ATTACK, HEAL, USE1 или USE2"
                + "Старайся разнообразивать свои ходы а не только атаковать, старайся использовать лимиты лечения по максимуму";

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                "Ты боевой ИИ. Отвечаешь строго одним словом из: ATTACK, HEAL, USE1, USE2. Без объяснений."));
        messages.add(Map.of("role", "user", "content", prompt));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 5);
        requestBody.put("temperature", 0.1); 

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Groq error body: " + response.body());
            return "ATTACK";
        }

        JsonNode root = objectMapper.readTree(response.body());
        String raw = root.path("choices").get(0).path("message").path("content").asText().trim().toUpperCase();

        String decision = "ATTACK";
        for (String word : raw.split("\\s+")) {
            String cleaned = word.replaceAll("[^A-Z0-9]", "");
            if (cleaned.equals("ATTACK") || cleaned.equals("HEAL")
                    || cleaned.equals("USE1") || cleaned.equals("USE2")) {
                decision = cleaned;
                break;
            }
        }

        System.out.println("AI decision: " + decision + " | raw: " + raw);
        return decision;
    }











    private String processAiAction(AiBattleState state, String decision) {
        if (decision.startsWith("HEAL")) {
            if (state.getAiHealsUsed() >= 2) {
                state.setAiHealWarnings(state.getAiHealWarnings() + 1);
                return "⚠️ Гемби попытался использовать HEAL сверх лимита! Предупреждение #" + state.getAiHealWarnings() + ". Действие пропущено.";
            }
            state.setAiHp(Math.min(45000, state.getAiHp() + 100));
            state.setAiHealsUsed(state.getAiHealsUsed() + 1);
            return "\n <tg-emoji emoji-id=\"" + star1EmojiId + "\">🌟</tg-emoji>" + " Гемби использует лечение! Восстановлено <b>100 HP</b>. (хилов использовано: " + state.getAiHealsUsed() + "/2)";

        } else if (decision.startsWith("USE1")) {
            if (state.getAiAbility1Cooldown() > 0) {
                return "⚠️ Гемби попытался использовать USE1, но способность на кулдауне! Действие пропущено.";
            }
            state.setAiAbility1Cooldown(10);
            return "\n <tg-emoji emoji-id=\"" + star3EmojiId + "\">🌟</tg-emoji>" + " Гемби использует <b>ДЕНЕЖНЫЙ ЗАЛП</b>! Урон: <b>500</b>";

        } else if (decision.startsWith("USE2")) {
            if (state.getAiAbility2Cooldown() > 0) {
                return "⚠️ Гемби попытался использовать USE2, но способность на кулдауне! Действие пропущено.";
            }
            state.setAiHp(Math.min(45000, state.getAiHp() + 300));
            state.setAiAbility2Cooldown(10);
            return "<tg-emoji emoji-id=\"" + star2EmojiId + "\">🌟</tg-emoji>" + " Гемби использует <b>Регенерацию</b>! Восстановлено <b>300 HP</b>.";

        } else {
            
            return "";
        }
    }


}

