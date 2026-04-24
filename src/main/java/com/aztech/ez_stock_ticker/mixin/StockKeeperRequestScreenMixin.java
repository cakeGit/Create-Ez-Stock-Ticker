
package com.aztech.ez_stock_ticker.mixin;

import com.aztech.ez_stock_ticker.ClientConfig;
import com.aztech.ez_stock_ticker.CreateEasyStockTicker;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = StockKeeperRequestScreen.class, remap = false)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu> {

    @Unique
    private static final ResourceLocation STOCK_KEEPER_PATCH = CreateEasyStockTicker.asResource("textures/gui/stock_keeper_patch.png");

    @Shadow @Final
    private static AllGuiTextures HEADER;

    @Shadow @Final
    private static AllGuiTextures BODY;

    @Shadow @Final
    private static AllGuiTextures FOOTER;

    @Shadow
    public EditBox searchBox;

    @Shadow
    int windowHeight;

    public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init_tail(CallbackInfo ci) {
        if (ClientConfig.CONFIG.autoFocusSearch.get()) {
            searchBox.setFocused(true);
        }
    }

    @Inject(
            method = "mouseClicked",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void mouseClickedTransferInject(double pMouseX,
                                            double pMouseY,
                                            int pButton,
                                            CallbackInfoReturnable<Boolean> cir,
                                            @Local(name = "current") int current,
                                            @Local(name = "existingOrder") BigItemStack existingOrder,
                                            @Local(name = "transfer") LocalIntRef transfer,
                                            @Local(name = "rmb") boolean rmb,
                                            @Local(name = "entry") BigItemStack entry) {
        if (rmb && ClientConfig.CONFIG.rightClickDivide.get()) {
            existingOrder.count = current / 2;
        }
    }

    @Inject(
        method = "mouseScrolled",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
            ordinal = 1, // This is the `existingOrder.count = current - transfer;` line
            shift = At.Shift.BY,
            by = -2
        )
    )
    private void mouseScrolled_removeItems(double mouseX,
                                           double mouseY,
                                           double scrollX,
                                           double scrollY,
                                           CallbackInfoReturnable<Boolean> cir,
                                           @Local(name = "existingOrder") BigItemStack existingOrder,
                                           @Local(name = "current") int current,
                                           @Local(name = "transfer") LocalIntRef transfer,
                                           @Local(name = "entry") BigItemStack entry) {
        int stackSnapping = entry.stack.getMaxStackSize() / 4;

        if (ClientConfig.CONFIG.scrollSnapping.get() && hasShiftDown()) {
            if (stackSnapping == 0) return; //Snap size 0 means it's a factory logistics fluid
            int target = (Math.floorDiv(current, stackSnapping) - 1) * stackSnapping;
            if (ClientConfig.CONFIG.preventStackDeletion.get()) {
                target = Math.max(1, target);
            }
            transfer.set(current - Math.max(0, target)); // Set the amount to transfer
        } else if (ClientConfig.CONFIG.preventStackDeletion.get()) {
            // Prevent scrolling to 0
            int target = current - transfer.get();
            if (target < 1) {
                transfer.set(current - 1); // Only transfer enough to reach 1
            }
        }
    }

    @Inject(
        method = "mouseScrolled",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;blockEntity:Lcom/simibubi/create/content/logistics/stockTicker/StockTickerBlockEntity;",
            shift = At.Shift.BY,
            by = -5
        )
    )
    private void mouseScrolled_addItems(double mouseX,
                                        double mouseY,
                                        double scrollX,
                                        double scrollY,
                                        CallbackInfoReturnable<Boolean> cir,
                                        @Local(name = "existingOrder") BigItemStack existingOrder,
                                        @Local(name = "current") int current,
                                        @Local(name = "transfer") LocalIntRef transfer,
                                        @Local(name = "entry") BigItemStack entry) {

        if (ClientConfig.CONFIG.scrollSnapping.get()) { //Stack size 0 means its a factory logistics fluid
            int stackSnapping = hasControlDown() ? 10 : (entry.stack.getMaxStackSize() / 4);

            if (hasShiftDown() || hasControlDown()) {
                if (stackSnapping == 0) return; //Snap size 0 means its a factory logistics fluid
                int target = ((Math.floorDiv(current, stackSnapping) + 1) * stackSnapping);
                target = Math.max(1, target);
                transfer.set(target - current); // Set the amount to transfer
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
        boolean lmbClicked = pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT;

        Pair<Integer, Integer> ezLocation = create_Ez_Stock_Ticker$getEzLocation();
        if (lmbClicked && pMouseX >= ezLocation.getFirst() && pMouseX <= ezLocation.getFirst() + 16 && pMouseY >= ezLocation.getSecond() && pMouseY <= ezLocation.getSecond() + 7) {
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
            Minecraft.getInstance().setScreen(new ConfigurationScreen(CreateEasyStockTicker.MOD_CONTAINER, this));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/gui/AllGuiTextures;render(Lnet/minecraft/client/gui/GuiGraphics;II)V", ordinal = 2, shift = At.Shift.AFTER))
    protected void renderForeground(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        Pair<Integer, Integer> ezLocation = create_Ez_Stock_Ticker$getEzLocation();
        graphics.blit(STOCK_KEEPER_PATCH, ezLocation.getFirst(), ezLocation.getSecond(), 0, 7, 16, 7);
    }

    @Inject(method = "renderForeground", at = @At("TAIL"))
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        Pair<Integer, Integer> ezLocation = create_Ez_Stock_Ticker$getEzLocation();

        if (mouseX >= ezLocation.getFirst() && mouseX <= ezLocation.getFirst() + 16 && mouseY >= ezLocation.getSecond() && mouseY <= ezLocation.getSecond() + 7) {
            graphics.renderComponentTooltip(font, List.of(
                            Component.translatable("ez_stock_ticker.gui.title").withStyle(ChatFormatting.YELLOW),
                            Component.translatable("ez_stock_ticker.gui.description").withStyle(ChatFormatting.DARK_GRAY),
                            Component.empty(),
                            Component.translatable("ez_stock_ticker.gui.click_to_configure").withStyle(ChatFormatting.GRAY)
                    ),
                    mouseX, mouseY);
        }
    }

    @Unique
    private Pair<Integer, Integer> create_Ez_Stock_Ticker$getEzLocation() {
        int x = getGuiLeft();
        int y = getGuiTop() + HEADER.getHeight() + FOOTER.getHeight();

        for (int i = 0; i < (windowHeight - HEADER.getHeight() - FOOTER.getHeight()) / BODY.getHeight(); i++) {
            y += BODY.getHeight();
        }

        int ezTooltipX = x + 13;
        int ezTooltipY = y - 13;
        return Pair.of(ezTooltipX, ezTooltipY);
    }

}