package com.klasix12.tgbot.service;


import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

public interface RockPaperScissorsService {
    String rockPaperScissorsGame(String userFigure, User tgUser);
}
