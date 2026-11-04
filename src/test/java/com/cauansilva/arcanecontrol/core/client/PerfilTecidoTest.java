package com.cauansilva.arcanecontrol.core.client;
import com.cauansilva.arcanecontrol.core.client.tecido.MalhaTecido;
import com.cauansilva.arcanecontrol.core.client.tecido.PerfilTecido;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/** Cloth weight/stiffness per outfit from assets/arcanecontrol/tecido/<estilo>.json. */
class PerfilTecidoTest {
    @Test void arquivosDosTrajesCarregamEAusenteDaOPadrao(){
        for(var e:new String[]{"shadow","ichigo_bankai","frieren","gojo","asta"})assertEquals(PerfilTecido.PADRAO,PerfilTecido.de(e),e);
        assertEquals(PerfilTecido.PADRAO,PerfilTecido.de("nao_existe"));
        var p=PerfilTecido.ler(JsonParser.parseString("{\"peso\":99,\"rigidez\":-1,\"arrasto\":\"x\"}").getAsJsonObject());
        assertEquals(3,p.peso(),1e-9);assertEquals(.2,p.rigidez(),1e-9);assertEquals(1,p.arrasto(),1e-9);assertEquals(1,p.inercia(),1e-9);
    }
    /** A heavier cape running forward is thrown back less than the default one. */
    @Test void capaPesadaBalancaMenos(){
        var leve=new MalhaTecido(3,4,.2,.2);var pesada=new MalhaTecido(3,4,.2,.2,new PerfilTecido(2.5,1,1,1,1));
        for(int i=0;i<80;i++){leve.tick(.3,0,0,0);pesada.tick(.3,0,0,0);}
        double zl=leve.ponto(1,3,1).z(),zp=pesada.ponto(1,3,1).z();
        assertTrue(zl>zp,"leve "+zl+" pesada "+zp);
    }
}
