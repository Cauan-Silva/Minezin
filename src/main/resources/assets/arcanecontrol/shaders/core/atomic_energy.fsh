#version 150
uniform float EffectTime;
uniform float EffectMode;
in vec4 vertexColor;
in vec2 energyUV;
out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}
float noise(vec2 p) {
    vec2 i=floor(p), f=fract(p);
    f=f*f*(3.0-2.0*f);
    return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),
               mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y);
}
float fbm(vec2 p) {
    return noise(p)*0.57+noise(p*2.03+7.1)*0.28+noise(p*4.07+13.7)*0.15;
}
void main() {
    vec2 p=energyUV;
    float flow, edge, core;
    if(EffectMode<0.5) {
        float warp=(fbm(vec2(p.x*2.0-EffectTime*2.2,p.y*3.0))-0.5)*0.28;
        float d=abs(p.y-0.5+warp);
        edge=exp(-d*d*22.0)*(1.0-smoothstep(0.32,0.5,abs(p.y-0.5)));
        core=exp(-d*d*450.0);
        flow=fbm(vec2(p.x*5.0-EffectTime*5.0,p.y*8.0));
    } else {
        // Coordinates wrap around the shell; domain warping breaks the uniform orb.
        vec2 q=vec2(sin(p.x*6.283185),cos(p.x*6.283185))*3.0;
        q+=vec2(p.y*8.0,EffectTime*0.8);
        float warp=fbm(q+vec2(EffectTime,-EffectTime));
        flow=fbm(q*2.4+warp*3.0);
        float veins=abs(sin(p.y*38.0+warp*12.0-EffectTime*3.5));
        core=pow(1.0-veins,12.0);
        edge=(0.08+flow*0.32+core*0.6)*sin(p.y*3.141593);
    }
    vec3 violet=mix(vec3(0.16,0.015,0.42),vec3(0.64,0.17,1.0),flow);
    vec3 energy=mix(violet,vec3(0.94,0.85,1.0),clamp(core,0.0,1.0));
    float alpha=edge*vertexColor.a*(0.55+flow*0.45);
    if(EffectMode>0.5){
        float impact=smoothstep(4.45,4.6,EffectTime);
        energy=mix(energy,mix(vec3(.35,.07,.8),vec3(.97,.9,1.0),.2+core*.7),impact);
        alpha=mix(alpha,vertexColor.a*(.12+pow(flow,.7)*.25+core*.6),impact);
    }
    if(EffectMode>1.5){float d=abs(p.y-.5);float white=exp(-d*d*35.0);energy=mix(vec3(.45,.08,1.0),vec3(1.0,.96,1.0),white);alpha=vertexColor.a*exp(-d*d*18.0);}
    if(EffectMode>2.5){
        float strands=pow(max(0.,sin(p.x*110.+fbm(p*12.-EffectTime)*4.-EffectTime*2.)),8.);
        float top=1.-smoothstep(.7,1.,p.y);
        energy=mix(vec3(.38,.045,.9),vec3(1.,.96,1.),strands*.8+flow*.25);
        alpha=vertexColor.a*(.1+strands*.5+flow*.16)*top;
    }
    if(alpha<0.008) discard;
    fragColor=vec4(energy*vertexColor.rgb,alpha);
}
