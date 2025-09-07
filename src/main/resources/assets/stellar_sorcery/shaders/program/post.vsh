#version 150

in vec2 Position;            // Input fra fullscreen quad
out vec2 texCoord;           // Går videre til fragment shader

void main() {
    texCoord = Position;     // Direkte pass-through
    gl_Position = vec4(Position * 2.0 - 1.0, 0.0, 1.0);
}
