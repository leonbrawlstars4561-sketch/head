package com.example.vaultbalance;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultBalancePlugin extends JavaPlugin {

    private static Economy economy = null;
    private BalanceScoreboardTask task;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Kein Vault-Economy-Plugin gefunden! Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Task, der alle 20 Ticks (1 Sekunde) die Balance aktualisiert
        task = new BalanceScoreboardTask(this, economy);
        task.runTaskTimer(this, 0L, 20L);

        getLogger().info("VaultBalance aktiviert. Aktualisierung alle 1 Sekunde.");
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
        }
        // Scoreboards bei Spielern zurücksetzen, damit nichts hängen bleibt
        if (task != null) {
            task.cleanupAll();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer()
                .getServicesManager()
                .getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public static Economy getEconomy() {
        return economy;
    }
}
