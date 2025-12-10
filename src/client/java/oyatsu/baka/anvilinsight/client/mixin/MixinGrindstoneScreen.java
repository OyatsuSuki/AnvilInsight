package oyatsu.baka.anvilinsight.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oyatsu.baka.anvilinsight.client.AnvilInsightConfig;
import oyatsu.baka.anvilinsight.client.helpers.RepairCostHelper;
import oyatsu.baka.anvilinsight.client.helpers.XPHelper;

@Mixin(GrindstoneScreen.class)
public abstract class MixinGrindstoneScreen extends HandledScreen<GrindstoneScreenHandler> {

    @Unique private int animationTimer = 0;
    @Unique private int snapshotCost = 0;
    @Unique private boolean wasOutputFull = false;
    @Unique private int lastTickMaxCost = 0;
    @Unique private boolean lastTickRelevant = false;
    @Unique private static final int ANIMATION_DURATION = 10;

    public MixinGrindstoneScreen(GrindstoneScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.handler == null) return;
        if (!AnvilInsightConfig.INSTANCE.enableGrindstoneGlobal) return;

        ItemStack input1 = this.handler.getSlot(0).getStack();
        ItemStack input2 = this.handler.getSlot(1).getStack();
        ItemStack output = this.handler.getSlot(2).getStack();

        int cost1 = RepairCostHelper.getRepairCost(input1);
        int cost2 = RepairCostHelper.getRepairCost(input2);
        int currentMaxCost = Math.max(cost1, cost2);

        boolean isOutputFull = !output.isEmpty();
        boolean areInputsEmpty = input1.isEmpty() && input2.isEmpty();

        if (wasOutputFull && !isOutputFull && areInputsEmpty) {
            ItemStack cursorStack = this.handler.getCursorStack();
            int cursorCost = RepairCostHelper.getRepairCost(cursorStack);
            // 防止撤回操作触发动画
            boolean isCancelOperation = !cursorStack.isEmpty() && cursorCost >= lastTickMaxCost;

            if (lastTickMaxCost > 0 && lastTickRelevant && !isCancelOperation) {
                this.snapshotCost = lastTickMaxCost;
                this.animationTimer = ANIMATION_DURATION;
            }
        }

        this.wasOutputFull = isOutputFull;
        if (isOutputFull) {
            this.lastTickMaxCost = currentMaxCost;
            this.lastTickRelevant = RepairCostHelper.shouldShow(input1) || RepairCostHelper.shouldShow(input2);
        }

        if (animationTimer > 0) {
            animationTimer--;
        }
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void drawFadeOutAnimation(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!AnvilInsightConfig.INSTANCE.enableGrindstoneGlobal || !AnvilInsightConfig.INSTANCE.showGrindstoneHint) return;
        if (animationTimer <= 0) return;

        float progress = (float) animationTimer / ANIMATION_DURATION;
        float alpha = progress > 0.6f ? 1.0f : progress / 0.6f;
        int alphaInt = (int) (alpha * 255);
        if (alphaInt <= 0) return;

        int centerX = this.x + 128;
        int centerY = this.y + 35;

        context.createNewRootLayer();

        Text line1 = Text.translatable("gui.anvil_insight.grindstone_reset").formatted(Formatting.GREEN);
        int textWidth = this.textRenderer.getWidth(line1);
        int finalTextColor = (alphaInt << 24) | 0x55FF55;
        context.drawTextWithShadow(this.textRenderer, line1, centerX - (textWidth / 2), centerY - 15, finalTextColor);

        int barWidth = 60;
        int barHeight = 5;
        int barX = centerX - (barWidth / 2);
        int barY = centerY - 5;

        context.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, (alphaInt << 24) | 0x222222);
        context.fill(barX, barY, barX + barWidth, barY + barHeight, (alphaInt << 24) | 0x333333);

        float barProgress = RepairCostHelper.getProgress(snapshotCost);
        int filledWidth = (int) (barWidth * barProgress);
        context.fill(barX, barY, barX + filledWidth, barY + barHeight, (alphaInt << 24) | RepairCostHelper.getColor(snapshotCost));

        String dropText = "-" + snapshotCost;
        int dropTextColor = (alphaInt << 24) | 0xFF5555;
        context.drawTextWithShadow(this.textRenderer, dropText, barX + barWidth + 4, barY - 1, dropTextColor);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);

        if (!AnvilInsightConfig.INSTANCE.enableGrindstoneGlobal || !AnvilInsightConfig.INSTANCE.showGrindstoneXp) return;

        ItemStack output = this.handler.getSlot(2).getStack();
        if (output.isEmpty()) return;

        ItemStack input1 = this.handler.getSlot(0).getStack();
        ItemStack input2 = this.handler.getSlot(1).getStack();

        // 计算经验值
        int power1 = XPHelper.getGrindstoneBasePower(input1);
        int power2 = XPHelper.getGrindstoneBasePower(input2);

        int totalPower = power1 + power2;

        if (totalPower > 0) {
            int[] range = XPHelper.calculateGrindstoneXpRange(totalPower);
            int minTotal = range[0];
            int maxTotal = range[1];

            String labelValue;

            if (minTotal != maxTotal) {
                labelValue = String.format("%d~%d", minTotal, maxTotal);
            } else {
                labelValue = "" + maxTotal;
            }

            Text labelText = Text.translatable("gui.anvil_insight.grindstone_label", labelValue).formatted(Formatting.GREEN);
            int textWidth = this.textRenderer.getWidth(labelText);
            int x = 137 - (textWidth / 2);
            int y = 58;

            context.createNewRootLayer();
            context.fill(x - 2, y - 2, x + textWidth + 2, y + 10, 1325400064);

            context.drawTextWithShadow(this.textRenderer, labelText, x, y, 0xFFFFFFFF);
        }
    }
}