package com.example.vaultbalance;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public class BalanceScoreboardTask extends BukkitRunnable {

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

            int rounded = (int) Math.round(balance);

            Score score = objective.getScore(target.getName());
            score.setScore(rounded);

            String abbreviated = abbreviate(balance);

            // Grünes "$" mit einem Leerzeichen vor der weißen Zahl
            Component numberComponent = Component.text("$", NamedTextColor.GREEN)
                    .append(Component.text(" ", NamedTextColor.WHITE))
                    .append(Component.text(abbreviated, NamedTextColor.WHITE));

            score.numberFormat(NumberFormat.fixed(numberComponent));
        }
    }

    /**
     * Formatiert einen Betrag abgekürzt:
     *
     * 950 -> "950"
     * 1500 -> "1.5k"
     * 2000 -> "2k"
     * 2500000 -> "2.5m"
     * 3000000000 -> "3b"
     * 4000000000000 -> "4t"
     */
    private String abbreviate(double amount) {
        double abs = Math.abs(amount);

        String suffix;
        double value;

        if (abs >= 1_000_000_000_000L) {
            value = amount / 1_000_000_000_000L;
            suffix = "T";
        } else if (abs >= 1_000_000_000L) {
            value = amount / 1_000_000_000L;
            suffix = "B";
        } else if (abs >= 1_000_000L) {
            value = amount / 1_000_000L;
            suffix = "M";
        } else if (abs >= 1_000L) {
            value = amount / 1_000L;
            suffix = "K";
        } else {
            return String.valueOf(Math.round(amount));
        }

        String formatted = String.format(Locale.US, "%.1f", value);

        if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
        }

        return formatted + suffix;
    }

    public void cleanupAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();

            if (board != null) {
                Objective objective = board.getObjective(OBJECTIVE_NAME);

                if (objective != null) {
                    objective.unregister();
                }
            }
        }
    }
}

