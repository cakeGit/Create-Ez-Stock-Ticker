package com.aztech.ez_stock_ticker;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(CreateEasyStockTicker.MOD_ID)
public class CreateEasyStockTicker {

    public static final String MOD_ID = "create_ez_stock_ticker";
    public static final String NAME = "Create: Ez Stock Ticker";

    public CreateEasyStockTicker(IEventBus eventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC);
        eventBus.addListener(CreateEasyStockTickerClient::onClientSetup);
    }

    public static ResourceLocation asResource(String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }

}
