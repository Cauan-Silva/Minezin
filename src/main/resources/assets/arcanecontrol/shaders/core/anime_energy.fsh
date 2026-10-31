#version 150
uniform float EffectTime;
uniform float EffectMode;
uniform vec3 Viewer;
in vec4 vertexColor;
in vec2 energyUV;
out vec4 fragColor;
const float PI=3.14159265;
float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float noise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y);}
float fbm(vec2 p){return noise(p)*.6+noise(p*2.03+7.1)*.27+noise(p*4.07+13.7)*.13;}
vec3 normalSphere(vec2 uv){float a=uv.x*2.0*PI,l=(uv.y-.5)*PI;return vec3(cos(a)*cos(l),sin(l),sin(a)*cos(l));}
void main(){
    vec2 uv=energyUV;float t=EffectTime,m=EffectMode;
    vec3 color=vertexColor.rgb;float alpha=vertexColor.a;
    if(m<.5){
        float warp=(fbm(vec2(uv.x*5.0-t*3.0,uv.y*6.0))-.5)*.22;
        float d=abs(uv.y-.5+warp);
        float border=exp(-pow((d-.33)*24.0,2.0));
        color=mix(vec3(.008,.003,.025),vec3(.55,.15,1.0),border);
        alpha*=1.0-smoothstep(.34,.5,d);
    } else if(m<1.5){
        float d=abs(uv.y-.5);
        float core=exp(-d*d*180.0),edge=exp(-d*d*26.0);
        color=mix(color,vec3(.94,.93,1.0),core);
        alpha*=edge*(1.0-smoothstep(.32,.5,d));
    } else if(m<2.5){
        vec3 n=normalSphere(uv);float rim=pow(1.0-abs(dot(n,normalize(Viewer))),2.0);
        vec2 p=vec2(uv.x*32.0,uv.y*16.0),r=vec2(1.7320508,1.0);
        vec2 a=mod(p,r)-r*.5,b=mod(p-r*.5,r)-r*.5;
        vec2 q=abs(dot(a,a)<dot(b,b)?a:b);
        float edge=max(q.x*.8660254+q.y*.5,q.y);
        float line=1.0-smoothstep(.015,.055,abs(edge-.5));
        color=mix(color,vec3(.95,.96,1.0),line*.7);
        alpha*=(.03+line*.4+rim*.5);
    } else if(m<3.5){
        vec3 n=normalSphere(uv);float rim=pow(1.0-abs(dot(n,normalize(Viewer))),5.0);
        color=vec3(.0005,.0003,.002)+vertexColor.rgb*rim*.6;
    } else if(m<4.5){
        float flow=fbm(vec2(uv.x*2.5-t*.7,uv.y*4.0));
        float bands=.5+.5*sin(uv.y*28.0+flow*2.2-t*2.0);
        float border=pow(sin(clamp(uv.y,0.0,1.0)*PI),.6);
        float streams=.65+.35*sin(uv.x*1.4-t*3.0+uv.y*5.0);
        color=mix(vec3(.25,.12,.045),vec3(1.0,.87,.58),.4+bands*.35+flow*.2);
        alpha*=border*(.45+bands*.35)*streams;
    } else if(m<5.5){
        float edge=pow(1.0-abs(uv.x-.5)*2.0, .25);
        float dissolve=fbm(uv*13.0+vec2(t*.8,-t*.2));
        color=mix(vec3(.008,.004,.018),vertexColor.rgb,.12+pow(1.0-edge,3.0));
        alpha*=smoothstep(.15,.5,dissolve)*(1.0-smoothstep(.43,.5,abs(uv.x-.5)));
    } else {
        float core=exp(-pow((uv.y-.5)*20.0,2.0));
        float pulse=.6+.4*noise(vec2(uv.x*12.0-t*2.0,uv.y*3.0));
        color=mix(color,vec3(1.0,.97,.9),core*.8);
        alpha*=pulse*(1.0-smoothstep(.3,.5,abs(uv.y-.5)));
    }
    if(alpha<.004)discard;fragColor=vec4(color,alpha);
}
