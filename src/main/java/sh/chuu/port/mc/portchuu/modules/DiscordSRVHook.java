package sh.chuu.port.mc.portchuu.modules;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.TextTemplates;

public class DiscordSRVHook {
    private final ChatHelper pe;

    public DiscordSRVHook(ChatHelper pe) {
        this.pe = pe;
    }

    @Subscribe
    public void chatEvent(DiscordGuildMessagePostProcessEvent ev) {
        Member u = ev.getMember();
        BaseComponent[] msg = pe.getChatComponents(TextTemplates.createDiscordTooltip(
                u.getEffectiveName(),
                u.getUser().getName() + "#" + u.getUser().getDiscriminator(),
                u.getId(),
                u.getUser().isBot()
        ), "&7[&3Discord&7] ", null, ev.getProcessedMessage());

        Bukkit.getConsoleSender().sendMessage(msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            //noinspection deprecation
            p.sendMessage(ChatMessageType.CHAT, msg);
        }
        ev.setCancelled(true);
    }
}
