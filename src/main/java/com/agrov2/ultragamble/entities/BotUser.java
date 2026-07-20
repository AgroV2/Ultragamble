package com.agrov2.ultragamble.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bot_users")
@Getter @Setter
public class BotUser {
    @Id
    private Long userId;
    private String displayName;
    private long balance = 1000;
    private long lastGambleTime;
    private long lastFiftyFiftyTime;
    private long lastFarmTime;
    private Long lastMultiplyTime = 0L;
    private long lastFishingTime = 0L;
    private Long FarmUpgrade = 0L;
    public long getFarmUpgradeSafe() {
        return FarmUpgrade == null ? 0L : FarmUpgrade;
    }
    private Long nextUpgradePrice = 1000L;
    public long getNextUpgradePriceSafe() {
        return nextUpgradePrice == null ? 1000L : nextUpgradePrice;
    }
    private Long lastBossfightTime = 0L;
    private long lastPokerTime = 0L;
    private Integer gambleUpgrade = 0;
    public Integer getGambleUpgrade() {
        return gambleUpgrade == null ? 0 : gambleUpgrade;
    }
    private Integer hp = 2500;
    private Integer maxHp = 2500;
    private Integer attack = 10;
    private Integer defense = 5;
    private String weapon = "Кулаки";
    private String armor = "Рубаха";
    private Integer bossLevel = 0;
    private String inventory = "Кулаки,Рубаха";
    private String consumables = "";
    private String aiProvider = "ANYMODEL";
    private String aiModel = "cc/claude-opus-4-8";
}

