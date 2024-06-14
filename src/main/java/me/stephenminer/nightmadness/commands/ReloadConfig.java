package me.stephenminer.nightmadness.commands;

import me.stephenminer.nightmadness.NightMadness;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadConfig implements CommandExecutor {
    private final NightMadness plugin;
    public ReloadConfig(){
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("night-madness.commands.reload")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
        }
        plugin.darkFile.reloadConfig();
        plugin.patrolManager.updateList();
        plugin.loadFiles();
        return false;
    }
}
