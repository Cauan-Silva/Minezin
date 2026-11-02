#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform vec2 InSize;
uniform float AtomicTick;
uniform float Strength;
uniform float Frames;
uniform vec2 ScreenCenter;
in vec2 texCoord;
out vec4 fragColor;
const vec3 VIOLET=vec3(.58,.12,1.);
float hash(float n){return fract(sin(n*127.1+311.7)*43758.5453);}
float luma(vec3 c){return dot(c,vec3(.2126,.7152,.0722));}
vec2 safeUV(vec2 uv){return clamp(uv,.5/InSize,1.-.5/InSize);}
vec3 scene(vec2 uv){return texture(DiffuseSampler,safeUV(uv)).rgb;}
float edgeAt(vec2 uv){
    vec2 px=1.5/InSize;float z=texture(DepthSampler,safeUV(uv)).r;
    float zx=texture(DepthSampler,safeUV(uv+vec2(px.x,0.))).r;
    float zy=texture(DepthSampler,safeUV(uv+vec2(0.,px.y))).r;
    float dz=(abs(z-zx)+abs(z-zy))/max(.00003,1.-z);
    float c=luma(scene(uv));float dc=abs(c-luma(scene(uv+vec2(px.x,0.))))+abs(c-luma(scene(uv+vec2(0.,px.y))));
    return clamp(dz*3.+dc*2.5,0.,1.);
}
void main(){
    vec3 original=scene(texCoord);float t=AtomicTick;
    float compression=smoothstep(50.,88.,t)*(1.-smoothstep(90.,102.,t));
    float blast=step(90.,t)*exp(-max(0.,t-90.)/24.);
    vec2 delta=texCoord-ScreenCenter;vec2 aspect=vec2(InSize.x/InSize.y,1.);vec2 p=delta*aspect;
    float r=length(p),a=atan(p.y,p.x);vec2 dir=normalize(delta+vec2(.000001));
    float front=max(0.,t-90.)*.025;
    float ring=exp(-pow((r-front)/.025,2.))*blast;
    vec2 uv=texCoord+dir*(compression*.014*exp(-r*3.)+ring*.018)*Strength;
    float tear=step(90.,t)*(1.-step(98.,t))*Frames;
    uv.x+=(hash(floor(texCoord.y*56.)+floor(t*3.)*71.)-.5)*.014*tear*Strength;
    float split=(.0007+blast*.006)*Strength;
    vec3 c=vec3(scene(uv+dir*split).r,scene(uv).g,scene(uv-dir*split).b);
    float charge=smoothstep(0.,75.,t)*(1.-smoothstep(90.,105.,t));
    float lum=luma(c);c=mix(c,vec3(lum),charge*.4);
    c*=1.-charge*(.55+.2*smoothstep(.1,.75,r));
    c+=VIOLET*compression*.07*exp(-r*5.);
    // Bounded radial taps bloom around the actual photographed energy, not a GUI flash.
    vec3 bloom=vec3(0.);
    for(int i=1;i<=8;i++){
        float f=float(i)/8.;vec3 tap=scene(uv-dir*f*(.015+blast*.055));
        bloom+=max(tap-vec3(.68),vec3(0.))*(1.-f*.55);
    }
    c+=bloom*(.18+blast*1.3)*exp(-r*.8);
    float streaks=pow(max(0.,sin(a*92.+hash(floor(a*40.))*3.)),24.);
    float radial=streaks*smoothstep(.12,.5,r)*(1.-smoothstep(.65,1.2,r));
    c+=mix(VIOLET,vec3(.96,.88,1.),blast)*radial*blast*.65;
    c+=VIOLET*ring*.32;
    if(Frames>.5&&t>=88.&&t<96.){
        float edge=edgeAt(uv);
        if(t>=88.&&t<90.){
            // Negative anticipation: the scene, its contours and the radial field all change.
            c=vec3(1.-smoothstep(.08,.42,luma(scene(uv))))*.68+VIOLET*radial*.7;
        }else if(t>=90.&&t<91.25){
            float ink=1.-smoothstep(.12,.28,luma(scene(uv)));ink=max(ink,edge*.8);
            c=mix(vec3(.99,.98,1.),vec3(.015,.005,.028),ink);
            c=mix(c,vec3(1.),exp(-r*20.)*.7);
        }else if(t>=91.25&&t<92.5){
            float ink=1.-smoothstep(.13,.32,luma(scene(uv)));
            c=mix(vec3(.008,.002,.02),vec3(.95,.84,1.),max(ink,edge));
            c+=radial*VIOLET*.8;
        }else if(t>=92.5&&t<94.){
            float ink=smoothstep(.08,.35,luma(scene(uv)));c=mix(vec3(.05,.005,.11),vec3(.76,.32,1.),ink);
            c+=vec3(.95,.86,1.)*(edge*.6+radial*.8);
        }else if(t>=94.&&t<96.){
            c=mix(vec3(.97,.95,1.),vec3(.025,.005,.065),edge);
            float cut=abs(p.x*.8+p.y*.6);c=mix(c,vec3(.3,.01,.65),1.-smoothstep(.006,.018,cut));
            c-=radial*.65;
        }
    }
    // Residual tint fades to identity before tick 240; no persistent color transform.
    float ash=smoothstep(125.,175.,t)*(1.-smoothstep(190.,240.,t));
    c=mix(c,mix(vec3(luma(c)),c,.7)+VIOLET*.018,ash*.32);
    fragColor=vec4(mix(original,clamp(c,0.,1.),clamp(Strength,0.,1.)),1.);
}
