package sh.chuu.port.mc.portchuu;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface TextTemplates {
    static BaseComponent injectPlayerTooltip(BaseComponent comp, String name, String type, String uuid, String suggestion) {
        ComponentBuilder hover = new ComponentBuilder(name + "\nType: " + type + "\n" + uuid);
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion));
        return comp;
    }

    static BaseComponent createPlayerTooltipLegacy(String display, String name, String uuid) {
        BaseComponent ret = createComponent(display);
        injectPlayerTooltip(ret, name, "minecraft:player", uuid, "/tell " + name + " ");
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
        return text.matches("[^\\s.]+(\\.[^\\s.]+)+|https?://\\S+");
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
            cb.append(TextComponent.fromLegacyText(sb.toString()), ComponentBuilder.FormatRetention.NONE);
    }

    static void injectURLTruncatedSingle(ComponentBuilder cb, String url, ChatColor color) {
        String u = ChatColor.stripColor(url);
        String d;
        int dslash = u.indexOf("//") + 1;

        // Shorten URL text if too long
        if (u.length() > 35) {
            int first = u.indexOf("/", dslash) + (6 + url.length() - u.length());
            if (url.charAt(first - 1) == '\u00A7') first++;
            int last = url.length() - 6;
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

    static BaseComponent unknownPlayer() {
        BaseComponent ret = new TranslatableComponent("argument.player.unknown");
        ret.setColor(ChatColor.RED);
        return ret;
    }

    static void adminBroadcast(BaseComponent msg, CommandSender sender) {
        sender.sendMessage(msg);
        BaseComponent send = new TranslatableComponent("chat.type.admin", sender.getName(), msg);
        send.setColor(ChatColor.GRAY);
        if (Bukkit.getConsoleSender() != sender) Bukkit.getConsoleSender().sendMessage(send);

        send.setItalic(true);
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
}
