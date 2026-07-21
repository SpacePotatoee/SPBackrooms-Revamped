#include veil:camera
#include veil:blend
#include veil:deferred_utils
#include spb-revamped:common
#include spb-revamped:puddles

#line 0 2
#define BLOCK_SOLID 0
#define BLOCK_CUTOUT 1
#define BLOCK_CUTOUT_MIPPED 2
#define BLOCK_TRANSLUCENT 3

#define ENTITY_SOLID 4
#define ENTITY_CUTOUT 5
#define ENTITY_TRANSLUCENT 6
#define ENTITY_TRANSLUCENT_EMISSIVE 7

#define PARTICLE 8
#define ARMOR_CUTOUT 9
#define LEAD 10
#define BREAKING 11
#define CLOUD 12
#define WORLD_BORDER 13

#define POWERPOLE 20
#define EMERGENCY_LIGHT 19
#define PLASTIC 22
#define SIGNPOLE 24
#define WINDOW 25

bool isBlock(uint material) {
    return material >= BLOCK_SOLID  && material <= BLOCK_TRANSLUCENT;
}

bool isEntity(uint material) {
    return material >= ENTITY_SOLID && material <= ENTITY_TRANSLUCENT_EMISSIVE;
}

bool isEmissive(uint material) {
    return material == ENTITY_TRANSLUCENT_EMISSIVE;
}

#line 2 0
#line 0 3
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragAlbedo;
layout(location = 2) out vec4 fragNormal;
layout(location = 3) out ivec4 fragMaterial;
layout(location = 4) out vec4 fragLightSampler;
layout(location = 5) out vec4 fragLightMap;

#line 31 0

uniform sampler2D DiffuseSampler0;
uniform sampler2D TransparentDepthSampler;
uniform sampler2D Sampler0;
uniform sampler2D PoleTexture;

uniform vec4 ColorModulator;
uniform vec3 cameraBobOffset;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;
in vec4 lightmapColor;
in vec3 normal;
flat in float blockMaterial;
in vec3 localPos;
in vec3 chunkOffset;

float sdCylinder(vec3 p, vec3 c){
    return length(p.xz-c.xy)-c.z;
}

float sdCappedCylinder( vec3 p, float h, float r ){
    vec2 d = abs(vec2(length(p.xz),p.y)) - vec2(r,h);
    return min(max(d.x,d.y),0.0) + length(max(d,0.0));
}

float map(vec3 p) {
    vec3 polePos = chunkOffset;
    p = mod(p, 1.0) - 1.0/2;
//    vec3 polePos = vec3(-1871.5, 31, 646.5);
    return sdCappedCylinder(p, 9, 0.15);
}

void raymarchLightPole(inout vec4 color, inout vec3 normal, float size) {
    color = vec4(0.0);
    vec3 rayOrigin = playerSpaceToWorldSpace(localPos);
    vec3 rayDir = normalize(localPos);
    float dist = 0.0;

    for(int i = 0; i <= 100; i++) {
        vec3 rayPos = rayOrigin + rayDir * dist;

        float d = map(rayPos);
        dist += d;

        if(d <= 0.001){
            color = texture(Sampler0, texCoord0);

            normal.xz = normalize(rayPos.xz - (floor(rayPos.xz) + 0.5));
            normal.y = 0.0;
            break;
        }

        if(distance(rayPos, rayOrigin) > size){
            break;
        }
    }

}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;


    int Mat = BLOCK_SOLID;
    if(blockMaterial > -0.5) {
        Mat = int(blockMaterial + 0.5);
    }

    vec3 materialNormal = normal;

    if(Mat == POWERPOLE){
        color = vec4(0.0);
        materialNormal = vec3(0.0);
        raymarchLightPole(color, materialNormal, 0.5);
        color.rgb *= dot(materialNormal, vec3(1,0,0)) * 0.5 + 0.5;
        materialNormal = worldToViewSpaceDirection(materialNormal);
    }

    /*
    if(Mat == SIGNPOLE){
        color = vec4(0.0);
        materialNormal = vec3(0.0);
        raymarchLightPole(color, materialNormal, 0.9);
        color.rgb *= dot(materialNormal, vec3(1,0,0)) * 0.5 + 0.5;
        materialNormal = worldToViewSpaceDirection(materialNormal);
    }
    */

    if(color.a < 0.4){
        discard;
    }

    vec4 LightmapColor = lightmapColor;
    if (Mat == EMERGENCY_LIGHT) {
        LightmapColor = vec4(1);
        color = texture(Sampler0, texCoord0);
    }

    fragAlbedo = vec4(color.rgb, 1.0);
    fragNormal = vec4(materialNormal, 1.0);
    fragMaterial = ivec4(Mat, 0, 0, 1);
    fragLightSampler = vec4(texCoord2, 0.0, 1.0);
    fragLightMap = LightmapColor;
}