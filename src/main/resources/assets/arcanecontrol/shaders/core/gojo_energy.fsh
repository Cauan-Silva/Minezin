#version 150
uniform float EffectTime;
uniform float EffectMode;
uniform vec3 Viewer;
in vec4 vertexColor;
in vec2 energyUV;
out vec4 fragColor;
const float PI=3.14159265;
float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float noise(vec2 p){
    vec2 i=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);
    return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y);
}
float fbm(vec2 p){return noise(p)*.57+noise(p*2.03+7.1)*.28+noise(p*4.07+13.7)*.15;}
vec3 sphereNormal(vec2 uv){
    float a=uv.x*2.0*PI,lat=(uv.y-.5)*PI;
    return vec3(cos(a)*cos(lat),sin(lat),sin(a)*cos(lat));
}
void main(){
    vec2 uv=energyUV;
    float time=EffectTime,mode=EffectMode;
    vec3 color=vertexColor.rgb;
    float alpha=vertexColor.a;
    if(mode<2.5){
        vec3 n=sphereNormal(uv);
        float facing=abs(dot(n,normalize(Viewer)));
        float rim=pow(1.0-facing,1.6);
        vec2 p=vec2(n.x+n.z,n.y)*5.0;
        float flow=fbm(p+vec2(time*.9,-time*.6));
        float veins=pow(1.0-abs(sin(flow*18.0+time*2.0+n.y*9.0)),14.0);
        if(mode<.5){
            // Azul: volume escuro que absorve, borda ciano e fluxo espiral convergente.
            float spiral=pow(.5+.5*sin(atan(n.z,n.x)*7.0+n.y*18.0+time*4.0),10.0);
            color=mix(vec3(.001,.008,.025),vec3(.04,.5,1.0),rim*.9);
            color+=vec3(.1,.65,1.0)*(veins*.07+spiral*rim*.6);
        } else if(mode<1.5){
            // Vermelho: fonte compacta branca, halo carmesim e linhas que irradiam.
            float radial=pow(.5+.5*sin(atan(n.z,n.x)*11.0+n.y*7.0-time*8.0),12.0);
            color=mix(vec3(1.0,.025,.035),vec3(1.0,.88,.74),pow(facing,5.0));
            color+=vec3(.8,.12,.1)*(veins*.4+radial*rim*.6);
        } else {
            // Roxo: massa violeta, laminas claras e residuos azuis/vermelhos sem arco-iris.
            float bands=pow(.5+.5*sin(n.y*29.0+flow*14.0-time*4.0),9.0);
            float volume=fbm(p*1.4+vec2(time*.6,-time*1.1));
            float core=pow(facing,2.0)*.65;
            color=mix(vec3(.14,.008,.3),vec3(.56,.15,.95),volume*.6+core);
            color+=vec3(.46,.22,.65)*(bands*.25+veins*.12);
            color=mix(color,vec3(.85,.46,1.0),core*.45+veins*.18);
        }
    } else if(mode<3.5){
        // Infinito: discreto, mais visivel nos pontos de aproximacao.
        float wave=pow(.5+.5*cos(uv.x*42.0-time*3.0+sin(uv.y*17.0)),18.0);
        float edge=pow(max(0.0,sin(uv.y*PI)),2.0);
        color=vec3(.55,.8,1.0);alpha*=wave*edge;
    } else if(mode<4.5){
        // Vazio Infinito: estrelas estaveis, nebulosa lenta e linhas de informacao.
        vec2 sky=vec2(uv.x*2.0,uv.y);
        float cloud=fbm(sky*7.0+vec2(time*.015,0));
        vec2 grid=sky*110.0,id=floor(grid),p=fract(grid)-.5;
        float seed=hash(id);
        float star=(1.0-smoothstep(.02,.13,length(p)))*step(.94,seed);
        float band=exp(-pow((uv.y-.46+sin(uv.x*12.0)*.07)*13.0,2.0));
        color=vec3(.001,.002,.012)+vec3(.035,.075,.18)*cloud*band;
        color+=vec3(.65,.82,1.0)*star*(.8+.2*sin(time*.7+seed*19.0));
        color+=vec3(.18,.24,.46)*pow(max(0.0,cloud-.2),2.0)*band;
    } else if(mode<5.5){
        // Fitas: material compartilhado; cor e movimento definidos pela tecnica.
        float warp=(fbm(vec2(uv.x*2.0-time*1.6,uv.y*3.0))-.5)*.18;
        float d=abs(uv.y-.5+warp);
        float core=exp(-d*d*800.0),edge=exp(-d*d*35.0);
        float pulse=.55+.45*fbm(vec2(uv.x*5.0-time*4.0,uv.y*4.0));
        color=mix(color,color*.4+vec3(.65),core*.65);
        alpha*=edge*pulse*(1.0-smoothstep(.35,.5,abs(uv.y-.5)));
    } else if(mode<6.5){
        vec3 n=sphereNormal(uv);
        float rim=pow(1.0-abs(dot(n,normalize(Viewer))),3.5);
        color*=.7+fbm(vec2(n.x+n.z,n.y)*8.0+time*.2)*.3;
        alpha*=rim*.6;
    } else {
        vec3 n=sphereNormal(uv);
        float f=fbm(vec2(n.x+n.z,n.y)*9.0+time*.05);
        color=vec3(.004,.005,.012)+vec3(.008,.012,.025)*pow(f,3.0);
    }
    if(alpha<.004) discard;
    fragColor=vec4(color,alpha);
}
