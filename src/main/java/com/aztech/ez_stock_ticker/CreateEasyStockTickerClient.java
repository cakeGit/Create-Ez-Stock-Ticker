package com.aztech.ez_stock_ticker;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateEasyStockTickerClient {
    public static void onClientSetup(FMLClientSetupEvent event) {
        //Preload the mixin to the stock keeper request screen
        StockKeeperRequestScreen.hasShiftDown();
    }
}
