package oyatsu.baka.anvilinsight.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            AnvilInsightConfig config = AnvilInsightConfig.INSTANCE;

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("gui.anvil_insight.config.title"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // =================================================
            // 常规设置 (General) - 全局开关
            // =================================================
            ConfigCategory general = builder.getOrCreateCategory(Text.translatable("gui.anvil_insight.config.category.general"));

            // 铁砧总开关
            general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.enable_anvil_global"), config.enableAnvilGlobal)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("gui.anvil_insight.config.enable_anvil_global.tooltip"))
                    .setSaveConsumer(newValue -> config.enableAnvilGlobal = newValue)
                    .build());

            // 砂轮总开关
            general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.enable_grindstone_global"), config.enableGrindstoneGlobal)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("gui.anvil_insight.config.enable_grindstone_global.tooltip"))
                    .setSaveConsumer(newValue -> config.enableGrindstoneGlobal = newValue)
                    .build());

            // 物品提示框模式
            general.addEntry(entryBuilder.startEnumSelector(
                            Text.translatable("gui.anvil_insight.config.tooltip_mode"),
                            AnvilInsightConfig.TooltipMode.class,
                            config.tooltipMode
                    )
                    .setDefaultValue(AnvilInsightConfig.TooltipMode.ADVANCED_ONLY)
                    .setEnumNameProvider(mode -> Text.translatable("gui.anvil_insight.config.tooltip_mode." + mode.name().toLowerCase()))
                    .setSaveConsumer(newValue -> config.tooltipMode = newValue)
                    .build());

            // =================================================
            // 铁砧设置 (Anvil)
            // =================================================
            ConfigCategory anvil = builder.getOrCreateCategory(Text.translatable("gui.anvil_insight.config.category.anvil"));

            // 显示进度条
            anvil.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.show_anvil_bar"), config.showAnvilBar)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.showAnvilBar = newValue)
                    .build());
            // 重置进度条位置
            anvil.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.reset_bar_button"), false)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("gui.anvil_insight.config.reset_bar.desc"))
                    .setSaveConsumer(value -> {
                        if (value) {
                            config.xOffset = AnvilInsightConfig.DEFAULT_BAR_X;
                            config.yOffset = AnvilInsightConfig.DEFAULT_BAR_Y;
                        }
                    })
                    .build());

            // 显示冲突附魔
            anvil.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.show_conflict_warning"), config.showConflictWarning)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.showConflictWarning = newValue)
                    .build());
            // 重置警告位置
            anvil.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.reset_icon_button"), false)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("gui.anvil_insight.config.reset_icon.desc"))
                    .setSaveConsumer(value -> {
                        if (value) {
                            config.iconXOffset = AnvilInsightConfig.DEFAULT_ICON_X;
                            config.iconYOffset = AnvilInsightConfig.DEFAULT_ICON_Y;
                        }
                    })
                    .build());

            // 拖拽锁定
            anvil.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.enable_dragging"), config.enableDragging)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("gui.anvil_insight.config.enable_dragging.tooltip"))
                    .setSaveConsumer(newValue -> config.enableDragging = newValue)
                    .build());

            // 显示百分比
            anvil.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.show_percentage"), config.showPercentage)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.showPercentage = newValue)
                    .build());

            // 显示 XP 消耗
            anvil.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.show_xp_cost"), config.showXpCost)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.showXpCost = newValue)
                    .build());

            // 显示修复耐久度增益
            anvil.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.show_repair_gain"), config.showRepairGain)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.showRepairGain = newValue)
                    .build());

            // =================================================
            // 砂轮设置 (Grindstone)
            // =================================================
            ConfigCategory grindstone = builder.getOrCreateCategory(Text.translatable("gui.anvil_insight.config.category.grindstone"));

            // 显示重置动画
            grindstone.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.show_grindstone_hint"), config.showGrindstoneHint)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.showGrindstoneHint = newValue)
                    .build());

            // 显示获益经验
            grindstone.addEntry(entryBuilder.startBooleanToggle(Text.translatable("gui.anvil_insight.config.show_grindstone_xp"), config.showGrindstoneXp)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.showGrindstoneXp = newValue)
                    .build());

            builder.setSavingRunnable(config::save);
            return builder.build();
        };
    }
}