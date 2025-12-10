package oyatsu.baka.anvilinsight.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oyatsu.baka.anvilinsight.client.*;
import oyatsu.baka.anvilinsight.client.helpers.*;

import java.util.List;

@Mixin(AnvilScreen.class)
public abstract class MixinAnvilScreen extends ForgingScreen<AnvilScreenHandler> {

    @Unique private DragTarget currentDragTarget = DragTarget.NONE;
    @Unique private int dragClickX = 0;
    @Unique private int dragClickY = 0;
    @Unique private int startX = 0;
    @Unique private int startY = 0;

    @Unique private static final int BAR_WIDTH = 140;
    @Unique private static final int BAR_HEIGHT = 8;

    public MixinAnvilScreen(AnvilScreenHandler handler, PlayerInventory playerInventory, Text title, Identifier texture) {
        super(handler, playerInventory, title, texture);
    }

    @Inject(method = "drawForeground", at = @At("TAIL"))
    private void drawRepairCostBarDraggable(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (!Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.enableAnvilGlobal)) return;

        ItemStack inputStack = this.handler.getSlot(0).getStack();
        ItemStack outputStack = this.handler.getSlot(2).getStack();
        ItemStack targetStack = outputStack.isEmpty() ? inputStack : outputStack;

        // 惩罚进度条
        if (Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.showAnvilBar) && RepairCostHelper.shouldShow(targetStack)) {
            int cost = RepairCostHelper.getRepairCost(targetStack);
            int centerX = (this.backgroundWidth - BAR_WIDTH) / 2;

            // 相对坐标
            int renderX = centerX + AnvilInsightConfig.INSTANCE.xOffset;
            int renderY = AnvilInsightConfig.INSTANCE.yOffset;

            // 拖拽高亮
            boolean isBarDragging = (currentDragTarget == DragTarget.BAR);
            int borderColor = isBarDragging ? 0xFFFFFF55 : 0xFF222222;

            context.fill(renderX - 1, renderY - 1, renderX + BAR_WIDTH + 1, renderY + BAR_HEIGHT + 1, borderColor);
            context.fill(renderX, renderY, renderX + BAR_WIDTH, renderY + BAR_HEIGHT, 0xFF333333);

            float progress = RepairCostHelper.getProgress(cost);
            if (cost > RepairCostHelper.MAX_LEVEL) progress = 1.0f;
            int filledWidth = (int) (BAR_WIDTH * progress);
            int color = RepairCostHelper.getColor(cost) | 0xFF000000;
            context.fill(renderX, renderY, renderX + filledWidth, renderY + BAR_HEIGHT, color);

            // 文字 & 预测
            String barText = "";
            if (Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.showPercentage)) {
                barText = (int) (RepairCostHelper.getProgress(cost) * 100) + "%";
            }

            boolean isResultMode = !outputStack.isEmpty();
            if (!isResultMode && this.handler.getLevelCost() > 0) {
                int nextCost = RepairCostHelper.getNextCost(cost);
                float nextProgress = RepairCostHelper.getProgress(nextCost);
                int predictionStartX = renderX + filledWidth;
                int predictionWidth = (int) (BAR_WIDTH * nextProgress) - filledWidth;
                if (predictionStartX + predictionWidth > renderX + BAR_WIDTH) predictionWidth = (renderX + BAR_WIDTH) - predictionStartX;
                if (predictionWidth > 0) {
                    context.fill(predictionStartX, renderY, predictionStartX + predictionWidth, renderY + BAR_HEIGHT, (RepairCostHelper.getColor(nextCost) & 0x00FFFFFF) | 0x80000000);
                }
                if (Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.showPercentage)) barText += " -> " + (int) (RepairCostHelper.getProgress(nextCost) * 100) + "%";
            }

            context.createNewRootLayer();
            if (!barText.isEmpty()) {
                int textWidth = this.textRenderer.getWidth(barText);
                context.drawTextWithShadow(this.textRenderer, barText, renderX + (BAR_WIDTH - textWidth) / 2, renderY + (BAR_HEIGHT - 8) / 2 + 1, 0xFFFFFFFF);
            }

            // Tooltip
            if (currentDragTarget == DragTarget.NONE) {
                int relMouseX = mouseX - this.x;
                int relMouseY = mouseY - this.y;
                if (relMouseX >= renderX && relMouseX <= renderX + BAR_WIDTH && relMouseY >= renderY && relMouseY <= renderY + BAR_HEIGHT) {
                    AnvilInsightConfig.TooltipMode mode = AnvilInsightConfig.INSTANCE.tooltipMode;
                    boolean shouldShow = (mode == AnvilInsightConfig.TooltipMode.ALWAYS) || (mode == AnvilInsightConfig.TooltipMode.ADVANCED_ONLY && this.client.options.advancedItemTooltips);
                    if (shouldShow) {
                        context.createNewRootLayer();
                        Text title = Text.translatable("gui.anvil_insight.repair_cost_level").formatted(Formatting.GOLD);
                        Text value = isResultMode ?
                                Text.translatable("gui.anvil_insight.result_penalty").formatted(Formatting.GRAY).append(Text.literal(cost + " / " + RepairCostHelper.MAX_LEVEL).formatted(RepairCostHelper.getColorFormat(cost))) :
                                Text.translatable("gui.anvil_insight.base_penalty").formatted(Formatting.GRAY).append(Text.literal(cost + " / " + RepairCostHelper.MAX_LEVEL).formatted(RepairCostHelper.getColorFormat(cost)));
                        context.drawTooltip(this.textRenderer, List.of(title, value), mouseX, mouseY);
                    }
                }
            }
        }

        // XP 消耗
        if (Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.showXpCost) && this.handler.getLevelCost() > 0) {
            int levelCost = this.handler.getLevelCost();
            Text costText = Text.translatable("container.repair.cost", levelCost);
            int costTextWidth = this.textRenderer.getWidth(costText);
            int textX1 = this.backgroundWidth - 8 - costTextWidth;
            int relMouseX = mouseX - this.x;
            int relMouseY = mouseY - this.y;
            if (relMouseX >= textX1 - 2 && relMouseX <= this.backgroundWidth - 8 && relMouseY >= 69 && relMouseY <= 78) {
                int xpPoints = XPHelper.calculateXpCost(levelCost);
                context.createNewRootLayer();
                context.drawTooltip(this.textRenderer, Text.translatable("gui.anvil_insight.xp_cost", xpPoints).formatted(Formatting.AQUA), mouseX, mouseY);
            }
        }

        // 附魔冲突警告
        if (Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.showConflictWarning)) {
            ItemStack rightStack = this.handler.getSlot(1).getStack();
            if (!inputStack.isEmpty() && !rightStack.isEmpty()) {
                List<Text> conflicts = ConflictHelper.getConflicts(inputStack, rightStack);
                if (!conflicts.isEmpty()) {
                    String warningIcon = "⚠";
                    int iconWidth = this.textRenderer.getWidth(warningIcon);

                    int iconX = 113 - (iconWidth / 2) + AnvilInsightConfig.INSTANCE.iconXOffset;
                    int iconY = 67 + AnvilInsightConfig.INSTANCE.iconYOffset;

                    context.createNewRootLayer();

                    boolean isIconDragging = (currentDragTarget == DragTarget.ICON);
                    int color = isIconDragging ? 0xFFFFFFFF : ((System.currentTimeMillis() / 500 % 2 == 0) ? 0xFFFFAA00 : 0xFFFF5555);

                    context.drawTextWithShadow(this.textRenderer, warningIcon, iconX, iconY, color);

                    if (isIconDragging) {
                        context.drawBorder(iconX - 2, iconY - 2, iconWidth + 4, 12, 0xFFFFFFFF);
                    }

                    if (currentDragTarget == DragTarget.NONE) {
                        int relMouseX = mouseX - this.x;
                        int relMouseY = mouseY - this.y;
                        if (relMouseX >= iconX - 2 && relMouseX <= iconX + iconWidth + 2 && relMouseY >= iconY - 2 && relMouseY <= iconY + 10) {
                            context.createNewRootLayer();
                            List<Text> tooltipLines = new java.util.ArrayList<>();
                            tooltipLines.add(Text.translatable("gui.anvil_insight.conflict_title").formatted(Formatting.RED, Formatting.BOLD));
                            tooltipLines.addAll(conflicts);
                            context.drawTooltip(this.textRenderer, tooltipLines, mouseX, mouseY);
                        }
                    }
                }
            }
        }
    }

    // === 统一拖拽逻辑 ===
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.enableDragging) ||
                !Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.enableAnvilGlobal)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 0) { // 左键
            ItemStack inputStack = this.handler.getSlot(0).getStack();

            // 检测进度条点击
            if (Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.showAnvilBar) &&
                    (RepairCostHelper.shouldShow(inputStack) || RepairCostHelper.shouldShow(this.handler.getSlot(2).getStack()))) {

                // 计算绝对坐标判定区
                int barCenterX = (this.backgroundWidth - BAR_WIDTH) / 2;
                int absBarX = this.x + barCenterX + AnvilInsightConfig.INSTANCE.xOffset;
                int absBarY = this.y + AnvilInsightConfig.INSTANCE.yOffset;

                int hitBoxExpand = 3;
                if (mouseX >= absBarX && mouseX <= absBarX + BAR_WIDTH &&
                        mouseY >= absBarY - hitBoxExpand && mouseY <= absBarY + BAR_HEIGHT + hitBoxExpand) {

                    currentDragTarget = DragTarget.BAR;
                    dragClickX = (int) mouseX;
                    dragClickY = (int) mouseY;
                    startX = AnvilInsightConfig.INSTANCE.xOffset;
                    startY = AnvilInsightConfig.INSTANCE.yOffset;
                    return true;
                }
            }

            // 检测图标点击
            if (Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.showConflictWarning) &&
                    !inputStack.isEmpty() && !this.handler.getSlot(1).getStack().isEmpty()) {

                // 只有存在冲突时图标才存在，才允许点击
                if (!ConflictHelper.getConflicts(inputStack, this.handler.getSlot(1).getStack()).isEmpty()) {
                    String warningIcon = "⚠";
                    int iconWidth = this.textRenderer.getWidth(warningIcon);

                    // 计算绝对坐标判定区
                    int absIconX = this.x + 113 - (iconWidth / 2) + AnvilInsightConfig.INSTANCE.iconXOffset;
                    int absIconY = this.y + 67 + AnvilInsightConfig.INSTANCE.iconYOffset;

                    if (mouseX >= absIconX - 2 && mouseX <= absIconX + iconWidth + 2 &&
                            mouseY >= absIconY - 2 && mouseY <= absIconY + 10) {

                        currentDragTarget = DragTarget.ICON;
                        dragClickX = (int) mouseX;
                        dragClickY = (int) mouseY;
                        startX = AnvilInsightConfig.INSTANCE.iconXOffset;
                        startY = AnvilInsightConfig.INSTANCE.iconYOffset;
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.enableDragging)) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

        int dx = (int) mouseX - dragClickX;
        int dy = (int) mouseY - dragClickY;

        if (currentDragTarget == DragTarget.BAR) {
            AnvilInsightConfig.INSTANCE.xOffset = startX + dx;
            AnvilInsightConfig.INSTANCE.yOffset = startY + dy;
            return true;
        } else if (currentDragTarget == DragTarget.ICON) {
            AnvilInsightConfig.INSTANCE.iconXOffset = startX + dx;
            AnvilInsightConfig.INSTANCE.iconYOffset = startY + dy;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (currentDragTarget != DragTarget.NONE && button == 0) {
            currentDragTarget = DragTarget.NONE;
            AnvilInsightConfig.INSTANCE.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}