package dev.kubabin.livechat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("livechat.json");

    private static FabricConfig config;

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    config = GSON.fromJson(reader, FabricConfig.class);
                }
            } else {
                config = new FabricConfig();
                save();
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load config", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());

            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save config", e);
        }
    }

    public static FabricConfig get() {
        return config;
    }
}
