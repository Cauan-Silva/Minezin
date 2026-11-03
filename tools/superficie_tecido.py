"""Python twin of SuperficieTecido.java (keep in step): the Cid coat as one stitched sheet over the posed cloth panels
and the collar as a curved shell, for the offline previews. Same order, same averaging, same hem teeth."""
import math
import numpy as np

SEGMENTOS,COLUNAS_COSTAS=6,6
PRETO,ROXO,OURO=0,1,2
ORDEM=['frente_dir_0','quinaf_dir_0','lado_dir_0','quina_dir_0']+[f'costas_{c}' for c in range(COLUNAS_COSTAS)]+['quina_esq_0','lado_esq_0','quinaf_esq_0','frente_esq_0']
def osso(p,s):return f'bk_capa_{p}_{s}'
def _d(a,b):return float(np.linalg.norm(np.asarray(a)-np.asarray(b)))
def _lerp(a,b,t):return np.asarray(a)+(np.asarray(b)-np.asarray(a))*t

def rotular(rep,dir_):
    """Corner labels from the rest vertices (never swap in motion): indices for top-start, top-end, bottom-start, bottom-end."""
    rep=np.asarray(rep,float);lo,hi=rep.min(0),rep.max(0);c=(lo+hi)/2;base=math.atan2(c[0],c[2]);g=[[],[],[],[]]
    for i,p in enumerate(rep):
        ang=math.atan2(p[0],p[2])-base
        while ang>math.pi:ang-=2*math.pi
        while ang<-math.pi:ang+=2*math.pi
        g[(0 if p[1]>c[1] else 2)+(0 if ang*dir_<0 else 1)].append(i)
    return g
def direcao(repouso):
    soma=0;ant=None
    for p in ORDEM:
        r=repouso.get(osso(p,0))
        if r is None:continue
        cc=np.mean(r,axis=0);a=math.atan2(cc[0],cc[2])
        if ant is not None:
            d=a-ant
            while d>math.pi:d-=2*math.pi
            while d<-math.pi:d+=2*math.pi
            soma+=d
        ant=a
    return 1 if soma>=0 else -1
def dente(c,k):return .35+.75*(((c*37+k*53+11)%10)/9.0)

def caixa(rep,pos):
    rep=np.asarray(rep,float);pos=np.asarray(pos,float);lo,hi=rep.min(0),rep.max(0)
    alvo=[lo,[hi[0],lo[1],lo[2]],[lo[0],hi[1],lo[2]],[lo[0],lo[1],hi[2]]]
    ids=[int(np.argmin(np.linalg.norm(rep-np.asarray(a),axis=1))) for a in alvo];o=pos[ids[0]]
    return (o,pos[ids[1]]-o,pos[ids[2]]-o,pos[ids[3]]-o)
def fora(p,c):
    o,*e=c;d=np.asarray(p)-o;t=[]
    for k in range(3):
        l2=float(e[k]@e[k])
        if l2<1e-12:return p
        t.append(float(d@e[k])/l2)
    m=.06
    if t[1]<0 or t[1]>1:return p
    eixo,melhor,alvo=-1,1e18,0
    for k in (0,2):
        if t[k]<-m or t[k]>1+m:return p
        a,b=t[k]+m,1+m-t[k]
        if a<melhor:melhor,eixo,alvo=a,k,-m
        if b<melhor:melhor,eixo,alvo=b,k,1+m
    return np.asarray(p)+e[eixo]*(alvo-t[eixo])
def colidir(p,obst):
    for c in obst:p=fora(p,c)
    return p
