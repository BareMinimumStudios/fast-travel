package net.fasttravel.config;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fasttravel.FastTravelMain;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

public class FastTravelConfig {

    public static ConfigClassHandler<FastTravelConfig> CONFIG = ConfigClassHandler.createBuilder(FastTravelConfig.class)
            .id(FastTravelMain.identifierOf("fasttravel"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("fasttravel.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting).setJson5(true).build()).build();

    @SerialEntry(comment = "time in ticks")
    @AutoGen(category = "main")
    @IntField
    public int teleportTime = 100;

    @SerialEntry(comment = "time in ticks")
    @AutoGen(category = "main")
    @IntField
    public int afterTeleportTime = 60;

    @SerialEntry(comment = "time in ticks")
    @AutoGen(category = "main")
    @IntField
    public int teleportCooldown = 100;

    @SerialEntry(comment = "map exploration chunk radius")
    @AutoGen(category = "main")
    @IntField
    public int chunkExplorationRadius = 6;

    @SerialEntry(comment = "render fog overlay on map")
    @AutoGen(category = "main")
    @IntField
    public boolean mapFogOverlay = true;

    @SerialEntry(comment = "monolith player check radius")
    @AutoGen(category = "main")
    @IntField
    public int monolithExploreRadius = 6;

    public static void load() {
        CONFIG.load();
    }

    public static void save() {
        CONFIG.save();
    }


    public static Screen configScreen(Screen parent) {
        return CONFIG.generateGui().generateScreen(parent);
    }
}
