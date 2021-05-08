package me.appl3.sleepcontrol;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SleepControl extends JavaPlugin implements Listener {
	ArrayList<Player> sleeping = new ArrayList<Player>();
	ArrayList<Player> voted = new ArrayList<Player>();
	
	public int voteCount = 0;
	public int votesNeeded = 0;
	
	public String prefix = "&8Sleep > ";
	
	public void onEnable() {
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7> &aSleepControl has been Enabled!"));
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String exactLabel, String[] args) {
		Player player = (Player) sender;
		String label = cmd.getLabel();
		
		if (sender instanceof Player) {
			if (label.equalsIgnoreCase("sleep")) {
				// Add one to the vote count to pass the night.
				if (!sleeping.isEmpty()) {
					if (!sleeping.contains(player)) {
						if (!voted.contains(player)) {
							voteCount++;
							voted.add(player);
							
							for (Player user : Bukkit.getOnlinePlayers()) {
								sendMessage(user, prefix + "&3&l" + player.getName().toString() + " &7voted to skip night. (&b" + voteCount + "/" + votesNeeded);
							}
							checkVotes(player);
						} else {
							sendMessage(player, "&cYou already voted to skip night!");
						}
					} else {
						sendMessage(player, "&cYou cannot vote to skip night if you are already sleeping!");
					}
				} else {
					sendMessage(player, "&cYou cannot vote to skip night until someone sleeps first!");
				}
			}
		}
		return true;
	}
	
	// Converts ampersands into symbols used for color coding.
	public void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	public void getSleepers() {
		sleeping.clear();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.isSleeping())
				sleeping.add(player);
			//else if (sleeping.contains(user))
			//	sleeping.remove(user);
		}
	}
	
	public void initSleep() {
		int numberOfPlayers = Bukkit.getOnlinePlayers().size(); // Get number of online players.
		votesNeeded = (numberOfPlayers - sleeping.size()) / 2; // Requires half of the players online to be asleep or voting to skip night.
		
		if (numberOfPlayers > 1 && votesNeeded > 0) { // Sleep vote will initiate with more than one player online.
			if ((numberOfPlayers % 2) != 0) // If there is an odd number of players, an extra vote is needed.
				votesNeeded++;
				
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (sleeping.isEmpty())
					sendMessage(player, prefix + "&b&l" + sleeping.get(0).getName().toString() + " &7is now resting. Type &6/sleep &7to vote to skip night or sleep in a bed. (&3" + voteCount + "/" + votesNeeded + " votes&7)");
				else
					sendMessage(player, prefix + "&3&l" + sleeping.get(sleeping.size() - 1).getName().toString() + " &7is also resting. (&b" + voteCount + "/" + votesNeeded + " votes&7)");
			}
		}
	}
	
	public void checkVotes(Player player) {
		int numberOfPlayers = Bukkit.getOnlinePlayers().size(); // Get number of online players.
		
		if (voteCount == votesNeeded || numberOfPlayers == 1) {
			player.getWorld().setTime(0); // Day time!
			voteCount = 0;
			votesNeeded = 0;
			
			for (Player user : Bukkit.getOnlinePlayers()) {
				sendMessage(user, prefix + "&7&lGood morning!");
			}
		}
	}
	
	public void decrementSleepCount(Player player) {
		if (player.getWorld().getTime() > 13000) {
			sleeping.remove(player);
			
			if (sleeping.size() > 0) { 
				// Re-calculate the sleep vote.
				int numberOfPlayers = Bukkit.getOnlinePlayers().size(); // Get number of online players.
				votesNeeded = (numberOfPlayers - sleeping.size()) / 2; // Requires half of the players online to be asleep or voting to skip night.
				
				for (Player user : Bukkit.getOnlinePlayers()) {
					sendMessage(user, prefix + "&b&l" + sleeping.get(0).getName().toString() + " &7is no longer resting. (&3" + voteCount + "/" + votesNeeded + " votes&7)");
				}
			} else {
				voteCount = 0; // If no one is sleeping, then the vote count is reset.
				
				for (Player user : Bukkit.getOnlinePlayers()) {
					sendMessage(user, prefix + "&cNo one is asleep. Votes reset.");
				}
			}
		}
	}
	
	@EventHandler
	public void onSleep(PlayerBedEnterEvent event) {
		Player player = event.getPlayer();
		
		if (player.getWorld().getEnvironment() == Environment.NORMAL && event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
			getSleepers();
			initSleep();
			checkVotes(player);
		}
	}
	
	@EventHandler
	public void onExitSleep(PlayerBedLeaveEvent event) {
		Player player = event.getPlayer();
		decrementSleepCount(player);
	}
	
	@EventHandler
	public void onExit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		
		if (player.isSleeping()) {
			decrementSleepCount(player);
			checkVotes(player);
		} else {
			if (voted.contains(player)) {
				voteCount--;
				voted.remove(player);
			}
			// Re-calculate the sleep vote.
			int numberOfPlayers = Bukkit.getOnlinePlayers().size(); // Get number of online players.
			votesNeeded = (numberOfPlayers - sleeping.size()) / 2; // Requires half of the players online to be asleep or voting to skip night.
			
			for (Player user : Bukkit.getOnlinePlayers()) {
				sendMessage(user, prefix + "&b&l" + sleeping.get(0).getName().toString() + " &7is no longer online. (&3" + voteCount + "/" + votesNeeded + " votes&7)");
			}
			checkVotes(player);
		}
	}
}
