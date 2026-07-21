#version 410 core
#include veil:camera

layout(location = 0) in vec3 Position;
layout(location = 1) in vec3 Normal;

uniform float GameTime;
uniform vec3 FlockCenters[14];
uniform int FlockAmount;

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

float hash(uint x) {
    return float(triple32(x)) / float(0xffffffffU);
}

out vec3 localPos;
out vec3 normal;
flat out int InstanceNum;

mat3 rotX(float rad) {
    float s = sin(rad);
    float c = cos(rad);
    return mat3(
    1, 0, 0,
    0, c, -s,
    0, s, c
    );
}

mat3 rotY(float rad) {
    float s = sin(rad);
    float c = cos(rad);
    return mat3(
    c, 0, s,
    0, 1, 0,
    -s, 0, c
    );
}

mat3 rotZ(float rad) {
    float s = sin(rad);
    float c = cos(rad);
    return mat3(
    c, -s, 0,
    s, c, 0,
    0, 0, 1
    );
}
mat3 createLookRotation(vec3 direction) {
    // Normalize the direction vector
    vec3 forward = normalize(direction);

    // Create a right vector perpendicular to both forward and world up
    vec3 worldUp = vec3(0.0, 1.0, 0.0);
    // Handle special case where forward is parallel to world up
    if (abs(dot(forward, worldUp)) > 0.99) {
        worldUp = vec3(0.0, 0.0, 1.0); // Use world Z instead
    }

    vec3 right = normalize(cross(worldUp, forward));

    // Create an up vector perpendicular to forward and right
    vec3 up = normalize(cross(forward, right));

    // Construct rotation matrix
    // The columns represent the transformed basis vectors
    return mat3(
    right,    // First column (right vector)
    up,       // Second column (up vector)
    forward   // Third column (forward vector)
    );
}

void main() {
    vec3 pos = Position;

    vec3 cameraPos = VeilCamera.CameraPosition;

    uint id = uint(gl_InstanceID);
    float h0 = hash(id * 3u + 1u);
    float h1 = hash(id * 7u + 13u);
    float h2 = hash(id * 101u + 7u);
    float h3 = hash(id * 31u + 3u);
    float h4 = hash(id * 61u + 29u);

    vec3 center = FlockCenters[FlockAmount > 0 ? gl_InstanceID % FlockAmount : 0];

    float radius = 1.4 + h1 * 2.8;
    float bob = 1.0 + h3 * 1.4;
    float phase = h0 * 6.2831853;

    // Every bird orbits in its own plane. With a shared plane the flock reads as a
    // flat disc; the tilt is what makes it a ball like the boids simulation produced.
    float ct = 2.0 * h2 - 1.0;
    float st = sqrt(max(1.0 - ct * ct, 0.0));
    float pa = h4 * 6.2831853;
    vec3 axis = vec3(st * cos(pa), ct, st * sin(pa));
    vec3 ref = abs(axis.y) > 0.9 ? vec3(1.0, 0.0, 0.0) : vec3(0.0, 1.0, 0.0);
    vec3 u = normalize(cross(ref, axis));
    vec3 v = normalize(cross(axis, u));

    // GameTime wraps once per in-game day, so every oscillator runs a whole number of
    // turns over that period. Fractional rates would snap the flock on the wrap.
    float T = GameTime * 6.2831853;
    float turns = floor(38.0 + h2 * 20.0);
    float t = T * turns + phase;
    float tb = T * floor(turns * 0.7) + phase;

    vec3 offset = u * cos(t) * radius + v * sin(t) * radius + axis * sin(tb) * bob;
    offset += vec3(sin(T * 13.0 + phase), cos(T * 11.0 + phase) * 0.4, cos(T * 17.0 + phase)) * 0.6;

    vec3 position = center + offset;
    vec3 rotation = normalize(-u * sin(t) * radius + v * cos(t) * radius
                              + axis * cos(tb) * bob * 0.7);

    vec3 tempNormal = Normal;


    mat3 rotationMatrix = createLookRotation(rotation);

    float scale = 0.1;
    pos = rotationMatrix * pos * scale;
    tempNormal = normalize(rotationMatrix * tempNormal);

    normal = tempNormal;

    localPos = (pos - cameraPos) + position;

    gl_Position = VeilCamera.ProjMat * VeilCamera.ViewMat * vec4(localPos , 1.0);

    InstanceNum = gl_InstanceID;
}