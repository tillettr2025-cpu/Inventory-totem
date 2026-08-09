package com.ryxn.autototem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoTotemConfig {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Path FILE =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("auto-totem.json");

    public boolean enabled = true;

    /*
     * Hotbar slot where the backup Totem goes.
     *
     * 0 = slot 1
     * 1 = slot 2
     * 2 = slot 3
     * 3 = slot 4
     * 4 = slot 5
     * ...
     * 8 = slot 9
     */
    public int backupSlot = 4;

    public static AutoTotemConfig load() {

        try {
            if (Files.exists(FILE)) {

                try (Reader reader =
                             Files.newBufferedReader(FILE)) {

                    AutoTotemConfig config =
                            GSON.fromJson(
                                    reader,
                                    AutoTotemConfig.class
                            );

                    if (config != null) {
                        return config;
                    }
                }
            }

        } catch (Exception ignored) {
        }

        AutoTotemConfig config =
                new AutoTotemConfig();

        config.save();

        return config;
    }

    public void save() {

        try {
            Files.createDirectories(
                    FILE.getParent()
            );

            try (Writer writer =
                         Files.newBufferedWriter(FILE)) {

                GSON.toJson(
                        this,
                        AutoTotemConfig.class,
                        writer
                );
            }

        } catch (Exception ignored) {
        }
    }
}
