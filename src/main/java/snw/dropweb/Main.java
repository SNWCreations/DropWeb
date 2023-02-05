package snw.dropweb;

import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public final class Main extends JavaPlugin implements Listener {
    private static final int MOVE_OFFSET = 3;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getScheduler().cancelTasks(this);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Item item = e.getItemDrop();
        if (item.getItemStack().getType() == Material.COBWEB) {
            // remove item after 1 tick
            new BukkitRunnable() {
                @Override
                public void run() {
                    item.remove(); // goodbye~ lol
                }
            }.runTaskLater(this, 1L);

            Player player = e.getPlayer();
            Location location = player.getLocation();
            Location basePoint = location.clone();
            Location lefttop = null; // expected NotNull in the end
            Location rightdown = null; // expected NotNull in the end
            float base = location.getYaw();

            if (inRange(base, -225, -135) || inRange(base, 135, 225)) {
                basePoint.add(0, 0, MOVE_OFFSET);
                lefttop = basePoint.clone().add(-1, 0, 0);
                rightdown = basePoint.clone().add(1, 0, 2);
            }
            if (inRange(base, -135, -45) || inRange(base, 225, 315)) {
                basePoint.add(-MOVE_OFFSET, 0, 0);
                lefttop = basePoint.clone().add(0, 0, -1);
                rightdown = basePoint.clone().add(-2, 0, 1);
            }
            if (
                    inRange(base, -45, 0) /* Do not think it is strange lol */
                    || inRange(base, -360, -315)
                    || inRange(base, 315, 361)
                    || inRange(base, 0, 45)
            ) {
                basePoint.add(0, 0, -MOVE_OFFSET);
                lefttop = basePoint.clone().add(1, 0, 0);
                rightdown = basePoint.clone().add(-1, 0, -2);
            }
            if (inRange(base, -315, -225) || inRange(base, 45, 135)) {
                basePoint.add(MOVE_OFFSET, 0, 0);
                lefttop = basePoint.clone().add(0, 0, 1);
                rightdown = basePoint.clone().add(2, 0, -1);
            }

            if (lefttop == null || rightdown == null) {
                getLogger().log(Level.SEVERE, "Unable to place block, player yaw: " + base);
                return;
            }

            // location.getWorld().setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);

            // fill
            int fill = fill(lefttop, rightdown, Material.AIR, Material.COBWEB);
            if (fill == 0) {
                return; // do nothing lol
            }

            // fill back
            Location finalRightdown = rightdown;
            Location finalLefttop = lefttop;
            new BukkitRunnable() {
                @Override
                public void run() {
                    fill(finalLefttop, finalRightdown, Material.COBWEB, Material.AIR);
                }
            }.runTaskLater(this, 20L * 5);
        }
    }

    private static boolean inRange(float source, float a, float b) {
        return source >= a && source < b;
    }

    private static int fill(Location lefttop, Location rightdown, Material from, Material to) {
        Validate.isTrue(lefttop.getWorld() != null, "world cannot be null");
        Validate.isTrue(rightdown.getWorld() != null, "world cannot be null");
        Validate.isTrue(lefttop.getWorld() == rightdown.getWorld(), "Cannot fill in different world");

        int successCount = 0;
        World world = lefttop.getWorld();
        int x1 = Math.min(lefttop.getBlockX(), rightdown.getBlockX());
        int x2 = Math.max(lefttop.getBlockX(), rightdown.getBlockX());
        int y1 = Math.min(lefttop.getBlockY(), rightdown.getBlockY());
        int y2 = Math.max(lefttop.getBlockY(), rightdown.getBlockY());
        int z1 = Math.min(lefttop.getBlockZ(), rightdown.getBlockZ());
        int z2 = Math.max(lefttop.getBlockZ(), rightdown.getBlockZ());
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == from) {
                        block.setType(to);
                        successCount++;
                    }
                }
            }
        }
        return successCount;
    }
}
