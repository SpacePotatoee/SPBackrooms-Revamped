#include veil:material
#include veil:deferred_buffers
#include veil:deferred_utils
#include veil:camera

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform mat3 NormalMat;

in vec4 VertexColor[];
in vec2 TexCoord0[];
in vec2 TexCoord2[];
in vec4 LightmapColor[];
in vec3 normall[];
in vec2 WorldFacePos[];
in vec3 WorldPos[];
in vec3 position[];
in float zoom[];
flat in float resolution[];
flat in float enableHeight[];
in float depth[];

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoordOffset;
out vec2 texCoord2;
out vec4 lightmapColor;
out vec3 normal;
out vec2 worldFacePos;
out vec3 worldPos;
out vec3 Pos;
out float Zoom;
flat out float Resolution;
flat out float Enableheight;
out float Depth;
out mat3 TBN;

void main() {
    float u = 1.0;
    float v = 1.0;
    for(int i = 0; i < 3; i++){
        u = min(TexCoord0[i].x, u);
        v = min(TexCoord0[i].y, v);
    }

    texCoordOffset = vec2(u, v);

    //TBN MATRIX
    vec3 edge0 = gl_in[1].gl_Position.xyz - gl_in[0].gl_Position.xyz;
    vec3 edge1 = gl_in[2].gl_Position.xyz - gl_in[0].gl_Position.xyz;
    vec2 deltaUV0 = TexCoord0[1] - TexCoord0[0];
    vec2 deltaUV1 = TexCoord0[2] - TexCoord0[0];

    float invDet = 1.0f / (deltaUV0.x * deltaUV1.y - deltaUV1.x * deltaUV0.y);

    vec3 tangent = vec3(invDet * (deltaUV1.y * edge0 - deltaUV0.y * edge1));
    vec3 biTangent = vec3(invDet * (-deltaUV1.x * edge0 + deltaUV0.x * edge1));

    vec3 T = normalize((VeilCamera.ViewMat * vec4(tangent, 0.0)).xyz);
    vec3 B = normalize((VeilCamera.ViewMat * vec4(biTangent, 0.0)).xyz);
    vec3 N = normall[1];
    TBN = mat3(T, B, N);
    TBN = transpose(TBN);

    for(int i = 0; i < 3; i++){
        gl_Position = ProjMat * ModelViewMat * gl_in[i].gl_Position;
        vertexColor = VertexColor[i];
        texCoord0 = TexCoord0[i];
        texCoord2 = TexCoord2[i];
        lightmapColor = LightmapColor[i];
        normal = normall[i];
        worldFacePos = WorldFacePos[i];
        worldPos = WorldPos[i];
        Pos = position[i];
        Zoom = zoom[i];
        Resolution = resolution[i];
        Enableheight = enableHeight[i];
        Depth = depth[i];
        EmitVertex();
    }

    EndPrimitive();
}