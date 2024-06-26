package me.stephenminer.nightmadness.mob;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.builder.GunBuilder;
import me.stephenminer.customitems.builder.ItemBuilder;
import me.stephenminer.customitems.gunutils.GunFire;
import me.stephenminer.customitems.gunutils.GunReader;
import me.stephenminer.customitems.gunutils.SpreadGunFire;
import me.stephenminer.nightmadness.NightMadness;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

/**
 * Use to parse strings that arent Materials if CustomItems is installed
 */
public class CustomItemReader {
    private final NightMadness plugin;
    private final CustomItems customItems;
    public CustomItemReader(){
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
        this.customItems = JavaPlugin.getPlugin(CustomItems.class);
    }

    public ItemStack read(String id){
        ItemBuilder builder = new ItemBuilder(id);
        GunBuilder gunBuilder = new GunBuilder(id, builder.getConfig());
        ItemStack item = builder.buildItem();
        if (gunBuilder.isGun()){
            GunReader reader = new GunReader(item, item.getItemMeta());
            while(!reader.getFiringStage().equals("ready to fire")){
                reader.setFiringStage();
            }
        }
        return builder.buildItem();
    }

    public GunFire createGunFire(LivingEntity shooter, ItemStack item, ItemMeta meta){
        if (!isGun(meta)) return null;
        GunReader reader = new GunReader(item, meta);
        GunBuilder.GunType type = reader.readType();
        if (type == GunBuilder.GunType.SPREAD){
            SpreadGunFire spreadGunFire;
            if (shooter.getPersistentDataContainer().getOrDefault(plugin.friendlyFire, PersistentDataType.BOOLEAN,false)){
                spreadGunFire = new SpreadGunFire(shooter, reader.readDamage(),reader.readRange(),reader.readDecayRate(),reader.readDecayRange(),reader.readProjectiles(), Collections.singleton(shooter.getType()));
            }else spreadGunFire = new SpreadGunFire(shooter, reader.readDamage(),reader.readRange(),reader.readDecayRate(),reader.readDecayRange(),reader.readProjectiles());
            return spreadGunFire;
        }else{
            GunFire gunFire;
            if (shooter.getPersistentDataContainer().getOrDefault(plugin.friendlyFire, PersistentDataType.BOOLEAN,false)) {
                gunFire = new GunFire(shooter, reader.readDamage(), reader.readRange(), reader.readDecayRate(), reader.readDecayRange(), Collections.singleton(shooter.getType()));
            }else gunFire = new GunFire(shooter, reader.readDamage(), reader.readRange(), reader.readDecayRate(), reader.readDecayRange());
            return gunFire;
        }
    }

    public boolean isGun(ItemMeta meta){
        return meta.getPersistentDataContainer().has(customItems.gun);
    }


}
