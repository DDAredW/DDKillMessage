package lunadev.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DDKillMessage extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        validateConfig();
    }

    private void validateConfig() {
        FileConfiguration config = getConfig();

        String soundName = config.getString("killSound");
        try {
            Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid killSound specified in config.yml. Using default value: ENTITY_PLAYER_LEVELUP");
            config.set("killSound", "ENTITY_PLAYER_LEVELUP");
            saveConfig();
        }

        boolean particlesEnabled = config.getBoolean("particles.enabled");
        String particleType = config.getString("particles.type");
        int particleCount = config.getInt("particles.count");
        
        if (particleCount < 1) {
            getLogger().warning("Invalid particle count specified in config.yml. Using default value: 20");
            config.set("particles.count", 20);
            saveConfig();
        }

        try {
            Particle.valueOf(particleType.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid particle type specified in config.yml. Using default value: CRIT_MAGIC");
            config.set("particles.type", "CRIT_MAGIC");
            saveConfig();
        }
    }

    private String applyColorCodes(String input) {
        Pattern pattern = Pattern.compile("&#([a-fA-F0-9]{6})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String color = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : color.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            String victimName = event.getEntity().getName();
            String title = getConfig().getString("title")
                    .replace("{killer}", killer.getName())
                    .replace("{victim}", victimName);
            String subtitle = getConfig().getString("subtitle")
                    .replace("{killer}", killer.getName())
                    .replace("{victim}", victimName);
            
            title = applyColorCodes(title);
            subtitle = applyColorCodes(subtitle);

            String soundName = getConfig().getString("killSound");
            Sound sound = Sound.valueOf(soundName.toUpperCase());

            killer.playSound(killer.getLocation(), sound, 1.0f, 1.0f);

            boolean particlesEnabled = getConfig().getBoolean("particles.enabled");
            String particleType = getConfig().getString("particles.type");
            int particleCount = getConfig().getInt("particles.count");

            if (particlesEnabled) {
                Particle particle = Particle.valueOf(particleType.toUpperCase());
                killer.getWorld().spawnParticle(particle, killer.getLocation(), particleCount);
            }

            killer.sendTitle(title, subtitle, 10, 70, 20);

            long displayTime = getConfig().getLong("displayTime") * 20L;
            Bukkit.getScheduler().runTaskLater(this, () -> {
                killer.resetTitle();
            }, displayTime);
        }
    }
}
