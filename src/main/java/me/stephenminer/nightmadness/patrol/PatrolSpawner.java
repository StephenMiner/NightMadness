package me.stephenminer.nightmadness.patrol;

import me.stephenminer.nightmadness.NightMadness;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class PatrolSpawner {
    private final NightMadness plugin;
    private Random random;
    private int delay,count;
    private final String id;
    private final PatrolReader reader;

    public PatrolSpawner(String id) {
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
        this.id = id;
        random = new Random();
        reader = new PatrolReader(id);
        int[] delayRange = reader.spawnInterval();
        delay = random.nextInt(delayRange[0],delayRange[1] +1);
    }

    public void tick(){
        if (count >= delay){

            int numPlayers = reader.maxPlayers();
            List<String> blacklist = reader.blacklistedWorlds();
            List<? extends Player> players = Bukkit.getOnlinePlayers().stream().filter(p->blacklist.contains(p.getWorld().getName())).collect(Collectors.toList());

            int upperBound = Math.min(numPlayers, Bukkit.getOnlinePlayers().size());
            for (int i = 0; i < upperBound; i++){
                Player instigator = findRandomPlayer(players);
                int chance = reader.spawnChance();
                if (random.nextInt(100) < chance){
                    spawnPatrol(instigator);
                }
            }
            int[] delayRange = reader.spawnInterval();
            delay = random.nextInt(delayRange[0],delayRange[1] +1);
            count = 0;
        }
    }

    private Player findRandomPlayer(List<? extends Player> players){
        int roll = random.nextInt(players.size());
        return players.remove(roll);
    }

    public void spawnPatrol(Player player){
        Location loc = generateSpawnLocation(player);
        if (loc == null){
            plugin.getLogger().info("Attempted to spawn patrol, but couldn't find a valid random location");
            return;
        }
        Patrol patrol = new Patrol(id, loc, player);
        patrol.init();
    }


    private Location generateSpawnLocation(Player player){
        int[] spawnRange = reader.spawnRange();
        Location pLoc = player.getLocation();
        World world = player.getWorld();
        int minx = pLoc.getBlockX() + spawnRange[0];
        int maxx = pLoc.getBlockX() + spawnRange[1];
        int minz = pLoc.getBlockZ() + spawnRange[0];
        int maxz = pLoc.getBlockZ() + spawnRange[1];
        Location loc = null;
        //Try 40 times to generate a valid location

        for (int i = 0; i < 40; i++){
            int x = random.nextInt(minx, maxx+1);
            int z = random.nextInt(minz,maxz + 1);
            int y = bestY(world,pLoc.getBlockY(), x, z);
            if (y != -100) {
                loc = new Location(world,x,y,z);
            }
        }
        return loc;
    }


    /**
     *
     * @param py
     * @param x
     * @param z
     * @return -100 if a valid y isn't found, otherwise a y-coordinate where a patrol can spawn
     */
    private int bestY(World world, int py, int x, int z){

        int uy = -100;
        for (int cy = py; cy <= py + reader.spawnRange()[1]-2; cy++){
            Block block = world.getBlockAt(x,cy,z);
            boolean valid = !block.isPassable() && block.getRelative(BlockFace.UP).isPassable() && block.getRelative(BlockFace.UP,2).isPassable();
            if (valid) {
                uy = cy;
                break;
            }
        }

        int dy = -100;
        for (int cy = py; cy >= py-reader.spawnRange()[1]-2; cy++){
            Block block = world.getBlockAt(x,cy,z);
            boolean valid = !block.isPassable() && block.getRelative(BlockFace.UP).isPassable() && block.getRelative(BlockFace.UP,2).isPassable();
            if (valid) {
                dy = cy;
                break;
            }
        }
        if (dy == uy) return dy;
        else {
            int ddist = Math.abs(py - dy);
            int udist = Math.abs(py - uy);
            if (udist > ddist) return dy;
            else return uy;
        }
    }



    public void updateValue(){
        int[] range = reader.spawnRange();
        if (range[0] == range[1]) delay = range[0];
        else delay = random.nextInt(range[0], range[1] + 1);
    }

}
