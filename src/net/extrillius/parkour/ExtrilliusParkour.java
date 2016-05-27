package net.extrillius.parkour;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Created by TechBug2012 on 5/27/16.
 */
public class ExtrilliusParkour extends JavaPlugin implements Listener {
    boolean build = false;
    boolean joined = false;

    public void onEnable() {

        getServer().getPluginManager().registerEvents(this, this);
    }
    //Building Permissions
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (!(p.hasPermission("parkour.admin"))) {
            p.sendMessage(ChatColor.RED + "Only admins can freely build!");
            p.sendMessage(ChatColor.RED + "Try typing " + ChatColor.AQUA + "/build");
        }
        if (!(p.hasPermission("parkour.build"))) {
            p.sendMessage(ChatColor.AQUA + "You don't have a build license!"); // add clickable links in the future
            p.sendMessage(ChatColor.AQUA + "You can purchase one from the shop in the hub");
            p.sendMessage(ChatColor.AQUA + "or you can purchase the $50 rank from our Buycraft page!");
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (!(p.hasPermission("parkour.admin"))) {
            p.sendMessage(ChatColor.RED + "Only admins can freely build!");
            p.sendMessage(ChatColor.RED + "Try typing " + ChatColor.AQUA + "/build");
        }
        if (!(p.hasPermission("parkour.build"))) {
            p.sendMessage(ChatColor.AQUA + "You don't have a build license!"); // add clickable links in the future
            p.sendMessage(ChatColor.AQUA + "You can purchase one from the shop in the hub");
            p.sendMessage(ChatColor.AQUA + "or you can purchase the $50 rank from our Buycraft page!");
        }
        if (p.getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(true);
            p.setGameMode(GameMode.SURVIVAL);
            p.sendMessage(ChatColor.AQUA + "Try again in survival mode.");
        }
    }
    //umm...Join Signs (I think?)
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase("[Join]") && getConfig().contains("maps." + event.getLine(1))) {
            event.setLine(0, "§7[§bJoin§7]");
            event.setLine(1, "§b" + event.getLine(1));
        }
        if (event.getLine(0).equalsIgnoreCase("[Stats]") && getConfig().contains("maps." + event.getLine(1))) {
            event.setLine(0, "§7[§bStats§7]");
            event.setLine(1, "§b" + event.getLine(1));
        }
    }
    //Menu Items Within Maps
    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        double x = getConfig().getDouble("startingpoint.X");
        double y = getConfig().getDouble("startingpoint.Y");
        double z = getConfig().getDouble("startingpoint.Z");
        ItemStack leaveStick = new ItemStack(Material.STICK, 1);
        ItemMeta leaveMeta = leaveStick.getItemMeta();
        leaveMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Leave the map");
        leaveStick.setItemMeta(leaveMeta);

        ItemStack killArrow = new ItemStack(Material.ARROW, 1);
        ItemMeta killMeta = killArrow.getItemMeta();
        killMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Suicide");
        killArrow.setItemMeta(killMeta);

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName(ChatColor.GOLD + "Server Selector " + ChatColor.GRAY + "(Right click)");
        compass.setItemMeta(compassMeta);

        Player p = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType() == Material.WALL_SIGN) {
                Sign clickedSign = (Sign) event.getClickedBlock().getState();

                if (clickedSign.getLine(0).equals("§7[§bJoin§7]")) {
                    p.teleport(new Location(p.getWorld(), x, y, z));
                    p.sendMessage(ChatColor.AQUA + "Joined " + ChatColor.GREEN + clickedSign.getLine(1));
                    joined = true;
                }
                if (clickedSign.getLine(0).equals("§7[§bStats§7]")) {
                    // get the top 3 scores from the config
                }
            }
        }
        if (joined) {
            p.getInventory().clear();
            p.getInventory().setItem(9, leaveStick);
            p.getInventory().setItem(5, killArrow);
        }
        else {
            p.getInventory().clear();
            p.getInventory().setItem(5, compass);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        Location location = p.getLocation();
        //set parkour starting point
        if (cmd.getName().equalsIgnoreCase("startingpoint")) {
            if (!(sender.hasPermission("parkour.build"))) {
                sender.sendMessage(ChatColor.AQUA + "You don't have a build license!"); // add clickable links in the future
                sender.sendMessage(ChatColor.AQUA + "You can purchase one from the shop in the hub");
                sender.sendMessage(ChatColor.AQUA + "or you can purchase the $50 rank from our Buycraft page!");
                return false;
            }
            if (args.length != 1) {
                p.sendMessage(ChatColor.RED + "Usage:");
                p.sendMessage(ChatColor.AQUA + "/startingpoint <mapname>");
                return false;
            }
            if (location == null) {
                p.sendMessage("You must be a player to use this command!");
                return false;
            }
            if (getConfig().contains("maps." + args[0])) {
                p.sendMessage(ChatColor.AQUA + "You've already defined a starting point for this map!");
                return false;
            }
            getConfig().set("maps." + args[0] + ".startingpoint.X", location.getX());
            getConfig().set("maps." + args[0] + ".startingpoint.Y", location.getY());
            getConfig().set("maps." + args[0] + ".startingpoint.Z", location.getZ());
            p.sendMessage(ChatColor.AQUA + "Starting point set.");
        }
        //set parkour checkpoint
        if (cmd.getName().equalsIgnoreCase("checkpoint") || cmd.getName().equalsIgnoreCase("cp")) {
            if (!(sender.hasPermission("parkour.build"))) {
                sender.sendMessage(ChatColor.AQUA + "You don't have a build license!"); // add clickable links in the future
                sender.sendMessage(ChatColor.AQUA + "You can purchase one from the shop in the hub");
                sender.sendMessage(ChatColor.AQUA + "or you can purchase the $50 rank from our Buycraft page!");
                return false;
            }
            if (args.length != 2) {
                p.sendMessage(ChatColor.RED + "Usage:");
                p.sendMessage(ChatColor.AQUA + "/checkpoint <value> <mapname>");
                return false;
            }
            if (location == null) {
                p.sendMessage("You must be a player to use this command!");
                return false;
            }
            if (getConfig().contains("maps." + args[0] + ".checkpoints." + args[1])) {
                p.sendMessage(ChatColor.AQUA + "You have already defined this checkpoint!");
                return false;
            }
            getConfig().set("maps." + args[0] + ".checkpoints." + args[1] + ".X", location.getX());
            getConfig().set("maps." + args[0] + ".checkpoints." + args[1] + ".Y", location.getY());
            getConfig().set("maps." + args[0] + ".checkpoints." + args[1] + ".Z", location.getZ());
            p.sendMessage(ChatColor.AQUA + "Checkpoint " + ChatColor.GREEN + args[1] + ChatColor.AQUA + " set.");
        }
        //set parkour finish point
        if (cmd.getName().equalsIgnoreCase("finishpoint")) {
            if (!(sender.hasPermission("parkour.build"))) {
                sender.sendMessage(ChatColor.AQUA + "You don't have a build license!"); // add clickable links in the future
                sender.sendMessage(ChatColor.AQUA + "You can purchase one from the shop in the hub");
                sender.sendMessage(ChatColor.AQUA + "or you can purchase the $50 rank from our Buycraft page!");
                return false;
            }
            if (args.length != 1) {
                p.sendMessage(ChatColor.RED + "Usage:");
                p.sendMessage(ChatColor.AQUA + "/finishpoint <mapname>");
                return false;
            }
            if (location == null) {
                p.sendMessage("You must be a player to use this command!");
                return false;
            }

            getConfig().set("maps." + args[0] + ".finishpoint.X", location.getX());
            getConfig().set("maps." + args[0] + ".finishpoint.Y", location.getY());
            getConfig().set("maps." + args[0] + ".finishpoint.Z", location.getZ());
        }
        //finish map
        if (cmd.getName().equalsIgnoreCase("finish")) {
            if (!(sender.hasPermission("parkour.build"))) {
                sender.sendMessage(ChatColor.AQUA + "You don't have a build license!"); // add clickable links in the future
                sender.sendMessage(ChatColor.AQUA + "You can purchase one from the shop in the hub");
                sender.sendMessage(ChatColor.AQUA + "or you can purchase the $50 rank from our Buycraft page!");
                return false;
            }
            if (args.length != 1) {
                p.sendMessage(ChatColor.RED + "Usage:");
                p.sendMessage(ChatColor.AQUA + "/finish <mapname>");
                return false;
            }
            if (location == null) {
                p.sendMessage("You must be a player to use this command!");
                return false;
            }
            if (args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("no")) {
                p.sendMessage(ChatColor.AQUA + "Hang on! We're not ready for this yet.");
                p.sendMessage(ChatColor.AQUA + "/finish <mapname>");
            }

            if (getConfig().contains("maps." + args[0] + ".startingpoint.X") &&
                    getConfig().contains("maps." + args[0] + ".checkpoints.") &&
                    getConfig().contains("maps." + args[0] + ".finishpoint.X")) {
                p.sendMessage(ChatColor.AQUA + "Your map is now finished!");
            }
            else {
                if (!(getConfig().contains("maps." + args[0] + ".startingpoint.X")) &&
                        !(getConfig().contains("maps." + args[0] + ".checkpoints.")) &&
                        !(getConfig().contains("maps." + args[0] + ".finishpoint.X"))) {
                    p.sendMessage(ChatColor.AQUA + "That map does not exist!");
                    return false;
                }
                if (!(getConfig().contains("maps." + args[0] + ".startingpoint.X"))) {
                    p.sendMessage(ChatColor.AQUA + "Oops! You're still missing your " + ChatColor.GREEN +
                            "starting point" + ChatColor.AQUA + "!");
                    return false;
                }
                if (!(getConfig().contains("maps." + args[0] + ".checkpoints."))) {
                    p.sendMessage(ChatColor.AQUA + "Are you sure you don't want any checkpoints in your map?");
                    p.sendMessage(ChatColor.GREEN + "/finish yes");
                    p.sendMessage(ChatColor.GREEN + "/finish no");

                    if (args[0].equalsIgnoreCase("yes")) {
                        p.sendMessage(ChatColor.AQUA + "Your map is now finished!");
                    }
                    else if (args[0].equalsIgnoreCase("no")) {
                        p.sendMessage(ChatColor.AQUA + "Stand where you want your checkpoints to be and type /pa checkpoint");
                    }
                    else {
                        p.sendMessage(ChatColor.GREEN + "/finish yes");
                        p.sendMessage(ChatColor.GREEN + "/finish no");
                        return false;
                    }
                }
                if (!(getConfig().contains("maps." + args[0] + ".finishpoint.X"))) {
                    p.sendMessage(ChatColor.AQUA + "Oops! You're still missing your " + ChatColor.GREEN +
                            "finish point" + ChatColor.AQUA + "!");
                }
            }
        }

        return true;
    }
}
