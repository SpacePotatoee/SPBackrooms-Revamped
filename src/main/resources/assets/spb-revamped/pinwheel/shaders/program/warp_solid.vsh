#include veil:camera
#include veil:deferred_utils
#include veil:fog
#include veil:light
//#include spb-revamped:common

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV2;
layout(location = 4) in vec3 Normal;
layout(location = 6) in float BlockMaterial1;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform mat3 NormalMat;
uniform float warpAngle;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord2;
out vec4 lightmapColor;
out vec3 normal;
flat out float blockMaterial;
out vec3 localPos;
out vec3 chunkOffset;

float mod289(float x){return x - floor(x * (1.0 / 289.0)) * 289.0;}
vec4 mod289(vec4 x){return x - floor(x * (1.0 / 289.0)) * 289.0;}
vec4 perm(vec4 x){return mod289(((x * 34.0) + 1.0) * x);}

float perlinNoise(vec3 p) {
    vec3 a = floor(p);
    vec3 d = p - a;
    d = d * d * (3.0 - 2.0 * d);

    vec4 b = a.xxyy + vec4(0.0, 1.0, 0.0, 1.0);
    vec4 k1 = perm(b.xyxy);
    vec4 k2 = perm(k1.xyxy + b.zzww);

    vec4 c = k2 + a.zzzz;
    vec4 k3 = perm(c);
    vec4 k4 = perm(c + 1.0);

    vec4 o1 = fract(k3 * (1.0 / 41.0));
    vec4 o2 = fract(k4 * (1.0 / 41.0));

    vec4 o3 = o2 * d.z + o1 * (1.0 - d.z);
    vec2 o4 = o3.yw * d.x + o3.xz * (1.0 - d.x);

    return o4.y * d.y + o4.x * (1.0 - d.y);
}

void main() {
    vec3 cameraPos = VeilCamera.CameraPosition;
    vec3 pos = Position + ChunkOffset;
    float dist = pos.z;
    chunkOffset = ChunkOffset + cameraPos;
    pos = playerSpaceToWorldSpace(pos);

    #ifdef WARP
    dist *= 0.03 * sin(warpAngle * 200.0);
    #else
    dist *= 0;
    #endif
    pos -= vec3(0.5, 21.5, 0.0);
	pos = vec3((pos.x*cos(dist)) - (pos.y * sin(dist)),(pos.y  * cos(dist)) + (pos.x * sin(dist)),pos.z);
    pos += vec3(0.5, 21.5, 0.0);

    pos = pos - cameraPos;

    localPos = pos;

    vec3 worldPos = localPos + cameraPos;
//    float noise = perlinNoise(vec3(worldPos.x, 0.0, worldPos.z) * 0.05);


    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
//    gl_Position = ProjMat * ModelViewMat * vec4(pos - vec3(0.0, length(localPos)*0.5, 0.0), 1.0);
//    gl_Position = ProjMat * ModelViewMat * vec4(pos + vec3(0.0, noise*10, 0.0), 1.0);

    vertexColor = Color;
    texCoord0 = UV0;
    texCoord2 = minecraft_sample_lightmap_coords(UV2);

    float multiplier = 1.0;
    #ifdef POOLROOMS
        multiplier = 3.0;
    #endif

    lightmapColor = pow(texture(Sampler2, texCoord2), vec4(multiplier));
    normal = NormalMat * Normal;
    blockMaterial = BlockMaterial1;
}