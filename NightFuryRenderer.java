package com.httymd.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.Identifier;

import com.httymd.HttymdMod;
import com.httymd.client.model.NightFuryModel;
import com.httymd.entity.NightFuryEntity;

public class NightFuryRenderer extends LivingEntityRenderer<NightFuryEntity, NightFuryRenderState, NightFuryModel> {
    private static final Identifier TEXTURE = HttymdMod.id("textures/entity/night_fury.png");

    public NightFuryRenderer(EntityRendererProvider.Context context) {
        super(context, new NightFuryModel(context.bakeLayer(NightFuryModel.LAYER)), 0.9f);
    }

    @Override
    public NightFuryRenderState createRenderState() {
        return new NightFuryRenderState();
    }

    @Override
    public void extractRenderState(NightFuryEntity entity, NightFuryRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.flyBlend = entity.getFlyBlend(partialTick);
    }

    @Override
    public Identifier getTextureLocation(NightFuryRenderState state) {
        return TEXTURE;
    }
}
