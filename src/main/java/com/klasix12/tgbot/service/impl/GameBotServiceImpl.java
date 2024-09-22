package com.klasix12.tgbot.service.impl;

import com.klasix12.tgbot.dto.RouletteResult;
import com.klasix12.tgbot.model.CustomUser;
import com.klasix12.tgbot.repository.CustomUserRepository;
import com.klasix12.tgbot.service.GameBotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Random;

@Service
public class GameBotServiceImpl implements GameBotService {
    private final Random random = new Random();
    private final List<String> figures = List.of("\uD83E\uDEA8", "✂️", "\uD83D\uDCC3");
    private final CustomUserRepository customUserRepository;
    private final String botUsername;

    public GameBotServiceImpl(@Value("${bot.username}") String botUsername, CustomUserRepository customUserRepository) {
        this.botUsername = botUsername;
        this.customUserRepository = customUserRepository;
    }

    @Override
    public String rockPaperScissorsGame(String userFigure, User tgUser) {
        String botFigure = figures.get(random.nextInt(figures.size()));

        CustomUser customUser = customUserRepository.findById(tgUser.getId()).orElse(
                CustomUser.builder()
                        .id(tgUser.getId())
                        .username(tgUser.getUserName())
                        .build()
        );

        String answer;
        if (userFigure.equals(botFigure)) {
            answer = String.format("""
                    Бот выбрал %s
                    Ничья.
                    Счет: %s %s : %s %s
                    """, botFigure, botUsername, customUser.getRPSloses(), customUser.getRPSwins(), tgUser.getUserName());
        } else if (isBotWin(userFigure, botFigure)) {
            customUser.setRPSloses(customUser.getRPSloses() + 1);
            answer = String.format("""
                    Бот выбрал %s
                    Вы проиграли.
                    Счет: %s %s : %s %s
                    """, botFigure, botUsername, customUser.getRPSloses(), customUser.getRPSwins(), tgUser.getUserName());
        } else {
            customUser.setRPSwins(customUser.getRPSwins() + 1);
            answer = String.format("""
                    Бот выбрал %s
                    Вы выиграли.
                    Счет: %s %s : %s %s
                    """, botFigure, botUsername, customUser.getRPSloses(), customUser.getRPSwins(), tgUser.getUserName());
        }
        customUserRepository.save(customUser);
        return answer;
    }

    @Override
    public RouletteResult rouletteGame(User tgUser) {
        int randomInt = random.nextInt(6);
        RouletteResult rouletteResult = new RouletteResult();
        CustomUser customUser = customUserRepository.findById(tgUser.getId()).orElse(
                CustomUser.builder()
                        .id(tgUser.getId())
                        .username(tgUser.getUserName())
                        .build());
        if (randomInt == 3) {
            rouletteResult.setWin(false);
            rouletteResult.setText(tgUser.getUserName() + " крутит барабан, подносит револьвер к виску и нажимает на курок...\n" +
                    "БАБАХ!!! Происходит выстрел.");
            customUser.setRouletteLoses(customUser.getRouletteLoses() + 1);
        } else {
            rouletteResult.setWin(true);
            rouletteResult.setText(tgUser.getUserName() + " крутит барабан, подносит револьвер к виску и нажимает на курок...\n" +
                    "Слышен щелчок. Ты пережил эту игру.");
            customUser.setRouletteWins(customUser.getRouletteWins() + 1);
        }
        customUserRepository.save(customUser);
        return rouletteResult;
    }

    @Override
    public String gameStats(User tgUser) {
        CustomUser customUser = customUserRepository.findById(tgUser.getId()).orElse(
                CustomUser.builder()
                        .id(tgUser.getId())
                        .username(tgUser.getUserName())
                        .build()
        );
        return String.format("""
                Статистика по играм:
                Камень Ножницы Бумага: Победы: %s Поражения: %s
                Рулетка: Победы: %s Поражения: %s
                """, customUser.getRPSwins(), customUser.getRPSloses(), customUser.getRouletteWins(), customUser.getRouletteLoses());
    }

    private boolean isBotWin(String userFigure, String botFigure) {
        return botFigure.equals("\uD83E\uDEA8") && userFigure.equals("✂️") ||
                botFigure.equals("✂️") && userFigure.equals("\uD83D\uDCC3") ||
                botFigure.equals("\uD83D\uDCC3") && userFigure.equals("\uD83E\uDEA8");
    }
}
