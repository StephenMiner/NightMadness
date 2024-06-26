package me.stephenminer.nightmadness.commands;

import me.stephenminer.nightmadness.NightMadness;
import me.stephenminer.nightmadness.patrol.PatrolSpawner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;

public class SpawnPatrol implements CommandExecutor, TabCompleter {
    private final NightMadness plugin;

    public SpawnPatrol(){
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (!sender.hasPermission("night-madness.commands.spawn")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        if (size < 2){
            String patrolId = args[0];
            if (!isPatrol(patrolId)){
                sender.sendMessage(ChatColor.RED + "No patrol found for id " + patrolId);
                return false;
            }
            if (sender instanceof Player player){
                PatrolSpawner spawner = new PatrolSpawner(patrolId);
                spawner.spawnPatrol(player);
                sender.sendMessage(ChatColor.GREEN + "Spawned patrol on player " + player.getName());
                return true;
            }else sender.sendMessage(ChatColor.RED + "You need to specify the player that the patrol should spawn on");
        }else{
            String patrolId = args[0];
            if (!isPatrol(patrolId)){
                sender.sendMessage(ChatColor.RED + "No patrol found for id " + patrolId);
                return false;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null){
                sender.sendMessage(ChatColor.RED + "Player " + args[1] + " not found");
                return false;
            }
            PatrolSpawner spawner = new PatrolSpawner(patrolId);
            spawner.spawnPatrol(target);
            sender.sendMessage(ChatColor.GREEN + "Spawned patrol on player " + target.getName());
            return true;
        }
        return false;
    }

    private boolean isPatrol(String id){
        return plugin.patrolFiles.containsKey(id);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return patrols(args[0]);
        if (size == 2) return players(args[1]);
        return null;
    }


    private List<String> patrols(String match){
        return plugin.filter(plugin.patrolFiles.keySet(), match);
    }

    private List<String> players(String match){
        Collection<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        return plugin.filter(names,match);
    }
}
