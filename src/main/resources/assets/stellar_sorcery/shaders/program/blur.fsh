#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform float Radius;
out vec4 fragColor;

void main() {
    vec2 texCoord = gl_FragCoord.xy / InSize;

    float offset = Radius / InSize.x; // blur-radius afhænger af opløsning
    vec3 result = texture(DiffuseSampler, texCoord).rgb * 0.36;
    result += texture(DiffuseSampler, texCoord + vec2(offset, 0.0)).rgb * 0.18;
    result += texture(DiffuseSampler, texCoord - vec2(offset, 0.0)).rgb * 0.18;
    result += texture(DiffuseSampler, texCoord + vec2(0.0, offset)).rgb * 0.14;
    result += texture(DiffuseSampler, texCoord - vec2(0.0, offset)).rgb * 0.14;

    fragColor = vec4(result, 1.0);
}