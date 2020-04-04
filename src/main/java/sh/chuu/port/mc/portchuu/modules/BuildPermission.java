package sh.chuu.port.mc.portchuu.modules;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.material.Bed;
import org.bukkit.material.Openable;
import sh.chuu.port.mc.portchuu.PortChuu;


public class BuildPermission implements Listener {
    private static final String BUILD_PERMISSION = "portchuu.build";
    private static final PermissionsModule permModule = PortChuu.getInstance().getPermissionsModule();

    // Sends message through Action Bar (area above item bar for e.g. bed message or dismount message)
    private void noPerm(Cancellable e, Player p) {
        if (p.hasPermission(BUILD_PERMISSION))
            return;

        e.setCancelled(true);

        String msg;
        if (permModule.isGraylisted(p))
            msg = "Building is disabled in this world.";
        else
            msg = "You are &cnot&r graylisted! Run &a/apply&r to get started!";
        p.sendActionBar('&', msg);// wtf
    }

    // Player and Entity
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void armorStandManipulate(PlayerArmorStandManipulateEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bucket(PlayerBucketEmptyEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bucket(PlayerBucketFillEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void shearEntity(PlayerShearEntityEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void unleashEntity(PlayerUnleashEntityEvent e) {
        noPerm(e, e.getPlayer());
    }

    // Advanced Player and Entity
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void hangingBreakByEntity(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player)
            noPerm(e, (Player) e.getRemover());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || e.getDamager().hasPermission(BUILD_PERMISSION))
            return;

        Entity damaged = e.getEntity();

        // Allow killing monsters... if they're not owned by someone (e.g. not named)
        if ((damaged instanceof Monster || damaged instanceof Slime) && ((LivingEntity) damaged).getRemoveWhenFarAway())
            return;

        noPerm(e, (Player) e.getDamager());
    }

    // Block
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPlace(BlockPlaceEvent e) {
        noPerm(e, e.getPlayer());
    }

    // Advanced Block
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void interact(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.isCancelled() || p.hasPermission(BUILD_PERMISSION))
            return;

        Action a = e.getAction();
        if (a == Action.RIGHT_CLICK_AIR || a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK)
            return;

        Block b = e.getClickedBlock();
        BlockState bs = b.getState();

        // Container: inventories; Openable: doors/gates
        if (b.getType() == Material.CRAFTING_TABLE || bs instanceof Container || bs instanceof Openable || bs instanceof EnderChest || bs instanceof Bed)
            return;

        noPerm(e, e.getPlayer());

    }

    // Inventory
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player) || e.getWhoClicked().hasPermission(BUILD_PERMISSION))
            return;

        InventoryType it = e.getInventory().getType();

        // Allow crafting table, enchantment table, Ender Chest, and anything else that doesn't affect other players.
        switch (it) {
            case PLAYER:
            case CRAFTING:
            case WORKBENCH:
            case ENDER_CHEST:
            case ENCHANTING:
            case CREATIVE:
                return;
        }

        noPerm(e, (Player) e.getWhoClicked());
    }
}
