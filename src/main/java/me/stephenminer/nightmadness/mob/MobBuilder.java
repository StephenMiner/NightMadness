package me.stephenminer.nightmadness.mob;

import me.stephenminer.nightmadness.NightMadness;
import me.stephenminer.nightmadness.files.MobFile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MobBuilder {
    protected final String id;
    protected final NightMadness plugin;
    protected MobFile file;
    protected LivingEntity living;
    protected final Location spawn;

    public MobBuilder(String id, Location loc){
        this.id = id;
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
        file = plugin.mobFiles.getOrDefault(id,null);
        this.spawn = loc;
        if (file == null){
            plugin.getLogger().warning("Attempted to build mob " + id + ", but mob doesn't have a file to load!");
            return;
        }

    }




    public void spawnMob(){
        World world = spawn.getWorld();
        LivingEntity living = (LivingEntity) world.spawnEntity(spawn, readType());
        this.living = living;
        buildAttributes();
        buildEquipment();
    }



    /**
     *
     * @return entity type read from the respective file in the mobs folder, null if a type isn't found
     */
    private EntityType readType(){
        String sType = file.getConfig().getString("type");
        try {
            return EntityType.valueOf(sType);
        }catch (Exception e){
            e.printStackTrace();
            plugin.getLogger().warning("Attempted to parse EntityType " + sType + " but this type doesn't exist");
        }
        return null;
    }

    private void buildAttributes(){
        List<String> sAttributes = file.getConfig().getStringList("attributes");
        List<AttributePair> attributes = new ArrayList<>();
        for (String entry : sAttributes){
            AttributePair pair = attriFromStr(entry);
            if (pair != null) attributes.add(pair);
        }
        for (AttributePair attribute : attributes){
            living.getAttribute(attribute.attribute()).setBaseValue(attribute.value());
        }
    }

    /**
     * Builds and adds equipment to the entity from file
     */
    private void buildEquipment(){
        List<String> sEquipment = file.getConfig().getStringList("equipment");
        List<EquipmentPair> equipment = new ArrayList<>();
        for (String entry : sEquipment){
            EquipmentPair pair = equipFromStr(entry);
            if (pair != null) equipment.add(pair);
        }
        EntityEquipment inv = living.getEquipment();
        if (inv == null) return;
        for (EquipmentPair pair : equipment){
            inv.setItem(pair.slot(),pair.item());
        }
    }

    /**
     *
     * @param str format as EquipmentSlot,Material or CustomItems item id
     * @return
     */
    private EquipmentPair equipFromStr(String str){
        String[] split = str.split(",");
        EquipmentSlot slot;
        //Make sure equipment slot is valid
        try{
            slot = EquipmentSlot.valueOf(split[0]);
        }catch (Exception e){
            e.printStackTrace();
            plugin.getLogger().warning("Attempted to parse EquipmentSlot " + split[0] + ", but this is not a real EquipmentSlot. Check that everything is correct and capitalized!");
            return null;
        }
        ItemStack item;
        Material mat = Material.matchMaterial(split[1]);
        //Is material valid?
        if (mat == null){
            //If Customitems is instealled, then we can try to generate an item based on the input String, otherwise, deadend
            if (!plugin.customItems){
                plugin.getLogger().warning(split[1] + " is not a real material!" );
                return null;
            }
            try {
                CustomItemReader reader = new CustomItemReader();
                item = reader.read(split[1]);
            }catch (Exception e){
                e.printStackTrace();
                plugin.getLogger().warning(split[1] + " is probably not a material or CustomItems item, make sure this is the case!");
                return null;
            }
        }else item = new ItemStack(mat);
        return new EquipmentPair(slot, item);
    }

    /**
     *
     * @param str format as Attribute,value (double)
     * @return Attribute pair parsed from str data
     */
    private AttributePair attriFromStr(String str){
        String[] split = str.split(",");
        Attribute attribute;
        try {
            attribute = Attribute.valueOf(split[0]);
        }catch (Exception e){
            e.printStackTrace();;
            plugin.getLogger().warning("Attempted to read Attribute from " + split[0] + ", but this is not a real minecraft attribute");
            return null;
        }
        double val = Double.parseDouble(split[1]);
        return new AttributePair(attribute,val);
    }


    public LivingEntity getEntity(){
        return living;
    }










}
