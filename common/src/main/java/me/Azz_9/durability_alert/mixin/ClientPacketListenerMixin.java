package me.Azz_9.durability_alert.mixin;

import static me.Azz_9.durability_alert.CommonClass.MINECRAFT;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.durability_alert.CommonClass;
import me.Azz_9.durability_alert.Config;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleContainerSetSlot", at = @At("HEAD"))
    private void onSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        ItemStack stack = packet.getItem();
		if (!RenderSystem.isOnRenderThread() || !Config.INSTANCE.enabled || MINECRAFT.level == null || MINECRAFT.player == null
				|| !durability_alert$checkableSlot(packet.getSlot())
                || GameType.CREATIVE.equals(MINECRAFT.player.gameMode())
                || GameType.SPECTATOR.equals(MINECRAFT.player.gameMode())
                || stack.isEmpty() || !stack.isDamageableItem())
            return;

		int newDamageValue = stack.getDamageValue();
		int old = MINECRAFT.player.inventoryMenu.getSlot(packet.getSlot()).getItem().getDamageValue();

		if (newDamageValue <= old) return;

        boolean isListed = Config.INSTANCE.itemList.contains(stack.getItem());
        boolean passesList = switch (Config.INSTANCE.listType) {
            case WHITELIST -> isListed;
            case BLACKLIST -> !isListed;
        };

		boolean passesItemType = true;
		if (durability_alert$isArmorSlot(packet.getSlot())) {
            passesItemType = Config.INSTANCE.checkArmorPieces || (Config.INSTANCE.checkElytraOnly && stack.is(Items.ELYTRA));
        }

        if (CommonClass.isDurabilityUnderThreshold(stack) && passesList && passesItemType) {
            CommonClass.pingPlayer(stack);
        }
    }

	@Unique
	private boolean durability_alert$checkableSlot(int slot) {
		return durability_alert$isArmorSlot(slot)
				|| durability_alert$isInventorySlot(slot)
				|| durability_alert$isHotBarSlot(slot)
				|| durability_alert$isOffhandSlot(slot);
	}

	@Unique
	private boolean durability_alert$isArmorSlot(int slot) {
		return InventoryMenu.ARMOR_SLOT_START <= slot && slot < InventoryMenu.ARMOR_SLOT_END;
	}

	@Unique
	private boolean durability_alert$isInventorySlot(int slot) {
		return InventoryMenu.INV_SLOT_START <= slot && slot < InventoryMenu.INV_SLOT_END;
	}

	@Unique
	private boolean durability_alert$isHotBarSlot(int slot) {
		return InventoryMenu.USE_ROW_SLOT_START <= slot && slot < InventoryMenu.USE_ROW_SLOT_END;
	}

	@Unique
	private boolean durability_alert$isOffhandSlot(int slot) {
		return InventoryMenu.SHIELD_SLOT == slot;
	}
}
