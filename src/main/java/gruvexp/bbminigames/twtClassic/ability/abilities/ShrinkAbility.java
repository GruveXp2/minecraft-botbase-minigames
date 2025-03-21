package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.attribute.Attribute;
import org.bukkit.scheduler.BukkitRunnable;

public class ShrinkAbility extends Ability {

    public ShrinkAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.SHRINK);
    }

    @Override
    public void use() {
        super.use();
        new BukkitRunnable() {
            int i = 1;
            @Override
            public void run() {
                if (i == 10) {
                    this.cancel();
                }
                bp.player.getAttribute(Attribute.SCALE).setBaseValue(1.0 - 0.5/10 * i);
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 1L);
        new BukkitRunnable() {
            int i = 1;
            @Override
            public void run() {
                if (i == 20) {
                    this.cancel();
                }
                bp.player.getAttribute(Attribute.SCALE).setBaseValue(0.5 + 0.5/20 * i);
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), 100L, 1L);
    }
}