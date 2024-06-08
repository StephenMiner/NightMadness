package me.stephenminer.nightmadness.commands;

import me.stephenminer.nightmadness.NightMadness;
import me.stephenminer.nightmadness.mob.MobBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class SpawnMob implements CommandExecutor, TabCompleter {
    private final NightMadness plugin;

    public SpawnMob(){
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        if (args.length < 2){
            sender.sendMessage(ChatColor.RED + "You need to input the mob you wish to spawn and how many to spawn");
            return false;
        }
        String mob = args[0];
        if (!validMob(mob)){
            sender.sendMessage(ChatColor.RED + mob + " isn't a mob, if there is a file in the plugin's 'mobs' folder and you are seeing this message, use the plugin's reload command.");
            return false;
        }
        int toSpawn;
        try {
            toSpawn = Integer.parseInt(args[1]);
        }catch (Exception ignored){
            sender.sendMessage(ChatColor.RED + args[1] + " needs to be an integer whole number");
            return false;
        }
        if (sender instanceof Player player){
            if (!player.hasPermission("night-madness.commands.spawn")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            spawnMobs(mob, player.getLocation(),toSpawn);
            sender.sendMessage(ChatColor.GREEN + "Spawned mobs");
            return true;
        }else if (args.length >= 3){
            String playerName = args[2];
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null){
                sender.sendMessage(ChatColor.RED + "Could not find an online player with the name " + playerName);
                return false;
            }
            spawnMobs(mob, player.getLocation(),toSpawn);
            sender.sendMessage(ChatColor.GREEN + "Spawned mobs");
            return true;
        }else{
            sender.sendMessage(ChatColor.RED + "If you aren't using this command as a player, you need to input a player as the third argument");
        }
        return false;
    }

    /**
     * Searches cached files in a hashmap to see if input exists as a key
     * @param input
     * @return
     */
    private boolean validMob(String input){
        return plugin.mobFiles.containsKey(input);
    }


    private void spawnMobs(String id, Location loc, int spawn){
        MobBuilder builder = new MobBuilder(id, loc);
        for (int i = 0; i < spawn; i++){
            builder.spawnMob();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return mobIds(args[0]);
        if (size == 2) return num();
        if (size == 3 && !(sender instanceof Player)) return playerNames(args[1]);
        return null;
    }


    private List<String> mobIds(String match){
        return plugin.filter(plugin.mobFiles.keySet(),match);
    }
    private List<String> num(){
        List<String> out = new ArrayList<>();
        out.add("[#-mobs-you-wish-to-spawn]");
        return out;
    }

    private List<String> playerNames(String match){
        return plugin.filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(),match);
    }
}
