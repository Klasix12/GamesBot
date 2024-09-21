package com.klasix12.tgbot.bot;

import com.klasix12.tgbot.service.RockPaperScissorsService;
import com.klasix12.tgbot.service.impl.RockPaperScissorsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Dice;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class RockPaperScissorsBot extends TelegramLongPollingBot {
    private final String botUsername;
    private static final Logger LOG = LoggerFactory.getLogger(RockPaperScissorsBot.class);
    private static final String START = "/start";
    private static final String INFO = "/info";
    private static final List<String> FIGURES = new ArrayList<>(List.of("\uD83E\uDEA8", "✂️", "\uD83D\uDCC3"));
    private final String RPS_rules = "Игра камень ножницы бумага.\n" +
            "Чтобы играть, отправь:\n" +
            "\uD83E\uDEA8 - камень\n" +
            "✂️ - ножницы\n" +
            "\uD83D\uDCC3 - бумага";
    private final RockPaperScissorsService rockPaperScissorsService;

    public RockPaperScissorsBot(@Value("${bot.token}") String botToken, @Value("${bot.username}") String botUsername, RockPaperScissorsService rockPaperScissorsService) {
        super(botToken);
        this.botUsername = botUsername;
        this.rockPaperScissorsService = rockPaperScissorsService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String messageText = update.getMessage().getText();
        Message message = update.getMessage();
        User user = update.getMessage().getFrom();
        switch (messageText) {
            case START, INFO -> sendReplyMessage(message, RPS_rules);
            default -> {
                if (FIGURES.contains(messageText)) {
                    String text = rockPaperScissorsService.rockPaperScissorsGame(messageText, user);
                    sendReplyMessage(message, text);
                }
            }
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    private void sendReplyMessage(Message message, String text) {
        String chatId = message.getChatId().toString();
        SendMessage sendMessage = new SendMessage(chatId, text);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(message.getMessageId());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error(e.getMessage());
        }
    }
}
