package com.sp.mixin.lightshadows;

import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.deferred.light.IndirectLight;
import foundry.veil.api.client.render.deferred.light.Light;
import foundry.veil.api.client.render.deferred.light.renderer.IndirectLightRenderer;
import foundry.veil.api.client.render.shader.definition.DynamicShaderBlock;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferSubData;
import static org.lwjgl.opengl.GL40C.GL_DRAW_INDIRECT_BUFFER;

@Mixin(value = IndirectLightRenderer.class, remap = false)
public abstract class IndirectLightRendererMixin<T extends Light & IndirectLight<T>> {
    @Shadow @Final protected int highResSize;
    @Shadow @Final protected int lowResSize;
    @Shadow @Final private int indirectVbo;

    @Shadow protected abstract boolean isVisible(T light, CullFrustum frustum);

    @Shadow protected abstract boolean shouldDrawHighResolution(T light, CullFrustum frustum);


    @Shadow @Final private DynamicShaderBlock<?> indirectBlock;

    /**
     * Veil advertises this renderer whenever OpenGL 4.0 is present, but it draws with
     * glMultiDrawElementsIndirect, which is 4.3. Apple's driver stops at 4.1, so the check
     * passes and the missing entry point then aborts the JVM. Narrowing it to the capability
     * actually used makes Veil fall back to the instanced point light renderer.
     */
    @Inject(method = "isSupported", at = @At("HEAD"), cancellable = true)
    private static void spb_revamped_1_20_1$requireMultiDrawIndirect(CallbackInfoReturnable<Boolean> cir) {
        GLCapabilities caps = GL.getCapabilities();
        cir.setReturnValue(caps.OpenGL43 || caps.GL_ARB_multi_draw_indirect);
    }

    /**
     * @author
     * @reason
     * {@link org.lwjgl.opengl.GL15C#glGetBufferSubData(int, long, IntBuffer)} was taking way too long to complete when getting called (causing a lot of lag). So commenting that out and using the very convenient second option VASTLY inscreases performance. No idea if the compute shader was needed though
     */
    @Overwrite
    private int updateVisibility(List<T> lights, CullFrustum frustum) {
//        if (this.sizeVbo != 0) {
//            VeilRenderSystem.setShader(VeilShaders.LIGHT_INDIRECT_SPHERE);
//            ShaderProgram shader = VeilRenderSystem.getShader();
//            if (shader != null && shader.isCompute()) {
//                try (MemoryStack stack = MemoryStack.stackPush()) {
//                    VeilRenderSystem.bind("VeilLightInstanced", this.instancedBlock);
//                    VeilRenderSystem.bind("VeilLightIndirect", this.indirectBlock);
//
//                    glBindBufferRange(GL_ATOMIC_COUNTER_BUFFER, 0, this.sizeVbo, 0, Integer.BYTES);
//                    glBufferSubData(GL_ATOMIC_COUNTER_BUFFER, 0, stack.callocInt(1));
//
//                    shader.setInt("HighResSize", this.highResSize);
//                    shader.setInt("LowResSize", this.lowResSize);
//                    shader.setInt("LightSize", this.lightSize / Float.BYTES);
//                    shader.setInt("PositionOffset", this.positionOffset);
//                    shader.setInt("RangeOffset", this.rangeOffset);
//
//                    profiler.swap("setting up frustum planes");
//                    Vector4fc[] planes = frustum.getPlanes();
//                    float[] values = new float[4 * planes.length];
//                    for (int i = 0; i < planes.length; i++) {
//                        Vector4fc plane = planes[i];
//                        values[i * 4] = plane.x();
//                        values[i * 4 + 1] = plane.y();
//                        values[i * 4 + 2] = plane.z();
//                        values[i * 4 + 3] = plane.w();
//                    }
//                    shader.setFloats("FrustumPlanes", values);
//
//                    shader.bind();
//
//                    profiler.swap("dispatch compute");
//                    glDispatchCompute(Math.min(lights.size(), VeilRenderSystem.maxComputeWorkGroupCountX()), 1, 1);
//                    glMemoryBarrier(GL_BUFFER_UPDATE_BARRIER_BIT | GL_ATOMIC_COUNTER_BARRIER_BIT);
//
//                    ShaderProgram.unbind();
//
//                    profiler.swap("Allocating");
//                    IntBuffer counter = stack.mallocInt(1);
//                    profiler.swap("Bind buffer 1");
//                    glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, this.sizeVbo);
//                    profiler.swap("Sub data");
//                    glGetBufferSubData(GL_ATOMIC_COUNTER_BUFFER, 0L, counter);
//                    profiler.swap("Bind buffer 2");
//                    glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, 0);
//                    return counter.get(0);
//                } finally {
//                    profiler.swap("Unbind blocks");
//                    VeilRenderSystem.unbind(this.instancedBlock);
//                    VeilRenderSystem.unbind(this.indirectBlock);
//                    profiler.swap("Bind buffer range");
//                    glBindBufferRange(GL_ATOMIC_COUNTER_BUFFER, 0, 0, 0, Integer.BYTES);
//                }
//            }
//        }

        int count = 0;
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, this.indirectVbo);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(this.lowResSize > 0 ? Integer.BYTES * 5 : Integer.BYTES);

            int index = 0;
            for (T light : lights) {
                if (this.isVisible(light, frustum)) {
                    if (this.lowResSize > 0) {
                        boolean highRes = this.shouldDrawHighResolution(light, frustum);
                        buffer.putInt(0, highRes ? this.highResSize : this.lowResSize);
                        buffer.putInt(4, 1);
                        buffer.putInt(8, !highRes ? this.highResSize : 0);
                        buffer.putInt(12, 0);
                        buffer.putInt(16, index);
                        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, count * Integer.BYTES * 5L, buffer);
                    } else {
                        buffer.putInt(0, index);
                        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, count * Integer.BYTES * 5L + 16, buffer);
                    }
                    count++;
                }
                index++;
            }
        }
        return count;
    }

}
