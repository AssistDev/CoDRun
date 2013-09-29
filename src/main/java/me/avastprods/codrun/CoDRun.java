package main.java.me.avastprods.codrun;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CoDRun extends JavaPlugin implements Listener {

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	HashMap<String, Long> lastMove = new HashMap<String, Long>();
	static HashMap<String, Location> lastLocation = new HashMap<String, Location>();
	static ArrayList<String> someList = new ArrayList<String>();

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (event.getPlayer().isSprinting()) {
			if (event.getFrom().getX() != event.getTo().getX() && event.getFrom().getZ() != event.getTo().getZ()) {
				handleMove(event.getPlayer());

				lastLocation.put(event.getPlayer().getName(), event.getPlayer().getLocation());
			}
		}
	}

	private void handleMove(final Player player) {
		if (player.getFoodLevel() > 0) {
			long cur = System.currentTimeMillis();

			if (lastMove.containsKey(player.getName())) {
				long old = lastMove.get(player.getName());

				if ((cur - old) / 200 > 0.1) {
					player.setFoodLevel(player.getFoodLevel() - 1);

					if (!someList.contains(player.getName())) {
						someList.add(player.getName());

						RegenTask task = new RegenTask();
						task.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, task, 0L, 20L), player);
					}

					lastMove.put(player.getName(), cur);
				}

			} else {
				lastMove.put(player.getName(), cur);
				handleMove(player);
			}
		}
	}
}

class RegenTask implements Runnable {

	private int id;
	private Player player;

	public int getId() {
		return id;
	}

	public void setId(int id, Player player) {
		this.id = id;
		this.player = player;
	}

	@Override
	public void run() {
		if (!handleTask()) {
			stop();
		}
	}

	private boolean handleTask() {
		if (player.getFoodLevel() < 20) {
			if (!player.isSprinting()) {
				player.setFoodLevel((CoDRun.lastLocation.get(player.getName()).getBlockX() == player.getLocation().getBlockX() && CoDRun.lastLocation.get(player.getName()).getBlockZ() == player.getLocation().getBlockZ()) ? player.getFoodLevel() + 2 : player.getFoodLevel() + 1);
			}

			return true;
		}

		return false;
	}

	public void stop() {
		Bukkit.getServer().getScheduler().cancelTask(getId());
		CoDRun.someList.remove(player.getName());
	}
}
