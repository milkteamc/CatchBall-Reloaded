package com.pohuang.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pohuang.ConfigSetting;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.util.StringUtil;

public class TabComplete implements TabCompleter {
    List<String> entityList = new ArrayList<>();
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        
        // tabComplete will be show suggest arument to commandSender
        if (command.getName().equals("ctb")) {
            final List<String> sort = new ArrayList<>();
            
            if (!sender.hasPermission("catchball.op")) { return null; }
            
            if (args.length == 1) { 
                StringUtil.copyPartialMatches(args[0], CommandCheck.getCommandArgument(), sort);
                return sort; 
            }

            if (args.length == 2) { 
                entityList.clear();

                if (args[0].equalsIgnoreCase("get")) {
                    StringUtil.copyPartialMatches(args[1], Arrays.asList("CatchBall", "GoldEgg"), sort);
                    return sort;

                } else if (args[0].equalsIgnoreCase("give")) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        sort.add(player.getName());
                    });
                    StringUtil.copyPartialMatches(args[0], sort, sort);
                    return sort;

                } else if (args[0].equalsIgnoreCase("add")) {

                    ConfigSetting.entityFile.getConfigurationSection("EntityList").getKeys(false).stream().
                        filter(entityName -> !ConfigSetting.catchableEntity.contains(EntityType.valueOf(entityName))).
                        forEach(entity -> entityList.add(entity));
                    
                    if (ConfigSetting.catchableEntity.size() < 75) { entityList.add("ALL"); }

                    StringUtil.copyPartialMatches(args[1], entityList, sort);
                    return sort;

                } else if (args[0].equalsIgnoreCase("remove")) {

                    ConfigSetting.entityFile.getConfigurationSection("EntityList").getKeys(false).stream().
                        filter(entityName -> ConfigSetting.catchableEntity.contains(EntityType.valueOf(entityName))).
                        forEach(entity -> entityList.add(entity));
                    
                    if (ConfigSetting.catchableEntity.size() == 75) { entityList.add("ALL"); }
                    
                    StringUtil.copyPartialMatches(args[1], entityList, sort);
                    return sort;
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("give")) {
                    StringUtil.copyPartialMatches(args[2], Arrays.asList("CatchBall", "GoldEgg"), sort);
                    return sort;
                }
            }

        }
        
        return null;
    }
}
