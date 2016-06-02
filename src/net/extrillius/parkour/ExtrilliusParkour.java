package net.extrillius.parkour;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/*
 * Created by TechBug2012 on 5/27/16.
 */
public class ExtrilliusParkour extends JavaPlugin implements Listener {

    /*
    TODO: Add a /add command for people with build licenses.
    */
    private ArrayList<Block> deathBlocks = new ArrayList<>();

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // unsure whether or not to add defaults. will skip for now
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        Location location = p.getLocation();

        if (cmd.getName().equalsIgnoreCase("create")) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/create <name>");
                return false;
            }
            else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            }
            else {
                getConfig().set("maps." + args[0], "");
                p.sendMessage(ChatColor.AQUA + "Map " + ChatColor.BOLD + args[0] + ChatColor.AQUA + " has been added.");
                p.sendMessage(ChatColor.AQUA + "Stand where you want your starting point to be and type " +
                        ChatColor.GREEN + "/startpoint <name>");
            }
        }
        if (cmd.getName().equalsIgnoreCase("startpoint")) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/startpoint <name>");
                return false;
            }
            else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            }
            else {
                getConfig().set("maps." + args[0] + ".startpoint.X", location.getX());
                getConfig().set("maps." + args[0] + ".startpoint.Y", location.getY());
                getConfig().set("maps." + args[0] + ".startpoint.Z", location.getZ());
                getConfig().set("maps." + args[0] + ".startpoint.YAW", location.getYaw());
                getConfig().set("maps." + args[0] + ".startpoint.PITCH", location.getPitch());
                p.sendMessage(ChatColor.AQUA + "Starting point for map " + ChatColor.GREEN + args[0] +
                        ChatColor.AQUA + " set!");
                p.sendMessage(ChatColor.AQUA + "Stand where you want your checkpoints to be and type " +
                        ChatColor.GREEN + "/checkpoint <value> <map>");
            }
        }
        if (cmd.getName().equalsIgnoreCase("checkpoint")) {
            if (args.length != 2) {
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/checkpoint <map> <value>");
                return false;
            }
            else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            }
            else if (!StringUtils.isNumeric(args[1])) {
                p.sendMessage(ChatColor.RED + "Your second argument must be an integer!");
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/checkpoint <map> <value>");
                return false;
            }
            else {
                int value = Integer.parseInt(args[1]);
                getConfig().set("maps." + args[0] + ".checkpoint." + value + ".X", location.getX());
                getConfig().set("maps." + args[0] + ".checkpoint." + value + ".Y", location.getY());
                getConfig().set("maps." + args[0] + ".checkpoint." + value + ".Z", location.getZ());
                getConfig().set("maps." + args[0] + ".checkpoint." + value + ".YAW", location.getYaw());
                getConfig().set("maps." + args[0] + ".checkpoint." + value + ".PITCH", location.getPitch());
                p.sendMessage(ChatColor.AQUA + "Checkpoint " + ChatColor.GREEN + value +
                        ChatColor.AQUA + " set.");
                p.sendMessage(ChatColor.AQUA + "After you finish setting your checkpoints, type " +
                        ChatColor.GREEN + "/endpoint <map>");
            }
        }
        if (cmd.getName().equalsIgnoreCase("endpoint")) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/endpoint <map>");
                return false;
            }
            else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            }
            else {
                getConfig().set("maps." + args[0] + ".endpoint.X", location.getX());
                getConfig().set("maps." + args[0] + ".endpoint.Y", location.getY());
                getConfig().set("maps." + args[0] + ".endpoint.Z", location.getZ());
                getConfig().set("maps." + args[0] + ".endpoint.YAW", location.getYaw());
                getConfig().set("maps." + args[0] + ".endpoint.PITCH", location.getPitch());
                p.sendMessage(ChatColor.AQUA + "Ending point for map " + ChatColor.GREEN + args[0] +
                        ChatColor.AQUA + " has been set!");
                p.sendMessage(ChatColor.AQUA + "Now, set your death blocks.");
                p.sendMessage(ChatColor.AQUA + "Stand on one of your death blocks and type "
                        + ChatColor.GREEN + "/deathblock <map>" +
                        ChatColor.AQUA + " for each death block you want to add in your map.");
            }
        }
        if (cmd.getName().equalsIgnoreCase("deathblock")) {
            if (args.length != 2) {
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/deathblock <value> <map>");
                return false;
            }
            else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            }
            else {
                Block deathBlock = location.getBlock().getRelative(BlockFace.DOWN);
                p.sendMessage(ChatColor.AQUA + "When you are finished, type " +
                        ChatColor.GREEN + "/finish <map>");
                deathBlocks.add(deathBlock);
                getConfig().set("maps." + args[0] + ".deathblocks", deathBlocks);
                p.sendMessage(ChatColor.AQUA + "Death block " +
                        ChatColor.GREEN + deathBlock + ChatColor.AQUA + " has been added.");
            }
        }
        if (cmd.getName().equalsIgnoreCase("finish")) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/finish <map>");
                return false;
            }
            else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            }
            else {
                if (!(getConfig().contains("maps." + args[0]))) {
                    p.sendMessage(ChatColor.RED + "The map " + ChatColor.AQUA + args[0] +
                            ChatColor.RED +
                            " does not exist in the config. " +
                            "Try spelling it differently, then running this command again.");
                    return false;
                }
                else if (!(getConfig().contains("maps." + args[0] + ".endpoint"))) {
                    p.sendMessage(ChatColor.RED + "The map " + ChatColor.AQUA + args[0] + ChatColor.RED +
                            "Does not have an endpoint set!");
                    p.sendMessage(ChatColor.RED + "Run the /endpoint command again, then run this command again.");
                    return false;
                }
                else if (!(getConfig().contains("maps." + args[0] + ".deathblocks"))) {
                    p.sendMessage(ChatColor.RED + "The map " + ChatColor.AQUA + args[0] + ChatColor.RED +
                            " does not have any death blocks set!");
                    p.sendMessage(ChatColor.RED + "Run the /deathblock command again, then run this command again.");
                    return false;
                }
                else {
                    p.sendMessage(ChatColor.AQUA + "The map " + ChatColor.GREEN + args[0] +
                            ChatColor.AQUA + " has been created!");
                    if (p.hasPermission("parkour.build")) {
                        p.sendMessage(ChatColor.AQUA + "If you'd like to add anyone to your map, type " +
                                ChatColor.GREEN + "/add <name> <map>");
                        p.sendMessage(ChatColor.AQUA + "Happy parkouring!");
                    }
                }
            }
        }

        return true;
    }
}
