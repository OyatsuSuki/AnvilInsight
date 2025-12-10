package oyatsu.baka.anvilinsight.client.helpers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class RepairCostHelper {
    // 常量定义
    public static final int MAX_LEVEL = 40;
    public static final int DANGER_THRESHOLD = 30;
    public static final int WARNING_THRESHOLD = 10;

    public static boolean shouldShow(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.isOf(Items.ENCHANTED_BOOK)) return false;
        if (getRepairCost(stack) > 0) return true;

        return stack.isDamageable();
    }

    public static int getRepairCost(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return stack.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
    }

    public static int getNextCost(int currentCost) {
        return currentCost * 2 + 1;
    }

    // === 颜色逻辑 ===
    public static int getColor(int cost) {
        if (cost == 0) return 0xFF55FFFF; // 青色 (完美)
        if (cost >= MAX_LEVEL) return 0xFFAA0000; // 深红 (报废)

        if (cost >= DANGER_THRESHOLD) return 0xFFFF5555; // 红色
        if (cost >= WARNING_THRESHOLD) return 0xFFFFAA00; // 金色
        return 0xFF55FF55; // 绿色
    }

    public static Formatting getColorFormat(int cost) {
        if (cost == 0) return Formatting.AQUA;
        if (cost >= MAX_LEVEL) return Formatting.DARK_RED;

        if (cost >= DANGER_THRESHOLD) return Formatting.RED;
        if (cost >= WARNING_THRESHOLD) return Formatting.GOLD;
        return Formatting.GREEN;
    }

    public static float getProgress(int cost) {
        return MathHelper.clamp((float) cost / (float) MAX_LEVEL, 0f, 1f);
    }

    public static Text getTooltipText(int cost) {
        Formatting color = getColorFormat(cost);
        int percent = (int) (getProgress(cost) * 100);

        int totalBlocks = 10;
        int filledBlocks = Math.round(getProgress(cost) * totalBlocks);

        StringBuilder bar = new StringBuilder();
        bar.append(" [");

        bar.append(color.toString());
        bar.append(Formatting.BOLD.toString());
        for (int i = 0; i < filledBlocks; i++) {
            bar.append("█");
        }

        bar.append(Formatting.DARK_GRAY.toString());
        bar.append(Formatting.BOLD.toString());
        for (int i = filledBlocks; i < totalBlocks; i++) {
            bar.append("█");
        }

        bar.append(Formatting.RESET.toString());
        bar.append(Formatting.GRAY.toString());
        bar.append("]");

        return Text.translatable("gui.anvil_insight.penalty").formatted(Formatting.GRAY)
                .append(Text.literal(bar.toString()))
                .append(Text.literal(" " + percent + "%").formatted(color));
    }
}