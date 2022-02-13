package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(usage());
            return true;
        }

        Player pl = Bukkit.getPlayerExact(args[0]);
        if (pl != null && pl.isOnline()) {
            sender.sendMessage(Component.text(pl.getName(), NamedTextColor.WHITE)
                    .append(Component.text(" is currently ", NamedTextColor.GRAY))
                    .append(Component.text("online", NamedTextColor.WHITE))
            );
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target;

            //noinspection deprecation
            target = Bukkit.getOfflinePlayer(args[0]);
            long time = target.getLastSeen();
            if (time == 0) {
                sender.sendMessage(TextTemplates.unknownPlayer());
                return;
            }
            int diff = (int)((System.currentTimeMillis() - time) / 1000);
            Locale locale = sender instanceof Player ? ((Player) sender).locale() : null;

            sender.sendMessage(Component.empty().append(Component.text(target.getName(), NamedTextColor.WHITE))
                    .append(Component.text(" was last seen ", NamedTextColor.GRAY))
                    .append(TextTemplates.timeText(time, diff, true, locale, null, NamedTextColor.WHITE))
            );
        });
        return true;
    }

    private Component usage() {
        return Component.text("Usage: /seen <offline player>", NamedTextColor.RED);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return ImmutableList.of();
    }
}