package com.danielm.modid.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;

public class ExplosiveSlimeRenderer extends SlimeRenderer {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("firstmod", "textures/entity/explosive_slime.png");

    public ExplosiveSlimeRenderer(final EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(final SlimeRenderState state) {
        return TEXTURE;
    }
}