package oyatsu.baka.anvilinsight.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnvilInsightConfig {

    public static final AnvilInsightConfig INSTANCE = new AnvilInsightConfig();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("anvil_insight.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum TooltipMode {
        OFF, ALWAYS, ADVANCED_ONLY
    }

    public static final int DEFAULT_BAR_X = 0;
    public static final int DEFAULT_BAR_Y = -12;
    public static final int DEFAULT_ICON_X = 0;
    public static final int DEFAULT_ICON_Y = 0;

    public Boolean enableAnvilGlobal = true;
    public Boolean enableGrindstoneGlobal = true;
    public Boolean enableDragging = false;

    public int xOffset = DEFAULT_BAR_X;
    public int yOffset = DEFAULT_BAR_Y;
    public int iconXOffset = DEFAULT_ICON_X;
    public int iconYOffset = DEFAULT_ICON_Y;

    public Boolean showAnvilBar = true;
    public Boolean showPercentage = true;
    public Boolean showXpCost = true;
    public Boolean showConflictWarning = true;
    public TooltipMode tooltipMode = TooltipMode.ADVANCED_ONLY;

    public Boolean showGrindstoneHint = true;
    public Boolean showGrindstoneXp = true;
    public Boolean showRepairGain = true;

    public void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            AnvilInsightConfig loaded = GSON.fromJson(json, AnvilInsightConfig.class);

            if (loaded.enableAnvilGlobal != null) this.enableAnvilGlobal = loaded.enableAnvilGlobal;
            if (loaded.enableGrindstoneGlobal != null) this.enableGrindstoneGlobal = loaded.enableGrindstoneGlobal;
            if (loaded.enableDragging != null) this.enableDragging = loaded.enableDragging;

            this.xOffset = loaded.xOffset;
            this.yOffset = loaded.yOffset;
            this.iconXOffset = loaded.iconXOffset;
            this.iconYOffset = loaded.iconYOffset;

            if (loaded.showAnvilBar != null) this.showAnvilBar = loaded.showAnvilBar;
            if (loaded.showPercentage != null) this.showPercentage = loaded.showPercentage;
            if (loaded.showXpCost != null) this.showXpCost = loaded.showXpCost;
            if (loaded.showConflictWarning != null) this.showConflictWarning = loaded.showConflictWarning;
            if (loaded.showRepairGain != null) this.showRepairGain = loaded.showRepairGain;

            this.tooltipMode = loaded.tooltipMode != null ? loaded.tooltipMode : TooltipMode.ADVANCED_ONLY;

            if (loaded.showGrindstoneHint != null) this.showGrindstoneHint = loaded.showGrindstoneHint;
            if (loaded.showGrindstoneXp != null) this.showGrindstoneXp = loaded.showGrindstoneXp;

            save();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}