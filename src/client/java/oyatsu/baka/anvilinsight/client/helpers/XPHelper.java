package oyatsu.baka.anvilinsight.client.helpers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;

public class XPHelper {

    // === 铁砧消耗计算 ===
    public static int calculateXpCost(int levelCost) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return 0;
        int currentLevel = player.experienceLevel;
        if (currentLevel < levelCost) {
            return calculateXpRange(0, levelCost);
        }
        return calculateXpRange(currentLevel - levelCost, currentLevel);
    }

    private static int calculateXpRange(int startLevel, int endLevel) {
        int totalXp = 0;
        for (int i = startLevel; i < endLevel; i++) {
            totalXp += getXpToNextLevel(i);
        }
        return totalXp;
    }

    private static int getXpToNextLevel(int level) {
        if (level >= 31) return 9 * level - 158;
        else if (level >= 16) return 5 * level - 38;
        else return 2 * level + 7;
    }

    // === 砂轮获得计算 ===

    /**
     * 返回非诅咒附魔的 MinPower 总和。这是计算经验值的基础 "Power" 值。
     * 对应 GrindstoneScreenHandler.getExperience(ItemStack) 的返回值 i.
     */
    public static int getGrindstoneBasePower(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        ItemEnchantmentsComponent enchantments = stack.getEnchantments();
        if (stack.isOf(Items.ENCHANTED_BOOK)) {
            ItemEnchantmentsComponent stored = stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            if (!stored.isEmpty()) {
                enchantments = stored;
            }
        }

        if (enchantments.isEmpty()) return 0;

        int totalPower = 0;

        for (var entry : enchantments.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> enchantmentReg = entry.getKey();
            int level = entry.getIntValue();
            if (enchantmentReg.isIn(net.minecraft.registry.tag.EnchantmentTags.CURSE)) {
                continue;
            }

            // 修正：原版使用 getMinPower(level)
            totalPower += enchantmentReg.value().getMinPower(level);
        }

        return totalPower;
    }

    /**
     * 根据总的附魔 Power 值计算最终获得的经验值范围 [最小经验值, 最大经验值]
     * 公式: j = ceil(totalPower / 2.0). 经验值范围: [j, 2j - 1]
     */
    public static int[] calculateGrindstoneXpRange(int totalPower) {
        if (totalPower <= 0) return new int[]{0, 0};

        // j = (int)Math.ceil((double)i / (double)2.0F);
        int j = (int) Math.ceil((double)totalPower / 2.0);

        // Min XP = j
        int minXp = j;
        // Max XP = j + (j - 1) = 2j - 1
        int maxXp = 2 * j - 1;

        return new int[]{minXp, maxXp};
    }
}