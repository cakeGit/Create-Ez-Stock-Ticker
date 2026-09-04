package com.aztech.ez_stock_ticker;

import com.aztech.ez_stock_ticker.foundation.StackSnapping;
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

    public final ModConfigSpec.ConfigValue<Boolean> enabled;

    public final ModConfigSpec.ConfigValue<Boolean> autoFocusSearch;
    public final ModConfigSpec.ConfigValue<Boolean> scrollSnapping;
    public final ModConfigSpec.ConfigValue<Boolean> rightClickDivide;
    public final ModConfigSpec.ConfigValue<Boolean> preventStackDeletion;

    public final ModConfigSpec.ConfigValue<String> defaultSnappingExpression;
    public final ModConfigSpec.ConfigValue<String> ctrlSnappingExpression;
    public final ModConfigSpec.ConfigValue<String> shiftSnappingExpression;
    public final ModConfigSpec.ConfigValue<String> ctrlShiftSnappingExpression;

    private ClientConfig(ModConfigSpec.Builder builder) {

        enabled = builder
                .translation("ez_stock_ticker.config.enabled")
                .comment("Whether the mod is enabled or not, hope you come back soon!")
                .define("enabled", true);

        autoFocusSearch = builder
                .translation("ez_stock_ticker.config.auto_focus_search")
                .comment("Automatically focuses the search bar when opening a stock keeper")
                .define("auto_focus_search_enabled", true);

        scrollSnapping = builder
                .translation("ez_stock_ticker.config.scroll_snapping")
                .comment("Whether items should be snapped to increments when scrolling, snapping in different increments when holding Shift or Ctrl")
                .define("scroll_snapping_enabled", true);

        rightClickDivide = builder
                .translation("ez_stock_ticker.config.right_click_divide")
                .comment("Right clicking an item in the order bar divides the requested amount by 2")
                .define("right_click_divide_enabled", true);

        preventStackDeletion = builder
                .translation("ez_stock_ticker.config.prevent_stack_deletion")
                .comment("Prevents scrolling from removing an item from the order bar entirely")
                .define("prevent_stack_deletion_enabled", true);

        defaultSnappingExpression = builder
                .translation("ez_stock_ticker.config.default_snapping_expression")
                .comment("The default snapping increment for scrolling. Can be a number or a simple expression starting with 'stack', like 'stack*5', 'stack*0.25', 'stack/4'.")
                .define("default_snapping_expression", "1", (value) -> StackSnapping.isValidExpression((String) value));

        ctrlSnappingExpression = builder
                .translation("ez_stock_ticker.config.ctrl_snapping_expression")
                .comment("The snapping increment for scrolling when the Ctrl key is pressed. Can be a number or a simple expression starting with 'stack', like 'stack*5', 'stack*0.25', 'stack/4'.")
                .define("ctrl_snapping_expression", "10", (value) -> StackSnapping.isValidExpression((String) value));

        shiftSnappingExpression = builder
                .translation("ez_stock_ticker.config.shift_snapping_expression")
                .comment("The snapping increment for scrolling when the Shift key is pressed. Can be a number or a simple expression starting with 'stack', like 'stack*5', 'stack*0.25', 'stack/4'.")
                .define("shift_snapping_expression", "stack/4", (value) -> StackSnapping.isValidExpression((String) value));

        ctrlShiftSnappingExpression = builder
                .translation("ez_stock_ticker.config.ctrl_shift_snapping_expression")
                .comment("The snapping increment for scrolling when both the Ctrl and Shift keys are pressed. Can be a number or a simple expression starting with 'stack', like 'stack*5', 'stack*0.25', 'stack/4'.")
                .define("ctrl_shift_snapping_expression", "stack", (value) -> StackSnapping.isValidExpression((String) value));
    }

}
