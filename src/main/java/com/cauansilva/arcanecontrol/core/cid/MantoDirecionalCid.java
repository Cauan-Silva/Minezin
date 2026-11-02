package com.cauansilva.arcanecontrol.core.cid;
/** Connected particle cloth for the cape; firm angular springs for the collar/front ornaments. */
public final class MantoDirecionalCid {
    private final com.cauansilva.arcanecontrol.core.client.tecido.MalhaTecido tecido=new com.cauansilva.arcanecontrol.core.client.tecido.MalhaTecido(5,5,.15,5.5/16);
    private final MolaMantoCid esquerda=new MolaMantoCid(1.25),direita=new MolaMantoCid(1.05);
    private final MolaMantoCid gola=new MolaMantoCid(1.8);
    public MantoDirecionalCid(){}
    public void reset(){tecido.reset();gola.reset();esquerda.reset();direita.reset();}
    public void tick(MolaMantoCid.Entrada e){
        tecido.tick(e.frente(),e.lado(),e.vertical(),e.giro());
        gola.tick(e);
        esquerda.tick(new MolaMantoCid.Entrada(e.frente(),e.lado()+e.giro()*.15,e.vertical(),e.giro(),e.noChao()));
        direita.tick(new MolaMantoCid.Entrada(e.frente(),e.lado()-e.giro()*.15,e.vertical(),e.giro(),e.noChao()));
    }
    public MolaMantoCid.Pose amostrar(int painel,int segmento,double parcial){if(painel<0||painel>=5)throw new IllegalArgumentException("Painel inválido");var r=tecido.rotacao(painel,segmento,parcial);return new MolaMantoCid.Pose(r.x(),-r.y(),-r.z());}
    public MolaMantoCid.Pose detalhe(String osso,double parcial){
        var base=osso.startsWith("cid_collar_")?gola.amostrar(0,parcial):(osso.equals("aba_esq")?esquerda:direita).amostrar(2,parcial);
        return DetalhesTrajeCid.pose(osso,base);
    }
}
