package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.awt.Color;

public class Board {

    private static Objective objective;
    private static Team sbTeam1;
    private static Team sbTeam2;
    public static ScoreboardManager manager = Bukkit.getScoreboardManager();

    public static void createBoard() {
        Scoreboard board = manager.getNewScoreboard();
        Component objectiveTitle = Component.text("BotBows").style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("Classic").color(NamedTextColor.AQUA));
        objective = board.registerNewObjective("botbows", Criteria.DUMMY, objectiveTitle);

        BotBowsTeam team1 = BotBows.settings.team1;
        BotBowsTeam team2 = BotBows.settings.team2;
        // setter inn scores
        setScore(darkenColor(team2.COLOR) + "TEAM " + team2.NAME.toUpperCase(), BotBows.settings.team2.size());

        setScore(darkenColor(team1.COLOR) + "TEAM " + team1.NAME.toUpperCase(), BotBows.getTotalPlayers() + 1);
        setScore(ChatColor.GRAY + "----------", BotBows.getTotalPlayers() + 2);
        setScore("", BotBows.getTotalPlayers() + 5);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Team stuff!
        sbTeam1 = board.registerNewTeam(team1.NAME);
        sbTeam2 = board.registerNewTeam(team2.NAME);

        sbTeam1.color((NamedTextColor) team1.COLOR);
        sbTeam2.color((NamedTextColor) team2.COLOR);

        for (BotBowsPlayer p : team1.getPlayers()) {
            sbTeam1.addEntry(p.player.getName());
        }
        for (BotBowsPlayer p : team2.getPlayers()) {
            sbTeam2.addEntry(p.player.getName());
        }
    }

    public static void updatePlayerScore(BotBowsPlayer p) {
        removePlayerScore(p);

        int hp = p.getHP();
        int maxHp = p.getMaxHP();
        int playerLineIndex; // which line of the scoreboard the player stats will be shown
        if (BotBows.settings.team1.hasPlayer(p)) { //
            playerLineIndex = BotBows.settings.team1.getPlayerID(p) + BotBows.settings.team2.size() + 1;
        } else {
            playerLineIndex = BotBows.settings.team2.getPlayerID(p);
        }

        String healthBar;
        if (maxHp > 5) {
            healthBar = ChatColor.RED + "▏".repeat(hp) + ChatColor.GRAY + "▏".repeat(maxHp - hp) + p.getTeam().COLOR + " " + p.player.getName();
        } else {
            healthBar = ChatColor.RED + "❤".repeat(hp) + ChatColor.GRAY + "❤".repeat(maxHp - hp) + p.getTeam().COLOR + " " + p.player.getName();
        }

        setScore(healthBar, playerLineIndex);
    }

    public static void removePlayerScore(BotBowsPlayer p) {
        Scoreboard sb = objective.getScoreboard();
        for (Objective ignored : sb.getObjectives()) {
            for (String entries : sb.getEntries()) {
                if (entries.contains(p.player.getName())) {
                    sb.resetScores(entries);
                }
            }
        }
    }

    public static void updateTeamScores() {
        Scoreboard sb = objective.getScoreboard();
        int winThreshold = BotBows.settings.getWinThreshold();

        for (Objective ignored : sb.getObjectives()) {
            for (String entries : sb.getEntries()) {
                if (entries.contains(BotBows.settings.team1.NAME + ": ")) {
                    sb.resetScores(entries);
                }
                if (entries.contains(BotBows.settings.team2.NAME + ": ")) {
                    sb.resetScores(entries);
                }
            }
        }
        BotBowsTeam team1 = BotBows.settings.team1;
        BotBowsTeam team2 = BotBows.settings.team2;
        int totalPlayers = BotBows.getTotalPlayers();
        if (winThreshold == -1) {
            setScore(team1 + ": " + ChatColor.RESET + team1.getPoints(), 4 + totalPlayers); // legger inn scoren til hvert team
            setScore(team2 + ": " + ChatColor.RESET + team2.getPoints(), 3 + totalPlayers);
        } else if (winThreshold >= 35) {
            setScore(team1 + ": " + ChatColor.RESET + team1.getPoints() + " / " + ChatColor.GRAY + winThreshold, 4 + totalPlayers); // legger inn scoren til hvert team
            setScore(team2 + ": " + ChatColor.RESET + team2.getPoints() + " / " + ChatColor.GRAY + winThreshold, 3 + totalPlayers);
        } else { // få plass til mest mulig streker
            String healthSymbol = getHealthSymbol(winThreshold);
            int team1Points = Math.min(BotBows.settings.getWinThreshold(), team1.getPoints());
            int team2Points = Math.min(BotBows.settings.getWinThreshold(), team2.getPoints());

            setScore(team1 + ": " + ChatColor.GREEN + healthSymbol.repeat(team1Points) + ChatColor.GRAY + healthSymbol.repeat(winThreshold - team1Points), 4 + totalPlayers); // legger inn scoren til hvert team
            setScore(team2 + ": " + ChatColor.GREEN + healthSymbol.repeat(team2Points) + ChatColor.GRAY + healthSymbol.repeat(winThreshold - team2Points), 3 + totalPlayers);
        }
    }

    private static String getHealthSymbol(int winThreshold) {
        String c = "";
        if (winThreshold < 8) {
            c = "█";
        } else if (winThreshold < 9) {
            c = "▉";
        } else if (winThreshold < 10) {
            c = "▊";
        } else if (winThreshold < 12) {
            c = "▋";
        } else if (winThreshold < 15) {
            c = "▌";
        } else if (winThreshold < 17) {
            c = "▍";
        } else if (winThreshold < 23) {
            c = "▎";
        } else if (winThreshold < 34) {
            c = "▏";
        }
        return c;
    }

    private static void setScore(String text, int score) {
        Score l1 = objective.getScore(text);
        l1.setScore(score); //nederst
    }

    public static void resetTeams() {
        sbTeam1.unregister();
        sbTeam2.unregister();
    }

    private static TextColor darkenColor(TextColor color) {
        if (color instanceof NamedTextColor) {
            String colorName = color.toString();
            if (colorName.equals("LIGHT_PURPLE")) {
                return NamedTextColor.DARK_PURPLE;
            } else if (colorName.startsWith("LIGHT_")) {
                return NamedTextColor.NAMES.value(colorName.replace("LIGHT_", "").toLowerCase());
            } else {
                return NamedTextColor.NAMES.value(("DARK_" + colorName).toLowerCase());
            }
        } else {
            // rethrn a textcolor which is the input mutiplyed saturation with 1.5, and value with 2/3
            // Extract RGB values
            int rgb = color.value();
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            // Convert to HSV
            float[] hsv = Color.RGBtoHSB(red, green, blue, null);

            // Adjust saturation and value
            hsv[1] = Math.min(1.0f, hsv[1] * 1.5f); // Saturation
            hsv[2] = hsv[2] * 2.0f / 3.0f;         // Value

            // Convert back to RGB
            int darkenedRgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);

            // Create new TextColor
            return TextColor.color(darkenedRgb);
        }
    }
}
