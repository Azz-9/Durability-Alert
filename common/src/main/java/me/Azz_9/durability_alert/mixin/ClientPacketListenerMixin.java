package me.Azz_9.durability_alert.mixin;

import static me.Azz_9.durability_alert.CommonClass.MINECRAFT;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
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
                || GameType.CREATIVE.equals(MINECRAFT.player.gameMode())
                || GameType.SPECTATOR.equals(MINECRAFT.player.gameMode())
                || stack.isEmpty() || !stack.isDamageableItem())
            return;

        int damageValue = stack.getDamageValue();
        int old = MINECRAFT.player.getInventory().getItem(packet.getSlot()).getDamageValue();

		if (damageValue == old) return;

        boolean isListed = Config.INSTANCE.itemList.contains(stack.getItem());
        boolean passesList = switch (Config.INSTANCE.listType) {
            case WHITELIST -> isListed;
            case BLACKLIST -> !isListed;
        };

        boolean passesItemType;
        if (isArmorPiece(packet.getSlot())) {
            passesItemType = Config.INSTANCE.checkArmorPieces || (Config.INSTANCE.checkElytraOnly && stack.is(Items.ELYTRA));
        } else {
            passesItemType = !Config.INSTANCE.checkElytraOnly;
        }

        if (CommonClass.isDurabilityUnderThreshold(stack) && passesList && passesItemType) {
            CommonClass.pingPlayer(stack);
        }
    }

    @Unique
    private boolean isArmorPiece(int slot) {
		// 5 = helmet, 8 = boots
		return 5 <= slot && slot <= 8;
    }
}
