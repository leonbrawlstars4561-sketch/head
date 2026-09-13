package com.example.vaultbalance;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Locale;

public class BalanceScoreboardTask extends BukkitRunnable {

```
private static final String OBJECTIVE_NAME = "vaultbal";

private final Plugin plugin;
private final Economy economy;

public BalanceScoreboardTask(Plugin plugin, Economy economy) {
    this.plugin = plugin;
    this.economy = economy;
}

@Override
public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
        updatePlayer(player);
    }
}

private void updatePlayer(Player player) {
    Scoreboard board = player.getScoreboard();

    if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(board);
    }

    Objective objective = board.getObjective(OBJECTIVE_NAME);

    if (objective == null) {
        objective = board.registerNewObjective(
                OBJECTIVE_NAME,
                "dummy",
                Component.empty()
        );

        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    for (Player target : Bukkit.getOnlinePlayers()) {
        double balance = economy.getBalance(target);

        int scoreValue;

        if (balance > Integer.MAX_VALUE) {
            scoreValue = Integer.MAX_VALUE;
        } else if (balance < Integer.MIN_VALUE) {
            scoreValue = Integer.MIN_VALUE;
        } else {
            scoreValue = (int) balance;
        }

        Score score = objective.getScore(target.getName());
        score.setScore(scoreValue);

        String abbreviated = abbreviate(balance);

        Component numberComponent = Component.text("$", NamedTextColor.GREEN)
                .append(Component.text(" ", NamedTextColor.WHITE))
                .append(Component.text(abbreviated, NamedTextColor.WHITE));

        score.numberFormat(NumberFormat.fixed(numberComponent));
    }
}

private String abbreviate(double amount) {
    double abs = Math.abs(amount);

    String suffix;
    double value;

    if (abs >= 1_000_000_000_000L) {
        value = amount / 1_000_000_000_000.0;
        suffix = "T";
    } else if (abs >= 1_000_000_000L) {
        value = amount / 1_000_000_000.0;
        suffix = "B";
    } else if (abs >= 1_000_000L) {
        value = amount / 1_000_000.0;
        suffix = "M";
    } else if (abs >= 1_000L) {
        value = amount / 1_000.0;
        suffix = "K";
    } else {
        return String.valueOf((long) amount);
    }

    double truncated;

    if (value >= 0) {
        truncated = Math.floor(value * 10.0) / 10.0;
    } else {
        truncated = Math.ceil(value * 10.0) / 10.0;
    }

    String formatted = String.format(Locale.US, "%.1f", truncated);

    if (formatted.endsWith(".0")) {
        formatted = formatted.substring(0, formatted.length() - 2);
    }

    return formatted + suffix;
}

public void cleanupAll() {
    for (Player player : Bukkit.getOnlinePlayers()) {
        Scoreboard board = player.getScoreboard();

        if (board == null) {
            continue;
        }

        Objective objective = board.getObjective(OBJECTIVE_NAME);

        if (objective != null) {
            objective.unregister();
        }
    }
}
```

}
