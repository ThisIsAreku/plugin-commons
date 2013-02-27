package fr.areku.commons;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class UpdateChecker implements Runnable {
	public static final String VERSION = "1.2";
	public static final long UPDATE_TIME = 86400;

	private JavaPlugin plugin;
	private Configuration update_cfg;
	private URL update_yml_url = null;
	private URL update_raw_url = null;
	private boolean use_raw_version = false;
	private String message_update_url = "";
	private boolean sys_enabled = false;
	private boolean error_shown = false;
	private BukkitTask taskID = null;

	public UpdateChecker(JavaPlugin plugin) throws MalformedURLException {
		this.plugin = plugin;
		this.update_cfg = YamlConfiguration.loadConfiguration(this.plugin
				.getResource("updater.yml"));
		String r, y;
		y = this.update_cfg.getString("update-yml-url", "");
		r = this.update_cfg.getString("update-raw-url", "");
		if (!"".equals(r)) {
			this.update_raw_url = new URL(r);
			this.use_raw_version = true;
		} else if (!"".equals(y)) {
			this.update_yml_url = new URL(y);
			this.use_raw_version = false;
		}
		this.message_update_url = this.update_cfg
				.getString("message-update-url");
		this.sys_enabled = this.update_cfg.getBoolean("enabled", true);
	}

	public void start() {
		if (this.sys_enabled) {
			taskID = Bukkit.getScheduler().runTaskTimerAsynchronously(
					this.plugin, this, 1, UPDATE_TIME * 20);
		} else {
			this.plugin.getLogger().log(Level.WARNING,
					"Updater is not available");
		}
	}

	public void stop() {
		if (this.sys_enabled && (taskID != null)) {
			taskID.cancel();
			taskID = null;
		}
	}

	@Override
	public void run() {
		try {
			String rversion = "";
			if (this.use_raw_version) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						this.update_raw_url.openStream()));

				String inputLine;
				while ((inputLine = in.readLine()) != null)
					rversion += inputLine;
				in.close();
				rversion = rversion.trim();
			} else {
				PluginDescriptionFile web_pdf = new PluginDescriptionFile(
						this.update_yml_url.openStream());
				rversion = web_pdf.getVersion();
			}
			if (!this.plugin.getDescription().getVersion().equals(rversion)) {
				this.plugin.getLogger().log(Level.WARNING,
						"New version found : " + rversion);
				this.plugin.getLogger().log(
						Level.WARNING,
						"Current version: "
								+ this.plugin.getDescription().getVersion());
				this.plugin.getLogger().log(Level.WARNING,
						"Go grab it from " + this.message_update_url + " !");
			}
		} catch (Exception e) {
			if (!error_shown) {
				this.plugin.getLogger().log(Level.WARNING,
						"The auto-updater encounter an unexpected error");
				this.plugin
						.getLogger()
						.log(Level.WARNING,
								"Don't panic ! Your plugin still works, but please report the error :)");
				this.plugin.getLogger().log(Level.WARNING,
						"The updater will now be disabled");
				this.plugin.getLogger().log(Level.WARNING,
						"Updater version " + VERSION);
				this.plugin.getLogger().log(Level.WARNING,
						"Attached to " + this.plugin.getName());
				this.plugin.getLogger().log(Level.WARNING,
						"Message: " + e.getLocalizedMessage());
				this.plugin.getLogger().log(Level.WARNING,
						"---------------------------------");
				stop();
				error_shown = true;
			}
		}
	}
}
