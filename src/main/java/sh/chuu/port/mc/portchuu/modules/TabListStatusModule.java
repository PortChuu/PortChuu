package sh.chuu.port.mc.portchuu.modules;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.PortChuu;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TabListStatusModule {
    private final TextComponent time = new TextComponent();
    private final TextComponent tps = new TextComponent();
    private final TextComponent ping = new TextComponent();
    private final TextComponent coords = new TextComponent();
    private final TextComponent direction = new TextComponent();
    private final TextComponent[] header;
    private final TextComponent[] footer;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
    private final DecimalFormat tpsFormat = new DecimalFormat("#.0#");


    public TabListStatusModule() {
        PortChuu plugin = PortChuu.getInstance();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::update, 20L, 10L);

        header = new TextComponent[]{
                new TextComponent("Port Chuu\n"),
                time
        };
        header[0].setColor(ChatColor.AQUA);
        header[0].setBold(true);
        time.setColor(ChatColor.GRAY);

        footer = new TextComponent[]{
                new TextComponent("TPS: "),
                tps,
                new TextComponent(" | "),
                new TextComponent("Ping: "),
                ping,
                coords,
                new TextComponent(" ("),
                direction,
                new TextComponent(")")
        };
        footer[0].setColor(ChatColor.GOLD);
        footer[2].setColor(ChatColor.DARK_GRAY);
        footer[3].setColor(ChatColor.DARK_AQUA);
        coords.setColor(ChatColor.GRAY);
        footer[6].setColor(ChatColor.DARK_GRAY);
        direction.setColor(ChatColor.GRAY);
        footer[8].setColor(ChatColor.DARK_GRAY);
    }

    private void update() {
        time.setText(timeFormat.format(new Date()));

        double tpsv = Bukkit.getTPS()[0];
        tps.setText(tpsFormat.format(tpsv));
        tps.setColor(TextTemplates.colorTPS(tpsv));

        for (Player p : Bukkit.getOnlinePlayers()) {
            int np = p.spigot().getPing();
            ping.setText(np + "ms\n");
            ping.setColor(TextTemplates.colorPing(np));

            Location loc = p.getLocation();
            coords.setText(loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
            direction.setText(direction(loc.getYaw()));
            p.setPlayerListHeaderFooter(header, footer);
        }
    }

    private String direction(float yaw) {
        // For some reason, bukkit yaw is between -360 and 360.
        if (yaw < 0)
            yaw = (yaw + 360) % 360;

        if (yaw <= 45.0 || yaw >= 315.0)
            return "S, +z";
        else if (yaw < 135.0)
            return "W, -x";
        else if (yaw <= 225.0)
            return "N, -z";
        else
            return "E, +x";
    }
}
