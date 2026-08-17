package com.danielm.modid.client;

import com.danielm.modid.FirstMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class FirstModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(FirstMod.SLIME_GRENADE_ENTITY, ThrownItemRenderer::new);
		EntityRendererRegistry.register(FirstMod.SLIME_MEGA_GRENADE_ENTITY, ThrownItemRenderer::new);
		EntityRendererRegistry.register(FirstMod.PRINCE_SLIME, SlimeRenderer::new);
		EntityRendererRegistry.register(FirstMod.KING_SLIME, SlimeRenderer::new);
	}
}