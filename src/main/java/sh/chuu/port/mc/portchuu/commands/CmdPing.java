package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.util.List;

public class CmdPing implements TabExecutor {
    private static final String OTHER_PERM = "portchuu.command.ping.other";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        if (args.length != 0 && sender.hasPermission(OTHER_PERM)) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(TextTemplates.unknownPlayer());
                return true;
            }
        } else {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("Pong! You're the server, <1ms!");
                return true;
            }
        }

        TextComponent a = new TextComponent();
        a.setColor(ChatColor.GRAY);
        if (sender == target) {
            a.setText("Pong! Your ping: ");
        }
        else {
            TextComponent b = new TextComponent(target.getName());
            b.setColor(ChatColor.WHITE);
            a.setText("Pong! ");
            a.addExtra(b);
            a.addExtra("'s ping: ");
        }

        int ping = target.spigot().getPing();
        TextComponent b = new TextComponent(ping + "ms");
        b.setColor(TextTemplates.colorPing(ping));
        a.addExtra(b);
        if (sender == target)
            sender.sendMessage(a);
        else
            sender.sendMessage(a);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission(OTHER_PERM))
            return null;
        return ImmutableList.of();
    }
}
