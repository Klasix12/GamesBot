package com.klasix12.tgbot.bot;

import com.klasix12.tgbot.service.RockPaperScissorsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
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
        registerBotCommand();
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
        if (isGetInfoCommand(messageText)) {
            sendReplyMessage(message, RPS_rules);
        } else if (FIGURES.contains(messageText)) {
            String text = rockPaperScissorsService.rockPaperScissorsGame(messageText, user);
            sendReplyMessage(message, text);
        }
    }

    public void registerBotCommand() {
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/info", "Получить информацию"));

        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            LOG.error(e.getMessage());
        }
    }

    public boolean isGetInfoCommand(String messageText) {
        return messageText.equals(START) ||
                messageText.equals(INFO) ||
                messageText.equals(START + "@" + getBotUsername()) ||
                messageText.equals(INFO + "@" + getBotUsername());
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
