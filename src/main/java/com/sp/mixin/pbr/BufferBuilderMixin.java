package com.sp.mixin.pbr;

import com.sp.mixininterfaces.BlockMaterial;
import com.sp.render.VertexFormats;
import com.sp.render.pbr.BlockIdMap;
import com.sp.render.pbr.PbrRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

/**
 * This Mixin adds additional vertex data to blocks when rendering:<br>
 * MaterialID for every block<br>
 * Zoom and Resolution for PBR blocks
 */
@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements BlockMaterial {
    @Shadow public abstract void nextElement();

    @Shadow private ByteBuffer buffer;
    @Shadow private int elementOffset;

    @Shadow public abstract void putFloat(int index, float value);

    @Unique boolean isRenderingBlock;
    @Unique Block currentBlock;
    @Unique VertexFormat currentFormat;

    @Override
    public void setCurrentBlock(Block block) {
        this.currentBlock = block;
    }

    @ModifyVariable(method = "begin", at = @At("HEAD"), argsOnly = true)
    private VertexFormat setFormat(VertexFormat format) {
        this.isRenderingBlock = false;
        currentFormat = format;

        //Rendering a normal block. Redirect it to include the Custom Material
        if (format == net.minecraft.client.render.VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL) {
            return setRendering(com.sp.render.VertexFormats.BLOCKS);
        }

        //Rendering a PBR block. Redirect it to include the Zoom and resolution
        if (format == com.sp.render.VertexFormats.PBR) {
            return setRendering(com.sp.render.VertexFormats.PBR);
        }

        return format;
    }


    @Inject(method = "next", at = @At("HEAD"))
    private void putBlockID(CallbackInfo ci){
        if (this.isRenderingBlock) {

            //Normal Block
            if(currentFormat == com.sp.render.VertexFormats.BLOCKS) {
                this.putFloat(0, BlockIdMap.getBlockID(this.currentBlock));
                this.nextElement();
            }

            //PBR block
            else if(this.currentFormat == com.sp.render.VertexFormats.PBR){
                PbrRegistry.PbrMaterial material = PbrRegistry.getMaterial(this.currentBlock);
                if (material == null) {
                    return;
                }

                this.putFloat(0, material.zoom());
                this.nextElement();

                this.putFloat(0, material.textureResolution());
                this.nextElement();

                this.putFloat(0, material.enableHeight() ? 1.0F : 0.0F);
                this.nextElement();

                this.putFloat(0, material.depthMultiplier());
                this.nextElement();

            }
        }
    }

    @Unique
    private void putInt(int offset, int value){
        this.buffer.putInt(this.elementOffset + offset, value);
    }

    @Unique
    private VertexFormat setRendering(VertexFormat format){
        this.isRenderingBlock = true;
        this.currentFormat = format;
        return format;
    }

}