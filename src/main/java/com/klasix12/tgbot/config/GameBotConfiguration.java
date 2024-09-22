package com.klasix12.tgbot.config;

import com.klasix12.tgbot.bot.GameBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class GameBotConfiguration {
    @Bean
    public TelegramBotsApi telegramBotsApi(GameBot gameBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(gameBot);
        return api;
    }
}
