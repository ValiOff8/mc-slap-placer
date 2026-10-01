package com.valithedev.slabplacer.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigStoreTest {
    @TempDir
    Path directory;

    @Test
    void missingConfigurationUsesDefaultsWithoutCreatingFiles() {
        Path file = directory.resolve("missing.json");
        assertEquals(PlacementMode.NO_FUNCTION, new ConfigStore(file).load().getMode());
        assertFalse(Files.exists(file));
    }

    @ParameterizedTest
    @EnumSource(PlacementMode.class)
    void roundTripPersistsExactlyOneModeAndCreatesParentDirectories(PlacementMode mode) throws IOException {
        Path file = directory.resolve("config/slabplacer.json");
        ConfigStore store = new ConfigStore(file);
        assertTrue(store.save(new SlabPlacerConfig(mode)));
        assertEquals(mode, store.load().getMode());
        var data = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        assertEquals(Set.of("mode"), data.keySet());
        assertEquals(mode.name(), data.get("mode").getAsString());
    }

    @Test
    void repeatedSaveReplacesTheModeAndLeavesNoTemporaryFiles() throws IOException {
        Path file = directory.resolve("slabplacer.json");
        ConfigStore store = new ConfigStore(file);
        assertTrue(store.save(new SlabPlacerConfig(PlacementMode.ONLY_TOP)));
        assertTrue(store.save(new SlabPlacerConfig(PlacementMode.PREVENT_DOUBLE)));
        assertEquals(PlacementMode.PREVENT_DOUBLE, store.load().getMode());
        try (var files = Files.list(directory)) {
            assertEquals(Set.of("slabplacer.json"), files.map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "null", "[]", "true", "{", "{\"mode\": }",
            "{\"mode\": 42}", "{\"mode\": true}", "{\"mode\": null}",
            "{\"mode\": \"UNKNOWN\"}", "{\"mode\": \"ONLY_TOP\"} {}",
            "{mode: 'ONLY_TOP'}", "{\"onlyTopSlabs\": 42}"
    })
    void malformedConfigurationUsesDefaultsAndPreservesTheOriginal(String json) throws IOException {
        Path file = directory.resolve("slabplacer.json");
        Files.writeString(file, json);
        assertEquals(PlacementMode.NO_FUNCTION, new ConfigStore(file).load().getMode());
        assertEquals(json, Files.readString(file));
    }

    @Test
    void missingFieldsUseDefaultsAndUnknownFieldsAreIgnored() throws IOException {
        Path file = directory.resolve("slabplacer.json");
        Files.writeString(file, "{\"futureOption\": 123}");
        assertEquals(PlacementMode.NO_FUNCTION, new ConfigStore(file).load().getMode());
        Files.writeString(file, "{\"mode\": \"ONLY_BOTTOM\", \"futureOption\": 123}");
        assertEquals(PlacementMode.ONLY_BOTTOM, new ConfigStore(file).load().getMode());
    }

    @Test
    void allLegacyCombinationsMigrateToTheirEffectiveModeWithoutRewritingTheFile() throws IOException {
        Path file = directory.resolve("slabplacer.json");
        PlacementMode[] expectedActiveModes = {
                PlacementMode.NO_FUNCTION, PlacementMode.ONLY_TOP,
                PlacementMode.ONLY_BOTTOM, PlacementMode.ONLY_TOP,
                PlacementMode.PREVENT_DOUBLE, PlacementMode.ONLY_TOP,
                PlacementMode.ONLY_BOTTOM, PlacementMode.ONLY_TOP
        };
        for (int mask = 0; mask < 16; mask++) {
            String json = """
                    {"onlyTopSlabs": %s, "onlyBottomSlabs": %s,
                     "preventDoubleSlabs": %s, "noFunction": %s}
                    """.formatted((mask & 1) != 0, (mask & 2) != 0,
                    (mask & 4) != 0, (mask & 8) != 0);
            Files.writeString(file, json);
            PlacementMode expected = mask < 8 ? expectedActiveModes[mask] : PlacementMode.NO_FUNCTION;
            assertEquals(expected, new ConfigStore(file).load().getMode(), "Legacy mask " + mask);
            assertEquals(json, Files.readString(file));
        }
    }

    @Test
    void legacyFileWithoutBypassFlagKeepsTheOriginalVanillaDefault() throws IOException {
        Path file = directory.resolve("slabplacer.json");
        Files.writeString(file, "{\"preventDoubleSlabs\": true}");
        assertEquals(PlacementMode.NO_FUNCTION, new ConfigStore(file).load().getMode());
    }

    @Test
    void savingAMigratedConfigurationRemovesTheLegacyFlags() throws IOException {
        Path file = directory.resolve("slabplacer.json");
        Files.writeString(file, """
                {"onlyTopSlabs": false, "onlyBottomSlabs": true,
                 "preventDoubleSlabs": true, "noFunction": false}
                """);
        ConfigStore store = new ConfigStore(file);
        assertTrue(store.save(store.load()));
        assertEquals(PlacementMode.ONLY_BOTTOM, store.load().getMode());
        assertEquals(Set.of("mode"), JsonParser.parseString(Files.readString(file)).getAsJsonObject().keySet());
    }

    @Test
    void explicitModeTakesPrecedenceOverLegacyFlags() throws IOException {
        Path file = directory.resolve("slabplacer.json");
        Files.writeString(file, """
                {"mode": "PREVENT_DOUBLE", "onlyTopSlabs": true, "noFunction": true}
                """);
        assertEquals(PlacementMode.PREVENT_DOUBLE, new ConfigStore(file).load().getMode());
    }

    @Test
    void readFailureUsesDefaults() {
        assertEquals(PlacementMode.NO_FUNCTION, new ConfigStore(directory).load().getMode());
    }

    @Test
    void writeFailureIsReportedWithoutChangingExistingFiles() throws IOException {
        Path parentFile = directory.resolve("not-a-directory");
        Files.writeString(parentFile, "existing content");
        ConfigStore store = new ConfigStore(parentFile.resolve("slabplacer.json"));
        assertFalse(store.save(new SlabPlacerConfig()));
        assertEquals("existing content", Files.readString(parentFile));
    }
}
