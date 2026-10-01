package com.valithedev.slabplacer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.Strictness;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Reads settings defensively and replaces the saved file only after a complete write. */
public final class ConfigStore {
    private static final Logger LOGGER = Logger.getLogger(ConfigStore.class.getName());
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setStrictness(Strictness.STRICT)
            .create();

    private final Path path;

    public ConfigStore(Path path) {
        this.path = Objects.requireNonNull(path, "path").toAbsolutePath().normalize();
    }

    /** Returns vanilla defaults if the file does not exist or cannot be read. */
    public SlabPlacerConfig load() {
        if (!Files.exists(path)) {
            return new SlabPlacerConfig();
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject data = GSON.fromJson(reader, JsonObject.class);
            if (data == null) {
                throw new JsonParseException("The configuration must be a JSON object.");
            }
            return new SlabPlacerConfig(readMode(data));
        } catch (IOException | JsonParseException | IllegalStateException exception) {
            LOGGER.log(Level.WARNING, "Could not read Slab Placer settings from " + path
                    + "; using defaults. The existing file was left unchanged.", exception);
            return new SlabPlacerConfig();
        }
    }

    private static PlacementMode readMode(JsonObject data) {
        if (data.has("mode")) {
            if (!(data.get("mode") instanceof JsonPrimitive value) || !value.isString()) {
                throw new JsonParseException("The placement mode must be a string.");
            }
            try {
                return PlacementMode.valueOf(value.getAsString());
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException("Unknown placement mode.", exception);
            }
        }

        // Preserve the effective behavior of configuration files from version 1.0.x.
        boolean top = readLegacyBoolean(data, "onlyTopSlabs", false);
        boolean bottom = readLegacyBoolean(data, "onlyBottomSlabs", false);
        boolean preventDouble = readLegacyBoolean(data, "preventDoubleSlabs", false);
        boolean noFunction = readLegacyBoolean(data, "noFunction", true);
        if (noFunction) {
            return PlacementMode.NO_FUNCTION;
        }
        if (top) {
            return PlacementMode.ONLY_TOP;
        }
        if (bottom) {
            return PlacementMode.ONLY_BOTTOM;
        }
        return preventDouble ? PlacementMode.PREVENT_DOUBLE : PlacementMode.NO_FUNCTION;
    }

    private static boolean readLegacyBoolean(JsonObject data, String name, boolean defaultValue) {
        if (!data.has(name)) {
            return defaultValue;
        }
        if (!(data.get(name) instanceof JsonPrimitive value) || !value.isBoolean()) {
            throw new JsonParseException("The legacy setting " + name + " must be a boolean.");
        }
        return value.getAsBoolean();
    }

    /** Returns false on a write failure so the settings screen can retain the unsaved draft. */
    public boolean save(SlabPlacerConfig config) {
        Objects.requireNonNull(config, "config");
        Path temporaryFile = null;
        try {
            Files.createDirectories(path.getParent());
            temporaryFile = Files.createTempFile(path.getParent(), "slab-placer-", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
                writer.write(System.lineSeparator());
            }
            try {
                Files.move(temporaryFile, path, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | JsonParseException exception) {
            LOGGER.log(Level.WARNING, "Could not save Slab Placer settings to " + path, exception);
            return false;
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException exception) {
                    LOGGER.log(Level.WARNING, "Could not remove temporary Slab Placer settings file "
                            + temporaryFile, exception);
                }
            }
        }
    }
}