def casaco(posados,repouso,pernas=()):
    S=SEGMENTOS;pecas=[p for p in ORDEM if all(osso(p,s) in posados and osso(p,s) in repouso for s in range(S))];out=[];n=len(pecas)
    if n<2:return out
    d=direcao(repouso);meio=[]
    for p in pecas:
        linha=[]
        for s in range(S):
            o=osso(p,s);idx=rotular(repouso[o],d);v=np.asarray(posados[o],float)
            linha.append([v[ix].mean(0) if len(ix) else v[0] for ix in idx])
        meio.append(linha)
    g=[[None]*(n+1) for _ in range(S+1)]
    for r in range(S+1):
        for c in range(n+1):
            acc=[]
            for i in (c-1,c):
                if 0<=i<n:
                    lado=0 if i==c else 1
                    if r<S:acc.append(meio[i][r][lado])
                    if r>0:acc.append(meio[i][r-1][2+lado])
            g[r][c]=colidir(np.mean(acc,axis=0),pernas)
    for r in range(S):
        for c in range(n):out+=[(g[r][c],g[r][c+1],g[r+1][c+1],PRETO),(g[r][c],g[r+1][c+1],g[r+1][c],PRETO)]
    for c in range(n):
        a,b=g[S][c],g[S][c+1];da,db=a-g[S-1][c],b-g[S-1][c+1]
        l1,l2,l3=dente(c,0),dente(c,1),dente(c,2)*.35
        m,dm=_lerp(a,b,.5),_lerp(da,db,.5)
        t1=colidir(_lerp(a,m,.45)+_lerp(da,dm,.45)*l1,pernas);t2=colidir(_lerp(m,b,.55)+_lerp(dm,db,.55)*l2,pernas);nt=colidir(m+dm*l3,pernas)
        out+=[(a,m,nt,PRETO),(a,nt,t1,PRETO),(m,b,nt,PRETO),(nt,b,t2,PRETO)]
    return out

GOLA_COLUNAS,GOLA_LINHAS,GOLA_ABERTURA,GOLA_BASE,GOLA_RAIO,GOLA_ALTURA,GOLA_ESPESSURA=32,16,28,21.6,6.8,10.4,.32
GOLA_OMBRO_X,GOLA_OMBRO_Z,GOLA_CABECA,GOLA_CABECA_MEIA=4.1,2.15,24.2,4.75
def altura_gola(u):
    lim=180-GOLA_ABERTURA;t=max(0,(abs(u)-(lim-34))/34)
    return GOLA_ALTURA*(.6+.4*math.cos(math.radians(u)/2)**2)*(.22+.78*math.sqrt(max(0,1-t*t)))
def quadrado(a,m,pw):return m/(abs(math.sin(a))**pw+abs(math.cos(a))**pw)**(1/pw)
def ponto_gola(u,v,prof):
    a=math.radians(u);sx,cz=math.sin(a),math.cos(a);y=GOLA_BASE+altura_gola(u)*v
    tronco=min(GOLA_OMBRO_X/max(1e-6,abs(sx)),GOLA_OMBRO_Z/max(1e-6,abs(cz)));cab=quadrado(a,GOLA_CABECA_MEIA,6)
    if y<GOLA_CABECA:t=(y-GOLA_BASE)/(GOLA_CABECA-GOLA_BASE);t=t*t*(3-2*t);r=tronco+(cab-tronco)*t
    else:t=min(1,(y-GOLA_CABECA)/(GOLA_BASE+GOLA_ALTURA-GOLA_CABECA));r=cab+(GOLA_RAIO-cab)*t**1.5
    r-=prof*GOLA_ESPESSURA
    return np.array([r*sx,y,r*cz])
def gola():
    out=[];lim=180-GOLA_ABERTURA;P=ponto_gola
    def quad(a,b,c,d,m):out.extend([(a,b,c,m),(a,c,d,m)])
    for i in range(GOLA_COLUNAS):
        u0=-lim+2*lim*i/GOLA_COLUNAS;u1=-lim+2*lim*(i+1)/GOLA_COLUNAS
        for j in range(GOLA_LINHAS):
            v0,v1=j/GOLA_LINHAS,(j+1)/GOLA_LINHAS;topo=j==GOLA_LINHAS-1
            quad(P(u0,v0,0),P(u1,v0,0),P(u1,v1,0),P(u0,v1,0),OURO if topo else PRETO)
            quad(P(u1,v0,1),P(u0,v0,1),P(u0,v1,1),P(u1,v1,1),OURO if topo else ROXO)
        quad(P(u0,1,0),P(u1,1,0),P(u1,1,1),P(u0,1,1),OURO)
    for u in (-lim,lim):
        for j in range(GOLA_LINHAS):
            v0,v1=j/GOLA_LINHAS,(j+1)/GOLA_LINHAS;quad(P(u,v0,0),P(u,v0,1),P(u,v1,1),P(u,v1,0),OURO)
    return out

def normal(t):
    n=np.cross(np.asarray(t[1])-t[0],np.asarray(t[2])-t[0]);l=np.linalg.norm(n);return n/l if l>1e-12 else np.array([0,1,0.])
