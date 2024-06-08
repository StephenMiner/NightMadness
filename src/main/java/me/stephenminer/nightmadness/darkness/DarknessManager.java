package me.stephenminer.nightmadness.darkness;

import me.stephenminer.nightmadness.NightMadness;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DarknessManager {
    private final NightMadness plugin;

    public HashMap<UUID, BlockState> savedStates;

    public Set<UUID> darkSafe;
    public boolean stop;

    public DarknessManager(){
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
        savedStates = new HashMap<>();
        darkSafe = new HashSet<>();
    }


    public void run(){
        new BukkitRunnable(){
            @Override
            public void run() {
                if (stop){
                    this.cancel();
                    return;
                }
                tick();
            }
        }.runTaskTimer(plugin,1,1);
    }

    public void tick(){
        List<String> blacklisted = plugin.darkFile.getConfig().getStringList("disable-in");
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player player : players){
            World world = player.getWorld();
            if (!blacklisted.isEmpty()){
                if (blacklisted.contains(world.getName())) continue;
            }
            handlePlayer(player);
        }

    }




    public void stop(){
        Collection<BlockState> states = savedStates.values();
        for (BlockState state : states){
            state.update();
        }
        savedStates.clone();
        darkSafe.clear();
    }



    private void handlePlayer(Player player){
        UUID uuid = player.getUniqueId();
        //these cannot be null
        ItemStack mainhand = player.getInventory().getItemInMainHand();
        ItemStack offhand = player.getInventory().getItemInOffHand();
        //Main-hand will take priority if 2 emitters are held at the same time
        if (plugin.emitters.containsKey(mainhand.getType())){
            int lighting = plugin.emitters.get(mainhand.getType()).light();
            light(player, lighting);
            darkSafe.add(uuid);
        }else if (plugin.emitters.containsKey(offhand.getType())){
            int lighting = plugin.emitters.get(offhand.getType()).light();
            light(player, lighting);
            darkSafe.add(uuid);
        }else darkSafe.remove(uuid);

        if (canBlind(player))
            applyDarknessEffects(player);
    }

    /**
     *
     * @param player
     * @return true if the player should recieve darkness effects, otherwise false
     */
    private boolean canBlind(Player player){
        if (!darkSafe.contains(player.getUniqueId()) || player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) return false;
        Block below = player.getLocation().getBlock();
        int lightlevel = below.getLightLevel() + below.getLightFromSky() + below.getLightFromBlocks();
        int checker = below.getLightFromSky() > 0 ? outdoorLightLevel() : indoorLightLevel();
        return lightlevel <= checker;
    }

    /**
     * Updates dynamic lighting
     * @param player player to spawn light for
     * @param lighting light level of spawned light
     */
    private void light(Player player, int lighting){
        UUID uuid = player.getUniqueId();
        //reset cached blockstate
        if (savedStates.containsKey(uuid)) savedStates.remove(uuid).update(true);
        if (!dynLighting()) return;
        //get a block at eye-level
        Block block = player.getEyeLocation().clone().getBlock();
        Material type = block.getType();
        if (block.isPassable() && type!=Material.VINE && type != Material.CAVE_VINES && type != Material.CAVE_VINES_PLANT){
            //save current blockstate
            savedStates.put(uuid, block.getState());
            //create light block
            block.setType(Material.LIGHT);
            Levelled data = (Levelled) block.getBlockData();
            data.setLevel(lighting);
            block.setBlockData(data);
        }
    }

    /**
     * Applies darkness potioneffects as defined in darkness.yml config file
     * @param player
     */
    private void applyDarknessEffects(Player player){
        List<PotionEffect> effects = plugin.darknessEffects;
        for (PotionEffect effect : effects) player.addPotionEffect(effect);
    }



    private int indoorLightLevel(){
        return plugin.darkFile.getConfig().getInt("min-light-level-inside");
    }
    private int outdoorLightLevel(){
        return plugin.darkFile.getConfig().getInt("min-light-level-outside");
    }
    private boolean dynLighting(){
        return plugin.darkFile.getConfig().getBoolean("torches.dynamic-lighting");
    }
}
