package sh.chuu.port.mc.portchuu.modules;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.PortChuu;

import java.text.DecimalFormat;

public class TabListStatusModule {

    public TabListStatusModule() {
        PortChuu plugin = PortChuu.getInstance();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::update, 20L, 20L);
    }

    private void update() {
        double tps = Bukkit.getTPS()[0];
        String t = new DecimalFormat("#.0#").format(tps);
        TextComponent[] footer = {
                new TextComponent("TPS: "),
                new TextComponent(t),
                new TextComponent(" | "),
                new TextComponent("XYZ: "),
                new TextComponent()
        };

        footer[0].setColor(ChatColor.GOLD);
        footer[1].setColor(tps >= 18.0 ? ChatColor.GREEN : tps >= 15.0 ? ChatColor.YELLOW : ChatColor.RED);
        footer[2].setColor(ChatColor.DARK_GRAY);
        footer[3].setColor(ChatColor.GRAY);
        footer[4].setColor(ChatColor.WHITE);

        for (Player p : Bukkit.getOnlinePlayers()) {
            Location loc = p.getLocation();
            footer[4].setText(loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
            p.setPlayerListHeaderFooter(null, footer);
        }
    }
}
