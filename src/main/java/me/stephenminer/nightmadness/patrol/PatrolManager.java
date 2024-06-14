package me.stephenminer.nightmadness.patrol;

import me.stephenminer.nightmadness.NightMadness;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatrolManager {
    private final NightMadness plugin;
    private HashMap<String, PatrolSpawner> spawners;

    public PatrolManager(){
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
        spawners = new HashMap<>();
    }



    public void run(){
        new BukkitRunnable(){
            @Override
            public void run(){
                for (PatrolSpawner spawner : spawners.values()){
                    spawner.tick();
                }
            }
        }.runTaskTimer(plugin,1,1);
    }

    /**
     * Will update all values of the internal spawner hashmap to make sure it has any new patrols added and removes any deleted
     * patrols. Internally only called on reload command
     */

    public void updateList(){
        for (String key : plugin.patrolFiles.keySet()){
            if (!spawners.containsKey(key)) spawners.put(key, new PatrolSpawner(key));
        }
        for (String key : spawners.keySet()){
            if (!plugin.patrolFiles.containsKey(key)) spawners.remove(key);
        }
    }

    /**
     * Adds PatrolSpawner entry to spawner HashMap if the patrolId is a real file in the patrols folder
     * @param patrolId
     */
    public void addEntry(String patrolId){
        if (!plugin.patrolFiles.containsKey(patrolId)) return;
        PatrolSpawner spawner = new PatrolSpawner(patrolId);
        spawners.put(patrolId,spawner);
    }

    /**
     * Removes a PatrolSpawner entry from the spawner HashMap if present
     * @param patrolId
     */
    public void removeEntry(String patrolId){
        spawners.remove(patrolId);
    }
}
