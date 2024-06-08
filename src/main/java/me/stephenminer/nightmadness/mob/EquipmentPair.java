package me.stephenminer.nightmadness.mob;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public record EquipmentPair(EquipmentSlot slot, ItemStack item) {
}
