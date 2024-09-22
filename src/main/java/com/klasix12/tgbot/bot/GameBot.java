package com.klasix12.tgbot.bot;

import com.klasix12.tgbot.dto.RouletteResult;
import com.klasix12.tgbot.service.GameBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberRestricted;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameBot extends TelegramLongPollingBot {
    private final String botUsername;
    private static final Logger LOG = LoggerFactory.getLogger(GameBot.class);
    private static final String START = "/start";
    private static final String INFO = "/info";
    private static final String ROULETTE = "/roulette";
    private static final String STATS = "/stats";
    private static final List<String> FIGURES = new ArrayList<>(List.of("\uD83E\uDEA8", "✂️", "\uD83D\uDCC3"));
    private final GameBotService gameBotService;

    public GameBot(@Value("${bot.token}") String botToken, @Value("${bot.username}") String botUsername, GameBotService gameBotService) {
        super(botToken);
        this.botUsername = botUsername;
        this.gameBotService = gameBotService;
        registerBotCommand();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String messageText = update.getMessage().getText();
        Message message = update.getMessage();
        User user = update.getMessage().getFrom();
        if (isGetInfoCommand(messageText)) {
            String rouletteRules = """
                    Игра в русскую рулетку.
                    Испытай свою удачу.
                    /roulette""";
            sendReplyMessage(message, rouletteRules);
            String RPS_rules = """
                    Игра камень ножницы бумага.
                    Чтобы играть, отправь:
                    \uD83E\uDEA8 - камень
                    ✂️ - ножницы
                    \uD83D\uDCC3 - бумага""";
            sendReplyMessage(message, RPS_rules);
        } else if (FIGURES.contains(messageText)) {
            String text = gameBotService.rockPaperScissorsGame(messageText, user);
            sendReplyMessage(message, text);
        } else if (isGetRouletteCommand(messageText)) {
            RouletteResult rouletteResult = gameBotService.rouletteGame(user);
            if (rouletteResult.isWin()) {
                sendReplyMessage(message, rouletteResult.getText());
            } else {
                sendReplyMessage(message, rouletteResult.getText());
                handleUserLoseRouletteGame(message);
            }
        } else if (isGetStatsCommand(messageText)) {
            String text = gameBotService.gameStats(user);
            sendReplyMessage(message, text);
        }
    }

    private void handleUserLoseRouletteGame(Message message) {
        ChatPermissions chatPermissions = getUserPermissions(message);
        try {
            if (chatPermissions == null) {
                return;
            }
            chatPermissions.setCanSendMessages(false);
            RestrictChatMember restrictChatMember = RestrictChatMember.builder()
                    .chatId(message.getChatId())
                    .userId(message.getFrom().getId())
                    .permissions(chatPermissions)
                    .useIndependentChatPermissions(true).build();
            long muteUntil = message.getDate() + 60;
            restrictChatMember.setUntilDate((int) muteUntil);
            execute(restrictChatMember);
        } catch (TelegramApiException e) {
            LOG.error(e.getMessage());
        }

    }

    private ChatPermissions getUserPermissions(Message message) {
        GetChatMember getChatMember = GetChatMember.builder().chatId(message.getChatId()).userId(message.getFrom().getId()).build();

        try {
            ChatMember chatMember = execute(getChatMember);
            if (chatMember.getStatus().equals("member")) {
                return ChatPermissions.builder()
                        .canSendMessages(true)
                        .canSendAudios(true)
                        .canSendDocuments(true)
                        .canSendPhotos(true)
                        .canSendVideos(true)
                        .canSendVideoNotes(true)
                        .canSendVoiceNotes(true)
                        .canSendPolls(true)
                        .canSendOtherMessages(true)
                        .canAddWebPagePreviews(true)
                        .canChangeInfo(true)
                        .canInviteUsers(true)
                        .canPinMessages(true)
                        .canManageTopics(true).build();
            } else if (chatMember.getStatus().equals("restricted")) {
                ChatMemberRestricted restrictedMember = (ChatMemberRestricted) chatMember;
                return ChatPermissions.builder()
                        .canSendMessages(restrictedMember.getCanSendMessages())
                        .canSendAudios(restrictedMember.getCanSendAudios())
                        .canSendDocuments(restrictedMember.getCanSendDocuments())
                        .canSendPhotos(restrictedMember.getCanSendPhotos())
                        .canSendVideos(restrictedMember.getCanSendVideos())
                        .canSendVideoNotes(restrictedMember.getCanSendVideoNotes())
                        .canSendVoiceNotes(restrictedMember.getCanSendVoiceNotes())
                        .canSendPolls(restrictedMember.getCanSendPolls())
                        .canSendOtherMessages(restrictedMember.getCanSendOtherMessages())
                        .canAddWebPagePreviews(restrictedMember.getCanAddWebpagePreviews())
                        .canChangeInfo(restrictedMember.getCanChangeInfo())
                        .canInviteUsers(restrictedMember.getCanInviteUsers())
                        .canPinMessages(restrictedMember.getCanPinMessages())
                        .canManageTopics(restrictedMember.getCanManageTopics()).build();
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void registerBotCommand() {
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/info", "Получить информацию"));
        botCommandList.add(new BotCommand("/roulette", "Игра в рулетку"));
        botCommandList.add(new BotCommand("/stats", "Получить статистику по играм"));

        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            LOG.error(e.getMessage());
        }
    }

    private boolean isGetInfoCommand(String messageText) {
        return messageText.equals(START) || messageText.equals(INFO) ||
                messageText.equals(START + "@" + getBotUsername()) ||
                messageText.equals(INFO + "@" + getBotUsername());
    }

    private boolean isGetRouletteCommand(String messageText) {
        return messageText.equals(ROULETTE) || messageText.equals(ROULETTE + "@" + getBotUsername());
    }

    private boolean isGetStatsCommand(String messageText) {
        return messageText.equals(STATS) || messageText.equals(STATS + "@" + getBotUsername());
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

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}
