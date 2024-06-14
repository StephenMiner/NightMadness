package me.stephenminer.nightmadness.patrol;

import me.stephenminer.nightmadness.NightMadness;
import me.stephenminer.nightmadness.files.PatrolFile;
import me.stephenminer.nightmadness.mob.MobBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class PatrolReader {
    private final NightMadness plugin;
    private final String id;
    private final PatrolFile file;

    public PatrolReader(String id){
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
        this.id = id;
        if (plugin.patrolFiles.containsKey(id)){
            file = plugin.patrolFiles.get(id);
        }else{
            plugin.getLogger().warning("Attempted to build patrol " + id + ", but could not find a file with that name");
            file = null;
        }
    }

    /**
     * Read spawn range for given patrol on file
     * @return translated spawn range as integer array with [0] being min and [1] being max. If no value can be read {24,34} will be returned
     */
    public int[] spawnRange(){
        if (file.getConfig().contains("spawn-range")){
            String[] split = file.getConfig().getString("spawn-range").split(",");
            int[] range = new int[2];
            range[0] = Integer.parseInt(split[0]);
            range[1] = Integer.parseInt(split[1]);
            return range;
        }else return new int[]{24,34};
    }

    /**
     * Read spawn-delay range for patrol in ticks
     * @return translated spawn interval as int[] with [0] being min & [1] max. If no value is found {12000,13200} will be returned
     */
    public int[] spawnInterval(){
        if (file.getConfig().contains("spawn-interval-range")){
            String[] split = file.getConfig().getString("spawn-range").split(",");
            int[] range = new int[2];
            range[0] = Integer.parseInt(split[0]);
            range[1] = Integer.parseInt(split[1]);
            return range;
        }else return new int[]{12000,13200};
    }

    /**
     * Chance for given patrol to spawn when a number 0-99 is rolled
     * @return read spawn-chance from file
     */
    public int spawnChance(){
        return file.getConfig().getInt("spawn-chance");
    }

    /**
     * Whether raider mobs should be set to target a block or not
     * @return true or false based on what the file says. If file says nothing on the matter, false will be returned
     */
    public boolean targetBlock(){
        return file.getConfig().getBoolean("raider-targeting.target-block");
    }

    /**
     * Whether raider mobs should immediately target a player when they spawn in or not
     * @return true or false based on what the patrol file says, if it says nothing, false is the default value
     */
    public boolean targetImmediately(){ return file.getConfig().getBoolean("raider-targeting.target-players-immediately"); }

    /**
     * Radius in which raider mobs will target a block centered on either the instigating player or the patrol's center (see targetCenter())
     * @return radius in blocks that a block can be targeted in
     */
    public int targetRadius(){
        return file.getConfig().getInt("raider-targeting.radius");
    }

    /**
     * Where the radius that raiders will target a block to patrol to will be centered on
     * @return either SPAWN or PLAYER for radius centered on patrol spawn or player location
     */
    public TargetType targetCenter(){
        if (!file.getConfig().contains("raider-targeting.center")) return TargetType.SPAWN;
        String sType = file.getConfig().getString("raider-targeting.center").toUpperCase();
        return TargetType.valueOf(sType);
    }

    /**
     * This number doesnt gurantee x amount of patrols to spawn, it will just attempt to spawn patrols for x players
     * @return maximum # of players the patrol can spawn for
     */
    public int maxPlayers(){
        String maxPlayers = file.getConfig().getString("max-players-affected");
        if (maxPlayers == null) return 1;
        if (maxPlayers.equalsIgnoreCase("ALL")) return Bukkit.getOnlinePlayers().size();
        else return Integer.parseInt(maxPlayers);
    }


    /**
     *
     * @return list of MobPairs generated from a stringlist on file
     */
    public List<MobPair> mobs(){
        List<String> entries = file.getConfig().getStringList("spawn");
        List<MobPair> out = new ArrayList<>();
        for (String entry : entries){
            MobPair pair = mobPairFromStr(entry);
            out.add(pair);
        }
        return out;
    }

    /**
     * Generated a MobPair from str
     * @param str format as "mob-type(file name in mobs folder or EntityType),int,boolean where int represent the number to spawn and boolean whether that number should be a random range or not
     * @return MobPair contained parsed data from str
     */
    private MobPair mobPairFromStr(String str){
        String[] split = str.split(",");
        String type = split[0];
        int spawn = Integer.parseInt(split[1]);
        boolean random = Boolean.parseBoolean(split[2]);
        return new MobPair(type, spawn, random);
    }

    public boolean hasSpawnSound(){
        return file.getConfig().contains("spawn-sound");
    }

    /**
     * Plays spawn sound for patrol as outlined in its file
     * @param loc where to play sound on
     */
    public void playSpawnSound(Location loc){
        Sound sound = Sound.valueOf(file.getConfig().getString("spawn-sound.sound"));
        float volume = (float) file.getConfig().getDouble("spawn-sound.volume");
        float pitch = (float) file.getConfig().getDouble("spawn-sound.pitch");
        loc.getWorld().playSound(loc,sound,volume,pitch);
    }

    public List<String> blacklistedWorlds(){
        return file.getConfig().getStringList("world-blacklist");
    }




    public String id(){ return id; }

    public enum TargetType{
        SPAWN,
        PLAYER
    }
}
