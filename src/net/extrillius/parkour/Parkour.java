package net.extrillius.parkour;

import com.greatmancode.craftconomy3.Common;
import com.greatmancode.craftconomy3.tools.interfaces.Loader;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/*
 * Created by TechBug2012 on 5/27/16.
 */
public class Parkour extends JavaPlugin implements Listener {

    /*
    TODO: Add a /add command for people with build licenses.
    TODO: Add special blocks and particle effects
    TODO: Test the checkpoint detection
    TODO: Fix the CraftConomy payment (method deprecated)
    checkpoint is reached more than once; (fixed; needs to be tested)
    difficulty is not being set in config. This causes a NullPointerException on finish. (fixed; needs to be tested)
    */
    private Set<String> joinedPlayers = new HashSet<>();
    private Set<String> hiddenPlayers = new HashSet<>();
    private HashMap<String, String> playerMap = new HashMap<>();
    private HashMap<String, String> playerCheckpoint = new HashMap<>();
    private HashMap<String, String> checkCheckpoint = new HashMap<>();
    private HashMap<String, Integer> deathCount = new HashMap<>();
    private Plugin plugin = getServer().getPluginManager().getPlugin("Craftconomy3");
    private Common craftconomy = (Common) ((Loader) plugin).getCommon();

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        this.saveDefaultConfig();
    }

    //Items players use within maps
    private void setMapInv(Player p) {
        ItemStack deathArrow = new ItemStack(Material.ARROW, 1);
        ItemMeta arrowMeta = deathArrow.getItemMeta();
        arrowMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Suicide");
        deathArrow.setItemMeta(arrowMeta);
        p.getInventory().setItem(1, deathArrow);

        ItemStack visBall = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta ballMeta = visBall.getItemMeta();
        ballMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Toggle player visibility");
        visBall.setItemMeta(ballMeta);
        p.getInventory().setItem(4, visBall);

        ItemStack leaveStick = new ItemStack(Material.STICK, 1);
        ItemMeta stickMeta = leaveStick.getItemMeta();
        stickMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Leave the map");
        leaveStick.setItemMeta(stickMeta);
        p.getInventory().setItem(6, leaveStick);
    }

    //Items players use within lobby
    private void setLobbyInv(Player p) {
        ItemStack serverCompass = new ItemStack(Material.COMPASS, 1);
        ItemMeta compassMeta = serverCompass.getItemMeta();
        compassMeta.setDisplayName(ChatColor.GOLD + "Server Selector " + ChatColor.GRAY + "(Right click)");
        serverCompass.setItemMeta(compassMeta);
        p.getInventory().setItem(1, serverCompass);

        ItemStack shopEmerald = new ItemStack(Material.EMERALD, 1);
        ItemMeta emeraldMeta = shopEmerald.getItemMeta();
        emeraldMeta.setDisplayName(ChatColor.GREEN + "Server Shop");
        shopEmerald.setItemMeta(emeraldMeta);
        p.getInventory().setItem(4, shopEmerald);
    }

    //Death counter
    private void killPlayer(Player p) {
        if (!deathCount.containsKey(p.getName())) {
            deathCount.put(p.getName(), 1);
        } else {
            deathCount.put(p.getName(), deathCount.get(p.getName()) + 1);
        }

        if (deathCount.get(p.getName()) == 1) {
            p.sendMessage(ChatColor.AQUA + "You have " + ChatColor.GREEN + "" + ChatColor.BOLD + "1 " +
                    ChatColor.AQUA + "death.");
        } else {
            p.sendMessage(ChatColor.AQUA + "You have " + ChatColor.GREEN + "" + ChatColor.BOLD
                    + deathCount.get(p.getName()) + ChatColor.AQUA + " deaths.");
        }
    }

    //Lobby teleport command
    @SuppressWarnings("deprecation")
    private void leaveMap(Player p) {
        p.performCommand("spawn");
        p.sendMessage(ChatColor.AQUA + "You left the map " + ChatColor.GREEN + "" + ChatColor.BOLD +
                playerMap.get(p.getName()));
        playerCheckpoint.remove(p.getName());
        playerMap.remove(p.getName());
        deathCount.remove(p.getName());
        joinedPlayers.remove(p.getName());
        p.getInventory().clear();
        setLobbyInv(p);
    }

    @SuppressWarnings("unused, deprecation")
    @EventHandler
    //Checkpoints, spawnpoint, deathblock, endpoint
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();

        if (joinedPlayers.contains(p.getName())) {
            if (p.getHealth() != 20) {
                p.setHealth(20);
            }
            if (p.getFoodLevel() != 20) {
                p.setFoodLevel(20);
                p.setSaturation(20);
            }
        }

        if (playerMap.containsKey(p.getName())) {
            for (String checkpoint : getConfig().getConfigurationSection("maps." + playerMap.get(p.getName()) +
                    ".checkpoint").getKeys(false)) { // Does not work
                if (p.getLocation().getBlockX() == getConfig().getInt("maps." + playerMap.get(p.getName()) +
                        ".checkpoint." + checkpoint + ".X")
                        && p.getLocation().getBlockY() == getConfig().getInt("maps." + playerMap.get(p.getName()) +
                        ".checkpoint." + checkpoint + ".Y")
                        && p.getLocation().getBlockZ() == getConfig().getInt("maps." + playerMap.get(p.getName()) +
                        ".checkpoint." + checkpoint + ".Z")) {
                    if (!(playerCheckpoint.isEmpty())) {
                        playerCheckpoint.clear();
                    }
                    if (!(checkCheckpoint.get(p.getName()).equals(playerCheckpoint.get(p.getName())))) {
                        playerCheckpoint.put(p.getName(), checkpoint);
                        p.sendMessage(ChatColor.AQUA + "You have reached checkpoint " + ChatColor.GREEN +
                                ChatColor.BOLD + checkpoint);
                        checkCheckpoint.put(p.getName(), checkpoint);
                    }
                }
            }

            if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().toString().equals(getConfig()
                    .getString("maps." + playerMap.get(p.getName()) + ".deathblocks"))) {
                Location cp = new Location(p.getWorld(), getConfig().getDouble("maps." + playerMap.get(p.getName()) +
                        ".checkpoint." + playerCheckpoint.get(p.getName()) + ".X"), getConfig().getDouble("maps." +
                        playerMap.get(p.getName()) +
                        ".checkpoint." + playerCheckpoint.get(p.getName()) + ".Y"), getConfig().getDouble("maps." +
                        playerMap.get(p.getName()) +
                        ".checkpoint." + playerCheckpoint.get(p.getName()) + ".Z"), getConfig().getInt("maps." +
                        playerMap.get(p.getName()) +
                        ".checkpoint." + playerCheckpoint.get(p.getName()) + ".YAW"), getConfig().getInt("maps." +
                        playerMap.get(p.getName()) +
                        ".checkpoint." + playerCheckpoint.get(p.getName()) + ".PITCH"));
                Location start = new Location(p.getWorld(), getConfig().getDouble("maps." + playerMap.get(p.getName()) +
                        ".startpoint.X"), getConfig().getDouble("maps." + playerMap.get(p.getName()) +
                        ".startpoint.Y"), getConfig().getDouble("maps." + playerMap.get(p.getName()) +
                        ".startpoint.Z"), getConfig().getInt("maps." + playerMap.get(p.getName()) +
                        ".startpoint.YAW"), getConfig().getInt("maps." + playerMap.get(p.getName()) +
                        ".startpoint.PITCH"));

                if (playerCheckpoint.containsKey(p.getName())) {
                    p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You died! " + ChatColor.AQUA +
                            "Teleporting you to checkpoint " + ChatColor.GREEN +
                            "" + ChatColor.BOLD + playerCheckpoint.get(p.getName()));
                    p.teleport(cp);
                } else {
                    p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You died! " + ChatColor.AQUA +
                            "Teleporting you to the start of the map.");
                    p.teleport(start);
                }
                killPlayer(p);
            }
            if (p.getLocation().getBlockX() == getConfig().getInt("maps." + playerMap.get(p.getName()) + ".endpoint.X")
                    && p.getLocation().getBlockY() == getConfig().getInt("maps." + playerMap.get(p.getName()) +
                    ".endpoint.Y")
                    && p.getLocation().getBlockZ() == getConfig().getInt("maps." + playerMap.get(p.getName()) +
                    ".endpoint.Z")) {

                leaveMap(p);

                Firework f = p.getWorld().spawn(p.getLocation(), Firework.class);
                FireworkMeta fm = f.getFireworkMeta();

                if (getConfig().getString("maps." + playerMap.get(p.getName()) + ".difficulty").equals("EASY")) {
                    craftconomy.getAccountManager().getAccount(p.getName(), false).deposit(20, p.getWorld()
                            .getName(), "$");
                    p.sendMessage(ChatColor.AQUA + "You have received " + ChatColor.DARK_AQUA + "$20" +
                            ChatColor.AQUA
                            + " for completing " + ChatColor.GREEN + "" + ChatColor.BOLD + playerMap.get
                            (p.getName()) +
                            ChatColor.AQUA + "!");
                    fm.addEffect(FireworkEffect.builder().withColor(Color.YELLOW).with(FireworkEffect.Type.BALL)
                            .build());
                    fm.setPower(1);
                    f.setFireworkMeta(fm);
                } else if (getConfig().getString("maps." + playerMap.get(p.getName()) + ".difficulty").equals
                        ("MEDIUM")) {
                    craftconomy.getAccountManager().getAccount(p.getName(), false).deposit(100, p.getWorld().
                            getName(), "$");
                    p.sendMessage(ChatColor.AQUA + "You have received " + ChatColor.DARK_AQUA + "$100" +
                            ChatColor.AQUA
                            + " for completing " + ChatColor.GREEN + "" + ChatColor.BOLD + playerMap.get
                            (p.getName()) +
                            ChatColor.AQUA + "!");
                    fm.addEffect(FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BALL)
                            .build());
                    fm.setPower(1);
                    f.setFireworkMeta(fm);
                } else if (getConfig().getString(playerMap.get(p.getName()) + ".difficulty").equals("HARD")) {
                    craftconomy.getAccountManager().getAccount(p.getName(), false).deposit(500, p.getWorld()
                            .getName(), "$");
                    p.sendMessage(ChatColor.AQUA + "You have received " + ChatColor.DARK_GREEN + "$500" +
                            ChatColor.AQUA
                            + " for completing " + ChatColor.GREEN + "" + ChatColor.BOLD + playerMap.get
                            (p.getName()) +
                            ChatColor.AQUA + "!");
                    fm.addEffect(FireworkEffect.builder().withColor(Color.GREEN).with(FireworkEffect.Type.BALL)
                            .build());
                    fm.setPower(1);
                    f.setFireworkMeta(fm);
                } else {
                    double value = Double.parseDouble(getConfig().getString("maps." + playerMap.get(p.getName())
                            + ".difficulty"));
                    craftconomy.getAccountManager().getAccount(p.getName(), false).deposit(value, p.getWorld()
                            .getName(), "$");
                    p.sendMessage(ChatColor.AQUA + "You have received " + ChatColor.DARK_GREEN + value +
                            ChatColor.AQUA
                            + " for completing " + ChatColor.GREEN + "" + ChatColor.BOLD + playerMap.get
                            (p.getName()) +
                            ChatColor.AQUA + "!");
                    fm.addEffect(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL)
                            .build());
                    fm.setPower(1);
                    f.setFireworkMeta(fm);
                }
            }
        }
    }


    @SuppressWarnings("unused")
    //using items within maps
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (joinedPlayers.contains(p.getName())) {
                if (p.getItemInHand().getType() == Material.ARROW) {
                    killPlayer(p);
                }
                if (p.getItemInHand().getType() == Material.SLIME_BALL) {
                    for (Player o : getServer().getOnlinePlayers()) {
                        if (!(hiddenPlayers.contains(p.getName()))) {
                            hiddenPlayers.add(p.getName());
                            p.hidePlayer(o);
                            p.sendMessage(ChatColor.AQUA + "Players are now invisible.");
                        } else {
                            hiddenPlayers.remove(p.getName());
                            p.showPlayer(o);
                            p.sendMessage(ChatColor.AQUA + "Players are now visible again.");
                        }
                    }
                }
                if (p.getItemInHand().getType() == Material.STICK) {
                    leaveMap(p);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (joinedPlayers.contains(p.getName())) {
            leaveMap(p);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (!(joinedPlayers.contains(p.getName()))) {
            p.getInventory().clear();
            setLobbyInv(p);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (joinedPlayers.contains(p.getName())) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (joinedPlayers.contains(p.getName())) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (joinedPlayers.contains(p.getName())) {
                event.setCancelled(true);
            }
        }
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        Location location = p.getLocation();
        //create maps
        if (cmd.getName().equalsIgnoreCase("create")) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/create <name>");
                return false;
            } else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            } else {
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
            } else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            } else {
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
            } else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            } else if (!StringUtils.isNumeric(args[1])) {
                p.sendMessage(ChatColor.RED + "Your second argument must be an integer!");
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/checkpoint <map> <value>");
                return false;
            } else {
                int value = Integer.parseInt(args[1]);
                getConfig().set("maps." + args[0] + ".checkpoint." + value + ".X", location.getBlockX());
                getConfig().set("maps." + args[0] + ".checkpoint." + value + ".Y", location.getBlockY());
                getConfig().set("maps." + args[0] + ".checkpoint." + value + ".Z", location.getBlockZ());
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
            } else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            } else {
                getConfig().set("maps." + args[0] + ".endpoint.X", location.getBlockX());
                getConfig().set("maps." + args[0] + ".endpoint.Y", location.getBlockY());
                getConfig().set("maps." + args[0] + ".endpoint.Z", location.getBlockZ());
                getConfig().set("maps." + args[0] + ".endpoint.YAW", location.getYaw());
                getConfig().set("maps." + args[0] + ".endpoint.PITCH", location.getPitch());
                saveConfig();
                p.sendMessage(ChatColor.AQUA + "Ending point for map " + ChatColor.GREEN + args[0] +
                        ChatColor.AQUA + " has been set!");
                p.sendMessage(ChatColor.AQUA + "Now, set your death blocks.");
                p.sendMessage(ChatColor.AQUA + "Stand on one of your death blocks and type "
                        + ChatColor.GREEN + "/deathblock <map>");
            }
        }
        if (cmd.getName().equalsIgnoreCase("deathblock")) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/deathblock <map>");
                return false;
            } else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            } else {
                String deathBlock = location.getBlock().getRelative(BlockFace.DOWN).getType().toString();
                getConfig().set("maps." + args[0] + ".deathblocks", deathBlock);
                saveConfig();
                p.sendMessage(ChatColor.AQUA + "Death block " +
                        ChatColor.GREEN + deathBlock.toLowerCase() +
                        ChatColor.AQUA + " has been added.");
                p.sendMessage(ChatColor.AQUA + "Type " + ChatColor.GREEN + "/difficulty " + ChatColor.AQUA +
                        "to set the difficulty level for your map.");
            }
        }
        if (cmd.getName().equalsIgnoreCase("difficulty")) {
            if (args.length != 2) {
                p.sendMessage(ChatColor.RED + "Difficulty levels:");
                p.sendMessage(ChatColor.AQUA + "Easy");
                p.sendMessage(ChatColor.AQUA + "Medium");
                p.sendMessage(ChatColor.AQUA + "Hard");
                p.sendMessage(ChatColor.RED + "Usage: " + ChatColor.AQUA + "/difficulty <map> <value>");
                return false;
            } else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            } else {
                if (args[1].equalsIgnoreCase("easy")) { // seems like this isn't setting correctly...
                    getConfig().set("maps." + args[0] + ".difficulty", args[1].toUpperCase());
                } else if (args[1].equalsIgnoreCase("medium")) {
                    getConfig().set("maps." + args[0] + ".difficulty", args[1].toUpperCase());
                } else if (args[1].equalsIgnoreCase("hard")) {
                    getConfig().set("maps." + args[0] + ".difficulty", args[1].toUpperCase());
                } else {
                    p.sendMessage(ChatColor.RED + "Difficulty levels:");
                    p.sendMessage(ChatColor.AQUA + "Easy");
                    p.sendMessage(ChatColor.AQUA + "Medium");
                    p.sendMessage(ChatColor.AQUA + "Hard");
                    return false;
                }
                p.sendMessage(ChatColor.AQUA + "Difficulty " + ChatColor.GREEN + args[1] + ChatColor.AQUA +
                        " set for map " + ChatColor.GREEN + args[0]);
                p.sendMessage(ChatColor.AQUA + "To check if everything's good to go with your map, type " +
                        ChatColor.GREEN + "/finish <map>");
            }
        }
        if (cmd.getName().equalsIgnoreCase("customvalue")) {
            if (!(p.hasPermission("parkour.admin"))) {
                p.sendMessage(ChatColor.RED + "Setting custom earning amounts is an admin-only command!");
                p.sendMessage(ChatColor.AQUA + "If you feel like your map deserves a higher earning amount, contact " +
                        "one of our admins.");
                return false;
            } else if (args.length != 2) {
                p.sendMessage(ChatColor.RED + "Usage: " + ChatColor.AQUA + "/customvalue <map> <value>");
                return false;
            } else if (!(StringUtils.isNumeric(args[1]))) {
                p.sendMessage(ChatColor.RED + "Your second argument must be an double value!");
                p.sendMessage(ChatColor.RED + "Usage: " + ChatColor.AQUA + "/customvalue <map> <value>");
                return false;
            } else {
                getConfig().set("maps." + args[0] + ".difficulty.", args[1]);
                p.sendMessage(ChatColor.AQUA + "Custom earning of " + ChatColor.GREEN + args[1] + ChatColor.AQUA +
                        " has been set for map " + ChatColor.GREEN + args[0]);
            }
        }
        if (cmd.getName().equalsIgnoreCase("finish")) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.AQUA + "/finish <map>");
                return false;
            } else if (!(p.hasPermission("parkour.admin")) || !(p.hasPermission("parkour.build"))) {
                p.sendMessage(ChatColor.RED + "You don't have permission to create maps!");
                if (!(p.hasPermission("parkour.build")) && !(p.hasPermission("parkour.admin"))) {
                    p.sendMessage(ChatColor.AQUA + "You can purchase a build license from the " +
                            ChatColor.GREEN + "server shop" + ChatColor.AQUA + ", or you can purchase one from our "
                            + ChatColor.GREEN + "Buycraft store" + ChatColor.AQUA + ".");
                }
                return false;
            } else {
                if (!(getConfig().contains("maps." + args[0]))) {
                    p.sendMessage(ChatColor.RED + "The map " + ChatColor.AQUA + args[0] +
                            ChatColor.RED +
                            " does not exist in the config. " +
                            "Try spelling it differently, then running this command again.");
                    return false;
                } else if (!(getConfig().contains("maps." + args[0] + ".endpoint"))) {
                    p.sendMessage(ChatColor.RED + "The map " + ChatColor.AQUA + args[0] + ChatColor.RED +
                            "does not have an endpoint set!");
                    p.sendMessage(ChatColor.RED + "Run the /endpoint command again, then run this command again.");
                    return false;
                } else if (!(getConfig().contains("maps." + args[0] + ".deathblocks"))) {
                    p.sendMessage(ChatColor.RED + "The map " + ChatColor.AQUA + args[0] + ChatColor.RED +
                            " does not have any death blocks set!");
                    p.sendMessage(ChatColor.RED + "Run the /deathblock command again, then run this command again.");
                    return false;
                } else if (!(getConfig().contains("maps." + args[0] + ".difficulty"))) {
                    p.sendMessage(ChatColor.RED + "The map " + ChatColor.AQUA + args[0] + ChatColor.RED + " does not " +
                            "have a difficulty set!");
                    p.sendMessage(ChatColor.RED + "Run the /difficulty command again, then run this command again.");
                    return false;
                } else {
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
        //join maps
        if (cmd.getName().equalsIgnoreCase("join")) {
            if (args.length == 0) {
                p.sendMessage(ChatColor.AQUA + "You must specify a map to join.");
                p.sendMessage(ChatColor.AQUA + "If you are unsure, there are some great maps at spawn!");
                return false;
            } else if (args.length > 1) {
                p.sendMessage(ChatColor.RED + "Usage: " + ChatColor.GRAY + "/join <map>");
                return false;
            }
            World world = p.getWorld();
            double startX = getConfig().getDouble("maps." + args[0] + ".startpoint.X");
            double startY = getConfig().getDouble("maps." + args[0] + ".startpoint.Y");
            double startZ = getConfig().getDouble("maps." + args[0] + ".startpoint.Z");
            float startYaw = getConfig().getInt("maps." + args[0] + ".startpoint.YAW");
            float startPitch = getConfig().getInt("maps." + args[0] + ".startpoint.PITCH");
            Location mapStart = new Location(world, startX, startY, startZ, startYaw, startPitch);

            if (!(getConfig().contains("maps." + args[0]))) {
                p.sendMessage(ChatColor.RED + "That map does not exist!");
                p.sendMessage(ChatColor.AQUA + "Try spelling it differently; also note that map are " +
                        "case-sensitive.");
                return false;
            } else {
                if (!(joinedPlayers.contains(p.getName()))) {
                    joinedPlayers.add(p.getName());
                }
                playerMap.put(p.getName(), args[0]);
                p.teleport(mapStart);
                p.getInventory().clear();
                setMapInv(p);
                if (p.getGameMode() != GameMode.SURVIVAL) {
                    p.setGameMode(GameMode.SURVIVAL);
                }
                p.sendMessage(ChatColor.AQUA + "You joined the map " + ChatColor.GREEN + "" + ChatColor.BOLD +
                        playerMap.get(p.getName()));
            }
        }
        if (cmd.getName().equalsIgnoreCase("leave")) {
            if (args.length != 0) {
                p.sendMessage(ChatColor.RED + "Usage: " + ChatColor.GRAY + "/leave");
                return false;
            }
            if (joinedPlayers.contains(p.getName())) {
                leaveMap(p);
            } else {
                p.sendMessage(ChatColor.AQUA + "You are not in a map!");
            }
        }

        return true;
    }
}
