package me.Azz_9.durability_alert.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.Azz_9.durability_alert.client.gui.components.toasts.CustomToastId;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

import static me.Azz_9.durability_alert.CommonClass.MINECRAFT;
import static me.Azz_9.durability_alert.Constants.CLOTH_CONFIG_ID_FABRIC;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded(CLOTH_CONFIG_ID_FABRIC)) {
            return parent -> {
                MINECRAFT.execute(() ->
                        SystemToast.add(
                                MINECRAFT.gui.toastManager(),
                                CustomToastId.MISSING_CLOTH_CONFIG,
                                Component.translatable("durability_alert.toast.missing_cloth_config.title"),
                                Component.translatable("durability_alert.toast.missing_cloth_config.message")
                        )
                );
                return null;
            };
        }
        return ClothConfigCompat::buildClothConfigScreen;
    }
}
