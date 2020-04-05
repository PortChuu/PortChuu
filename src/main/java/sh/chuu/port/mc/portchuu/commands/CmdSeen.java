package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.chuu.port.mc.portchuu.PortChuu;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.util.List;
import java.util.Locale;

public class CmdSeen implements TabExecutor {
    private final PortChuu plugin = PortChuu.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(usage());
            return true;
        }

        Player pl = Bukkit.getPlayerExact(args[0]);
        if (pl != null && pl.isOnline()) {
            sender.sendMessage(new ComponentBuilder(pl.getDisplayName())
                    .color(ChatColor.WHITE)
                    .append(" is currently ")
                    .color(ChatColor.GRAY)
                    .append("online")
                    .color(ChatColor.WHITE)
                    .create()
            );
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target;

            //noinspection deprecation
            target = Bukkit.getOfflinePlayer(args[0]);
            long time = target.getLastSeen();
            int diff = (int)((System.currentTimeMillis() - time) / 1000);
            Locale locale = sender instanceof Player ? TextTemplates.locale(((Player) sender).getLocale()) : null;

            sender.sendMessage(new ComponentBuilder(target.getName())
                    .color(ChatColor.WHITE)
                    .append(" was last seen ")
                    .color(ChatColor.GRAY)
                    .append(TextTemplates.timeText(time, diff, true, locale, null, ChatColor.WHITE))
                    .create()
            );
        });
        return true;
    }

    private BaseComponent usage() {
        String msg = "Usage: /seen <offline player>";
        BaseComponent ret = new TextComponent(msg);
        ret.setColor(net.md_5.bungee.api.ChatColor.RED);
        return ret;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return ImmutableList.of();
    }
}