package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.extras.StickSlap;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsMap;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TestCommand implements CommandExecutor {

    public static boolean rotation = true;
    public static boolean verboseDebugging = false;
    public static boolean debugging = true;
    public static boolean test1 = false;
    public static boolean test2 = false;
    public static Inventory testInv = Bukkit.createInventory(null, 54, Component.text("Lagre-Chest"));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        Player p = (Player) sender;
        if (args.length >= 1) {
            switch (args[0]) {
                case "t" ->
                        BotBows.debugMessage("The team of " + p.getName() + " is " + BotBows.getBotBowsPlayer(p).getTeam());
                case "w" -> {
                    BotBows.joinGame(Bukkit.getPlayer("GruveXp"));
                    BotBows.joinGame(Bukkit.getPlayer("Spionagent54"));
                    BotBows.settings.setMap(BotBowsMap.ICY_RAVINE);
                    BotBows.settings.setWinThreshold(-1);
                    BotBows.settings.healthMenu.enableCustomHP();
                    BotBowsPlayer judith = BotBows.getBotBowsPlayer(Bukkit.getPlayer("Spionagent54"));

                    judith.setMaxHP(20);
                    Bukkit.dispatchCommand(Objects.requireNonNull(Bukkit.getPlayer("GruveXp")), "botbows:start");  // tester om dungeonen funker
                }
                case "a" -> {
                    rotation = !rotation;
                    BotBows.debugMessage("New location logic set to: " + rotation);
                }
                case "b" -> {
                    verboseDebugging = !verboseDebugging;
                    BotBows.debugMessage("Verbose debugging set to: " + verboseDebugging);
                }
                case "toggle_debugging" -> {
                    debugging = !debugging;
                    BotBows.debugMessage("Debugging set to: " + debugging);
                }
                case "t1" -> {
                    test1 = !test1;
                    BotBows.debugMessage("Test1 set to: " + test1);
                }
                case "t2" -> {
                    test2 = !test2;
                    BotBows.debugMessage("Test2 set to: " + test2);
                }
                case "inv" -> p.openInventory(testInv);
                case "set_blaze_rod_cooldown" -> StickSlap.cooldown = Integer.parseInt(args[1]);
                default -> BotBows.debugMessage("Wrong arg");
            }
            return true;
        }
        //BotBowsManager.debugMessage(STR."\{p.getName()}is \{isInDungeon(p) ? "" : "not"} in a dungeon\{BotBowsManager.isInDungeon(p) ? STR.", section\{BotBowsManager.getSection(p)}" : ""}");
        return true;
    }
}
