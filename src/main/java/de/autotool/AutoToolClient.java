package de.autotool;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.lwjgl.sdl.SDLScancode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

import de.autotool.mixin.MultiPlayerGameModeInvoker;

public class AutoToolClient implements ClientModInitializer {
	public static final String MOD_ID = "autotool";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final int HOTBAR_SIZE = 9;

	private static KeyMapping toggleKey;

	/** Slot the player held before AutoTool switched, or -1 if nothing to restore. */
	private static int previousSlot = -1;
	/** Slot AutoTool switched to; used to detect manual slot changes by the player. */
	private static int switchedSlot = -1;

	@Override
	public void onInitializeClient() {
		AutoToolConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, MOD_ID));
		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.autotool.toggle", InputConstants.Type.KEYBOARD, SDLScancode.SDL_SCANCODE_K, category));

		// Fired client-side by Fabric API right before the "start destroying block" packet is sent,
		// so the server already sees the new tool when mining starts.
		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
			if (level.isClientSide() && player instanceof LocalPlayer localPlayer) {
				onStartDestroyBlock(Minecraft.getInstance(), localPlayer, pos);
			}

			return InteractionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(AutoToolClient::onEndTick);
	}

	private static void onEndTick(Minecraft client) {
		while (toggleKey.consumeClick()) {
			AutoToolConfig.enabled = !AutoToolConfig.enabled;
			AutoToolConfig.save();

			if (client.player != null) {
				client.gui.hud.setOverlayMessage(Component.translatable(AutoToolConfig.enabled ? "autotool.enabled" : "autotool.disabled"), false);
			}
		}

		LocalPlayer player = client.player;

		if (player == null || client.gameMode == null) {
			previousSlot = -1;
			return;
		}

		if (previousSlot == -1) return;

		Inventory inventory = player.getInventory();

		// The player picked another slot by hand: don't fight them.
		if (inventory.getSelectedSlot() != switchedSlot) {
			previousSlot = -1;
			return;
		}

		if (!client.options.keyAttack.isDown()) {
			selectSlot(client, inventory, previousSlot);
			previousSlot = -1;
		}
	}

	private static void onStartDestroyBlock(Minecraft client, LocalPlayer player, BlockPos pos) {
		if (!AutoToolConfig.enabled || client.gameMode == null || client.level == null) return;
		if (player.isSpectator() || (AutoToolConfig.ignoreCreative && player.isCreative())) return;

		BlockState state = client.level.getBlockState(pos);
		float hardness = state.getDestroySpeed(client.level, pos);

		// Unbreakable (bedrock) or instantly broken (flowers, torches): no tool needed.
		if (hardness <= 0) return;

		Inventory inventory = player.getInventory();
		int currentSlot = inventory.getSelectedSlot();
		int bestSlot = currentSlot;
		double bestScore = score(inventory.getItem(currentSlot), state);

		for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
			if (slot == currentSlot) continue;

			double score = score(inventory.getItem(slot), state);

			if (score > bestScore) {
				bestScore = score;
				bestSlot = slot;
			}
		}

		if (bestSlot == currentSlot) return;

		if (AutoToolConfig.switchBack && previousSlot == -1) {
			previousSlot = currentSlot;
		}

		switchedSlot = bestSlot;
		selectSlot(client, inventory, bestSlot);
	}

	/**
	 * Rates how well a stack breaks the given block. Higher is better; a negative value means
	 * the stack must not be used (about to break).
	 */
	private static double score(ItemStack stack, BlockState state) {
		if (isAlmostBroken(stack)) return -1;

		double score = 0;

		// Getting drops at all (e.g. pickaxe on stone/ores) beats any amount of speed.
		if (!state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state)) {
			score += 100_000;
		}

		float speed = stack.getDestroySpeed(state);

		if (speed > 1.0F) {
			int efficiency = getEfficiencyLevel(stack);

			if (efficiency > 0) {
				// Same bonus vanilla applies via the mining efficiency attribute.
				speed += efficiency * efficiency + 1;
			}
		}

		return score + speed * 100;
	}

	private static boolean isAlmostBroken(ItemStack stack) {
		if (stack.getMaxDamage() <= 0) return false;

		return stack.getMaxDamage() - stack.getDamageValue() <= AutoToolConfig.minDurability;
	}

	private static int getEfficiencyLevel(ItemStack stack) {
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
			if (entry.getKey().is(Enchantments.EFFICIENCY)) {
				return entry.getIntValue();
			}
		}

		return 0;
	}

	private static void selectSlot(Minecraft client, Inventory inventory, int slot) {
		if (inventory.getSelectedSlot() == slot) return;

		inventory.setSelectedSlot(slot);
		((MultiPlayerGameModeInvoker) client.gameMode).autotool$ensureHasSentCarriedItem();
	}
}
