#version 410 core
#include veil:camera

layout(location = 0) in vec3 Position;
layout(location = 1) in vec3 Normal;

uniform float GameTime;
uniform sampler2D WindNoise;
uniform int NumOfInstances;
uniform float density;
uniform float grassHeight;
uniform float FrustumPlanes[24];

bool testSphere(float x, float y, float z, float r) {
    return FrustumPlanes[0] * x + FrustumPlanes[1] * y + FrustumPlanes[2] * z + FrustumPlanes[3] >= -r &&
    FrustumPlanes[4] * x + FrustumPlanes[5] * y + FrustumPlanes[6] * z + FrustumPlanes[7] >= -r &&
    FrustumPlanes[8] * x + FrustumPlanes[9] * y + FrustumPlanes[10] * z + FrustumPlanes[11] >= -r &&
    FrustumPlanes[12] * x + FrustumPlanes[13] * y + FrustumPlanes[14] * z + FrustumPlanes[15] >= -r &&
    FrustumPlanes[16] * x + FrustumPlanes[17] * y + FrustumPlanes[18] * z + FrustumPlanes[19] >= -r &&
    FrustumPlanes[20] * x + FrustumPlanes[21] * y + FrustumPlanes[22] * z + FrustumPlanes[23] >= -r;
}

uint triple32(uint x) {
    x ^= x >> 17;
    x *= 0xed5ad4bbU;
    x ^= x >> 11;
    x *= 0xac4c1b51U;
    x ^= x >> 15;
    x *= 0x31848babU;
    x ^= x >> 14;
    return x;
}

float hash(uint x){
    return float( triple32(x) ) / float( 0xffffffffU );
}

float hash12(vec2 p){
    vec3 p3  = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

mat2 rot2D(float angle) {
    float rad = (angle * 3.151592)/180.0;
    float s = sin(rad);
    float c = cos(rad);
    return mat2(c, -s, s, c);
}

float getGrassHeightGradient(float height){
    return height / grassHeight;
}

out vec3 localPos;
out vec3 normal;

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
    vec3 pos = Position;

    vec3 cameraPos = VeilCamera.CameraPosition;
    vec3 player = vec3(floor(cameraPos.x), 0, floor(cameraPos.z));

    float cameraX = mod(cameraPos.x, 1);
    float cameraZ = mod(cameraPos.z, 1);
    cameraPos.xz = vec2(cameraX, cameraZ);

    // Integer division matches the grid the compute pass built; float division
    // shifts the whole field by half a blade for odd NumOfInstances.
    float halfInstances = float(NumOfInstances / 2) - 0.5;
    float gridX = float(gl_InstanceID % NumOfInstances) - halfInstances;
    float gridZ = float(gl_InstanceID / NumOfInstances) - halfInstances;

    vec3 offset = vec3(gridX / density, 31, gridZ / density);

    #ifdef LEVEL324
    offset = vec3(gridX / density, (31 + 34), gridZ / density);
    #endif

    vec3 cullPos = offset - cameraPos;

    bool isInRoad = false;

    #ifdef LEVEL324
    isInRoad = (mod(player.x + offset.x, 1000) < 8) || ((player.z + offset.z > 0 && player.z + offset.z < 27) && (player.x + offset.x > 8 && player.x + offset.x < 56));
    #endif

    if (isInRoad || length(cullPos.xz - cameraPos.xz) >= 90 || !testSphere(cullPos.x, cullPos.y, cullPos.z, 1.0)) {
        gl_Position = vec4(0.0, 0.0, 2.0, 1.0);
        return;
    }

    vec3 WorldPos = offset + floor(VeilCamera.CameraPosition);
    float rand = hash12(WorldPos.xz);

    vec3 tempNormal = Normal;
    float randAngle = rand * 360;
    mat2 randRot = rot2D(randAngle);
    pos.xz *= randRot;
    tempNormal.xz *= randRot;






    localPos = (pos - cameraPos) + offset;


    float windStrength = 1;

    #ifdef LEVEL324
        windStrength = 1.5;
    #endif

    vec3 worldPos = localPos + VeilCamera.CameraPosition;
    float grassGradient = getGrassHeightGradient(pos.y);
    float windtexture = texture(WindNoise, (worldPos.xz * 0.03) + GameTime * 100 + rand* 0.1).r - 0.3;
    float heightTexture = clamp((texture(WindNoise, (WorldPos.xz * 0.1)).r * texture(WindNoise, (WorldPos.xz * 0.01)).r), 0.0, 1.0);
    heightTexture = 2.5 * (heightTexture - 0.5) + 0.5;
    localPos.y += heightTexture * grassGradient;
    localPos.xz -= 2 * (grassGradient * grassGradient) * (windtexture * windStrength) * (grassHeight + heightTexture);
    tempNormal.y = 2 * (grassGradient * grassGradient) * (windtexture * windStrength) * (grassHeight + heightTexture);

    normal = tempNormal;

    gl_Position = VeilCamera.ProjMat * VeilCamera.ViewMat * vec4(localPos , 1.0);
}