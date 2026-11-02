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
    } else if(m<6.5){
        float core=exp(-pow((uv.y-.5)*20.0,2.0));
        float pulse=.6+.4*noise(vec2(uv.x*12.0-t*2.0,uv.y*3.0));
        color=mix(color,vec3(1.0,.97,.9),core*.8);
        alpha*=pulse*(1.0-smoothstep(.3,.5,abs(uv.y-.5)));
    }
    if(m>=6.5&&m<7.5){
        float d=abs(uv.y-.5),warp=(fbm(uv*vec2(5.,9.)-vec2(t*2.,t))-.5)*.12;
        float rim=exp(-pow((d-.34+warp)*35.,2.));
        color=mix(vertexColor.rgb*.07,vec3(.88,.035,.14),rim);
        color+=vec3(.6,.06,.12)*pow(noise(uv*24.-t),8.);
        alpha=vertexColor.a*(1.-smoothstep(.37,.5,d));
    }else if(m>=7.5&&m<8.5){
        color=vertexColor.rgb*(.85+.15*noise(uv*4.));alpha=vertexColor.a;
    }
    if(m>=8.5&&m<9.5){
        float bend=fbm(vec2(uv.x*9.-t*.55,uv.y*7.+t*.12));
        float filament=pow(max(0.,sin(uv.x*38.-uv.y*17.+bend*4.-t*1.6)),12.);
        float edge=pow(max(0.,sin(clamp(uv.y,0.,1.)*PI)),1.4);
        color=mix(vertexColor.rgb*.25,vec3(.83,.88,1.),filament*.8);
        alpha*=edge*(.08+filament*.65);
    }else if(m>=9.5&&m<10.5){
        float d=abs(uv.y-.5),tip=pow(max(0.,sin(clamp(uv.x/4.,0.,1.)*PI)),.3);
        float core=exp(-d*d*240.);
        color=mix(vertexColor.rgb,vec3(1.,.8,.84),core*.8);
        alpha*=exp(-d*d*45.)*tip;
    }
    if(m>=10.5&&m<11.5){
        float u=clamp(uv.x,0.,1.),v=uv.y;
        float flow=fbm(vec2(u*9.-t*2.7,v*6.+t*.65));
        float edge=abs(v-.5)+(flow-.5)*.22;
        float silhouette=1.-smoothstep(.28,.51,edge);
        float veins=pow(max(0.,sin(u*39.+flow*5.-t*7.+v*11.)),9.);
        float torn=smoothstep(.11,.36,fbm(vec2(u*24.-t*1.8,v*12.)));
        color=mix(vertexColor.rgb*.12,vertexColor.rgb,veins*.65+pow(clamp(edge*2.,0.,1.),3.)*.7);
        alpha*=silhouette*torn*pow(max(0.,sin(u*PI)),.3);
    }else if(m>=11.5&&m<12.5){
        float core=exp(-pow((uv.y-.5)*13.,2.));
        float travelling=.55+.45*pow(.5+.5*sin(uv.x*32.-t*11.),4.);
        color=mix(vertexColor.rgb,vec3(.95,.85,1.),core*.7);
        alpha*=core*travelling;
    }
    if(m>=12.5&&m<13.5){
        // A dense, flowing column with an incandescent core and a turbulent violet mantle.
        float theta=uv.x*6.2831853;
        vec2 q=vec2(sin(theta),cos(theta))*3.+vec2(uv.y*13.,-t*2.7);
        float turbulence=fbm(q+fbm(q*1.8)*2.);
        float strata=pow(.5+.5*sin(uv.y*58.-t*13.+turbulence*5.),3.);
        float veins=pow(.5+.5*sin(theta*11.+uv.y*18.-t*6.+turbulence*4.),5.);
        float hot=clamp(strata*.5+veins*.45+turbulence*.35,0.,1.);
        color=mix(vertexColor.rgb*.65,vec3(1.,.88,1.),hot);
        alpha=vertexColor.a*(.7+.3*turbulence)*(1.-smoothstep(.88,1.,uv.y));
    }else if(m>=13.5&&m<14.5){
        float d=abs(uv.y-.5),flow=fbm(vec2(uv.x*19.-t*3.,uv.y*5.+t));
        float ridge=exp(-pow((d+(flow-.5)*.12)*9.,2.));
        color=mix(vertexColor.rgb,vec3(1.,.92,1.),ridge*.65);
        alpha*=ridge*(.7+.3*flow);
    }
    if(m>=14.5&&m<15.5){
        float d=abs(uv.y-.5),core=exp(-d*d*160.),halo=exp(-d*d*12.);
        color=mix(vertexColor.rgb*1.4,vec3(1.,.92,1.),core);
        alpha*=clamp(core+halo*.25,0.,1.);
    }
    if(m>=15.5&&m<16.5){
        // Singularity glow: uv.x runs 0..8 around the core, uv.y from the core edge (0) outwards (1).
        float r=clamp(uv.y,0.,1.),ang=uv.x*PI*.25;
        float flick=fbm(vec2(cos(ang),sin(ang))*2.5+vec2(r*4.-t*2.,t*.7));
        float rim=exp(-r*r*120.),bloom=pow(1.-r,2.6);
        color=mix(vec3(1.,.3,.08),vec3(1.,.8,.28),bloom);
        color=mix(color,vec3(1.,.97,.82),rim);
        alpha=clamp(alpha*(rim+bloom*(.5+.5*flick)),0.,1.);
    }else if(m>=16.5&&m<17.5){
        // Flare streak through the singularity: white-hot middle, ragged red brush edges, tapered tips.
        float x=clamp(uv.x,0.,1.),d=abs(uv.y-.5)*2.;
        float rag=fbm(vec2(x*30.-t*7.,uv.y*3.+t));
        float taper=pow(sin(x*PI),.8),w=taper*(.5+.5*rag);
        float core=exp(-pow(d/(w*.3+.02),2.)),mid=exp(-pow((x-.5)*5.,2.));
        float body=1.-smoothstep(w*.65,w+.001,d);
        color=mix(vec3(.95,.13,.05),vec3(1.,.78,.28),clamp(core*.8+mid*.5,0.,1.));
        color=mix(color,vec3(1.,.98,.86),core*mid);
        alpha=clamp(alpha*(body*.85+core)*taper,0.,1.);
    }else if(m>=17.5&&m<18.5){
        // Burst spike: broad base narrowing to a needle, near-white along the spine.
        float x=clamp(uv.x,0.,1.),d=abs(uv.y-.5)*2.,w=pow(1.-x,1.3)+.04;
        float spine=exp(-pow(d/(w*.3),2.));
        color=mix(vertexColor.rgb,vec3(1.,.96,1.),spine*.85);
        alpha*=(1.-smoothstep(w*.75,w,d))*(.6+.4*spine);
    }
    if(m>=18.5&&m<19.5){
        // Revealed mana column: soft translucent body with brighter rims, flowing upwards, fading at the top.
        float x=clamp(uv.x,0.,1.),d=abs(uv.y-.5)*2.;
        float flow=fbm(vec2(uv.y*3.,x*6.-t*1.5));
        float rim=exp(-pow((d-.85)*6.,2.));
        float body=exp(-d*d*2.2)*.55+rim*.45;
        color=mix(vertexColor.rgb,vec3(.95,.98,1.),flow*.35+rim*.4);
        alpha*=body*(.7+.5*flow)*(1.-smoothstep(.55,1.,x))*smoothstep(0.,.04,x)*(1.-smoothstep(.98,1.,d));
    }
    if(m>=19.5&&m<20.5){
        // Cel-shaded wind band: hard light/shadow split and torn edges, like the anime's dust tornado.
        float d=abs(uv.y-.5)*2.,n=fbm(vec2(uv.x*3.-t*2.,uv.y*4.));
        color=mix(vertexColor.rgb*.68,vertexColor.rgb,step(.5,n+(uv.y-.5)*.6));
        float torn=smoothstep(.25,.55,fbm(vec2(uv.x*9.-t*3.,uv.y*6.)));
        alpha*=(1.-smoothstep(.6,1.,d+(n-.5)*.5))*(.5+.5*torn);
    }
    if(m>=20.5&&m<21.5){
        // Qual's original killing magic: near-black core, violet halo and a grainy sparkling edge.
        float d=abs(uv.y-.5)*2.,g=noise(vec2(uv.x*40.-t*20.,uv.y*30.));
        float core=1.-smoothstep(.35,.55,d+(g-.5)*.15);
        float edge=smoothstep(.3,.6,d)*(1.-smoothstep(.85,1.,d));
        float spark=step(.82,noise(vec2(uv.x*90.-t*35.,uv.y*60.+t*9.)))*edge;
        color=mix(vertexColor.rgb,vec3(.04,.02,.07),core)+vec3(.9,.85,1.)*spark;
        alpha*=clamp(max(core,edge*.55+spark),0.,1.);
    }
    if(alpha<.004)discard;fragColor=vec4(color,alpha);
}
