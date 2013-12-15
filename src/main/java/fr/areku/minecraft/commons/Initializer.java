package fr.areku.minecraft.commons;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.SQLException;

/**
 * Copyright (C) plugin-commons - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexandre, 15/12/13
 */
public class Initializer extends JavaPlugin {
    private File cfgFile;
    private boolean mysql_enabled;
    private String mysql_url;
    private String mysql_user;
    private String mysql_pass;

    @Override
    public void onDisable() {
        try {
            MySQLPool.getPool().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        try {
            loadConfig();
            if (mysql_enabled) {
                MySQLPool.setConnectionData(mysql_url, mysql_user, mysql_pass);
                new MySQLPool();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() throws IOException, InvalidConfigurationException {
        File cfgDir = this.getDataFolder();
        cfgFile = new File(cfgDir, "config.yml");

        if (!cfgDir.exists())
            cfgDir.mkdirs();

        if (!cfgFile.exists())
            copy(this.getResource("config.yml"), cfgFile);

        this.getConfig().load(cfgFile);
        this.getConfig().addDefaults(YamlConfiguration.loadConfiguration(this.getResource("config.yml")));
        this.getConfig().options().copyDefaults(true);

        this.mysql_url = this.getConfig().getString("mysql.url");
        this.mysql_user = this.getConfig().getString("mysql.user");
        this.mysql_pass = this.getConfig().getString("mysql.pass");
        this.mysql_enabled = this.getConfig().getBoolean("mysql.enabled");

        this.getConfig().save(cfgFile);
    }

    private void copy(InputStream src, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = src.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        src.close();
        out.close();
    }
}
