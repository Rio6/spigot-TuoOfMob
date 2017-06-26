package tw.mayortw;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public class TuoOfMobPlugin extends JavaPlugin implements Listener {

    private int selectedIndex = -1;
    private List<MobRoot> rootMobs = new ArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().runTaskTimer(this, () -> {
            for(MobRoot root : rootMobs) {
                root.updatePosition();
            }
        }, 30, 1);

        getLogger().info("TuoOfMob Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("TuoOfMob Disabled");
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent eve) {

        Player player = eve.getPlayer();
        Entity entity = eve.getRightClicked();

        if(player.getInventory().getItemInMainHand().getType() == Material.BONE) {

            select(entity);
            eve.setCancelled(true);

        } else if(player.getInventory().getItemInMainHand().getType() == Material.FEATHER) {

            if(selectedIndex >= 0 && selectedIndex < rootMobs.size()) {
                rootMobs.get(selectedIndex).addEntity(entity);
            }
            eve.setCancelled(true);

        } else if(player.getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {

            rootMobs.get(selectedIndex).removeEntity(entity);
            eve.setCancelled(true);

        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent eve) {

        if(eve.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Material material = eve.getMaterial();
        if(material == Material.BONE) {
            deselect();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean rst = false;

        if(cmd.getName().equalsIgnoreCase("tuo")) {
            if(args.length > 0) {
                switch(args[0].toLowerCase()) {
                    case "sel":
                        if(args.length > 1) {

                            int range;
                            try {
                                range = Integer.parseInt(args[1]);
                            } catch(NumberFormatException e) {
                                rst = false;
                                break;
                            }

                            if(selectedIndex < 0 || selectedIndex >= rootMobs.size()) {
                                sender.sendMessage("No selected root");
                                rst = true;
                            } else if(!(sender instanceof Entity)) {
                                sender.sendMessage("You are not entity");
                                rst = true;
                            } else {
                                Entity senderEnt = (Entity) sender;
                                MobRoot root = rootMobs.get(selectedIndex);
                                for(Entity entity : senderEnt.getNearbyEntities(range, range, range)) {
                                    if(!root.getRootEntity().equals(entity)) {
                                        root.addEntity(entity);
                                    }
                                }
                                rst = true;
                            }
                        } else {
                            rst = false;
                        }
                        break;
                    case "clear":
                        if(selectedIndex < 0 || selectedIndex >= rootMobs.size()) {
                            sender.sendMessage("No selected root");
                        } else {
                            rootMobs.get(selectedIndex).removeAllEntity();
                        }
                        rst = true;
                        break;
                    default:
                        rst = false;
                }
            } else {
                rst = false;
            }
        }

        return rst;
    }

    private MobRoot findRoot(Entity entity) {
        for(MobRoot root : rootMobs) {
            if(root.getRootEntity().equals(entity))
                return root;
        }
        return null;
    }

    private void select(Entity entity) {
        if(selectedIndex >= 0 && selectedIndex < rootMobs.size() &&
                rootMobs.get(selectedIndex).getRootEntity().equals(entity))
            return;
        deselect();

        MobRoot root = findRoot(entity);
        if(root == null) {
            root = new MobRoot(entity);
            rootMobs.add(root);
            getLogger().info("Added " + root.toString() + " from list");
        }
        selectedIndex = rootMobs.indexOf(root);

        root.setMark(true);

        getLogger().info("Selected " + root.toString());
    }

    private void deselect() {
        if(selectedIndex >= 0 && selectedIndex < rootMobs.size()) {

            MobRoot root = rootMobs.get(selectedIndex);

            root.setMark(false);

            getLogger().info("Deselected " + root.toString());

            if(root.entityCount() <= 0) {
                MobRoot removed = rootMobs.remove(selectedIndex);
                getLogger().info("Removed " + removed.toString() + " from list");
            }

            selectedIndex = -1;
        }
    }
}