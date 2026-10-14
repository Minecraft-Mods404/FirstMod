package com.danielm.modid.client;

import com.danielm.modid.SlimeMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class SlimeModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(SlimeMod.SLIME_GRENADE_ENTITY, ThrownItemRenderer::new);
		EntityRendererRegistry.register(SlimeMod.SLIME_MEGA_GRENADE_ENTITY, ThrownItemRenderer::new);
		EntityRendererRegistry.register(SlimeMod.PRINCE_SLIME, SlimeRenderer::new);
		EntityRendererRegistry.register(SlimeMod.KING_SLIME, SlimeRenderer::new);		
		EntityRendererRegistry.register(SlimeMod.EXPLOSIVE_SLIME_CUBE, ExplosiveSlimeRenderer::new);
	}
}