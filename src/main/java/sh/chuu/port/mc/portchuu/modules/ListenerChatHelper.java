package sh.chuu.port.mc.portchuu.modules;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import sh.chuu.port.mc.portchuu.PortChuu;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ListenerChatHelper implements Listener {
    private static final String URL_PERM = "portchuu.chat.url";

    private final PortChuu plugin;
    private final BaseComponent[] chatComponents;
    private final String chatFormat;
    private final ChatColor chatPreColor;
    private final ChatColor chatNameColor;
    private final ChatColor chatSufColor;
    private final int chatPrePos;
    private final int chatNamePos;
    private final int chatSufPos;
    private final TextComponent chatMsg;

    public ListenerChatHelper(PortChuu plugin, String chatFormat) {
        this.plugin = plugin;
        this.chatFormat = chatFormat;

        if (chatFormat != null && chatFormat.indexOf(ChatColor.COLOR_CHAR) != -1) {
            int pi = chatFormat.indexOf("%pre%");
            int ni = chatFormat.indexOf("%name%");
            int si = chatFormat.indexOf("%suf%");
            int mi = chatFormat.indexOf("%msg%");
            if (ni == -1 || mi == -1) {
                this.chatPreColor = null;
                this.chatNameColor = null;
                this.chatSufColor = null;
                this.chatNamePos = this.chatPrePos = this.chatSufPos = -1;
                this.chatMsg = null;
                this.chatComponents = null;
                return;
            }

            ArrayList<BaseComponent> template = new ArrayList<>();
            this.chatMsg = new TextComponent("m");
            int i = 0;
            if (ni < mi) {
                if (pi == -1) {
                    this.chatPreColor = null;
                    this.chatPrePos = -1;
                } else {
                    Collections.addAll(template, TextComponent.fromLegacyText(chatFormat.substring(i, pi)));
                    i = pi + 5;
                    this.chatPreColor = template.get(template.size() - 1).getColorRaw();
                    template.removeIf(tc -> ((TextComponent) tc).getText().length() == 0); // name pos depends on this
                    template.add(new TextComponent("p"));
                    this.chatPrePos = template.size() - 1;
                }

                Collections.addAll(template, TextComponent.fromLegacyText(chatFormat.substring(i, ni)));
                i = ni + 6;
                this.chatNameColor = template.get(template.size() - 1).getColorRaw();
                template.removeIf(tc -> ((TextComponent) tc).getText().length() == 0); // name pos depends on this

                template.add(new TextComponent("n"));
                this.chatNamePos = template.size() - 1;

                if (si == -1) {
                    this.chatSufColor = null;
                    this.chatSufPos = -1;
                } else {
                    Collections.addAll(template, TextComponent.fromLegacyText(chatFormat.substring(i, si)));
                    i = si + 5;
                    this.chatSufColor = template.get(template.size() - 1).getColorRaw();
                    template.removeIf(tc -> ((TextComponent) tc).getText().length() == 0); // name pos depends on this
                    template.add(new TextComponent("s"));
                    this.chatSufPos = template.size() - 1;
                }

                Collections.addAll(template, TextComponent.fromLegacyText(chatFormat.substring(i, mi)));
                i = mi + 5;
                this.chatMsg.setColor(template.get(template.size() - 1).getColorRaw());

                template.add(this.chatMsg);

                String remain = chatFormat.substring(i);
                if (!remain.isEmpty())
                    Collections.addAll(template, TextComponent.fromLegacyText(remain));
                template.removeIf(tc -> ((TextComponent) tc).getText().length() == 0);
            } else {
                Collections.addAll(template, TextComponent.fromLegacyText(chatFormat.substring(i, mi)));
                i = mi + 5;
                this.chatMsg.setColor(template.get(template.size() - 1).getColorRaw());

                template.add(this.chatMsg);

                if (pi == -1) {
                    this.chatPreColor = null;
                    this.chatPrePos = -1;
                } else {
                    Collections.addAll(template, TextComponent.fromLegacyText(chatFormat.substring(i, pi)));
                    i = pi + 5;
                    this.chatPreColor = template.get(template.size() - 1).getColorRaw();
                    template.removeIf(tc -> ((TextComponent) tc).getText().length() == 0); // name pos depends on this
                    template.add(new TextComponent("p"));
                    this.chatPrePos = template.size() - 1;
                }

                Collections.addAll(template, TextComponent.fromLegacyText(chatFormat.substring(i, ni)));
                i = ni + 6;
                this.chatNameColor = template.get(template.size() - 1).getColorRaw();
                template.removeIf(tc -> ((TextComponent) tc).getText().length() == 0); // name pos depends on this

                template.add(new TextComponent("n"));
                this.chatNamePos = template.size() - 1;

                if (si == -1) {
                    this.chatSufColor = null;
                    this.chatSufPos = -1;
                } else {
                    Collections.addAll(template, TextComponent.fromLegacyText(chatFormat.substring(i, si)));
                    i = si + 5;
                    this.chatSufColor = template.get(template.size() - 1).getColorRaw();
                    template.removeIf(tc -> ((TextComponent) tc).getText().length() == 0); // name pos depends on this
                    template.add(new TextComponent("s"));
                    this.chatSufPos = template.size() - 1;
                }

                String remain = chatFormat.substring(i);
                if (!remain.isEmpty())
                    Collections.addAll(template, TextComponent.fromLegacyText(remain));

                template.removeIf(tc -> ((TextComponent) tc).getText().length() == 0);
            }
            this.chatComponents = template.toArray(new BaseComponent[0]);
            this.chatMsg.setText("");
        } else {
            this.chatPreColor = null;
            this.chatNameColor = null;
            this.chatSufColor = null;
            this.chatNamePos = this.chatPrePos = this.chatSufPos = -1;
            this.chatMsg = null;
            this.chatComponents = null;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChatFormat(AsyncPlayerChatEvent ev) {
        if (chatFormat == null)
            return;

        Player p = ev.getPlayer();
        BaseComponent name = TextTemplates.createPlayerTooltipLegacy(p.getDisplayName(), p.getName(), p.getUniqueId().toString());
        if (chatComponents == null) {
            BaseComponent send = new TranslatableComponent(chatFormat, name, ev.getMessage());
            for (Player pl : ev.getRecipients()) {
                //noinspection deprecation
                pl.sendMessage(ChatMessageType.CHAT, send);
            }
            ev.getRecipients().clear();
            return;
        }

        String pre, suf;
        LuckPerms lp = plugin.getLpAPI();

        if (lp != null) {
            CachedMetaData md = lp.getUserManager().getUser(p.getUniqueId()).getCachedData().getMetaData(QueryOptions.defaultContextualOptions());
            pre = md.getPrefix();
            suf = md.getSuffix();
        } else {
            pre = null;
            suf = null;
        }

        getChatComponents(name, pre, suf, ev.getMessage());

        for (Player pl : ev.getRecipients()) {
            //noinspection deprecation
            pl.sendMessage(ChatMessageType.CHAT, chatComponents);
        }
        ev.setFormat(TextComponent.toLegacyText(chatComponents));
        ev.getRecipients().clear();
    }

    public BaseComponent[] getChatComponents(BaseComponent name, String pre, String suf, String msg) {
        chatComponents[chatNamePos] = name;
        if (chatComponents[chatNamePos].getColorRaw() == null)
            chatComponents[chatNamePos].setColor(chatNameColor);

        if (chatPrePos != -1 && pre != null) {
            chatComponents[chatPrePos] = TextTemplates.createComponent(ChatColor.translateAlternateColorCodes('&', pre));
            if (chatComponents[chatPrePos].getColorRaw() == null)
                chatComponents[chatPrePos].setColor(chatPreColor);
        }
        else {
            chatComponents[chatPrePos] = new TextComponent();
        }
        if (chatSufPos != -1 && suf != null) {
            chatComponents[chatSufPos] = TextTemplates.createComponent(ChatColor.translateAlternateColorCodes('&', suf));
            if (chatComponents[chatSufPos].getColorRaw() == null)
                chatComponents[chatSufPos].setColor(chatSufColor);
        }
        else {
            chatComponents[chatSufPos] = new TextComponent();
        }

        chatMsg.setExtra(Arrays.asList(TextTemplates.url(msg, ChatColor.AQUA)));

        return chatComponents;
    }
}
