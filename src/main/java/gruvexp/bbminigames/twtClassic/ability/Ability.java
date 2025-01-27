package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Ability { // each player has some ability objects.

    protected final BotBowsPlayer player;
    protected AbilityType type;

    protected int baseCooldown; // seconds
    protected float cooldownMultiplier = 1.0f;
    private int currentCooldown;
    private int effectiveCooldown;

    private final int hotBarSlot;

    protected Ability(BotBowsPlayer player, int hotBarSlot) {
        this.player = player;
        this.hotBarSlot = hotBarSlot;
    }

    public AbilityType getType() {
        return type;
    }

    public int getHotBarSlot() {
        return hotBarSlot;
    }

    public void setCooldownMultiplier(float multiplier) {
        this.cooldownMultiplier = multiplier;
        effectiveCooldown = (int) (baseCooldown * cooldownMultiplier);
    }

    public int getEffectiveCooldown() {
        return effectiveCooldown;
    }

    public void use() {
        Inventory inv = player.player.getInventory();
        new BukkitRunnable() {
            ItemStack cooldownItem = type.getCooldownItems()[0].clone();
            int currentCooldown = effectiveCooldown;
            @Override
            public void run() {
                switch (currentCooldown) {
                    case 10 -> cooldownItem = type.getCooldownItems()[1].clone();
                    case 5 -> cooldownItem = type.getCooldownItems()[2].clone();
                    case 2 -> cooldownItem = type.getCooldownItems()[3].clone();
                    case 0 -> {
                        inv.setItem(hotBarSlot, type.getAbilityItem());
                        cancel();
                        return;
                    }
                }
                cooldownItem.setAmount(currentCooldown);
                inv.setItem(hotBarSlot, cooldownItem);
                currentCooldown--;
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 20L);
    }
}
