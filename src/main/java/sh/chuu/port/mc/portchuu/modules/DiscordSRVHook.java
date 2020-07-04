package sh.chuu.port.mc.portchuu.modules;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.awt.*;

public class DiscordSRVHook {
    private final ListenerChatHelper pe;

    public DiscordSRVHook(ListenerChatHelper pe) {
        this.pe = pe;
    }

    public void sendChat(String msg) {
        TextChannel chan = DiscordSRV.getPlugin().getMainTextChannel();
        if (chan != null)
            chan.sendMessage(msg).complete();
    }

    @Subscribe
    public void chatEvent(DiscordGuildMessagePostProcessEvent ev) {
        Member u = ev.getMember();
        Color c = u.getColor();
        String color = c == null ? "" : ChatColor.of(c).toString();
        BaseComponent[] msg = pe.getChatComponents(TextTemplates.createDiscordTooltip(
                color + u.getEffectiveName(),
                u.getUser().getName() + "#" + u.getUser().getDiscriminator(),
                u.getId(),
                u.getUser().isBot()
        ), "&7[&x&7&2&8&9&D&ADiscord&7] ", null, ev.getProcessedMessage());

        Bukkit.getConsoleSender().sendMessage(msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            //noinspection deprecation
            p.sendMessage(ChatMessageType.CHAT, msg);
        }
        ev.setCancelled(true);
    }
}
