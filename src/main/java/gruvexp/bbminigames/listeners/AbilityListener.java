package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.Util;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.abilities.*;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class AbilityListener implements Listener {

    public static HashMap<Arrow, BukkitTask> thunderArrows = new HashMap<>();
    public static HashMap<Arrow, BukkitTask> splashArrows = new HashMap<>();

    public static void onAbilityUse(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        ItemStack abilityItem = e.getItem();
        AbilityType type = AbilityType.fromItem(abilityItem);
        if (type == null) return;
        //BotBows.debugMessage("Used ability: " + type.name());
        if (!lobby.isGameActive() && !TestCommand.testAbilities) {
            //BotBows.debugMessage("Game is not active: cancelling");
            e.setCancelled(true); // kanke bruke abilities i lobbyen
            return;
        }
        BotBows.debugMessage("Ability gets used");
        switch (type) {
            case ENDER_PEARL, SHRINK, RADAR, THUNDER_BOW, SALMON_SLAP -> bp.getAbility(type).use();
            case WIND_CHARGE -> {
                BotBows.debugMessage("Items in hand: " + abilityItem.getAmount());
                bp.registerUsedAbilityItem(abilityItem.getAmount());
            }
            case FLOAT_SPELL -> {
                bp.getAbility(type).use();
                Block block = e.getClickedBlock();
                if (block == null) return;
                Chicken chicken = (Chicken) Main.WORLD.spawnEntity(block.getLocation().add(0.5, 1, 0.5), EntityType.CHICKEN);
                FloatSpellAbility.animateChicken(chicken);
                ((FloatSpellAbility) bp.getAbility(type)).handleUsage(chicken);
                // Reduce the item count (simulate spawn egg usage)
                if (abilityItem.getAmount() > 1) {
                    abilityItem.setAmount(abilityItem.getAmount() - 1);
                } else {
                    p.getInventory().remove(abilityItem);
                }
                e.setCancelled(true); // gjør sånn at det ikke spawnes 2 stykker
            }
            case BUBBLE_JET -> {
                p.resetPlayerWeather();
                p.getInventory().getItemInMainHand().addEnchantment(Enchantment.RIPTIDE, 3);
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    if (!bp.lobby.settings.stormHazard.isActive()) {
                        p.setPlayerWeather(WeatherType.CLEAR);
                    } else {
                        p.resetPlayerWeather();
                    }
                }, 60L);
            }
            case CREEPER_TRAP -> {
                Block block = Util.getTargetBlock(p, 5);
                if (block.getType() == Material.AIR) return;
                Location placeLoc = block.getLocation().add(0.5, 1, 0.5);
                CreeperTrapAbility creeperTrapAbility = (CreeperTrapAbility) bp.getAbility(type);
                creeperTrapAbility.use(placeLoc);
            }
        }
    }

    public static void onSlap(EntityDamageByEntityEvent e, Player attacker, Player defender, ItemStack weapon) {
        Lobby lobby = BotBows.getLobby(attacker);
        if (lobby == null) return;
        BotBowsPlayer attackerBp = lobby.getBotBowsPlayer(attacker);
        BotBowsPlayer defenderBp = lobby.getBotBowsPlayer(defender);
        if (defenderBp == null) return;
        AbilityType type = AbilityType.fromItem(weapon);
        if (type == AbilityType.LONG_ARMS) {
            attackerBp.getAbility(AbilityType.LONG_ARMS).use();
            defenderBp.handleHit(Component.text(" was long-slapped by "), attackerBp);
        } else if (weapon.getType() == Material.SALMON) {
            defenderBp.handleHit(Component.text(" was slapped by "), attackerBp);
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player p)) return;
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        if (e.getEntity() instanceof WindCharge) {
            if (!bp.hasAbilityEquipped(AbilityType.WIND_CHARGE)) return;
            if (bp.getUsedAbilityItemAmount() != 1) return;
            bp.getAbility(AbilityType.WIND_CHARGE).use();
        } else if (e.getEntity() instanceof Arrow arrow) {
            if (p.getInventory().getItemInMainHand().getType() == Material.BOW) {
                if (bp.hasAbilityEquipped(AbilityType.SPLASH_BOW)) {
                    arrow.setColor(Color.RED);
                    BukkitTask arrowTrail = new SplashBowAbility.SplashArrowTrailGenerator(arrow, bp.getTeam().dyeColor.getColor()).runTaskTimer(Main.getPlugin(), 1L, 1L);
                    arrow.getVelocity().multiply(0.5f);
                    splashArrows.put(arrow, arrowTrail);
                    bp.getAbility(AbilityType.SPLASH_BOW).use();
                } else if (bp.hasAbilityEquipped(AbilityType.THUNDER_BOW)) {
                    arrow.setColor(Color.AQUA);
                    BukkitTask arrowTrail = new ThunderBowAbility.ThunderArrowTrailGenerator(arrow, bp.getTeam().dyeColor.getColor()).runTaskTimer(Main.getPlugin(), 1L, 1L);
                    thunderArrows.put(arrow, arrowTrail);

                }
            } else if (p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW) {
                arrow.setGravity(false);
            }
        } else if (e.getEntity() instanceof ThrownPotion potion) {
            if (potion.getItem().getType() == Material.LINGERING_POTION) {
                LingeringPotionAbility.giveRandomEffect(potion.getItem());
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

    @EventHandler
    public void onAbilityDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        AbilityType type = AbilityType.fromItem(e.getItemDrop().getItemStack());
        if (type == null) return;
        if (!bp.hasAbilityEquipped(type)) return; // kan droppe itemet hvis det ikke var equippa
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

    @EventHandler
    public void onSwing(PlayerAnimationEvent e) { // left clicking but not hitting anything
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        AbilityType type = AbilityType.fromItem(p.getInventory().getItemInMainHand());
        if (type == null) return;
        if (lobby.isGameActive() && type == AbilityType.LONG_ARMS) {
            bp.getAbility(AbilityType.LONG_ARMS).use();
        }
    }

    @EventHandler
    public void onRiptide(PlayerRiptideEvent e) {
        Player attacker = e.getPlayer();
        Lobby lobby = BotBows.getLobby(attacker);
        if (lobby == null) return;
        BotBowsPlayer attackerBp = lobby.getBotBowsPlayer(attacker);

        if (!attackerBp.hasAbilityEquipped(AbilityType.BUBBLE_JET)) return;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (!attackerBp.lobby.settings.stormHazard.isActive()) {
                attacker.setPlayerWeather(WeatherType.CLEAR);
            } else {
                attacker.resetPlayerWeather();
            }
        }, 10L);
        attackerBp.getAbility(AbilityType.BUBBLE_JET).use();
    }
}
