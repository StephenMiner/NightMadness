While not completed, the goal of this plugin is to make mob spawns a bit more formidable or at least make the world and the night feel more hostile.

You'll be able to add "custom"
mobs and mob patrols. Patrols will be the primary avenue through which these mobs will spawn. While I said custom they are fairly normal mobs in that you can really just edit the equipment they wear
and their attributes, but if you use my CustomItems/ItemBuilder2 plugin, you can use items from that plugin (Including guns) for the mobs with their file names. 

You can also add debuffs that players will recieve for being in darkness such as Blindness as well as define exactly what lightlevels should be considered in darkness although there are separate 
values for outside darkness and indoor darkness as defined in the config. The default values are the ones I personally use on my server so are more finely tuned. Of course this can be disabled entirely.
If players are holding a lightsource, then the debuffs will not be applied to them

Dynamic Lighting system. Regardless of whether this is the right name for what it is, this is just what I've come to call it. You can enable or disable this, but if enabled, if a player holds
a torch (or any item defined in the config) it will emit a lightlevel in the world at the players location (also defined in the config). The only thing about this feature is that it creates a real lightsource
so the safety of 2 block tall plants is not guranteed.
