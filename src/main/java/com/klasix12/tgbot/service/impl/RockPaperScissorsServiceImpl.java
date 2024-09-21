package com.klasix12.tgbot.service.impl;

import com.klasix12.tgbot.model.CustomUser;
import com.klasix12.tgbot.repository.CustomUserRepository;
import com.klasix12.tgbot.service.RockPaperScissorsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;

@Service
public class RockPaperScissorsServiceImpl implements RockPaperScissorsService {
    private final Random random = new Random();
    private final List<String> figures = List.of("\uD83E\uDEA8", "✂️", "\uD83D\uDCC3");
    private final CustomUserRepository customUserRepository;
    private final String botUsername;

    public RockPaperScissorsServiceImpl(@Value("${bot.username}") String botUsername, CustomUserRepository customUserRepository) {
        this.botUsername = botUsername;
        this.customUserRepository = customUserRepository;
    }

    @Override
    public String rockPaperScissorsGame(String userFigure, User tgUser) {
        String botFigure = figures.get(random.nextInt(figures.size()));

        CustomUser customUser = customUserRepository.findById(tgUser.getId()).orElse(
                CustomUser.builder()
                        .id(tgUser.getId())
                        .build()
        );

        String answer;
        if (userFigure.equals(botFigure)) {
            answer = String.format("""
                    Бот выбрал %s
                    Ничья.
                    Счет: %s %s : %s %s
                    """, botFigure, botUsername, customUser.getLoses(), customUser.getWins(), tgUser.getUserName());
        } else if (isBotWin(userFigure, botFigure)) {
            customUser.setLoses(customUser.getLoses() + 1);
            answer = String.format("""
                    Бот выбрал %s
                    Вы проиграли.
                    Счет: %s %s : %s %s
                    """, botFigure, botUsername, customUser.getLoses(), customUser.getWins(), tgUser.getUserName());
        } else {
            customUser.setWins(customUser.getWins() + 1);
            answer = String.format("""
                    Бот выбрал %s
                    Вы выиграли.
                    Счет: %s %s : %s %s
                    """, botFigure, botUsername, customUser.getLoses(), customUser.getWins(), tgUser.getUserName());
        }
        customUserRepository.save(customUser);
        return answer;
    }

    private boolean isBotWin(String userFigure, String botFigure) {
        return botFigure.equals("\uD83E\uDEA8") && userFigure.equals("✂️") ||
                botFigure.equals("✂️") && userFigure.equals("\uD83D\uDCC3") ||
                botFigure.equals("\uD83D\uDCC3") && userFigure.equals("\uD83E\uDEA8");
    }
}
