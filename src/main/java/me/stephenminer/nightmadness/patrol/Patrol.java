package me.stephenminer.nightmadness.patrol;

import me.stephenminer.nightmadness.NightMadness;
import me.stephenminer.nightmadness.mob.MobBuilder;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class Patrol {
    public static final NamespacedKey PATROL_KEY = new NamespacedKey(JavaPlugin.getPlugin(NightMadness.class),"patrol");
    private final NightMadness plugin;
    private final Location spawnpoint, patrolPoint;
    private final World world;
    private final Player player;
    private final Random random;
    private PatrolReader reader;
    private boolean captain;


    public Patrol(String id, Location spawnpoint, Player instigator){
        this.plugin = JavaPlugin.getPlugin(NightMadness.class);
        reader = new PatrolReader(id);
        this.spawnpoint  = spawnpoint;
        world = spawnpoint.getWorld();
        this.player = instigator;
        random = new Random();
        patrolPoint = generatePatrolLocation();
    }

    public void init(){
        spawnMobs();
        if (reader.hasSpawnSound()) reader.playSpawnSound(spawnpoint);
    }

    private Location generatePatrolLocation(){
        PatrolReader.TargetType type = reader.targetCenter();
        Location center;
        if (type == PatrolReader.TargetType.PLAYER) center = player.getLocation();
        else center = spawnpoint;
        Random random = new Random();
        int radius = reader.targetRadius();
        int x = random.nextInt(center.getBlockX() + radius + 1 - (center.getBlockX() + radius)) + center.getBlockX() - radius;
        int z = random.nextInt(center.getBlockZ() + radius + 1- (center.getBlockZ() - radius)) + center.getBlockZ() - radius;
        int y = center.getWorld().getHighestBlockYAt(x,z, HeightMap.MOTION_BLOCKING_NO_LEAVES);

        return new Location(center.getWorld(),x,y,z);
    }

    public void spawnMobs(){
        List<MobPair> mobs = reader.mobs();
        mobs.forEach(this::spawnMob);
    }

    private void spawnMob(MobPair mobData){
        int toSpawn = 0;
        if (mobData.random()) toSpawn = random.nextInt(mobData.toSpawn() + 1);
        else toSpawn = mobData.toSpawn();
        try{
            EntityType type = EntityType.valueOf(mobData.mobId());
            for (int i = 0; i < toSpawn; i++){
                LivingEntity living = (LivingEntity) world.spawnEntity(mobPosition(), type);
                updateTargeting(living);
                System.out.println(2000);
                System.out.println(mobPosition());
                living.damage(20);
            }
            return;
        }catch (Exception ignored){}
        MobBuilder builder;
        for (int i = 0; i < toSpawn; i++){
            builder = new MobBuilder(mobData.mobId(), mobPosition());
            builder.spawnMob();
            updateTargeting(builder.getEntity());

        }
    }

    private void updateTargeting(LivingEntity living){
        if (!(living instanceof Mob)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Mob mob = (Mob) living;
                if (mob.isDead()){
                    this.cancel();
                    return;
                }
                if(reader.targetImmediately()) {
                    mob.setTarget(player);
                } else if(mob instanceof Raider raider &&reader.targetBlock()) {
                    if (!captain) raider.setPatrolLeader(true);
                    raider.setPatrolTarget(patrolPoint.getBlock());
                }
            }
        }.runTaskTimer(plugin,1,10);
    }



    private Location mobPosition(){
        int radius = 5;
        int x = random.nextInt(spawnpoint.getBlockX() + radius + 1 - (spawnpoint.getBlockX() - radius)) + (spawnpoint.getBlockX() - radius);
        int z = random.nextInt(spawnpoint.getBlockZ() + radius + 1 - (spawnpoint.getBlockZ() - radius)) + (spawnpoint.getBlockZ() - radius);
        int y = spawnpoint.getWorld().getHighestBlockYAt(x,z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        return new Location(spawnpoint.getWorld(),x,y,z);
    }





    private boolean validPos(int x, int y, int z){
        return !world.getBlockAt(x,y,z).isPassable() && world.getBlockAt(x,y+1,z).isPassable() && world.getBlockAt(x,y + 2, z).isPassable();
    }
}
