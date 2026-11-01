package com.cauansilva.arcanecontrol.core.client.render;

import com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PerfilVfxGojoTest {
    @Test void fusaoTerminaNoDisparoEProjetilSegueOMesmoPassoDoServidor() {
        assertEquals(3.2,PerfilVfxGojo.separacaoRoxo(0));
        assertEquals(0,PerfilVfxGojo.separacaoRoxo(PerfilVfxGojo.ROXO_DISPARO));
        assertEquals(3.2,PerfilVfxGojo.distanciaRoxo(30));
        assertEquals(4.8,PerfilVfxGojo.distanciaRoxo(31),1e-9);
        double separacao=3.2;
        for(double t=0;t<30;t+=.01) {
            double atual=PerfilVfxGojo.separacaoRoxo(t);
            assertTrue(atual>=0 && atual<=separacao && separacao-atual<.01);separacao=atual;
        }
        double anterior=3.2;
        for(double t=30;t<85;t+=.05) {
            double d=PerfilVfxGojo.distanciaRoxo(t);
            assertTrue(d>=anterior && d-anterior<=.081);anterior=d;
        }
    }
    @Test void ambienteNaoVazaParaForaDoDominioOuAposEncerrar() {
        assertEquals(0,PerfilVfxGojo.presencaDominio(0,0));
        assertEquals(1,PerfilVfxGojo.presencaDominio(50,10));
        assertEquals(0,PerfilVfxGojo.presencaDominio(50,26.01));
        assertEquals(0,PerfilVfxGojo.presencaDominio(400,0));
        assertEquals(0,PerfilVfxGojo.presencaDominio(420,0));
        for(double t=0;t<=400;t+=.2) for(double d=0;d<=30;d+=.5) {
            double p=PerfilVfxGojo.presencaDominio(t,d);
            assertTrue(p>=0 && p<=1);
            if(d>=PerfilVfxGojo.raioDominio(t)) assertEquals(0,p);
        }
    }
    @Test void fasesRespeitamDuracoesEOpacidadesTemEntradaESaida() {
        assertEquals(13,PerfilVfxGojo.VERMELHO_DISPARO);
        for(int tipo:new int[]{1,2,4,5}) {
            int fim=PerfilVfxGojo.duracao(tipo);
            assertEquals(0,PerfilVfxGojo.opacidade(tipo,0));
            assertEquals(0,PerfilVfxGojo.opacidade(tipo,fim));
            assertEquals(0,PerfilVfxGojo.opacidade(tipo,fim+1));
            assertEquals(1,PerfilVfxGojo.opacidade(tipo,10));
            for(double t=0;t<=fim;t+=.1) {
                double a=PerfilVfxGojo.opacidade(tipo,t);assertTrue(a>=0 && a<=1);
            }
        }
    }
}
