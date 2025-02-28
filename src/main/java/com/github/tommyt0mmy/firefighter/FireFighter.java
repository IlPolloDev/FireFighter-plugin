package com.github.tommyt0mmy.firefighter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.github.tommyt0mmy.firefighter.commands.Fireset;
import com.github.tommyt0mmy.firefighter.commands.Firetool;
import com.github.tommyt0mmy.firefighter.commands.Help;
import com.github.tommyt0mmy.firefighter.events.FireExtinguisherActivation;
import com.github.tommyt0mmy.firefighter.events.FiresetWand;
import com.github.tommyt0mmy.firefighter.events.RewardsetGUI;
import com.github.tommyt0mmy.firefighter.tabcompleters.FiresetTabCompleter;
import com.github.tommyt0mmy.firefighter.tabcompleters.HelpTabCompleter;

public class FireFighter extends JavaPlugin {

    public File datafolder = getDataFolder();
    public String prefix = "[" + this.getDescription().getPrefix() + "] ";
    public final String version = this.getDescription().getVersion();
    public boolean startedMission = false;
    public HashMap< UUID, Integer > PlayerContribution = new HashMap<>();
    public String missionName = "";
    public HashMap< UUID, Location > fireset_first_position = new HashMap< UUID, Location >();
    public HashMap< UUID, Location > fireset_second_position = new HashMap< UUID, Location >();
    @SuppressWarnings("serial")
	public final Map < String, String > permissions = new HashMap < String, String > () {
        {
            put("firefighter", "firefighter.help");
            put("firetool_get", "firefighter.firetool.get");
            put("firetool_use", "firefighter.firetool.use");
            put("firetool.freeze-durability", "firefighter.firetool.freeze-durability");
            put("rewardset", "firefighter.fireset.rewardset");
            put("fireset", "firefighter.fireset");
            put("set_wand", "firefighter.setwand");
            put("onduty", "firefighter.onduty");
        }
    };

    @SuppressWarnings("serial")
    public Map < String, String > messages = new HashMap < String, String > () {
        {
            put("invalid_permissions", ChatColor.DARK_RED + prefix + "Invalid permissions!");
            put("page_not_found", ChatColor.DARK_RED + prefix + "Page not found!");
            put("hold_right_click", "Hold Right Click");
            put("unknown_command", "Unknown command. Type '/help' for help.");
            put("fireset_wand_instructions", ChatColor.YELLOW + prefix + "Left click: select pos #1; Right click: select pos #2");
            put("fireset_first_position_set", ChatColor.YELLOW + prefix + "First position set at <x>, <y>, <z>");
            put("fireset_second_position_set", ChatColor.YELLOW + prefix + "Second position set at <x>, <y>, <z>");
            put("fireset_wand_setted", ChatColor.YELLOW + prefix + "New wand setted!");
            put("fireset_mission_not_found", ChatColor.DARK_RED + prefix + "Mission not found!");
            put("fireset_delete", ChatColor.YELLOW + prefix + "Mission successfully deleted!");
            put("fireset_invalid_selection", ChatColor.DARK_RED + prefix + "Invalid selection!");
            put("fireset_added_mission", ChatColor.YELLOW + prefix + "Mission successfully added!");
            put("received_reward", ChatColor.YELLOW + prefix + "You received a reward for completing the mission!");
        }
    };

    Server server = Bukkit.getServer();
    public Logger console = getLogger();

    public void onEnable() {
        console.info("FireFighter v" + version + " enabled succesfully");
        loadEvents();
        loadCommands();
        loadConfigs();
        @SuppressWarnings("unused")
        BukkitTask task = new MissionsHandler(this).runTaskTimer(this, 0, (int)(getConfig().get("missions_interval")) * 20);
    }

    public void onDisable() {
        console.info("FireFighter v" + version + " disabled succesfully");
    }

    private void loadEvents() {
        this.getServer().getPluginManager().registerEvents(new FireExtinguisherActivation(this), this);
        this.getServer().getPluginManager().registerEvents(new FiresetWand(this), this);
        this.getServer().getPluginManager().registerEvents(new RewardsetGUI(this), this);
    }

    private void loadCommands() {
        getCommand("firefighter").setExecutor(new Help(this));
        getCommand("fireset").setExecutor(new Fireset(this));
        getCommand("firetool").setExecutor(new Firetool(this));
        getCommand("firefighter").setTabCompleter(new HelpTabCompleter(this));
        getCommand("fireset").setTabCompleter(new FiresetTabCompleter(this));
    }

    protected void loadConfigs() {
        File configFile = new File(datafolder, "config.yml");
        if (!configFile.exists()) {
            getConfig().options().copyDefaults(true);
            getConfig().set("prefix", prefix);
            getConfig().set("messages.hold_right_click", messages.get("hold_right_click"));
            getConfig().set("missions_interval", 3600);
            getConfig().set("fire_lasting_seconds", 300);
            ItemStack wand = new ItemStack(Material.STICK);
            ItemMeta wandMeta = wand.getItemMeta();
            wandMeta.setDisplayName(ChatColor.YELLOW + "Fireset Wand");
            wand.setItemMeta(wandMeta);
            getConfig().set("fireset.wand", wand);
        }
        setMessage("invalid_permissions");
        setMessage("page_not_found");
        setMessage("unknown_command");
        setMessage("fireset_wand_instructions");
        setMessage("fireset_first_position_set");
        setMessage("fireset_second_position_set");
        setMessage("fireset_wand_setted");
        setMessage("fireset_mission_not_found");
        setMessage("fireset_delete");
        setMessage("fireset_invalid_selection");
        setMessage("received_reward");
        saveConfig();
        readConfigs();
    }

    private void readConfigs() {
        prefix = (String) getConfig().get("prefix");
        messages.put("hold_right_click", (String) getConfig().get("messages.hold_right_click"));
        readMessage("invalid_permissions");
        readMessage("page_not_found");
        readMessage("unknown_command");
        readMessage("fireset_wand_instructions");
        readMessage("fireset_first_position_set");
        readMessage("fireset_second_position_set");
        readMessage("fireset_wand_setted");
        readMessage("fireset_mission_not_found");
        readMessage("fireset_delete");
        readMessage("fireset_invalid_selection");
        readMessage("received_reward");
        int fls = Integer.valueOf(getConfig().get("fire_lasting_seconds").toString());
        int mi = Integer.valueOf(getConfig().get("missions_interval").toString());
        if (fls >= mi) {
            console.warning("Loading the file 'config.yml' an error was encountered!");
            console.warning("The value 'fire_lasting_seconds' cannot be greater than 'missions_interval'");
            console.warning("Resetting 'missions_interval' and 'fire_lasting_seconds'");
            getConfig().set("missions_interval", 3600);
            getConfig().set("fire_lasting_seconds", 300);
            saveConfig();
        }
    }

    private void readMessage(String name) {
        messages.put(name, ((String) getConfig().get("messages." + name)).replace("<prefix>", prefix));
    }

    private void setMessage(String name) {
    	if (getConfig().get(name) == null) {
    		getConfig().set("messages." + name, messages.get(name).replace(prefix, "<prefix>"));
    	}
    }

    public String getPermission(String commandName) {
        return permissions.get(commandName);
    }
}