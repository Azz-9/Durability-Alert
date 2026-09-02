package me.Azz_9.durability_alert.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import me.Azz_9.durability_alert.platform.services.IPlatformHelper;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
	public @NotNull String getPlatformName() {
        return "NeoForge";
    }

    @Override
	public boolean isModLoaded(@NotNull String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
	public @NotNull Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}