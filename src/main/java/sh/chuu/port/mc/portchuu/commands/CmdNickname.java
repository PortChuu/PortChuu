package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.NicknameManager;
import sh.chuu.port.mc.portchuu.PortChuu;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.util.ArrayList;
import java.util.List;

public class CmdNickname implements TabExecutor {
    private static final String LIST_PERM = "portchuu.command.nickname.list";
    private static final String OTHER_PERM = "portchuu.command.nickname.other";
    private static final String SELF_PERM = "portchuu.command.nickname.self";
    private static final String ALLSTYLE_PERM = "portchuu.command.nickname.allstyle";
    private static final String COLOR_PERM = "portchuu.command.nickname.color";
    private static final String MULTICOLOR_PERM = "portchuu.command.nickname.color.multiple";
    private static final String BOLD_PERM = "portchuu.command.nickname.bold";
    private static final String ITALIC_PERM = "portchuu.command.nickname.italic";
    private static final String MAGIC_PERM = "portchuu.command.nickname.magic";
    private static final String STRIKETHROUGH_PERM = "portchuu.command.nickname.strikethrough";
    private static final String UNDERLINE_PERM = "portchuu.command.nickname.underline";

    private static final String SET = "set";
    private static final String SETOTHER = "setother";
    private static final String CLEAR = "clear";
    private static final String CLEAROTHER = "clearother";
    private static final String LIST = "list";

    private final NicknameManager nicknameManager;

    public CmdNickname(NicknameManager nicknameManager) {
        this.nicknameManager = nicknameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(usage(sender));
            return true;
        }
        // "usage: /nickname <set <nickname>|clear|setother <player> <nickname>|clearother <player>|list>"

        if (args[0].equalsIgnoreCase(SET)) {
            if (args.length < 2) sender.sendMessage(usage(sender));
            else setNick((Player) sender, args[1]);
            return true;
        }
        if (args[0].equalsIgnoreCase(CLEAR)) {
            setNick((Player) sender, null);
            return true;
        }
        if (args[0].equalsIgnoreCase(SETOTHER)) {
            if (args.length < 3) sender.sendMessage(usage(sender));
            else setNick(sender, args[1], args[2]);
            return true;
        }
        if (args[0].equalsIgnoreCase(CLEAROTHER)) {
            if (args.length < 2) sender.sendMessage(usage(sender));
            else setNick(sender, args[1], null);
            return true;
        }
        if (args[0].equalsIgnoreCase(LIST)) {
            sender.sendMessage("This is not implemented yet");
            return true;
        }

