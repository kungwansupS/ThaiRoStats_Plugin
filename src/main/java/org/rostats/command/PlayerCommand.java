package org.rostats.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rostats.ROStatsPlugin;
import org.rostats.gui.MainMenu;

public class PlayerCommand implements CommandExecutor {

    private final ROStatsPlugin plugin;

    public PlayerCommand(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlayers only.");
            return true;
        }

        // เปิด Main Menu ทันที ไม่ว่าจะมี args หรือไม่
        new MainMenu(plugin).open(player);
        return true;
    }
}