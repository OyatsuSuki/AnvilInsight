// DurabilityHelper.java
package oyatsu.baka.anvilinsight.client.helpers;

import net.minecraft.item.ItemStack;

public class DurabilityHelper {

    /**
     * 计算铁砧操作中，成品相对于输入物品所获得的耐久度修复量 (绝对值)。
     * @param inputStack 铁砧槽位 0 的输入物品。
     * @param outputStack 铁砧槽位 2 的成品物品。
     * @return 获得的耐久度绝对值。
     */
    public static int getRepairGain(ItemStack inputStack, ItemStack outputStack) {
        if (inputStack.isEmpty() || outputStack.isEmpty()) {
            return 0;
        }

        // 只有可损坏物品才能谈论耐久修复
        if (!inputStack.isDamageable() || !outputStack.isDamageable()) {
            return 0;
        }

        // inputDamage: 实际损坏值 (MaxDamage - RemainingDurability)
        int inputDamage = inputStack.getDamage();
        int outputDamage = outputStack.getDamage();

        // 修复量 = 输入物品的损坏 - 输出物品的损坏
        // 注意：由于铁砧修复会重置一些附魔信息，我们仅依赖 Damage 值对比。
        int gain = inputDamage - outputDamage;

        // 避免出现负值或非修复操作
        return Math.max(0, gain);
    }

    /**
     * 计算耐久度修复的百分比。
     * @param inputStack 铁砧槽位 0 的输入物品。
     * @param gain 获得的耐久度绝对值。
     * @return 获得的耐久度百分比 (0-100)。
     */
    public static int getRepairGainPercentage(ItemStack inputStack, int gain) {
        if (gain <= 0 || !inputStack.isDamageable()) {
            return 0;
        }
        int maxDamage = inputStack.getMaxDamage();
        if (maxDamage <= 0) {
            return 0;
        }
        return (int) Math.round((double) gain / (double) maxDamage * 100.0);
    }
}