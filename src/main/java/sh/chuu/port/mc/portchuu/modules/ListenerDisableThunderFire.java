package sh.chuu.port.mc.portchuu.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class ListenerDisableThunderFire implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void noThunderFire(BlockIgniteEvent ev) {
        if (ev.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING)
            ev.setCancelled(true);
    }
}