        sender.sendMessage(usage(sender));
        return true;
    }

    private void setNick(Player p, String nick) {
        setNick(p, p, nick);
    }

    private void setNick(CommandSender sender, String targetStr, String nick) {
        Player pTest = Bukkit.getPlayer(targetStr);
        if (pTest == null) {
            // because getOfflinePlayer makes web call
            Bukkit.getScheduler().runTaskAsynchronously(PortChuu.getInstance(), () -> {
                //noinspection deprecation
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetStr);
                if (!target.hasPlayedBefore()) {
                    sender.sendMessage(TextTemplates.unknownPlayer());
                    return;
                }
                Bukkit.getScheduler().runTask(PortChuu.getInstance(), () -> setNick(sender, target, nick));
            });
        } else {
            setNick(sender, pTest, nick);
        }
    }

    private void setNick(CommandSender sender, OfflinePlayer target, String nick) {
        if (sender == target && !sender.hasPermission(SELF_PERM)) {
            sender.sendMessage(noPermission("You don't have permission to set own nickname!"));
            return;
        }
        if (sender != target && !sender.hasPermission(OTHER_PERM)) {
            sender.sendMessage(noPermission("You don't have permission to set others' nickname!"));
            return;
        }

        if (nick == null || nick.isEmpty()) {
            clearNick(sender, target);
            return;
        }

        String nickColored = ChatColor.translateAlternateColorCodes('&', nick);
        String newNick;

        if (nickColored.indexOf(ChatColor.COLOR_CHAR) != -1 && !sender.hasPermission(ALLSTYLE_PERM)) {
            boolean color = sender.hasPermission(COLOR_PERM),
                    multicolor = sender.hasPermission(MULTICOLOR_PERM),
                    bold = sender.hasPermission(BOLD_PERM),
                    italic = sender.hasPermission(ITALIC_PERM),
                    strikethrough = sender.hasPermission(STRIKETHROUGH_PERM),
                    underline = sender.hasPermission(UNDERLINE_PERM),
                    magic = sender.hasPermission(MAGIC_PERM);

            char mainColor;
            char[] charArray = nickColored.toCharArray();

            if (!multicolor && color && charArray[0] == ChatColor.COLOR_CHAR && "0123456789AaBbCcDdEeFf".indexOf(charArray[1]) != -1)
                mainColor = charArray[1];
            else mainColor = 'r';
            StringBuilder out = new StringBuilder();

            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (c == ChatColor.COLOR_CHAR) {
                    char c2 = charArray[++i];

                    if (c2 >= '0' && c2 <= '9' || c2 >= 'A' && c2 <= 'F' || c2 >= 'a' && c2 <= 'f' || c2 == 'r') {
                        out.append(c);
                        if (multicolor) out.append(c2);
                        else out.append(mainColor);
                    } else {
                        boolean check;
                        switch (c2) {
                            case 'k':
                                check = magic;
                                break;
                            case 'l':
                                check = bold;
                                break;
                            case 'm':
                                check = strikethrough;
                                break;
                            case 'n':
                                check = underline;
                                break;
                            case 'o':
                                check = italic;
                                break;
                            default:
                                check = false;
                        }
                        if (check) {
                            out.append(c);
                            out.append(c2);
                        }
                    }
                } else {
                    out.append(c);
                }
            }

            newNick = out.toString();
        } else {
            newNick = nickColored;
        }

        String oldNick = nicknameManager.setNick(target, newNick);
        BaseComponent[] prevNick = oldNick == null ? new BaseComponent[]{new TextComponent(target.getName())} : TextComponent.fromLegacyText(oldNick);

        sender.sendMessage(new ComponentBuilder(target.getName())
                .color(ChatColor.WHITE)
                .append("'s nickname set from '")
                .color(ChatColor.GRAY)
                .append(prevNick, ComponentBuilder.FormatRetention.NONE)
                .append("' to '", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.GRAY)
                .append(TextComponent.fromLegacyText(newNick, null), ComponentBuilder.FormatRetention.NONE)
                .append("'", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.GRAY)
                .create()
        );
    }

    private void clearNick(CommandSender sender, OfflinePlayer p) {
        String oldNick = nicknameManager.setNick(p, null);
        if (oldNick == null) {
            sender.sendMessage(new ComponentBuilder(p.getName())
                    .color(ChatColor.WHITE)
                    .append(" did not have any nickname.", ComponentBuilder.FormatRetention.NONE)
                    .color(ChatColor.GRAY)
                    .create()
            );
            return;
        }

        sender.sendMessage(new ComponentBuilder("Nickname cleared from '")
                .color(ChatColor.GRAY)
                .appendLegacy(oldNick)
                .append("' to '", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.GRAY)
                .append(p.getName(), ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.WHITE)
                .append("'", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.GRAY)
                .create()
        );
    }

    private BaseComponent noPermission(String msg) {
        TextComponent ret = new TextComponent(msg);
        ret.setColor(ChatColor.RED);
        return ret;
    }


    private BaseComponent[] usage(CommandSender sender) {
        boolean self = sender instanceof Player && sender.hasPermission(SELF_PERM),
                other = sender.hasPermission(OTHER_PERM),
                list = sender.hasPermission(LIST_PERM),
                color = sender.hasPermission(COLOR_PERM),
                multicolor = sender.hasPermission(MULTICOLOR_PERM),
                bold = sender.hasPermission(BOLD_PERM),
                italic = sender.hasPermission(ITALIC_PERM),
                strikethrough = sender.hasPermission(STRIKETHROUGH_PERM),
                underline = sender.hasPermission(UNDERLINE_PERM),
                magic = sender.hasPermission(MAGIC_PERM);
        StringBuilder us = new StringBuilder("Usage: /nickname <");
        if (self) us.append("set <nickname>|clear");
        if (self && other) us.append('|');
        if (other) us.append("setother <player> <nickname>|clearother <player>");
        if ((self || other) && list) us.append('|');
        if (list) us.append("list");
        us.append(">\n");

        StringBuilder as = new StringBuilder("Available styles: ");
        int len = as.length();

        if (multicolor) as.append("[Multcolor '&{0-9,a-f}'] ");
        else if (color) as.append("[Single Color '&{0-9,a-f}'] ");
        if (magic) as.append("[Magic '&k'] ");
        if (bold) as.append("[Bold '&l'] ");
        if (strikethrough) as.append("[Strikethrough '&m'] ");
        if (underline) as.append("[Underline '&n'] ");
        if (italic) as.append("[Italic '&o']");

        if (as.length() == len) as.append("(none)");

        TextComponent ret1 = new TextComponent(us.toString());
        ret1.setColor(ChatColor.RED);
        TextComponent ret2 = new TextComponent(as.toString());
        ret2.setColor(ChatColor.GRAY);
        return new BaseComponent[]{ret1, ret2};
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission(SELF_PERM)) {
                if (SET.startsWith(args[0].toLowerCase())) list.add(SET);
                if (CLEAR.startsWith(args[0].toLowerCase())) list.add(CLEAR);
            }
            if (sender.hasPermission(OTHER_PERM)) {
                if (SETOTHER.startsWith(args[0].toLowerCase())) list.add(SETOTHER);
                if (CLEAROTHER.startsWith(args[0].toLowerCase())) list.add(CLEAROTHER);
            }

/* TODO
            if (sender.hasPermission(LIST_PERM)) {
                if (LIST.startsWith(args[0].toLowerCase())) list.add(LIST);
            }
*/
            return list;
        }

        if (args.length == 2 && sender.hasPermission(OTHER_PERM)) {
            List<String> list = new ArrayList<>();
            if (args[0].equalsIgnoreCase(SETOTHER) || args[0].equalsIgnoreCase(CLEAROTHER)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    String name = p.getName();
                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) list.add(name);
                }
            }
            return list;
        }

        return ImmutableList.of();
    }
}
