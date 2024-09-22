package com.klasix12.tgbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomUser {
    @Id
    private long id;
    private String username;
    private int RPSwins;
    private int RPSloses;
    private int rouletteWins;
    private int rouletteLoses;
}
