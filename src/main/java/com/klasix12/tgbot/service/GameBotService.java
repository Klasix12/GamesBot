package com.klasix12.tgbot.service;


import com.klasix12.tgbot.dto.RouletteResult;
import org.telegram.telegrambots.meta.api.objects.User;

public interface GameBotService {
    String rockPaperScissorsGame(String userFigure, User tgUser);

    RouletteResult rouletteGame(User tgUser);
    String gameStats(User tgUser);
}
