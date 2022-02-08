package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.PortChuu;
import sh.chuu.port.mc.portchuu.TextTemplates;
import sh.chuu.port.mc.portchuu.modules.PermissionsModule;

import java.util.ArrayList;
import java.util.List;

public class CmdGreylist implements TabExecutor {
    private static final String ADD_PERM = "portchuu.command.greylist.add";
    private static final String GREYLIST_URL = "https://port.chuu.sh/greylist";
    private static final String ADD = "add";
    private static final String CHECK = "check";

    private final PermissionsModule module = PortChuu.getInstance().getPermissionsModule();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission(ADD_PERM)) {
            if (args.length < 2) {
                sender.sendMessage(usageAdd());
                return true;
            }
            if (args[0].equalsIgnoreCase(ADD))
                doGreylist(sender, args[1]);
            else if (args[0].equalsIgnoreCase(CHECK))
                checkGreylist(sender, args[1]);
            return true;
        }

        if (args.length == 0) {
            checkGreylist(sender, null);
            return true;
        }
        checkGreylist(sender, args[0]);
        return true;
    }

    private void doGreylist(CommandSender sender, String name) {
        module.getUUID(name).thenAcceptAsync(uuid -> {
            if (uuid == null) {
                // UUID doesn't exist!
                sender.sendMessage(TextTemplates.unknownPlayer());
                return;
            }

            module.greylist(uuid).thenAcceptAsync(success -> {
                if (success)
                    broadcastGreylist(name);
                else
                    sender.sendMessage(new ComponentBuilder(name)
                            .color(ChatColor.WHITE)
                            .append(" is already greylisted!")
                            .color(ChatColor.GRAY)
                            .create()
                    );
            });
        });
    }

    private void checkGreylist(CommandSender sender, String name) {
        if (name == null) {
            // check for self
            if (module.isGreylisted(sender))
                sender.sendMessage(new ComponentBuilder("You are ")
                        .color(ChatColor.GRAY)
                        .append("greylisted")
                        .color(ChatColor.GREEN)
                        .create()
                );
            else
                sender.sendMessage(new ComponentBuilder("You're not greylisted. ")
                        .color(ChatColor.GRAY)
                        .append("Apply for the greylist here!")
                        .color(ChatColor.AQUA)
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, GREYLIST_URL))
                        .create()
                );
            return;
        }
        // check for other
        Player p = Bukkit.getPlayerExact(name);
        if (p != null) {
            sendGreylistedMessage(sender, name, module.isGreylisted(p));
        } else {
            module.getUUID(name).thenAcceptAsync(uuid -> {
                if (uuid == null) {
                    // UUID doesn't exist!
                    sender.sendMessage(TextTemplates.unknownPlayer());
                    return;
                }
                module.isGreylistedOffline(uuid).thenAccept(listed -> sendGreylistedMessage(sender, name, listed));
            });
        }
    }

    private void sendGreylistedMessage(CommandSender sender, String name, boolean listed) {
        if (listed)
            sender.sendMessage(new ComponentBuilder(name)
                    .color(ChatColor.WHITE)
                    .append(" is ")
                    .color(ChatColor.GRAY)
                    .append("greylisted")
                    .color(ChatColor.GREEN)
                    .create()
            );
        else
            sender.sendMessage(new ComponentBuilder(name)
                    .color(ChatColor.WHITE)
                    .append(" is ")
                    .color(ChatColor.GRAY)
                    .append("not")
                    .color(ChatColor.RED)
                    .append(" greylisted")
                    .color(ChatColor.GRAY)
                    .create()
            );
    }

    private void broadcastGreylist(String name) {
        BaseComponent send = new TextComponent(name + " is now greylisted!");
        send.setColor(ChatColor.YELLOW);
        Bukkit.broadcast(send);
        Bukkit.getConsoleSender().sendMessage(send);
        PortChuu.getInstance().sendToDiscord(":tada: **" + name + " is now greylisted!**");
    }

    private BaseComponent usageAdd() {
        String msg = "Usage: /greylist <add|check> <player>";
        BaseComponent ret = new TextComponent(msg);
        ret.setColor(ChatColor.RED);
        return ret;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.hasPermission(ADD_PERM)) {
            if (args.length == 1) {
                ArrayList<String> list = new ArrayList<>();
                if (ADD.startsWith(args[0].toLowerCase())) list.add(ADD);
                if (CHECK.startsWith(args[0].toLowerCase())) list.add(CHECK);
                return list;
            }
            if (args.length == 2) {
                return null;
            }
        } else {
            if (args.length == 1) {
                return null;
            }
        }
        return ImmutableList.of();
    }
}
