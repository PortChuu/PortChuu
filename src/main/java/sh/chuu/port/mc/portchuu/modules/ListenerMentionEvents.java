package sh.chuu.port.mc.portchuu.modules;

import net.ess3.api.events.PrivateMessagePreSendEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ListenerMentionEvents implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void privateMessageMention(PrivateMessagePreSendEvent ev) {
        if (ev.isCancelled())
            return;
        Player p = Bukkit.getPlayerExact(ev.getRecipient().getName());
        //noinspection ConstantConditions Player will always be != null
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1, 0.6f);
    }
}
