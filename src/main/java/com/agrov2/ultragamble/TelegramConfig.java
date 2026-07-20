package com.agrov2.ultragamble;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient; // проверь свой импорт

@Configuration // Говорит Спрингу, что внутри этого класса создаются Бины
public class TelegramConfig {

    @Value("${BOT_TOKEN}") // Spring сам вытащит токен из переменных окружения или application.properties
    private String botToken;

    @Bean // Теперь TelegramClient официально становится Спринг-Бином!
    public TelegramClient telegramClient() {
        if (botToken == null || botToken.isEmpty()) {
            throw new RuntimeException("Ошибка: Переменная окружения BOT_TOKEN не установлена!");
        }
        return new OkHttpTelegramClient(botToken);
    }
}
