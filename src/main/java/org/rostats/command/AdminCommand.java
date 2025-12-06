package org.rostats.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.rostats.ROStatsPlugin;
import org.rostats.data.PlayerData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final ROStatsPlugin plugin;

    public AdminCommand(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("rostats.admin")) {
            sender.sendMessage("§cNo Permission.");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // Save All
        if (sub.equals("save")) {
            for (Player p : Bukkit.getOnlinePlayers()) plugin.getDataManager().savePlayerData(p);
            sender.sendMessage("§aSaved data.");
            return true;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cTarget offline.");
            return true;
        }

        PlayerData data = plugin.getStatManager().getData(target.getUniqueId());

        switch (sub) {
            case "check":
                sender.sendMessage("§6--- " + target.getName() + " ---");
                sender.sendMessage("§eBase Lv: " + data.getBaseLevel());
                sender.sendMessage("§eJob Lv: " + data.getJobLevel());
                break;

            case "levelup": // /roadmin levelup <player> <base_exp> [job_exp]
                if (args.length < 3) return true;
                try {
                    long baseExp = Long.parseLong(args[2]);
                    // If job_exp is not provided, calculate it using the ratio
                    long jobExp = (args.length > 3) ? Long.parseLong(args[3]) : (long) (baseExp * plugin.getConfig().getDouble("exp-formula.job-exp-ratio", 0.75));

                    data.addBaseExp(baseExp, target.getUniqueId());
                    data.addJobExp(jobExp, target.getUniqueId());

                    sender.sendMessage("§aAdded EXP (Base: " + baseExp + ", Job: " + jobExp + ").");
                    update(target);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid EXP value!");
                }
                break;

            case "set": // /roadmin set <player> <stat> <val>
                if (args.length < 4) return true;
                String key = args[2].toLowerCase();
                try {
                    int val = Integer.parseInt(args[3]);

                    if (key.equals("points")) data.setStatPoints(val);
                    else if (key.equals("baselevel")) data.setBaseLevel(val);
                    else if (key.equals("joblevel")) data.setJobLevel(val); // NEW: set joblevel
                    else plugin.getStatManager().setStat(target.getUniqueId(), key.toUpperCase(), val);

                    sender.sendMessage("§aValue set.");
                    update(target);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid value!");
                }
                break;

            case "resetcount": // /roadmin resetcount <player> <val>
                if (args.length < 3) return true;
                try {
                    data.setResetCount(Integer.parseInt(args[2]));
                    sender.sendMessage("§aReset count updated.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid value!");
                }
                break;

            case "fullheal":
                target.setHealth(target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                data.setCurrentSP(data.getMaxSP());
                update(target);
                sender.sendMessage("§aHealed.");
                break;
        }
        return true;
    }

    private void update(Player p) {
        plugin.getAttributeHandler().updatePlayerStats(p);
        plugin.getManaManager().updateBar(p);
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage("§c/roadmin save");
        s.sendMessage("§c/roadmin levelup <player> <BaseEXP> [JobEXP]"); // UPDATED help
        s.sendMessage("§c/roadmin set <player> <STAT> <VAL>");
        s.sendMessage("§c/roadmin fullheal <player>");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("levelup", "set", "check", "fullheal", "resetcount", "save"), completions);
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                completions.addAll(Arrays.asList("STR", "AGI", "VIT", "INT", "DEX", "LUK", "Points", "BaseLevel", "JobLevel")); // NEW: JobLevel
            }
        }

        return completions;
    }
}