package com.aztech.ez_stock_ticker;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {

    public static final ClientConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    //CONFIG and CONFIG_SPEC are both built from the same builder, so we use a static block to separate the properties
    static {
        Pair<ClientConfig, ModConfigSpec> pair =
            new ModConfigSpec.Builder().configure(ClientConfig::new);

        //Store the resulting values
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public final ModConfigSpec.ConfigValue<Boolean> autoFocusSearch;
    public final ModConfigSpec.ConfigValue<Boolean> scrollSnapping;
    public final ModConfigSpec.ConfigValue<Boolean> rightClickDivide;
    public final ModConfigSpec.ConfigValue<Boolean> preventStackDeletion;

    private ClientConfig(ModConfigSpec.Builder builder) {
        autoFocusSearch = builder
                .translation("ez_stock_ticker.config.auto_focus_search")
                .comment("Automatically focuses the search bar when opening a stock keeper")
                .define("auto_focus_search_enabled", true);

        scrollSnapping = builder
                .translation("ez_stock_ticker.config.scroll_snapping")
                .comment("Shift scrolls in 16-item increments")
                .define("scroll_snapping_enabled", true);

        rightClickDivide = builder
                .translation("ez_stock_ticker.config.right_click_divide")
                .comment("Right clicking an item in the order bar divides the requested amount by 2")
                .define("right_click_divide_enabled", true);

        preventStackDeletion = builder
                .translation("ez_stock_ticker.config.prevent_stack_deletion")
                .comment("Prevents scrolling from removing an item from the order bar entirely")
                .define("prevent_stack_deletion_enabled", true);
    }

}
