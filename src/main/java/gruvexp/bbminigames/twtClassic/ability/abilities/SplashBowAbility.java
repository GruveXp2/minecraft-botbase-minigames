package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SplashBowAbility extends Ability {

    public static final double SPLASH_RADIUS = 3.0;

    public SplashBowAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.SPLASH_BOW;
        this.baseCooldown = type.getBaseCooldown();
        player.player.getInventory().setItem(18, new ItemStack(Material.ARROW, 64));
    }

    public static void handleArrowHit(Player attacker, Location hitLoc) {
        Main.WORLD.spawnParticle(Particle.EXPLOSION_EMITTER, hitLoc, 5, SPLASH_RADIUS/4, SPLASH_RADIUS/4, SPLASH_RADIUS/4, 5);
        Main.WORLD.spawnParticle(Particle.DUST, hitLoc, 1000, 3, 3, 3, 0.4, new Particle.DustOptions(Color.RED, 5));  // Red color
        for (Entity entity : Main.WORLD.getNearbyEntities(hitLoc, SPLASH_RADIUS*2, SPLASH_RADIUS*2, SPLASH_RADIUS*2, entity -> entity instanceof Player)) {
            Player p = (Player) entity;
            p.sendMessage("You are within 3 blocks of the arrow impact!");
            Lobby lobby = BotBows.getLobby(p);
            if (lobby == null) return;
            if (lobby != BotBows.getLobby(attacker)) return;
            BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
            bp.handleHit(lobby.getBotBowsPlayer(attacker), Component.text(" was splash bowed by "));
        }
    }

    public static class ArrowTrailGenerator extends BukkitRunnable {

        private final Arrow arrow;

        public ArrowTrailGenerator(Arrow arrow) {
            this.arrow = arrow;
        }

        @Override
        public void run() {
            Main.WORLD.spawnParticle(Particle.DUST, arrow.getLocation(), 5, 1, 1, 1, 0.1, new Particle.DustOptions(Color.RED, 1));  // Red color
        }
    }
}
