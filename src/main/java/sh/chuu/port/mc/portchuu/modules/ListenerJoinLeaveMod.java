package sh.chuu.port.mc.portchuu.modules;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sh.chuu.port.mc.portchuu.PortChuu;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.UUID;

public class ListenerJoinLeaveMod implements Listener {
    private static final String GRAYLIST_URL = "https://port.chuu.sh/graylist";
    private static final String WEBSITE_URL = "https://port.chuu.sh/";
    private final PortChuu plugin = PortChuu.getInstance();
    private final NicknameModule nicknameModule = plugin.getNicknameModule();
    private final PermissionsModule permModule = plugin.getPermissionsModule();

    private final HashMap<UUID, Long> lastseen = new LinkedHashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void joinTimestamp(AsyncPlayerPreLoginEvent ev) {
        lastseen.put(ev.getUniqueId(), Bukkit.getOfflinePlayer(ev.getUniqueId()).getLastSeen());
        // Remove in case the player is banned out of the server
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> lastseen.remove(ev.getUniqueId()), 100L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent ev) {
        Player p = ev.getPlayer();
        nicknameModule.initPlayerNick(p);
        BaseComponent name = TextTemplates.createPlayerTooltipLegacy(p.getDisplayName(), p.getName(), p.getUniqueId().toString());

        if (p.hasPlayedBefore()) {
            int diff = (int) (System.currentTimeMillis() - lastseen.remove(p.getUniqueId())) / 1000;
            if (diff > 10)
                p.sendMessage(welcomeBack(p.getLastLogin(), diff, TextTemplates.locale((p.getLocale()))));
        } else {
            p.sendMessage(newPlayer(name));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline())
                    p.performCommand("portchuu:info");
            }, 20L);
        }

        if (!permModule.isGraylisted(p))
            p.sendMessage(needGraylist());

        String joinMsg = ev.getJoinMessage();
        if (joinMsg != null && !joinMsg.isEmpty()) {
            ev.setJoinMessage(null);
            BaseComponent bc = new TranslatableComponent("multiplayer.player.joined", name);
            bc.setColor(ChatColor.YELLOW);
            TextTemplates.broadcastExcept(bc, ev.getPlayer());
        }

        notifyDisabledChat(p);
    }

    private BaseComponent[] newPlayer(BaseComponent name) {
        BaseComponent[] send = new ComponentBuilder(name)
                .color(ChatColor.YELLOW)
                .append(" joined the Port Chuu for the first time!")
                .create();
        Bukkit.broadcast(send);

        return new ComponentBuilder(" Welcome to the ")
                .color(ChatColor.WHITE)
                .append("Port Chuu")
                .color(ChatColor.AQUA)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, WEBSITE_URL))
                .append("!", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.WHITE)
                .append(" Familiarize yourself with the rules with the ")
                .color(ChatColor.GRAY)
                .append("information booklet (/info)")
                .color(ChatColor.AQUA)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/portchuu:info"))
                .append(". Enjoy your stay here~!", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.GRAY)
                .create();
    }

    private BaseComponent[] needGraylist() {
        return new ComponentBuilder(" You are not graylisted. ")
                .color(ChatColor.WHITE)
                .append("Apply for the graylist here!")
                .color(ChatColor.AQUA)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, GRAYLIST_URL))
                .create();
    }

    private BaseComponent[] welcomeBack(long lastLoginTime, long diff, Locale locale) {
        return new ComponentBuilder("=====")
                .color(ChatColor.DARK_GRAY)
                .bold(true)
                .append(" Welcome back to the ")
                .color(ChatColor.WHITE)
                .append("Port Chuu")
                .color(ChatColor.AQUA)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, WEBSITE_URL))
                .append("! ", ComponentBuilder.FormatRetention.FORMATTING)
                .color(ChatColor.WHITE)
                .append("=====\n")
                .color(ChatColor.DARK_GRAY)
                .append(" Your last login was ", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.GRAY)
                .append(TextTemplates.timeText(lastLoginTime, (int) diff, true, locale, null, ChatColor.WHITE))
                .append(".")
                .color(ChatColor.GRAY)
                .create();
    }

    private void notifyDisabledChat(Player p) {
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        pm.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.SETTINGS) {
            @Override
            public void onPacketReceiving(PacketEvent ev) {
                if (p != ev.getPlayer())
                    return;

                if (ev.getPacket().getEnumModifier(EnumWrappers.ChatVisibility.class, 2).read(0) != EnumWrappers.ChatVisibility.FULL) {
                    // If player's chat settings is disabled, notify them.
                    ev.getPlayer().sendMessage(new ComponentBuilder(new TranslatableComponent("chat.cannotSend"))
                            .color(ChatColor.RED)
                            .append(" (")
                            .color(ChatColor.GRAY)
                            .append(new TranslatableComponent("menu.options"))
                            .color(ChatColor.WHITE)
                            .append(" > ")
                            .color(ChatColor.GRAY)
                            .append(new TranslatableComponent("options.chat.title"))
                            .color(ChatColor.WHITE)
                            .append(" > ")
                            .color(ChatColor.GRAY)
                            .append(new TranslatableComponent("options.chat.visibility"))
                            .color(ChatColor.WHITE)
                            .append(": ")
                            .append(new TranslatableComponent("options.chat.visibility.system"))
                            .append(" => ")
                            .color(ChatColor.YELLOW)
                            .append(new TranslatableComponent("options.chat.visibility"))
                            .color(ChatColor.GREEN)
                            .append(": ")
                            .append(new TranslatableComponent("options.chat.visibility.full"))
                            .append(")")
                            .color(ChatColor.GRAY)
                            .create()
                    );
                }

                pm.removePacketListener(this);
            }
        });    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent ev) {
        Player p = ev.getPlayer();
        nicknameModule.unloadPlayerNick(p);
        String testMsg = ev.getQuitMessage();
        if (testMsg != null && !testMsg.isEmpty()) {
            ev.setQuitMessage(null);
            BaseComponent bc = new TranslatableComponent("multiplayer.player.left",
                    TextTemplates.createPlayerTooltipLegacy(p.getDisplayName(), p.getName(), p.getUniqueId().toString())
            );
            bc.setColor(ChatColor.YELLOW);
            TextTemplates.broadcastExcept(bc, ev.getPlayer());
        }
    }}
