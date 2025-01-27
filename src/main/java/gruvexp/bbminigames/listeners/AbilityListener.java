package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.abilities.SplashBowAbility;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class AbilityListener implements Listener {

    public static HashMap<Arrow, BukkitTask> splashArrows = new HashMap<>();

    @EventHandler
    public void onAbilityUse(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        ItemStack abilityItem = e.getItem();
        AbilityType type = AbilityType.fromItem(abilityItem);
        if (type == null) return;
        BotBows.debugMessage("Used ability: " + type.name());
        if (!lobby.isGameActive()) {
            BotBows.debugMessage("Game is not active: cancelling");
            e.setCancelled(true); // kanke bruke abilities i lobbyen
            return;
        }
        BotBows.debugMessage("Ability gets used");
        switch (type) {
            case ENDER_PEARL -> bp.getAbility(type).use();
            case WIND_CHARGE -> {
                BotBows.debugMessage("Items in hand: " + abilityItem.getAmount());
                bp.registerUsedAbilityItem(abilityItem.getAmount());
            }
        }
    }

    @EventHandler
    public void onWindChargeAbilityThrow(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player p)) return;
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        if (e.getEntity() instanceof WindCharge) {
            if (!bp.isAbilityEquipped(AbilityType.WIND_CHARGE)) return;
            if (bp.getUsedAbilityItemAmount() != 1) return;
            bp.getAbility(AbilityType.WIND_CHARGE).use();
        } else if (e.getEntity() instanceof Arrow arrow) {
            if (p.getInventory().getItemInMainHand().getType() == Material.BOW) {
                if (!bp.isAbilityEquipped(AbilityType.SPLASH_BOW)) return;
                arrow.setColor(Color.RED);
                BukkitTask arrowTrail = new SplashBowAbility.ArrowTrailGenerator(arrow).runTaskLater(Main.getPlugin(), 1L);
                splashArrows.put(arrow, arrowTrail);
                bp.getAbility(AbilityType.SPLASH_BOW).use();
            } else if (p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW) {
                arrow.setGravity(false);
            }
        }
    }

    @EventHandler
    public void onPotionAbilityUse(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        AbilityType type = AbilityType.fromItem(e.getItem());
        if (type == null) return;
        if (!lobby.isGameActive()) {
            e.setCancelled(true); // kanke bruke abilities i lobbyen
            return;
        }
        switch (type) {
            case SPEED_POTION, INVIS_POTION -> bp.getAbility(type).use();
        }
    }

    public static void onPlayerRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        AbilityType type = AbilityType.fromItem(e.getItem());
        if (type == null) return;
        if (!lobby.isGameActive()) {
            e.setCancelled(true); // kanke bruke abilities i lobbyen
            return;
        }
        switch (type) {
            case SHRINK, RADAR -> bp.getAbility(type).use();
        }
    }

    @EventHandler
    public void onAbilityDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        AbilityType type = AbilityType.fromItem(e.getItemDrop().getItemStack());
        if (type == null) return;
        if (!bp.isAbilityEquipped(type)) return; // kan droppe itemet hvis det ikke var equippa
        e.setCancelled(true); // kanke droppe ability items
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        if (!(projectile instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player attacker)) {return;} // den som skøyt
        if (!splashArrows.containsKey(arrow)) return;
        Location hitLoc;
        if (e.getHitEntity() != null) {
            hitLoc = e.getHitEntity().getLocation();
        } else {
            hitLoc = e.getHitBlock().getLocation();
        }
        SplashBowAbility.handleArrowHit(attacker, hitLoc);
        splashArrows.get(arrow).cancel();
        splashArrows.remove(arrow);
        arrow.remove();
    }
}
