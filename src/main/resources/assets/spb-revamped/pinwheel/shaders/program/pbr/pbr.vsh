#include veil:light
#include veil:deferred_utils

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV2;
layout(location = 4) in vec3 Normal;
layout(location = 5) in float Zoom;
layout(location = 6) in float Resolution;
layout(location = 7) in float EnableHeight;
layout(location = 8) in float DepthMultiplier;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform mat3 NormalMat;

out vec4 VertexColor;
out vec2 TexCoord0;
out vec2 TexCoord2;
out vec4 LightmapColor;
out vec3 normall;
out vec2 WorldFacePos;
out vec3 WorldPos;
out vec3 position;
out float zoom;
flat out float resolution;
flat out float enableHeight;
out float depth;

void main() {
    vec3 pos = Position + ChunkOffset;
    position = pos;
    gl_Position = vec4(pos, 1.0);

    zoom = Zoom;
    resolution = Resolution;
    enableHeight = EnableHeight;
    depth = DepthMultiplier;

    VertexColor = Color;
    TexCoord0 = UV0;
    TexCoord2 = minecraft_sample_lightmap_coords(UV2);
    LightmapColor = pow(texture(Sampler2, TexCoord2), vec4(1));
    normall = NormalMat * Normal;

    vec3 worldNormal = abs(viewToWorldSpaceDirection(normall));
    WorldPos = playerSpaceToWorldSpace(pos);

    WorldFacePos = (Position.zx * worldNormal.y) + (Position.xy * worldNormal.z) + (vec2(-Position.z, -Position.y) * worldNormal.x);
}


