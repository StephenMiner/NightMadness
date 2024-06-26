package me.stephenminer.nightmadness;

import me.stephenminer.nightmadness.commands.ReloadConfig;
import me.stephenminer.nightmadness.commands.SpawnMob;
import me.stephenminer.nightmadness.commands.SpawnPatrol;
import me.stephenminer.nightmadness.darkness.LightEmitter;
import me.stephenminer.nightmadness.files.ConfigFile;
import me.stephenminer.nightmadness.files.MobFile;
import me.stephenminer.nightmadness.files.PatrolFile;
import me.stephenminer.nightmadness.listeners.MobEvents;
import me.stephenminer.nightmadness.patrol.PatrolManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public final class NightMadness extends JavaPlugin {
    public PatrolManager patrolManager;
    public ConfigFile darkFile;

    public HashMap<Material, LightEmitter> emitters;
    public List<PotionEffect> darknessEffects;
    public HashMap<String, MobFile> mobFiles;
    public HashMap<String, PatrolFile> patrolFiles;
    public boolean customItems;


    /*
    Custom Mob Attribute Keys
     */
    public NamespacedKey gunnerKey;
    public NamespacedKey id;
    public NamespacedKey friendlyFire;


    @Override
    public void onEnable() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this,()->{
            checkDirectory();
            initFiles();


        buildEmitters();
        customItems = Bukkit.getServer().getPluginManager().isPluginEnabled("CustomItems");
        this.getLogger().info("CustomItems Present: " + customItems);
        gunnerKey = new NamespacedKey(this, "gunner");
        id = new NamespacedKey(this,"id");
        friendlyFire = new NamespacedKey(this, "friendlyfire");
        mobFiles = new HashMap<>();
        patrolFiles = new HashMap<>();
        darknessEffects = new ArrayList<>();
        patrolManager = new PatrolManager();
        patrolManager.run();
        registerEvents();
        addCommands();
        },2);
    }

    private void registerEvents(){
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new MobEvents(),this);
    }
    private void addCommands(){
        SpawnPatrol spawnPatrol = new SpawnPatrol();
        getCommand("spawnpatrol").setExecutor(spawnPatrol);
        getCommand("spawnpatrol").setTabCompleter(spawnPatrol);

        SpawnMob spawnMob = new SpawnMob();
        getCommand("spawnmob").setExecutor(spawnMob);
        getCommand("spawnmob").setTabCompleter(spawnMob);

        getCommand("nightmadness-reload").setExecutor(new ReloadConfig());
    }
    private void initFiles(){
        darkFile = new ConfigFile(this, "darkness");
    }

    public void loadFiles(){
        loadDarknessData();
        buildEmitters();
        mobFiles = new HashMap<>();
        patrolFiles = new HashMap<>();
        loadMobFiles();
        loadPatrolFiles();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     *
     * @param str format as Material,Integer,Boolean (Material, light-level, offhand)
     * @return LightEmitter object containing parsed data from str
     */
    public LightEmitter emitterFromString(String str){
        String[] split = str.split(",");
        Material mat = Material.matchMaterial(split[0]);
        if (mat == null) {
            this.getLogger().warning("Attempted to generate material from name "+ split[0] + " but failed, maybe this isn't a true material");
            return null;
        }
        int light = Integer.parseInt(split[1]);
        boolean offhand = Boolean.parseBoolean(split[2]);
        return new LightEmitter(mat, light, offhand);

    }

    /**
     * Resets and populates cached list with potion effects players will get in darkness
     */
    public void loadDarknessData(){
        List<String> sPots = this.darkFile.getConfig().getStringList("darkness-effects");
        for (String potString : sPots){
            PotionEffect unpacked = unpackPotString(potString);
            if (unpacked == null) continue;
            this.darknessEffects.add(unpacked);
        }
    }

    /**
     * Searches plugin directory for mob and patrol folders. If they aren't present, the folders will be created
     */
    private void checkDirectory(){
        String dataFolderPath = this.getDataFolder().getPath();
        File mobFolder = new File(dataFolderPath, "mobs");
        if (mobFolder.exists()) mobFolder.mkdir();
        File patrolFolder = new File(dataFolderPath,"patrols");
        if (patrolFolder.exists()) patrolFolder.mkdir();
    }
    /**
     *
     * @param str format as Type,duration,amplifier
     * @return PotionEffect from data parsed from str
     */
    public PotionEffect unpackPotString(String str){
        String[] split = str.split(",");
        try {
            PotionEffectType type = PotionEffectType.getByName(split[0]);
            if (type == null) return null;
            int duration = Integer.parseInt(split[1]);
            int amp = Integer.parseInt(split[2]);
            return new PotionEffect(type, duration, amp);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Resets and populates cached HashMap emitters;
     */
    private void buildEmitters(){
        emitters = new HashMap<>();
        List<String> sEmitters = this.darkFile.getConfig().getStringList("torches.held-light-emitters");
        for (String sEmitter : sEmitters){
            LightEmitter emitter = emitterFromString(sEmitter);
            if (emitter == null){
                this.getLogger().warning("Somthing went wrong parsing emitter " + sEmitter + ", most likely, something is wrong with the material");
                continue;
            }
            this.emitters.put(emitter.mat(),emitter);
        }
    }

    /**
     * Loads all of the mob files, called on reload cmd and startup
     */
    public void loadMobFiles(){
        File parent = new File(this.getDataFolder().getPath(), "mobs");
        if (!parent.exists()) parent.mkdir();
        File[] files = parent.listFiles();
        if (files == null || files.length == 0) return;
        for (File file : files){
            String name = file.getName();
            if (name.contains(".yml")){
                name = name.replace(".yml","");
                MobFile mobFile = new MobFile(this, name);
                mobFiles.put(name, mobFile);
            }
        }
    }

    /**
     * Loads all of the patrol files, called on reload cmd and startup
     */
    public void loadPatrolFiles(){
        File parent = new File(this.getDataFolder().getPath(), "patrols");
        if (!parent.exists()) parent.mkdir();
        File[] files = parent.listFiles();
        if (files == null || files.length == 0) return;
        for (File file : files){
            String name = file.getName();
            if (name.contains(".yml")){
                name = name.replace(".yml","");
                PatrolFile patrolFile = new PatrolFile(this, name);
                patrolFiles.put(name, patrolFile);
            }
        }
    }
    public MobFile findMobFile(String id){
        File parent = new File(this.getDataFolder().getPath(), "items");
        if (!parent.exists()) parent.mkdir();
        File child = new File(parent, id + ".yml");
        if (child.exists()) return new MobFile(this, id);
        return null;
    }
    public PatrolFile findPatrolFile(String id){
        File parent = new File(this.getDataFolder().getPath(), "items");
        if (!parent.exists()) parent.mkdir();
        File child = new File(parent, id + ".yml");
        if (child.exists()) return new PatrolFile(this, id);
        return null;
    }


    /**
     *
     * @param base Provides strings
     * @param match to check contents of base against
     * @return new List containing elements of base that contain match
     */
    public List<String> filter(Collection<String> base, String match){
        match = ChatColor.stripColor(match.toLowerCase());
        List<String> filtered = new ArrayList<>();
        for (String entry : base){
            String temp = ChatColor.stripColor(entry.toLowerCase());
            if (temp.contains(match)) filtered.add(temp);
        }
        return filtered;
    }


}
