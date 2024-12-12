package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class TeamsMenu extends SettingsMenu {

    BotBowsTeam team1;
    BotBowsTeam team2;

    @Override
    public String getMenuName() {
        return "Select teams";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        // if you click on a player then they change teams
        Player clicker = (Player) e.getWhoClicked();
        switch (e.getCurrentItem().getType()) {
            case PLAYER_HEAD -> {
                Player p = Bukkit.getPlayer(UUID.fromString(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "uuid"), PersistentDataType.STRING)));
                BotBowsTeam team = BotBows.getBotBowsPlayer(p).getTeam();
                team.getOppositeTeam().join(BotBows.getBotBowsPlayer(p));
                recalculateTeam();
                settings.healthMenu.updateMenu(); // pga teammembers endres må health settings oppdateres pga det er basert på farger
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.mapMenu.open(clicker);
                } else if (e.getSlot() == getSlots() - 4) {
                    settings.healthMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        super.setMenuItems(); // settings init
        init();
        setColoredGlassPanes();
        setPageButtons(2, true, true, null);
    }

    public void init() {
        team1 = settings.team1;
        team2 = settings.team2;
    }

    public void setColoredGlassPanes() {
        ItemStack team1Pane = makeItem(team1.getGlassPane(), Component.text("Team " + team1, team1.COLOR));
        ItemStack team2Pane = makeItem(team2.getGlassPane(), Component.text("Team " + team2, team2.COLOR));
        inventory.setItem(0, team1Pane);
        inventory.setItem(1, team1Pane);
        inventory.setItem(7, team1Pane);
        inventory.setItem(8, team1Pane);
        inventory.setItem(9, team2Pane);
        inventory.setItem(10, team2Pane);
        inventory.setItem(16, team2Pane);
        inventory.setItem(17, team2Pane);
    }

    public void recalculateTeam() {
        inventory.remove(Material.PLAYER_HEAD); // Fjerner player heads sånn at det kan kalkuleres pånytt

        for (int i = 0; i < team1.size(); i++) { // team 1
            ItemStack p = makeHeadItem(team1.getPlayer(i).PLAYER, team1.COLOR);
            inventory.setItem(2 + i, p);
        }
        for (int i = 0; i < team2.size(); i++) { // team 2
            ItemStack p = makeHeadItem(team2.getPlayer(i).PLAYER, team2.COLOR);
            inventory.setItem(11 + i, p);
        }
    }
}
