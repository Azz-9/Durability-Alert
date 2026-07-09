package me.Azz_9.durability_alert;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as NeoForge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

	public static Minecraft MINECRAFT;

	private static final @NonNull Map<String, Long> lastPingTime = new HashMap<>();

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {

        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.

		MINECRAFT = Minecraft.getInstance();
		Config.load();
	}

	public static boolean isDurabilityUnderThreshold(ItemStack stack) {
		if (stack == null || !stack.isDamageableItem() || stack.getMaxDamage() == 0) {
			return false;
		}

		return (double) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage() * 100 < Config.INSTANCE.threshold;
	}

	public static void pingPlayer(ItemStack stack) {

		long currentTime = System.currentTimeMillis();

		LocalPlayer player = MINECRAFT.player;

		if (player != null && (!lastPingTime.containsKey(stack.getItem().getDescriptionId())
				|| currentTime - lastPingTime.get(stack.getItem().getDescriptionId()) > Config.INSTANCE.minAlertIntervalSeconds * 1000L)) {

			lastPingTime.put(stack.getItem().getDescriptionId(), currentTime);

			// play sound, display message or both based on the selected option in the config menu
			if (Config.INSTANCE.alertType != Config.AlertType.SOUND) {
				Component message = Component.translatable("durability_alert.message", stack.getItemName().getString().toLowerCase()).withStyle(ChatFormatting.RED);
				player.sendOverlayMessage(message);
			}
			if (Config.INSTANCE.alertType != Config.AlertType.MESSAGE) {
				MINECRAFT.getSoundManager().play(SimpleSoundInstance.forUI(Config.INSTANCE.sound, Config.INSTANCE.pitch, Config.INSTANCE.volume));
			}
		}
	}
}