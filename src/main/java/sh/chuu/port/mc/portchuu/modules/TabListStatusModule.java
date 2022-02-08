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
    private final TextComponent hour = new TextComponent();
    private final TextComponent colon = new TextComponent(":");
    private final TextComponent minute = new TextComponent();
    private final TextComponent tps = new TextComponent();
    private final TextComponent ping = new TextComponent();
    private final TextComponent direction = new TextComponent();
    private final TextComponent[] header;
    private final TextComponent[] footer;
    private final SimpleDateFormat[] timeFormat = {new SimpleDateFormat("hh"), new SimpleDateFormat("mm")};
    private final DecimalFormat tpsFormat = new DecimalFormat("#.0#");


    public TabListStatusModule() {
        PortChuu plugin = PortChuu.getInstance();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::update, 20L, 10L);

        header = new TextComponent[]{
                new TextComponent("Port Chuu\n"),
                hour, colon, minute
        };
        header[0].setColor(ChatColor.AQUA);
        header[0].setBold(true);
        hour.setColor(ChatColor.GRAY);
        colon.setColor(ChatColor.GRAY);
        minute.setColor(ChatColor.GRAY);

        footer = new TextComponent[]{
                new TextComponent("TPS: "),
                tps,
                new TextComponent(" | "),
                new TextComponent("Ping: "),
                ping,
                new TextComponent("- "),
                direction,
                new TextComponent(" -")
        };
        footer[0].setColor(ChatColor.GOLD);
        footer[2].setColor(ChatColor.DARK_GRAY);
        footer[3].setColor(ChatColor.DARK_AQUA);
        footer[5].setColor(ChatColor.DARK_GRAY);
        direction.setColor(ChatColor.GRAY);
        footer[7].setColor(ChatColor.DARK_GRAY);
    }

    private void update() {
        Date d = new Date();
        hour.setText(timeFormat[0].format(d));
        minute.setText(timeFormat[1].format(d));
        colon.setColor(colon.getColor() == ChatColor.GRAY ? ChatColor.BLACK : ChatColor.GRAY);

        double tpsv = Bukkit.getTPS()[0];
        tps.setText(tpsFormat.format(tpsv));
        tps.setColor(TextTemplates.colorTPS(tpsv));

        for (Player p : Bukkit.getOnlinePlayers()) {
            int np = p.spigot().getPing();
            ping.setText(np + "ms\n");
            ping.setColor(TextTemplates.colorPing(np));

            Location loc = p.getLocation();
            direction.setText(direction(loc.getYaw()));
            p.setPlayerListHeaderFooter(header, footer);
        }
    }

    private String direction(float yaw) {
        // For some reason, bukkit yaw is between -360 and 360.
        if (yaw < 0)
            yaw = (yaw + 360) % 360;

        if (yaw <= 22.5 || yaw >= 337.5)
            return "S, +z";
        else if (yaw < 67.5)
            return "SW, -x +z";
        else if (yaw <= 112.5)
            return "W, -x";
        else if (yaw < 157.5)
            return "NW, -x -z";
        else if (yaw <= 202.5)
            return "N, -z";
        else if (yaw < 247.0)
            return "NE, +x -z ";
        else if (yaw <= 292.0)
            return "E, +x";
        else
            return "SE, +x +z";
    }
}
