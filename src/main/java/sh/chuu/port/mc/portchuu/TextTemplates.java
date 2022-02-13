package sh.chuu.port.mc.portchuu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.TimeZone;

public interface TextTemplates {
    static BaseComponent injectPlayerTooltip(BaseComponent comp, String name, String type, String uuid, String suggestion) {
        ComponentBuilder hover = new ComponentBuilder(name + "\nType: " + type + "\n" + uuid);
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion));
        return comp;
    }

    static BaseComponent createPlayerTooltipLegacy(String display, String name, String uuid) {
        BaseComponent ret = createComponent(display);
        injectPlayerTooltip(ret, name, "Player", uuid, "/tell " + name + " ");
        return ret;
    }

    static BaseComponent createDiscordTooltip(String display, String name, String id, boolean isBot) {
        BaseComponent ret = createComponent(display);
        injectPlayerTooltip(ret, name, isBot ? "discord:bot" : "discord:user", id, id);
        return ret;
    }

    static TextComponent createComponent(String legacy) {
        BaseComponent[] nameComp = TextComponent.fromLegacyText(legacy);
        TextComponent ret;
        if (nameComp.length == 1)
            ret = (TextComponent) nameComp[0];
        else
            ret = new TextComponent(nameComp);
        return ret;
    }

    static boolean hasURL(String text) {
        return text.matches("[^/\\s]+\\.[^/\\d\\s.]{2,}[^/]?\\S*|https?://\\S+");
    }

    static BaseComponent[] url(String spacedString, ChatColor linkColor) {
        ComponentBuilder cb = new ComponentBuilder();
        injectURL(cb, spacedString.split(" "), 0, linkColor);
        return cb.create();
    }

    static void injectURL(ComponentBuilder cb, String[] arrayString, int fromIndex, ChatColor linkColor) {
        StringBuilder sb = new StringBuilder();
        for (int i = fromIndex; i < arrayString.length; i++) {
            if (i != fromIndex) sb.append(' ');

            if (hasURL(arrayString[i])) {
                cb.append(sb.toString(), ComponentBuilder.FormatRetention.NONE);
                injectURLTruncatedSingle(cb, arrayString[i], linkColor);
                sb = new StringBuilder();
            } else if (arrayString[i].matches("/?r/\\S+")) {
                // Reddit
                cb.append(sb.toString(), ComponentBuilder.FormatRetention.NONE);

                String sr = ChatColor.stripColor(arrayString[i]);
                String url = "https://reddit.com" + (sr.startsWith("/") ? "" : "/") + sr;

                injectURLSingle(cb, arrayString[i], url, linkColor);
                sb = new StringBuilder();
            } else {
                sb.append(arrayString[i]);
            }
        }
        if (sb.length() != 0)
            cb.append(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(sb.toString()), ComponentBuilder.FormatRetention.NONE);
    }

    static void injectURLTruncatedSingle(ComponentBuilder cb, String url, ChatColor color) {
        String u = ChatColor.stripColor(url);
        String d;
        int dslash = u.indexOf("//") + 2;
        int firstSlashI = u.indexOf("/", dslash);

        // Shorten URL text if too long
        if (u.length() > 35 && firstSlashI != -1) {
            int first = firstSlashI + (6 + url.length() - u.length());
            if (url.charAt(first - 1) == '\u00A7') first++;
            int last = url.length() - 10;
            if (url.charAt(last - 1) == '\u00A7') last--;
            if (first <= last) d = url.substring(0, first) + "\u2026" + url.substring(last);
            else d = url;
        } else {
            d = url;
        }

        injectURLSingle(cb, d, u, color);
    }

    static void injectURLSingle(ComponentBuilder cb, String text, String href, ChatColor color) {
        cb
                .append(text)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        href.startsWith("http://") || href.startsWith("https://")
                                ? href
                                : "http://" + href
                ));
        if (cb.getCurrentComponent().getColorRaw() == null)
            cb.color(color);

    }

    static Component unknownPlayer() {
        return Component.translatable("argument.player.unknown", NamedTextColor.RED);
    }

    static void adminBroadcast(Component msg, CommandSender sender) {
        sender.sendMessage(msg);
        Component send = Component.translatable("chat.type.admin", NamedTextColor.GRAY, TextDecoration.ITALIC);
        if (Bukkit.getConsoleSender() != sender) Bukkit.getConsoleSender().sendMessage(send);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("minecraft.admin.command_feedback") && p != sender)
                p.sendMessage(send);
        }
    }

    static void broadcastExcept(BaseComponent msg, CommandSender sender) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p != sender)
                p.sendMessage(msg);
        }
    }

    static Locale locale(String loc) {
        String[] p = loc.split("_");
        if (p.length == 1)
            return new Locale(p[0]);
        if (p.length == 2)
            return new Locale(p[0], p[1]);
        return new Locale(p[0], p[1], p[2]);
    }

    static Component timeText(long time, int diff, boolean past, Locale locale, TimeZone timeZone, TextColor focusColor) {
        TimeZone zone = timeZone == null ? TimeZone.getDefault() : timeZone;
        Locale loc = locale == null ? Locale.getDefault() : locale;
        Builder ret = Component.empty().toBuilder();
        if (diff < 60) {
            if (!past) {
                ret.append(Component.text("in "));
            }

            ret.append(Component.text(diff + " second" + (diff == 1 ? "" : "s"), focusColor));

            if (past) {
                ret.append(Component.text(" ago"));
            }
        } else {
            int di;
            if (diff < 3600) {
                if (!past) ret.append(Component.text("in "));

                di = diff / 60;
                ret.append(Component.text(di + " minute" + (di == 1 ? "" : "s"), focusColor));

                if (past) ret.append(Component.text(" ago"));
            } else if (diff < 86400) {
                if (!past) ret.append(Component.text("in "));

                di = diff / 3600;
                ret.append(Component.text(di + " hour" + (di == 1 ? "" : "s"), focusColor));

                if (past) ret.append(Component.text(" ago"));
            } else if (diff < 604800) {
                if (!past) ret.append(Component.text("in "));

                di = diff / 86400;
                int h = diff / 3600 % 24;

                ret.append(Component.text(di + " day" + (di == 1 ? "" : "s"), focusColor));
                ret.append(Component.text(" and " + h + " hour" + (h == 1 ? "" : "s")));

                if (past) ret.append(Component.text(" ago"));
            } else {
                ret.append(Component.text("in "));
                DateFormat d = DateFormat.getDateInstance(1, loc);
                d.setTimeZone(zone);

                ret.append(Component.text(d.format(time), focusColor));

                DateFormat t = DateFormat.getTimeInstance(0, loc);
                t.setTimeZone(zone);
                ret.append(Component.text(" at " + t.format(time)));
            }
        }

        DateFormat d = DateFormat.getDateTimeInstance(0, 0, loc);
        d.setTimeZone(zone);

        ret.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text(d.format(time))));
        return ret.build();
    }

    static ChatColor colorTPS(double tps) {
        return tps >= 20.1 ? ChatColor.AQUA : tps >= 18.0 ? ChatColor.GREEN : tps >= 15.0 ? ChatColor.YELLOW : ChatColor.RED;
    }

    static ChatColor colorPing(int ping) {
        return ping <= 60 ? ChatColor.AQUA : ping <= 150 ? ChatColor.GREEN : ping <= 400 ? ChatColor.YELLOW : ChatColor.RED;
    }

    static String humanReadableBytes(long bytes) {
        DecimalFormat f = new DecimalFormat("#.##");
        if (bytes < 0x400L)
            return bytes + "";
        if (bytes < 0x100000L)
            return f.format(((double)bytes)/0x400L) + "k";
        if (bytes < 0x40000000L)
            return f.format(((double)bytes)/0x100000L) + "M";
        if (bytes < 0x10000000000L)
            return f.format(((double)bytes)/0x40000000L) + "G";
        return f.format(((double)bytes)/0x10000000000L) + "T";
    }
}
