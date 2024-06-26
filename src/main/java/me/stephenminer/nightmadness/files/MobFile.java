package me.stephenminer.nightmadness.files;

import me.stephenminer.nightmadness.NightMadness;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MobFile {
    private NightMadness plugin;
    private String file;
    private File parent;

    public MobFile(NightMadness plugin, String file) {
        System.out.println(0);
        this.plugin = plugin;
        this.file = file;
        try {
            parent = new File(this.plugin.getDataFolder(), "mobs");
            if (!parent.exists()) {
                parent.mkdir();
                System.out.println(1);
            }
        }catch (Exception e){ e.printStackTrace(); }
        saveDefaultConfig();
    }

    private FileConfiguration dataConfig = null;

    private File configFile = null;

    public void reloadConfig() {
        if (this.configFile == null) {
            this.configFile = new File(parent, file + ".yml");
            try {
                if (!configFile.exists()) configFile.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
/*
        InputStream defaultStream = this.plugin.getDataFolder().(file + ".yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }

 */
    }
    public FileConfiguration getConfig(){
        if (this.dataConfig == null)
            reloadConfig();

        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null)
            return;
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            Bukkit.broadcastMessage("COULD NOT SAVE TO CONFIG FILE: " + this.configFile);
        }
    }
    public void saveDefaultConfig(){
        if (this.configFile == null)
            this.configFile = new File(parent, file + ".yml");
        if (!this.configFile.exists()){
            try {
                configFile.createNewFile();
            }catch (Exception e){e.printStackTrace();}
            //this.plugin.saveResource(file + ".yml", false);
        }
    }
}
