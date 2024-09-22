package com.klasix12.tgbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RouletteResult {
    private boolean isWin;
    private String text;
}
