package net.torocraft.torohealth.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SwordItem;
import net.torocraft.torohealth.ToroHealth;
import net.torocraft.torohealth.util.Config.Mode;

public class HoldingWeaponUpdater {
  public static void update() {
    if (Mode.NONE.equals(ToroHealth.CONFIG.inWorld.mode)) return;
    PlayerEntity player = MinecraftClient.getInstance().player;
    if (player == null) {
      ToroHealth.IS_HOLDING_WEAPON = false;
      return;
    }
    ToroHealth.IS_HOLDING_WEAPON = isWeapon(player.getMainHandStack()) || isWeapon(player.getOffHandStack());
  }

  private static boolean isWeapon(ItemStack item) {
    return item.getItem() instanceof SwordItem ||
        item.getItem() instanceof BowItem ||
        item.getItem() instanceof PotionItem;
  }
}
