package com.aztech.ez_stock_ticker.mixin;

import com.aztech.ez_stock_ticker.ClientConfig;
import com.aztech.ez_stock_ticker.CreateEasyStockTicker;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Mixin(value = StockKeeperRequestScreen.class, remap = false)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu>
    implements ScreenWithStencils {

    @Unique
    private static final ResourceLocation STOCK_KEEPER_PATCH = CreateEasyStockTicker.asResource("textures/gui/stock_keeper_patch.png");

    @Shadow public EditBox searchBox;

    @Shadow public boolean refreshSearchNextTick;

    @Shadow public boolean moveToTopNextTick;

    public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Shadow protected abstract void syncJEI();

    @Shadow public AddressEditBox addressBox;

    @Shadow private int itemsX;

    @Shadow @Final private int cols;

    @Shadow @Final private int colWidth;

    @Shadow private int windowHeight;

    @Shadow protected abstract Couple<Integer> getHoveredSlot(int x, int y);

    @Shadow protected abstract int getMaxScroll();

    @Shadow private boolean scrollHandleActive;

    @Shadow private boolean isAdmin;

    @Shadow public LerpedFloat itemScroll;

    @Shadow private int lockY;

    @Shadow private int lockX;

    @Shadow private boolean isLocked;

    @Shadow private StockTickerBlockEntity blockEntity;

    @Shadow protected abstract void sendIt();

    @Shadow protected abstract boolean isConfirmHovered(int mouseX, int mouseY);

    @Shadow private int itemsY;

    @Shadow public List<StockKeeperRequestScreen.CategoryEntry> categories;

    @Shadow public List<List<BigItemStack>> displayedItems;

    @Shadow @Final private int rowHeight;

    @Shadow private Set<Integer> hiddenCategories;

    @Shadow @Final private Couple<Integer> noneHovered;

    @Shadow public List<BigItemStack> itemsToOrder;

    @Shadow public List<CraftableBigItemStack> recipesToOrder;

    @Shadow public abstract void requestCraftable(CraftableBigItemStack cbis, int requestedDifference);

    @Shadow @Nullable protected abstract BigItemStack getOrderForItem(ItemStack stack);

    @Shadow @Final private static AllGuiTextures HEADER;

    @Shadow @Final private static AllGuiTextures BODY;

    @Shadow @Final private static AllGuiTextures FOOTER;

    @Inject(method = "init", at = @At("TAIL"))
    private void init_tail(CallbackInfo ci) {
        searchBox.setFocused(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
        boolean lmbClicked = pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT;

        //EZ Toggle
        Pair<Integer, Integer> ezLocation = getEzLocation();
        if (lmbClicked && pMouseX >= ezLocation.getFirst() && pMouseX <= ezLocation.getFirst() + 16 && pMouseY >= ezLocation.getSecond() && pMouseY <= ezLocation.getSecond() + 7) {
            boolean newValue = !ClientConfig.CONFIG.isEzStockTickerEnabled.get();
            ClientConfig.CONFIG.isEzStockTickerEnabled.set(newValue);
            ClientConfig.CONFIG.isEzStockTickerEnabled.save();
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "mouseClicked",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
            ordinal = 1,
            shift = At.Shift.AFTER
        )
    )
    private void mouseClickedTransferInject(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir,  @Local(name = "current") int current, @Local(name = "existingOrder") BigItemStack existingOrder, @Local(name = "transfer") LocalIntRef transfer, @Local(name = "rmb") boolean rmb) {
        boolean isEzEnabled = ClientConfig.CONFIG.isEzStockTickerEnabled.get(); //Replace with client side config
        if (rmb && isEzEnabled) {
            transfer.set(existingOrder.count == 1 ? 1 : current / 2);
            existingOrder.count = current - transfer.get(); //Apply in the updated value because bytecode shift doesent update locals in an expression
        }
    }

    /**
     * @author cake
     * @reason Patching too many lines, so just overwrite :p
     **/
    @Overwrite
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;

        Couple<Integer> hoveredSlot = getHoveredSlot((int) mouseX, (int) mouseY);
        boolean noHover = hoveredSlot == noneHovered;

        if (noHover || hoveredSlot.getFirst() >= 0 && !hasShiftDown() && getMaxScroll() != 0) {
            int maxScroll = getMaxScroll();
            int direction = (int) (Math.ceil(Math.abs(scrollY)) * -Math.signum(scrollY));
            float newTarget = Mth.clamp(Math.round(itemScroll.getChaseTarget() + direction), 0, maxScroll);
            itemScroll.chase(newTarget, 0.5, LerpedFloat.Chaser.EXP);
            return true;
        }

        boolean orderClicked = hoveredSlot.getFirst() == -1;
        boolean recipeClicked = hoveredSlot.getFirst() == -2;
        BigItemStack entry = recipeClicked ? recipesToOrder.get(hoveredSlot.getSecond())
            : orderClicked ? itemsToOrder.get(hoveredSlot.getSecond())
            : displayedItems.get(hoveredSlot.getFirst()).get(hoveredSlot.getSecond());

        boolean remove = scrollY < 0;
        int stackSnapping = hasControlDown() ? 10 : (entry.stack.getMaxStackSize() / 4);
        int transfer = Mth.ceil(Math.abs(scrollY)) * (hasControlDown() ? 10 : 1);

        if (recipeClicked && entry instanceof CraftableBigItemStack cbis) {
            requestCraftable(cbis, remove ? -transfer : transfer);
            return true;
        }

        boolean isEzEnabled = ClientConfig.CONFIG.isEzStockTickerEnabled.get();

        BigItemStack existingOrder = orderClicked ? entry : getOrderForItem(entry.stack);
        if (existingOrder == null) {
            if (itemsToOrder.size() >= cols || remove)
                return true;
            itemsToOrder.add(existingOrder = new BigItemStack(entry.stack.copyWithCount((hasShiftDown() && isEzEnabled) ? stackSnapping : 1), 0));
            playUiSound(SoundEvents.WOOL_STEP, 0.75f, 1.2f);
            playUiSound(SoundEvents.BAMBOO_WOOD_STEP, 0.75f, 0.8f);
        }

        int current = existingOrder.count;

        if (isEzEnabled) {
            if (hasShiftDown() || hasControlDown()) {
                int target = ((Math.floorDiv(current, stackSnapping) + (remove ? -1 : 1)) * stackSnapping);
                target = Math.max(1, target);
                transfer = (remove ? -1 : 1) * (target - current);
            } else if (remove) {
                //Otherwise just prevent deleting the stack
                int target = current + -transfer;
                target = Math.max(1, target);
                transfer = Math.abs(target - current);
            }
        }

        if (remove) {
            existingOrder.count = current - transfer;
            if (existingOrder.count <= 0) {
                itemsToOrder.remove(existingOrder);
                playUiSound(SoundEvents.WOOL_STEP, 0.75f, 1.8f);
                playUiSound(SoundEvents.BAMBOO_WOOD_STEP, 0.75f, 1.8f);
            } else if (existingOrder.count != current)
                playUiSound(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 0.25f, 1.2f);
            return true;
        }

        existingOrder.count = current + Math.min(transfer, blockEntity.getLastClientsideStockSnapshotAsSummary()
            .getCountOf(entry.stack) - current);

        if (existingOrder.count != current && current != 0)
            playUiSound(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 0.25f, 1.2f);

        return true;
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/gui/UIRenderHelper;swapAndBlitColor(Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/pipeline/RenderTarget;)V", shift = At.Shift.BEFORE))
    protected void renderForeground(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        //Render the "EZ" icon, shift the uv y by 7 px if enabled, 0 of enabled
        boolean isEzEnabled = ClientConfig.CONFIG.isEzStockTickerEnabled.get(); //Replace with client side config
        Pair<Integer, Integer> ezLocation = getEzLocation();
        int v = isEzEnabled ? 7 : 0;
        graphics.blit(STOCK_KEEPER_PATCH, ezLocation.getFirst(), ezLocation.getSecond(), 0, v, 16, 7);
    }
    @Inject(method = "renderForeground", at = @At("TAIL"))
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        //Render the "EZ" icon, shift the uv y by 7 px if enabled, 0 of enabled
        Pair<Integer, Integer> ezLocation = getEzLocation();

        if (mouseX >= ezLocation.getFirst() && mouseX <= ezLocation.getFirst() + 16 && mouseY >= ezLocation.getSecond() && mouseY <= ezLocation.getSecond() + 7) {
            graphics.renderComponentTooltip(font, ClientConfig.CONFIG.isEzStockTickerEnabled.get() ? List.of(
                    Component.translatable("ez_stock_ticker.gui.enabled").withStyle(ChatFormatting.GREEN),
                    Component.translatable("ez_stock_ticker.gui.enabled_description_1").withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable("ez_stock_ticker.gui.enabled_description_2").withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable("ez_stock_ticker.gui.click_to_toggle").withStyle(ChatFormatting.GRAY)
                ) : List.of(
                    Component.translatable("ez_stock_ticker.gui.disabled").withStyle(ChatFormatting.RED),
                    Component.translatable("ez_stock_ticker.gui.disabled_description_1").withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable("ez_stock_ticker.gui.disabled_description_2").withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable("ez_stock_ticker.gui.click_to_toggle").withStyle(ChatFormatting.GRAY)
                ),
                mouseX, mouseY);
        }
    }

    private Pair<Integer, Integer> getEzLocation() {
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
