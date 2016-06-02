package net.extrillius.parkour;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*
 * Created by TechBug2012 on 5/27/16.
 */
public class Parkour extends JavaPlugin implements Listener {

    /*
    TODO: Add a /add command for people with build licenses.
    TODO: Find a way to get the map name and make an onJoin method that handles everything that happens in a map,
    TODO: ...including the player boolean
    */
    private ArrayList<Material> deathBlocks = new ArrayList<>();
    private Set<String> joinedPlayers = new HashSet<>();


    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        this.saveDefaultConfig();
    }

    public void setMapInv(Player p) {
        ItemStack deathArrow = new ItemStack(Material.ARROW, 1);
        ItemMeta arrowMeta = deathArrow.getItemMeta();
        arrowMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Suicide");
        p.getInventory().setItem(2, deathArrow);

        ItemStack visBall = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta ballMeta = visBall.getItemMeta();
        ballMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Players are visible");
        p.getInventory().setItem(5, visBall);

        ItemStack leaveStick = new ItemStack(Material.STICK, 1);
        ItemMeta stickMeta = leaveStick.getItemMeta();
        stickMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Leave the map");
        p.getInventory().setItem(7, leaveStick);
    }
    public void setLobbyInv(Player p) {
        ItemStack serverCompass = new ItemStack(Material.COMPASS, 1);
        ItemMeta compassMeta = serverCompass.getItemMeta();
        compassMeta.setDisplayName(ChatColor.GOLD + "Server Selector " + ChatColor.GRAY + "(Right click)");
        p.getInventory().setItem(2, serverCompass);

        ItemStack shopEmerald = new ItemStack(Material.EMERALD, 1);
        ItemMeta emeraldMeta = shopEmerald.getItemMeta();
        emeraldMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Server Shop");
        p.getInventory().setItem(5, shopEmerald);
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
                if (getConfig().contains("maps." + args[0])) {
                    p.sendMessage(ChatColor.RED + "That map already exists. Try a different name.");
                    return false;
                }
                getConfig().set("maps." + args[0], "");
                saveConfig();
                p.sendMessage(ChatColor.AQUA + "Map " + ChatColor.GREEN + ChatColor.BOLD + args[0] + ChatColor.AQUA +
                        " has been added.");
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
                saveConfig();
                p.sendMessage(ChatColor.AQUA + "Starting point for map " + ChatColor.GREEN + args[0] +
                        ChatColor.AQUA + " set!");
                p.sendMessage(ChatColor.AQUA + "Stand where you want your checkpoints to be and type " +
                        ChatColor.GREEN + "/checkpoint <map> <value>");
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
                saveConfig();
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
                saveConfig();
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
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/deathblock <map> <value>");
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
                Material deathBlock = location.getBlock().getRelative(BlockFace.DOWN).getType();
                p.sendMessage(ChatColor.AQUA + "When you are finished, type " +
                        ChatColor.GREEN + "/finish <map>");
                deathBlocks.add(deathBlock);
                getConfig().set("maps." + args[0] + ".deathblocks", deathBlocks);
                saveConfig();
                p.sendMessage(ChatColor.AQUA + "Death block " +
                        ChatColor.GREEN + deathBlock.toString().toLowerCase() +
                        ChatColor.AQUA + " has been added.");
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
                                ChatColor.GREEN + "/add <map> <name>");
                        p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Happy parkouring!");
                    }
                }
            }
        }

        if (cmd.getName().equalsIgnoreCase("join")) {
            World world = p.getWorld();
            double startX = getConfig().getDouble("maps." + args[0] + ".startpoint.X");
            double startY = getConfig().getDouble("maps." + args[0] + ".startpoint.Y");
            double startZ = getConfig().getDouble("maps." + args[0] + ".startpoint.Z");
            float startYaw = getConfig().getInt("maps." + args[0] + ".startpoint.YAW");
            float startPitch = getConfig().getInt("maps." + args[0] + ".startpoint.PITCH");
            Location mapStart = new Location(world, startX, startY, startZ, startYaw, startPitch);
            if (args.length != 1) {
                p.sendMessage(ChatColor.AQUA + "You must specify a map to join.");
                p.sendMessage(ChatColor.AQUA + "If you are unsure, there are some great maps at spawn!");
                return false;
            }
            else {
                if (!(getConfig().contains("maps." + args[0]))) {
                    p.sendMessage(ChatColor.RED + "That map does not exist!");
                    p.sendMessage(ChatColor.AQUA + "Try spelling it differently; also note that map names must be " +
                            "case-sensitive.");
                    return false;
                }
                else {
                    joinedPlayers.add(p.getName());
                    p.teleport(mapStart);
                    p.getInventory().clear();
                    setMapInv(p);
                }
            }
        }

        return true;
    }
}
