package com.aztech.ez_stock_ticker;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class CreateEasyStockTickerClient {
    public static void onClientSetup(FMLClientSetupEvent event) {
        //Preload the mixin to the stock keeper request screen
        StockKeeperRequestScreen.hasShiftDown();

        ModLoadingContext.get().getActiveContainer().registerExtensionPoint(IConfigScreenFactory.class,
                (ignored, parent) -> new ConfigurationScreen(CreateEasyStockTicker.MOD_CONTAINER, parent)
        );
    }

}
