package com.example.mobgather;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MobGatherTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("help");
            if (sender.hasPermission("mobgather.toggle")) {
                completions.add("toggle");
            }
            if (sender.hasPermission("mobgather.admin")) {
                completions.add("reload");
                completions.add("cooldown");
                completions.add("give");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("toggle")) {
                completions.add("cooldown");
                completions.add("consume");
            } else if (args[0].equalsIgnoreCase("cooldown") || args[0].equalsIgnoreCase("give")) {
                return null;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("toggle")) {
                if (sender.hasPermission("mobgather.toggle.others")) {
                    return null;
                }
            } else if (args[0].equalsIgnoreCase("give")) {
                completions.add("zombie");
                completions.add("spider");
                completions.add("skeleton");
                completions.add("creeper");
                completions.add("slime");
                completions.add("wither");
                completions.add("player");
                completions.add("dragon");
            }
        }

        return completions;
    }
}
