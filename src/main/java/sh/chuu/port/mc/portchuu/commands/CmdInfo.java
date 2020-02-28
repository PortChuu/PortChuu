package sh.chuu.port.mc.portchuu.commands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sh.chuu.port.mc.portchuu.BookLibrary;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class CmdInfo implements TabExecutor {
    private final ItemStack book = BookLibrary.rules();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Figure out how to make books work
        if (sender instanceof Player) {
            Player p = (Player) sender;
            ItemStack temp = p.getInventory().getItemInOffHand();
            p.getInventory().setItemInOffHand(this.book);
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.OPEN_BOOK);
            packet.getEnumModifier(EnumWrappers.Hand.class, 0)
                    .write(0, EnumWrappers.Hand.OFF_HAND);
            try {
                pm.sendServerPacket(p, packet);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(
                        "Cannot send packet " + packet, e);
            }
            p.getInventory().setItemInOffHand(temp);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return ImmutableList.of();
    }
}
