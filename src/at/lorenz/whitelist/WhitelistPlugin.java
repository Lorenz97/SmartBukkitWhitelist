package at.lorenz.whitelist;

import at.lorenz.whitelist.utils.CommandHelper;
import at.lorenz.whitelist.utils.uuidfetcher.UUIDFetcher;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WhitelistPlugin extends JavaPlugin implements Listener {

    private final Collection<UUID> whitelist = new ArrayList<>();
    private File file = new File("wlist.txt");
    private boolean enabled = false;

    @Override
    public void onEnable() {
        if (file.isFile()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String entry : config.getStringList("whitelist")) {
                whitelist.add(UUID.fromString(entry));
            }
            enabled = config.getBoolean("enabled");
        } else {
            updateConfig();
        }
        Bukkit.getPluginManager().registerEvents(this, this);

        PluginCommand cmd = getCommand("wlist");
        cmd.setExecutor((sender, command, label, args) -> {
            if (args.length != 0) {
                switch (args[0].toLowerCase()) {
                    case "add":
                        if (args.length != 2) {
                            sender.sendMessage("§c/" + label + " add <player>");
                            return true;
                        }
                        UUID uuid = UUIDFetcher.getUUID(args[1]);
                        if (uuid == null) {
                            sender.sendMessage("§cPlayer " + args[1] + " does not exist");
                            return true;
                        }
                        String name = UUIDFetcher.getName(uuid);
                        if (whitelist.contains(uuid)) {
                            sender.sendMessage("§e" + name + " §cis already on the whitelist");
                            return true;
                        }
                        whitelist.add(uuid);
                        notify("§bAdded §e" + name + " §bto the whitelist");
                        updateConfig();
                        return true;
                    case "remove":
                        if (args.length != 2) {
                            sender.sendMessage("§c/" + label + " add <player>");
                            return true;
                        }
                        uuid = UUIDFetcher.getUUID(args[1]);
                        if (uuid == null) {
                            sender.sendMessage("§cPlayer " + args[1] + " does not exist");
                            return true;
                        }
                        name = UUIDFetcher.getName(uuid);
                        if (!whitelist.contains(uuid)) {
                            sender.sendMessage("§e" + name + " §cis not on the whitelist");
                            return true;
                        }
                        whitelist.remove(uuid);
                        notify("§bRemoved §e" + name + " §bfrom the whitelist");
                        updateConfig();
                        return true;
                    case "list":
                        if (whitelist.isEmpty()) {
                            sender.sendMessage("§bThe whitelist is empty");
                            return true;
                        }
                        StringBuilder builder = new StringBuilder();
                        for (UUID uuid1 : whitelist) {
                            Player player = Bukkit.getPlayer(uuid1);
                            if (player != null) {
                                builder.append("§a");
                                builder.append(player.getName());
                            } else {
                                String name1 = UUIDFetcher.getName(uuid1);
                                builder.append("§e");
                                builder.append(name1);
                            }
                            builder.append("§7, ");
                        }
                        String a = "§bAll §e" + whitelist.size() + " §bplayers on the whitelist:\n";
                        String b = builder.toString();
                        b = b.substring(0, b.length() - 4);
                        sender.sendMessage(a + b);
                        return true;
                    case "info":
                        sender.sendMessage("§bWhitelist is " + (enabled ? "§aenabled" : "§cdiabled"));
                        return true;
                    case "enable":
                        if (enabled) {
                            sender.sendMessage("§cWhitelist is already enabled");
                            return true;
                        }
                        enabled = true;
                        notify("§bWhitelist §aenabled");
                        updateConfig();
                        return true;
                    case "disable":
                        if (!enabled) {
                            sender.sendMessage("§cWhitelist is not enabled");
                            return true;
                        }
                        enabled = false;
                        notify("§bWhitelist §cdisabled");
                        updateConfig();
                        return true;
                }
            }
            sender.sendMessage("§c/" + label + " <add;remove;list;info;enable;disable> ...");
            return true;
        });
        cmd.setTabCompleter((sender, command, label, args) -> {
            List<String> list = new ArrayList<>();
            if (args.length == 1) {
                list.add("add");
                list.add("remove");
                list.add("list");
                list.add("info");
                list.add("enable");
                list.add("disable");
                return CommandHelper.makeTabComplete(args, list);
            }
            if (args.length == 2) {
                String arg = args[0];
                switch (arg.toLowerCase()) {
                    case "add":
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (!whitelist.contains(player.getUniqueId())) {
                                list.add(player.getName());
                            }
                        }
                        return CommandHelper.makeTabComplete(args, list);
                    case "remove":
                        for (UUID uuid : whitelist) {
                            list.add(UUIDFetcher.getName(uuid));
                        }
                        return CommandHelper.makeTabComplete(args, list);
                }
            }
            return list;
        });
    }

    private void notify(String text) {
        Bukkit.getConsoleSender().sendMessage(text);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp() || player.hasPermission("wlist")) {
                player.sendMessage(text);
            }
        }
    }

    private void updateConfig() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("enabled", enabled);
        config.set("whitelist", whitelist.stream().map(UUID::toString).collect(Collectors.toList()));
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    void onJoin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        if (!enabled || whitelist.contains(uuid)) {
            return;
        }
        String name = event.getName();
        notify("§bWhitelist knock: §e" + name);
        TextComponent component = new TextComponent("§aACCEPT");
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlist add " + name));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp() || player.hasPermission("wlist")) {
                player.spigot().sendMessage(component);
            }
        }
        for (int i = 0; i < 50; i++) {
            if (!enabled || whitelist.contains(uuid)) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST);
        event.setKickMessage("§bWhitelist");
    }
}
