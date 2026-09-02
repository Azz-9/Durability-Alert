package me.Azz_9.durability_alert.platform;

import net.fabricmc.loader.api.FabricLoader;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import me.Azz_9.durability_alert.platform.services.IPlatformHelper;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
	public @NotNull String getPlatformName() {
        return "Fabric";
    }

    @Override
	public boolean isModLoaded(@NotNull String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
	public @NotNull Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
