package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class AbilityListener implements Listener {

    @EventHandler
    public void onAbilityUse(PlayerInteractEvent e) {
        if (e.getItem() != null && e.getHand() == EquipmentSlot.HAND) {
            Player p = e.getPlayer();
            BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
            if (bp == null) return;
            AbilityType type = AbilityType.fromItem(e.getItem());
            if (type == null) return;
            if (!BotBows.activeGame) {
                e.setCancelled(true); // kanke bruke abilities i lobbyen
                return;
            }
            switch (type) {
                case ENDER_PEARL, SPEED_POTION, INVIS_POTION -> bp.getAbility(type).use();
                case WIND_CHARGE -> {
                    if (e.getItem().getAmount() == 1) bp.getAbility(type).use();
                }
            }
        }
    }
}
