package org.monstercraft.info;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class represents the main plugin. All actions related to the plugin are forwarded by this class
 * 
 * @author Fletch_to_99 <fletchto99@hotmail.com>
 * 
 */
public class MonsterTell extends JavaPlugin implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (player.getLastPlayed() == 0) {
            player.sendMessage(ChatColor.BLUE
                    + "You must tell the owner how you found this server before you can play! Use /tell (message)");
            player.sendMessage(ChatColor.BLUE + "Use" + ChatColor.AQUA
                    + "\"/tell (message)\"" + ChatColor.BLUE
                    + " and get some rewards!");
            blocked_players.add(player.getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        if (blocked_players.contains(player.getName())) {
            player.sendMessage(ChatColor.BLUE
                    + "You must tell the owner how you found this server before you can play! Use /tell (message)");
            player.sendMessage(ChatColor.BLUE + "Use" + ChatColor.AQUA
                    + "\"/tell (message)\"" + ChatColor.BLUE
                    + " and get some rewards!");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (blocked_players.contains(player.getName())) {
            player.sendMessage(ChatColor.BLUE
                    + "You must tell the owner how you found this server before you can play! Use /tell (message)");
            player.sendMessage(ChatColor.BLUE + "Use" + ChatColor.AQUA
                    + "\"/tell (message)\"" + ChatColor.BLUE
                    + " and get some rewards!");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (blocked_players.contains(player.getName())) {
            player.sendMessage(ChatColor.BLUE
                    + "You must tell the owner how you found this server before you can play! Use /tell (message)");
            player.sendMessage(ChatColor.BLUE + "Use" + ChatColor.AQUA
                    + "\"/tell (message)\"" + ChatColor.BLUE
                    + " and get some rewards!");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/tell")) {
            return;
        }
        final Player player = event.getPlayer();
        if (blocked_players.contains(player.getName())) {
            player.sendMessage(ChatColor.BLUE
                    + "You must tell the owner how you found this server before you can play! Use /tell (message)");
            player.sendMessage(ChatColor.BLUE + "Use" + ChatColor.AQUA
                    + "\"/tell (message)\"" + ChatColor.BLUE
                    + " and get some rewards!");
            event.setCancelled(true);
        }
    }

    private final ArrayList<String> blocked_players = new ArrayList<String>();

    private File file;

    private int id = 0;

    private int amount = 0;

    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        id = getConfig().getInt("ID");
        amount = getConfig().getInt("AMOUNT");
        file = new File(getDataFolder(), "Messages.txt");
        if (!file.exists()) {
            new File(file.getParent()).mkdirs();
            try {
                file.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        getServer().getPluginManager().registerEvents(this, this);
        try {
            new Metrics(this).start();
        } catch (final IOException e) {
        }
    }

    /**
     * Handles commands.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
            final String label, final String[] args) {
        if (label.equalsIgnoreCase("tell")) {
            final StringBuilder sb = new StringBuilder();
            sb.append(sender.getName() + " - ");
            for (final String arg : args) {
                sb.append(arg + " ");
            }
            final String message = sb.toString().trim();
            if (message == null
                    || message.length() <= 10 + (sender.getName() + " - ")
                            .length()) {
                sender.sendMessage(ChatColor.RED
                        + "You must leave a valid message of 10 characters or more!");
                return true;
            }
            try {
                final BufferedWriter bw = new BufferedWriter(new FileWriter(
                        file, true));
                bw.append(message + "\r\n");
                bw.flush();
                bw.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            if (id > 0 && amount > 0) {
                sender.sendMessage(ChatColor.GREEN
                        + "Thanks for the information! Here is your reward!");
                if (sender instanceof Player) {
                    ((Player) sender).getInventory().addItem(
                            new ItemStack(id, amount));
                }
            } else {
                sender.sendMessage(ChatColor.GREEN
                        + "Thanks for the information!");
            }
            blocked_players.remove(sender.getName());
            return true;
        } else if (label.equalsIgnoreCase("tellreload")) {
            if (sender.hasPermission("monstertell.reload")) {
                try {
                    getConfig().load(new File(getDataFolder(), "config.yml"));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                id = getConfig().getInt("ID");
                amount = getConfig().getInt("AMOUNT");
                sender.sendMessage(ChatColor.GREEN
                        + "Successfully reloaded the rewards!");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "You don't have permissions!");
        }
        return true;
    }
}
