package com.cauansilva.arcanecontrol.core.cid;
/** Independent spring responses per panel and for the collar; no render-time integration. */
public final class MantoDirecionalCid {
    private final MolaMantoCid[] paineis=new MolaMantoCid[5];
    private final MolaMantoCid gola=new MolaMantoCid(1.8);
    public MantoDirecionalCid(){for(int i=0;i<5;i++)paineis[i]=new MolaMantoCid(.88+i*.06);}
    public void reset(){for(var p:paineis)p.reset();gola.reset();}
    public void tick(MolaMantoCid.Entrada e){
        for(int i=0;i<5;i++){
            double diferenca=1+(i-2)*.025;
            paineis[i].tick(new MolaMantoCid.Entrada(e.frente(),e.lado()*diferenca,e.vertical(),e.giro()*diferenca,e.noChao()));
        }
        gola.tick(e);
    }
    public MolaMantoCid.Pose amostrar(int painel,int segmento,double parcial){if(painel<0||painel>=5)throw new IllegalArgumentException("Painel inválido");return paineis[painel].amostrar(segmento,parcial);}
    public MolaMantoCid.Pose detalhe(String osso,double parcial){
        var base=osso.startsWith("cid_collar_")?gola.amostrar(0,parcial):paineis[2].amostrar(2,parcial);
        return DetalhesTrajeCid.pose(osso,base);
    }
}
