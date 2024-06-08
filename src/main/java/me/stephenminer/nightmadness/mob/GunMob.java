package me.stephenminer.nightmadness.mob;

import me.stephenminer.nightmadness.NightMadness;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Requires CustomItems plugin (ItemBuilder2)
 */
public class GunMob extends CustomMob{
    public static NamespacedKey GUNNER_KEY = new NamespacedKey(JavaPlugin.getPlugin(NightMadness.class), "gunner");
    private String itemModel;

    public GunMob(String gunModel){
        
    }


}
