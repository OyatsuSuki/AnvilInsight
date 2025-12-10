package oyatsu.baka.anvilinsight.client.helpers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConflictHelper {

    public static List<Text> getConflicts(ItemStack input1, ItemStack input2) {
        List<Text> conflicts = new ArrayList<>();

        if (input1.isEmpty() || input2.isEmpty()) return conflicts;

        ItemEnchantmentsComponent enchants1 = getEnchantments(input1);
        ItemEnchantmentsComponent enchants2 = getEnchantments(input2);

        if (enchants1.isEmpty() || enchants2.isEmpty()) return conflicts;

        Map<Enchantment, List<Text>> byLeft = new LinkedHashMap<>();
        Map<Enchantment, List<Text>> byRight = new LinkedHashMap<>();

        for (var entry2 : enchants2.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> e2Reg = entry2.getKey();
            Enchantment e2 = e2Reg.value();

            for (var entry1 : enchants1.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> e1Reg = entry1.getKey();
                Enchantment e1 = e1Reg.value();

                if (e1Reg.equals(e2Reg)) continue;

                if (!Enchantment.canBeCombined(e1Reg, e2Reg)) {
                    Text t1 = e1.description();
                    Text t2 = e2.description();
                    addToMap(byLeft, e1, t2);
                    addToMap(byRight, e2, t1);
                }
            }
        }

        boolean useLeftMap = byLeft.size() <= byRight.size();
        Map<Enchantment, List<Text>> finalMap = useLeftMap ? byLeft : byRight;

        for (Map.Entry<Enchantment, List<Text>> entry : finalMap.entrySet()) {
            Enchantment keyEnch = entry.getKey();
            List<Text> valList = entry.getValue();

            if (useLeftMap) {
                MutableText header = Text.empty()
                        .append(keyEnch.description())
                        .append(" ↔")
                        .formatted(Formatting.RED, Formatting.BOLD);
                conflicts.add(header);

                for (Text rightText : valList) {
                    conflicts.add(Text.literal("  ✖ ").append(rightText).formatted(Formatting.RED));
                }
            }
            else {
                for (Text leftText : valList) {
                    MutableText header = Text.empty()
                            .append(leftText)
                            .append(" ↔")
                            .formatted(Formatting.RED, Formatting.BOLD);
                    conflicts.add(header);
                }
                conflicts.add(Text.literal("  ✖ ").append(keyEnch.description()).formatted(Formatting.RED));
            }
        }

        return conflicts;
    }

    private static void addToMap(Map<Enchantment, List<Text>> map, Enchantment key, Text value) {
        List<Text> list = map.computeIfAbsent(key, k -> new ArrayList<>());
        String newValStr = value.getString();
        boolean exists = false;
        for (Text t : list) {
            if (t.getString().equals(newValStr)) {
                exists = true;
                break;
            }
        }
        if (!exists) list.add(value);
    }

    private static ItemEnchantmentsComponent getEnchantments(ItemStack stack) {
        if (stack.isOf(Items.ENCHANTED_BOOK)) {
            return stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        }
        return stack.getEnchantments();
    }
}