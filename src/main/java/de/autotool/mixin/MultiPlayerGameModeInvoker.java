package de.autotool.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeInvoker {
	/** Sends the currently selected hotbar slot to the server right away. */
	@Invoker("ensureHasSentCarriedItem")
	void autotool$ensureHasSentCarriedItem();
}
