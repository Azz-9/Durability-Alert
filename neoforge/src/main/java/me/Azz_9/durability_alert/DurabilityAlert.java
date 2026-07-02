package me.Azz_9.durability_alert;

import me.Azz_9.durability_alert.client.gui.components.toasts.CustomToastId;
import me.Azz_9.durability_alert.compat.ClothConfigCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import static me.Azz_9.durability_alert.CommonClass.MINECRAFT;
import static me.Azz_9.durability_alert.Constants.CLOTH_CONFIG_ID_NEOFORGE;

@Mod(Constants.MOD_ID)
public class DurabilityAlert {

    public DurabilityAlert(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.

        eventBus.addListener(FMLClientSetupEvent.class, (event) -> CommonClass.init());

        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> {
                    if (!ModList.get().isLoaded(CLOTH_CONFIG_ID_NEOFORGE)) {
                        Minecraft.getInstance().execute(() ->
                                SystemToast.add(
                                        MINECRAFT.gui.toastManager(),
                                        CustomToastId.MISSING_CLOTH_CONFIG,
                                        Component.translatable("durability_alert.toast.missing_cloth_config.title"),
                                        Component.translatable("durability_alert.toast.missing_cloth_config.message")
                                )
                        );
                        return null;
                    }
                    return (container, parent) -> ClothConfigCompat.buildClothConfigScreen(parent);
                }
        );
    }
}