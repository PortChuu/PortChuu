package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CmdReport implements TabExecutor {
    private static final String REPORT_URL = "https://port.chuu.sh/report";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(new ComponentBuilder("Submit a report using this form")
                .color(ChatColor.AQUA)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, REPORT_URL))
                .create()
        );
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return ImmutableList.of();
    }
}
