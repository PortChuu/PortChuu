package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CmdGamemode implements TabExecutor {
    private static final String SURVIVAL = "survival";
    private static final String SPECTATOR = "spectator";
    private static final String OTHER_PERM = "portchuu.command.gamemode.other";


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HumanEntity target;
        if (args.length < 2) {
            if (!(sender instanceof HumanEntity)) {
                sender.sendMessage("Usage: /gamemode <spectator|survival> <player>");
                return true;
            } else {
                target = ((HumanEntity) sender);
            }
        } else {
            Player p = Bukkit.getPlayerExact(args[1]);
            if (p != null) {
                target = p;
            } else {
                try {
                    target = Bukkit.getPlayer(UUID.fromString(args[1]));
                } catch (IllegalArgumentException e) {
                    target = null;
                }
                if (target == null) {
                    sender.sendMessage(invalidEntity());
                    return true;
                }
            }
        }


        BaseComponent gamemode;
        if (args.length == 0) {
            sender.sendMessage(usage(sender));
            return true;
        } else if (SPECTATOR.equalsIgnoreCase(args[0])) {
            if (target.getGameMode() == GameMode.SPECTATOR)
                return true;
            target.setGameMode(GameMode.SPECTATOR);
            gamemode = new TranslatableComponent("gameMode.spectator");
        } else if (SURVIVAL.equalsIgnoreCase(args[0])) {
            if (target.getGameMode() == GameMode.SURVIVAL)
                return true;
            target.setGameMode(GameMode.SURVIVAL);
            gamemode = new TranslatableComponent("gameMode.survival");
        } else {
            sender.sendMessage(usage(sender));
            return true;
        }
        BaseComponent msg;
        if (sender == target) {
            msg = new TranslatableComponent("commands.gamemode.success.self", gamemode);
        } else {
            msg = new TranslatableComponent("commands.gamemode.success.other", sender.getName(), gamemode);
        }
        TextTemplates.adminBroadcast(msg, sender);
        return true;
    }

    private BaseComponent usage(CommandSender sender) {
        BaseComponent ret;
        if (sender.hasPermission(OTHER_PERM))
            ret = new TextComponent("Usage: /gamemode <survival|spectator> [player]");
        else
            ret = new TextComponent("Usage: /gamemode <survival|spectator>");
        ret.setColor(ChatColor.RED);
        return ret;
    }

    private BaseComponent invalidEntity() {
        BaseComponent ret = new TranslatableComponent("argument.entity.invalid");
        ret.setColor(ChatColor.RED);
        return ret;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (SPECTATOR.startsWith(args[0].toLowerCase())) list.add(SPECTATOR);
            if (SURVIVAL .startsWith(args[0].toLowerCase())) list.add(SURVIVAL );
            return list;
        }
        if (args.length == 2 && sender.hasPermission(OTHER_PERM)) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                String name = p.getName().toLowerCase();
                if (name.startsWith(args[1].toLowerCase())) list.add(name);
            }
            return list;
        }
        return ImmutableList.of();
    }
}
