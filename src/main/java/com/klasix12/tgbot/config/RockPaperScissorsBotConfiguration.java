package com.klasix12.tgbot.config;

import com.klasix12.tgbot.bot.RockPaperScissorsBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class RockPaperScissorsBotConfiguration {
    @Bean
    public TelegramBotsApi telegramBotsApi(RockPaperScissorsBot rockPaperScissorsBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(rockPaperScissorsBot);
        return api;
    }
}
