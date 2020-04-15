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
import sh.chuu.port.mc.portchuu.modules.DiscordSRVHook;
import sh.chuu.port.mc.portchuu.modules.PermissionsModule;

import java.util.ArrayList;
import java.util.List;

public class CmdGraylist implements TabExecutor {
    private static final String ADD_PERM = "portchuu.command.graylist.add";
    private static final String GRAYLIST_URL = "https://port.chuu.sh/graylist";
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
                doGraylist(sender, args[1]);
            else if (args[0].equalsIgnoreCase(CHECK))
                checkGraylist(sender, args[1]);
            return true;
        }

        if (args.length == 0) {
            checkGraylist(sender, null);
            return true;
        }
        checkGraylist(sender, args[0]);
        return true;
    }

    private void doGraylist(CommandSender sender, String name) {
        module.getUUID(name).thenAcceptAsync(uuid -> {
            if (uuid == null) {
                // UUID doesn't exist!
                sender.sendMessage(TextTemplates.unknownPlayer());
                return;
            }

            module.graylist(uuid).thenAcceptAsync(success -> {
                if (success)
                    broadcastGraylist(name);
                else
                    sender.sendMessage(new ComponentBuilder(name)
                            .color(ChatColor.WHITE)
                            .append(" is already graylisted!")
                            .color(ChatColor.GRAY)
                            .create()
                    );
            });
        });
    }

    private void checkGraylist(CommandSender sender, String name) {
        if (name == null) {
            // check for self
            if (module.isGraylisted(sender))
                sender.sendMessage(new ComponentBuilder("You are ")
                        .color(ChatColor.GRAY)
                        .append("graylisted")
                        .color(ChatColor.GREEN)
                        .create()
                );
            else
                sender.sendMessage(new ComponentBuilder("You're not graylisted. ")
                        .color(ChatColor.GRAY)
                        .append("Apply for the graylist here!")
                        .color(ChatColor.AQUA)
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, GRAYLIST_URL))
                        .create()
                );
            return;
        }
        // check for other
        Player p = Bukkit.getPlayerExact(name);
        if (p != null) {
            sendGraylistedMessage(sender, name, module.isGraylisted(p));
        } else {
            module.getUUID(name).thenAcceptAsync(uuid -> {
                if (uuid == null) {
                    // UUID doesn't exist!
                    sender.sendMessage(TextTemplates.unknownPlayer());
                    return;
                }
                module.isGraylisted(uuid).thenAccept(listed -> sendGraylistedMessage(sender, name, listed));
            });
        }
    }

    private void sendGraylistedMessage(CommandSender sender, String name, boolean listed) {
        if (listed)
            sender.sendMessage(new ComponentBuilder(name)
                    .color(ChatColor.WHITE)
                    .append(" is ")
                    .color(ChatColor.GRAY)
                    .append("graylisted")
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
                    .append(" graylisted")
                    .color(ChatColor.GRAY)
                    .create()
            );
    }

    private void broadcastGraylist(String name) {
        BaseComponent send = new TextComponent(name + " is now graylisted!");
        send.setColor(ChatColor.YELLOW);
        Bukkit.broadcast(send);
        Bukkit.getConsoleSender().sendMessage(send);
        PortChuu.getInstance().sendToDiscord(":tada: **" + name + " is now graylisted!**");
    }

    private BaseComponent usageAdd() {
        String msg = "Usage: /graylist <add|check> <player>";
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
