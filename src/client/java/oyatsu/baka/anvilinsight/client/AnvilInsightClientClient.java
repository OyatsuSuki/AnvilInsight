package oyatsu.baka.anvilinsight.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import oyatsu.baka.anvilinsight.client.helpers.RepairCostHelper;
import oyatsu.baka.anvilinsight.client.helpers.DurabilityHelper;

import java.util.List;

public class AnvilInsightClientClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AnvilInsightConfig.INSTANCE.load();

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            AnvilScreen anvilScreen = MinecraftClient.getInstance().currentScreen instanceof AnvilScreen screen ? screen : null;

            // 显示 Repair Cost Level
            AnvilInsightConfig.TooltipMode mode = AnvilInsightConfig.INSTANCE.tooltipMode;
            if (mode != AnvilInsightConfig.TooltipMode.OFF) {
                if (mode != AnvilInsightConfig.TooltipMode.ADVANCED_ONLY || type.isAdvanced()) {
                    if (RepairCostHelper.shouldShow(stack)) {
                        int cost = RepairCostHelper.getRepairCost(stack);
                        Text tooltipText = RepairCostHelper.getTooltipText(cost);
                        insertTooltipSmartly(lines, tooltipText);
                    }
                }
            }

            // 铁砧修复耐久度显示 (仅在成品槽位)
            if (Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.enableAnvilGlobal) &&
                    Boolean.TRUE.equals(AnvilInsightConfig.INSTANCE.showRepairGain) &&
                    anvilScreen != null) {
                Slot outputSlot = anvilScreen.getScreenHandler().getSlot(2);

                if (outputSlot.hasStack() && stack == outputSlot.getStack()) {
                    AnvilScreenHandler handler = anvilScreen.getScreenHandler();
                    ItemStack inputStack = handler.getSlot(0).getStack();

                    int gain = DurabilityHelper.getRepairGain(inputStack, stack);

                    if (gain > 0) {
                        int percent = DurabilityHelper.getRepairGainPercentage(inputStack, gain);

                        Text gainValueText = Text.literal(String.format(" +%d%% (+%d)", percent, gain))
                                .formatted(Formatting.GREEN);

                        Text gainText = Text.translatable("gui.anvil_insight.repair_gain")
                                .formatted(Formatting.GRAY)
                                .append(gainValueText);

                        int insertIndex = -1;
                        for (int i = lines.size() - 1; i >= 0; i--) {
                            Text line = lines.get(i);
                            if (line.getContent() instanceof TranslatableTextContent translatable) {
                                String key = translatable.getKey();
                                if (key.equals("item.durability")) {
                                    insertIndex = i + 1;
                                    break;
                                }
                            }
                        }

                        if (insertIndex != -1) {
                            lines.add(insertIndex, gainText);
                        }
                    }
                }
            }
        });
    }

    private void insertTooltipSmartly(List<Text> lines, Text tooltipToAdd) {
        int insertIndex = -1;

        for (int i = lines.size() - 1; i >= 0; i--) {
            Text line = lines.get(i);

            if (line.getContent() instanceof TranslatableTextContent translatable) {
                String key = translatable.getKey();

                if (key.startsWith("item.modifiers.")) {
                    insertIndex = i;
                    break;
                }
            }
        }

        if (insertIndex != -1) {
            lines.add(insertIndex, Text.empty());
            lines.add(insertIndex, tooltipToAdd);
        } else {
            if (lines.size() > 0) {
                lines.add(tooltipToAdd);
            } else {
                lines.add(tooltipToAdd);
            }
        }
    }
}