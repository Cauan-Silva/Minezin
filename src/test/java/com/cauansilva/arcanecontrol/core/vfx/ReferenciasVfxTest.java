package com.cauansilva.arcanecontrol.core.vfx;
import com.cauansilva.arcanecontrol.core.cid.FormaEnergiaAtomic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ReferenciasVfxTest {
    @Test void dividerCresceAntesDoGolpeERestauraOItemSemMudarAEmpunhadura(){
        assertEquals(1,PerfilDivider.comprimento(-1,false));assertEquals(1,PerfilDivider.comprimento(0,false));
        assertEquals(3.6,PerfilDivider.comprimento(18,false),1e-5);
        assertEquals(1,PerfilDivider.comprimento(32,false));assertEquals(1,PerfilDivider.largura(32));
        double anterior=1;for(double t=0;t<=16;t+=.25){double c=PerfilDivider.comprimento(t,false);assertTrue(c>=anterior);anterior=c;}
    }
    @Test void primeiraPessoaTemExtensaoLimitadaEValoresSaoFinitos(){
        for(double t=-1;t<=40;t+=.1){double fp=PerfilDivider.comprimento(t,true),tp=PerfilDivider.comprimento(t,false);assertTrue(Double.isFinite(fp)&&fp>=1&&fp<=2.20001&&fp<=tp+.00001);}
    }
    @Test void atomicCampoEColunaSaoLimitadosSemEsferaDominante(){
        for(int j=0;j<42;j++)for(int i=0;i<=8;i++){
            var p=FormaEnergiaAtomic.campo(j,i/8d,80);assertTrue(Double.isFinite(p.x())&&Double.isFinite(p.z())&&p.y()>=0&&p.y()<=20);
        }
        var baixo=FormaEnergiaAtomic.coluna(0,0,110);var alto=FormaEnergiaAtomic.coluna(0,1,110);
        assertEquals(0,baixo.y());assertEquals(48,alto.y());assertTrue(Math.abs(alto.x())>Math.abs(baixo.x()));
        assertEquals(0,FormaEnergiaAtomic.coluna(0,.5,240).x());
    }
}
