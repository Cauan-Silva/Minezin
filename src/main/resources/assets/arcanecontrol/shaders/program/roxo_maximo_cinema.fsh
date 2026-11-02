#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform vec2 InSize;
uniform float RoxoTick;
uniform float Strength;
uniform float Frames;
uniform vec2 ScreenCenter;
in vec2 texCoord;
out vec4 fragColor;
float luma(vec3 c){return dot(c,vec3(.2126,.7152,.0722));}
float hash(float n){return fract(sin(n*127.1+311.7)*43758.5453);}
vec2 safeUV(vec2 p){return clamp(p,.5/InSize,1.-.5/InSize);}
vec3 scene(vec2 p){return texture(DiffuseSampler,safeUV(p)).rgb;}
float edgeAt(vec2 uv){
    vec2 px=1.5/InSize;float z=texture(DepthSampler,safeUV(uv)).r;
    float dz=(abs(z-texture(DepthSampler,safeUV(uv+vec2(px.x,0))).r)+abs(z-texture(DepthSampler,safeUV(uv+vec2(0,px.y))).r))/max(.00003,1.-z);
    float dc=abs(luma(scene(uv))-luma(scene(uv+vec2(px.x,0))))+abs(luma(scene(uv))-luma(scene(uv+vec2(0,px.y))));
    return clamp(dz*3.+dc*2.5,0.,1.);
}
void main(){
    float t=RoxoTick;vec3 original=scene(texCoord);
    vec2 delta=texCoord-ScreenCenter,aspect=vec2(InSize.x/InSize.y,1.);
    vec2 p=delta*aspect,dir=normalize(delta+vec2(.000001));float r=length(p),a=atan(p.y,p.x);
    float charge=smoothstep(52.,87.,t)*(1.-step(90.,t));
    float blast=step(90.,t)*exp(-max(0.,t-90.)/9.);
    float front=max(0.,t-90.)*.075;
    float ring=exp(-pow((r-front)/.06,2.))*blast;
    vec2 uv=texCoord+dir*(charge*.022*exp(-r*3.)+ring*.035)*Strength;
    float split=(charge*.0015+blast*.009)*Strength;
    vec3 c=vec3(scene(uv+dir*split).r,scene(uv).g,scene(uv-dir*split).b);
    c=mix(c,vec3(luma(c))*.55,charge*.72);
    float hush=smoothstep(80.,88.,t)*(1.-step(90.,t));
    c*=1.-hush*(Frames>.5?.975:.22);
    c+=vec3(.42,.04,.8)*edgeAt(uv)*charge*.13;
    vec3 bloom=vec3(0.);
    for(int i=1;i<=8;i++){
        float u=float(i)/8.;bloom+=max(scene(uv-dir*u*(.02+blast*.09))-vec3(.58),vec3(0.))*(1.-u*.5);
    }
    c+=bloom*(.15+blast*1.9)*exp(-r*.5);
    float lines=pow(max(0.,sin(a*113.+hash(floor(a*47.))*4.)),22.);
    c+=mix(vec3(.65,.07,1.),vec3(1.,.9,1.),blast)*lines*smoothstep(.13,.5,r)*blast*.95;
    c+=vec3(.65,.17,1.)*ring*.8;
    if(Frames>.5&&t>=90.&&t<93.){
        float edge=edgeAt(texCoord),ink=step(.38,luma(original));
        if(t<90.7)c=mix(vec3(.99,.95,1.),vec3(.035,.002,.075),clamp(edge*.85+ink*.28,0.,1.));
        else if(t<91.6)c=vec3(.003,.001,.012)+vec3(.95,.86,1.)*clamp(edge*1.4+ink*.08,0.,1.);
        else c=mix(c,vec3(.98,.88,1.),(1.-smoothstep(91.6,93.,t))*.75);
    }
    fragColor=vec4(mix(original,clamp(c,0.,1.),clamp(Strength,0.,1.)),1.);
}
