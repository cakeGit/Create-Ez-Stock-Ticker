package com.aztech.ez_stock_ticker;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateEasyStockTicker.MOD_ID)
public class CreateEasyStockTicker {

    public static final String MOD_ID = "create_ez_stock_ticker";
    public static final ModContainer MOD_CONTAINER = ModList.get().getModContainerById(MOD_ID).orElseThrow();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_CONTAINER.getModInfo().getDisplayName());

    public CreateEasyStockTicker(IEventBus eventBus, ModContainer modContainer) {
        attemptConfigMigration();
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC);
        eventBus.addListener(CreateEasyStockTickerClient::onClientSetup);
    }

    public static ResourceLocation asResource(String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }

    @Deprecated(forRemoval = true)
    private static void attemptConfigMigration() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("create_ez_stock_ticker-client.toml");
        if (!Files.exists(configPath)) return;

        try (CommentedFileConfig config = CommentedFileConfig.of(configPath)) {
            config.load();

            if (!config.getOrElse("ez_stock_ticker_enabled", true)) {
                ClientConfig.CONFIG_SPEC.correct(config);

                config.set("auto_focus_search_enabled", false);
                config.set("scroll_snapping_enabled", false);
                config.set("right_click_divide_enabled", false);
                config.set("prevent_stack_deletion_enabled", false);
                config.save();
                LOGGER.info("Config changes migrated");
            }
        } catch (Exception e) {
            LOGGER.error("Config migration errored", e);
        }
    }

}
