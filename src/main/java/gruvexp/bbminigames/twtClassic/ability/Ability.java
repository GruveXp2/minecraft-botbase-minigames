package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Ability {

    protected final BotBowsPlayer player;
    protected AbilityType type;

    protected int baseCooldown; // seconds
    protected float cooldownMultiplier = 1.0f;
    private int effectiveCooldown;

    private final int hotBarSlot;
    private CooldownTimer cooldownTimer;

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

    public void resetCooldown() {
        if (cooldownTimer != null) {
            cooldownTimer.cancel();
        }
    }

    public void obtain() {
        resetCooldown();
        Inventory inv = player.player.getInventory();
        inv.setItem(hotBarSlot, type.getAbilityItem());
    }

    public void hit() {
        if (cooldownTimer != null) {
            cooldownTimer.hit();
        }
    }

    public void use() {
        if (!player.lobby.botBowsGame.canMove) return;
        Inventory inv = player.player.getInventory();
        if (baseCooldown > 0) {
            cooldownTimer = new CooldownTimer(inv);
            cooldownTimer.runTaskTimer(Main.getPlugin(), 0L, 20L);
        } else {
            inv.setItem(hotBarSlot, type.getCooldownItems()[0].clone());
        }
    }

    private ItemStack getCooldownItem(int cooldown) {
        if (cooldown > 10) {
            return type.getCooldownItems()[0].clone();
        } else if (cooldown > 5) {
            return type.getCooldownItems()[1].clone();
        } else if (cooldown > 2) {
            return type.getCooldownItems()[2].clone();
        } else {
            return type.getCooldownItems()[3].clone();
        }
    }

    private class CooldownTimer extends BukkitRunnable {
        int currentCooldown = effectiveCooldown;
        ItemStack cooldownItem = getCooldownItem(currentCooldown);
        private final Inventory inv;

        private CooldownTimer(Inventory inv) {
            this.inv = inv;
        }

        @Override
        public void run() {
            switch (currentCooldown) {
                case 10 -> cooldownItem = type.getCooldownItems()[1].clone();
                case 5 -> cooldownItem = type.getCooldownItems()[2].clone();
                case 2 -> cooldownItem = type.getCooldownItems()[3].clone();
                case 0 -> {
                    obtain();
                    return;
                }
            }
            cooldownItem.setAmount(currentCooldown);
            inv.setItem(hotBarSlot, cooldownItem);
            currentCooldown--;
        }

        public void hit() { // when someone hits you with a bow, the cooldown wont go down until the damage cooldown is complete (when barrier blocks get removed)
            currentCooldown += BotBows.HIT_DISABLED_ITEM_TICKS / 20;
        }
    }
}
