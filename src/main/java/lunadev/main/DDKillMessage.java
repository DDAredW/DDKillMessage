package lunadev.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DDKillMessage extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
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

            // Пример использования форматирования и цветных кодов
            title = ChatColor.translateAlternateColorCodes('&', title);
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);

            // Получение звука из конфигурации
            String soundName = getConfig().getString("killSound");
            Sound sound = Sound.valueOf(soundName.toUpperCase());

            // Звуковой эффект при появлении сообщения
            killer.playSound(killer.getLocation(), sound, 1.0f, 1.0f);

            // Отображение сообщений в титуле и субтитуле
            killer.sendTitle(title, subtitle, 10, 70, 20);

            // Время отображения сообщения
            long displayTime = getConfig().getLong("displayTime") * 20L; // Перевод секунд в тики
            Bukkit.getScheduler().runTaskLater(this, () -> {
                // Очистка сообщения
                killer.resetTitle(); // Сброс титула с экрана
            }, displayTime);
        }
    }
}
