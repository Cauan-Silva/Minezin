#version 150
in vec4 Position;
uniform mat4 ProjMat;
uniform vec2 OutSize;
out vec2 texCoord;
void main(){vec4 p=ProjMat*vec4(Position.xy,0.,1.);gl_Position=vec4(p.xy,.2,1.);texCoord=Position.xy/OutSize;}
