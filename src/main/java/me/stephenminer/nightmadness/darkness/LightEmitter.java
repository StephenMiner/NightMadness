package me.stephenminer.nightmadness.darkness;

import org.bukkit.Material;

/**
 * A record container for light emitters
 * @param mat material to emit light when held
 * @param light light level material should give off when held
 * @param offhand whether light will be emitted in offhand or not
 */
public record LightEmitter(Material mat, int light, boolean offhand) {


    @Override
    public String toString(){
        return mat.name() + "," + light + "," + offhand;
    }
}
