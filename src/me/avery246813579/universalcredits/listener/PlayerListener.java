package me.avery246813579.universalcredits.listener;

import me.avery246813579.universalcredits.UniversalCredits;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener{

	UniversalCredits plugin;
	
	public PlayerListener ( UniversalCredits plugin ){
		this.plugin = plugin;
	}
	
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        plugin.getBalance().put(p.getName(), 0);
        UniversalCredits.getApi().refresh(p.getName());
        if (!plugin.getSaveThread().isAlive()){
        	System.out.println("Save thread is dead");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        plugin.getBalance().remove(p.getName());
    }
}
