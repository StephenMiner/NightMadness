package me.stephenminer.nightmadness.listeners;

import me.stephenminer.nightmadness.NightMadness;
import me.stephenminer.nightmadness.mob.CustomItemReader;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class MobEvents implements Listener {
    private final NightMadness plugin;
    public MobEvents(){
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
    }

    @EventHandler
    public void shootCrossbow(EntityShootBowEvent event){
        if (!plugin.customItems) return;
        LivingEntity living = event.getEntity();
        if (living instanceof Player) return;
        if (!living.getPersistentDataContainer().has(plugin.gunnerKey)) return;
        ItemStack crossbow = event.getBow();
        ItemMeta meta = crossbow.getItemMeta();
        CustomItemReader reader = new CustomItemReader();
        if (reader.isGun(meta)){
            reader.createGunFire(living,crossbow,meta).shoot();
        }
    }

}
