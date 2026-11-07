package com.cauansilva.arcanecontrol.core.boneco;

/**
 * Sessao de treino (sem Minecraft): soma os golpes a partir do primeiro; sem golpes por {@link #OCIOSO} ticks a sessao
 * acaba. DPS = total / tempo decorrido (minimo 1 s, para o primeiro golpe nao virar um numero absurdo).
 */
public final class SessaoDps {
    public static final int OCIOSO=100;
    private long inicio=-1,ultimo;
    private double total,maior;
    private int golpes;

    public void registrar(long tick,double dano){
        if(dano<=0)return;
        if(inicio<0||expirou(tick)){inicio=tick;total=0;maior=0;golpes=0;}
        ultimo=tick;total+=dano;golpes++;maior=Math.max(maior,dano);
    }
    public boolean ativa(){return inicio>=0;}
    public boolean expirou(long agora){return inicio>=0&&agora-ultimo>OCIOSO;}
    public double segundos(){return inicio<0?0:Math.max(1,(ultimo-inicio)/20.0);}
    public double dps(){return inicio<0?0:total/segundos();}
    public double total(){return total;}
    public double maior(){return maior;}
    public int golpes(){return golpes;}
    public void encerrar(){inicio=-1;total=0;maior=0;golpes=0;}
    /** Texto curto para o nome flutuante. */
    public String painel(){return String.format(java.util.Locale.ROOT,"DPS %.1f | Máx %.1f | Total %.0f",dps(),maior,total);}
    public String resumo(){return String.format(java.util.Locale.ROOT,"%.1f DPS em %.1f s — %d golpes, maior %.1f, total %.0f",dps(),segundos(),golpes,maior,total);}
}
