package com.cauansilva.arcanecontrol.core.vfx;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.*;

class KilluaBaseTest {
    @Test void narukamiSaltaEDescarregaDeCima() {
        assertEquals(Narukami.ALTURA, Narukami.corpo(Narukami.SALTO).y(), 1e-6, "topo do salto");
        assertTrue(Narukami.mao(Narukami.DESCARGA - 2).y() > Narukami.corpo(Narukami.DESCARGA - 2).y() + 2, "mao erguida na estrela");
        assertTrue(Narukami.alphaEstrela(Narukami.DESCARGA - 1) > .8);
        assertEquals(0, Narukami.alphaRaio(Narukami.DESCARGA - .5), 1e-9);
        assertTrue(Narukami.alphaRaio(Narukami.DESCARGA + 1) > .9);
        var r = Narukami.raio(Narukami.IMPACTO);
        assertEquals(Narukami.alvo().z(), r.get(r.size() - 1).z(), 1e-9, "raio termina no alvo");
        assertTrue(r.get(0).y() > Narukami.alvo().y() + 2, "raio vem de cima");
        assertTrue(Narukami.alphaColuna(Narukami.DESCARGA + 2) > .9);
        assertEquals(0, Narukami.corpo(Narukami.POUSO).y(), 1e-9, "pousa no chao");
        assertTrue(Narukami.alphaParalisia(Narukami.IMPACTO + 10) > .9);
        assertEquals(0, Narukami.alphaParalisia(Narukami.IMPACTO + Narukami.PARALISIA + .5), 1e-9, "paralisia de 1 s");
    }
    @Test void echoTeleportaQuatroBlocos() {
        assertEquals(Echo.DISTANCIA, Echo.corpo(Echo.TELEPORTE).z(), 1e-9);
        assertEquals(0, Echo.alphaCorpo(Echo.TELEPORTE), 1e-9);
        for (int n = 0; n < Echo.IMAGENS; n++) assertTrue(Echo.nasce(n) < Echo.TELEPORTE);
        for (int n = 0; n < Echo.IMAGENS; n++) assertTrue(Echo.alphaImagem(n, Echo.FIM) < .05, "imagens somem");
    }
    @Test void yoyoDaUmaVoltaEVolta() {
        assertEquals(Yoyo.RAIO, Yoyo.raio(Yoyo.LANCA + 5), 1e-9);
        assertEquals(2 * Math.PI, Yoyo.angulo(Yoyo.LANCA + Yoyo.GIRO), 1e-6);
        assertEquals(0, Yoyo.raio(Yoyo.LANCA + Yoyo.GIRO + Yoyo.RECOLHE), 1e-6);
        assertEquals(Yoyo.PUXA_ATE, Yoyo.puxao(Yoyo.FIM), 1e-6);
        double max = 0;
        for (double t = 0; t < Yoyo.FIM; t += .5) max = Math.max(max, Yoyo.acerto(t));
        assertTrue(max > .5, "o giro acerta o mob puxado");
    }
    @Test void garrasFurtivoDoisSegundos() {
        assertEquals(40, Garras.FURTIVO);
        assertTrue(Garras.alphaCorpo(Garras.SOME + 10) < .15);
        assertEquals(1, Garras.alphaCorpo(Garras.GOLPE), 1e-9);
        assertTrue(Garras.corpo(Garras.GOLPE).z() < KilluaBase.ALVO - .9);
        assertTrue(Garras.alphaSangue(Garras.GOLPE + 10) > .5);
    }
    @Test void animacoes() throws Exception {
        var j = com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/killua.json").readAllBytes())).getAsJsonObject().getAsJsonObject("animations");
        for (String n : List.of("killua_narukami", "killua_echo", "killua_ioio", "killua_garras")) assertTrue(j.has(n), n);
    }

    @Test void exportaParaPrevia() throws Exception {
        var raiz = new LinkedHashMap<String, Object>();
        var z = new LinkedHashMap<String, Object>();
        for (double t = 0; t <= Narukami.FIM; t += .5) {
            var m = new LinkedHashMap<String, Object>();
            m.put("corpo", Narukami.corpo(t)); m.put("mao", Narukami.mao(t));
            var fl = new ArrayList<Object>(); for (int k = 0; k < Narukami.FLARES; k++) fl.add(Narukami.flare(k, t));
            var re = new ArrayList<Object>(); for (int k = 0; k < Narukami.RAIOS_ESTRELA; k++) re.add(Narukami.raioEstrela(k, t));
            var f = new ArrayList<Object>(); for (int k = 0; k < Narukami.FAISCAS; k++) f.add(Narukami.faisca(k, t));
            m.put("alphaEstrela", Narukami.alphaEstrela(t)); m.put("raioEstrela", Narukami.raioEstrela(t)); m.put("flares", fl);
            m.put("raiosEstrela", re); m.put("anelEstrela", Narukami.anelEstrela(t)); m.put("faiscas", f);
            var au = new ArrayList<Object>(); for (int k = 0; k < Narukami.ARCOS_AURA; k++) au.add(Narukami.arcoAura(k, t));
            m.put("aura", au); m.put("alphaAura", Narukami.alphaAura(t));
            var co = new ArrayList<Object>(); for (int k = 0; k < Narukami.RISCOS_COLUNA; k++) co.add(List.of(Narukami.riscoColuna(k, t), Narukami.larguraRisco(k)));
            var tr = new ArrayList<Object>(); for (int k = 0; k < Narukami.RAIOS_TRANCADOS; k++) tr.add(Narukami.raioTrancado(k, t));
            var ce = new ArrayList<Object>(); for (int k = 0; k < Narukami.RAIOS_CEU; k++) ce.add(Narukami.raioCeu(k, t));
            var g = new ArrayList<Object>(); for (int k = 0; k < Narukami.GALHOS; k++) g.add(Narukami.galho(k, t));
            m.put("coluna", co); m.put("alphaColuna", Narukami.alphaColuna(t)); m.put("raio", Narukami.raio(t)); m.put("trancados", tr);
            m.put("ceu", ce); m.put("galhos", g); m.put("alphaRaio", Narukami.alphaRaio(t));
            var ex = new ArrayList<Object>(); for (int k = 0; k < Narukami.RAIOS_EXPLOSAO; k++) ex.add(Narukami.raioExplosao(k, t));
            m.put("clarao", Narukami.clarao(t)); m.put("explosao", ex); m.put("alphaExplosao", Narukami.alphaExplosao(t));
            m.put("ondaV", Narukami.ondaVertical(t)); m.put("ondaC", Narukami.ondaChao(t)); m.put("alphaOnda", Narukami.alphaOnda(t));
            m.put("voo", Narukami.faiscasVoo(t));
            var p = new ArrayList<Object>(); for (int k = 0; k < 8; k++) p.add(Narukami.arcoParalisia(k, t));
            var ga = new ArrayList<Object>(); for (int k = 0; k < Narukami.ARCOS_GAIOLA; k++) ga.add(Narukami.arcoGaiola(k, t));
            m.put("paralisia", p); m.put("gaiola", ga); m.put("anelPar", Narukami.anelParalisia(t)); m.put("alphaParalisia", Narukami.alphaParalisia(t));
            m.put("pouso", Narukami.pouso(t));
            z.put(Double.toString(t), m);
        }
        raiz.put("narukami", Map.of("quadros", z, "fim", Narukami.FIM, "impacto", Narukami.IMPACTO, "descarga", Narukami.DESCARGA,
                "salto", Narukami.SALTO, "pouso", Narukami.POUSO));
        var x = new LinkedHashMap<String, Object>();
        for (double t = 0; t <= Echo.FIM; t += .5) {
            var m = new LinkedHashMap<String, Object>();
            m.put("corpo", Echo.corpo(t)); m.put("alphaCorpo", Echo.alphaCorpo(t));
            var im = new ArrayList<Object>(); for (int n = 0; n < Echo.IMAGENS; n++) im.add(List.of(Echo.posImagem(n), Echo.alphaImagem(n, t)));
            var ri = new ArrayList<Object>(); for (int k = 0; k < 8; k++) ri.add(Echo.risco(k, t));
            m.put("imagens", im); m.put("riscos", ri); m.put("alphaRiscos", Echo.alphaRiscos(t));
            var ps = new ArrayList<Object>(); var po = new ArrayList<Object>();
            for (int n = 0; n < Echo.IMAGENS; n++) { ps.add(List.of(Echo.passo(n, t), Echo.alphaPasso(n, t))); po.addAll(Echo.poeira(n, t)); }
            var bo = new ArrayList<Object>(); for (int k = 0; k < 3; k++) bo.add(Echo.borrao(k, t));
            var es = new ArrayList<Object>(); for (int k = 0; k < 3; k++) { es.add(Echo.estouro(false, k, t)); es.add(Echo.estouro(true, k, t)); }
            var ve = new ArrayList<Object>(); for (int k = 0; k < 10; k++) ve.add(Echo.vento(k, t));
            po.addAll(Echo.poeiraChegada(t));
            m.put("passos", ps); m.put("poeira", po); m.put("borrao", bo); m.put("alphaBorrao", Echo.alphaBorrao(t));
            m.put("estouros", es); m.put("alphaEstouro", Echo.alphaEstouro(t)); m.put("vento", ve);
            x.put(Double.toString(t), m);
        }
        raiz.put("echo", Map.of("quadros", x, "fim", Echo.FIM, "teleporte", Echo.TELEPORTE));
        var c = new LinkedHashMap<String, Object>();
        for (double t = 0; t <= Yoyo.FIM; t += .5) {
            var m = new LinkedHashMap<String, Object>();
            var yo = new ArrayList<Object>();
            for (int lado : new int[]{1, -1}) {
                var ra = new ArrayList<Object>(); for (int i = 0; i <= 16; i++) ra.add(Yoyo.rastro(lado, i / 16d, t));
                var fb = new ArrayList<Object>(); var fc = new ArrayList<Object>();
                for (int i = 0; i <= 16; i++) { fb.add(Yoyo.faixa(lado, i / 16d, t, 0)); fc.add(Yoyo.faixa(lado, i / 16d, t, 1)); }
                yo.add(Map.of("pos", Yoyo.yoyo(lado, t), "fio", Yoyo.fio(lado, t), "rastro", ra, "eixo", Yoyo.eixo(lado, t),
                        "faixaB", fb, "faixaC", fc, "poeira", Yoyo.poeira(lado, t)));
            }
            var on = new ArrayList<Object>(); for (int k = 0; k < 3; k++) on.add(Yoyo.onda(k, t));
            var pl = new ArrayList<Object>(); for (int k = 0; k < 5; k++) pl.add(Yoyo.puxaoLinha(k, t));
            m.put("onda", on); m.put("alphaOnda", Yoyo.alphaOnda(t)); m.put("faiscas", Yoyo.faiscasAcerto(t));
            m.put("puxao", pl); m.put("alphaPuxao", Yoyo.alphaPuxao(t));
            m.put("yoyos", yo); m.put("giro", Yoyo.giroProprio(t)); m.put("angulo", Yoyo.angulo(t)); m.put("alphaRastro", Yoyo.alphaRastro(t));
            m.put("mob", Yoyo.puxao(t)); m.put("acerto", Yoyo.acerto(t));
            c.put(Double.toString(t), m);
        }
        raiz.put("yoyo", Map.of("quadros", c, "fim", Yoyo.FIM, "lanca", Yoyo.LANCA, "giro", Yoyo.GIRO));
        var v = new LinkedHashMap<String, Object>();
        for (double t = 0; t <= Garras.FIM; t += .5) {
            var m = new LinkedHashMap<String, Object>();
            m.put("corpo", Garras.corpo(t)); m.put("alphaCorpo", Garras.alphaCorpo(t)); m.put("alphaFumaca", Garras.alphaFumaca(t));
            var fu = new ArrayList<Object>(); for (int k = 0; k < 12; k++) fu.add(Garras.fumaca(k, t));
            var co = new ArrayList<Object>();
            for (int k = 0; k < Garras.CORTES; k++) { var l = new ArrayList<Object>(); for (int i = 0; i <= 12; i++) l.add(Garras.corte(k, i / 12d, t)); co.add(l); }
            var go = new ArrayList<Object>(); for (int k = 0; k < Garras.GOTAS; k++) { var g = Garras.gota(k, t); if (g != null) go.add(g); }
            m.put("fumaca", fu); m.put("mao", Garras.mao(t)); m.put("unha", Garras.unha(t)); m.put("cortes", co);
            m.put("alphaCorte", Garras.alphaCorte(t)); m.put("gotas", go); m.put("alphaSangue", Garras.alphaSangue(t)); m.put("critico", Garras.critico(t));
            var fa = new ArrayList<Object>(); for (int k = 0; k < 6; k++) fa.add(Garras.fioAura(k, t));
            var pf = new ArrayList<Object>(); for (int n = 0; n < Garras.PASSOS; n++) pf.add(List.of(Garras.passoFurtivo(n, t), Garras.alphaPassoFurtivo(n, t)));
            m.put("fragmentos", Garras.fragmentos(t)); m.put("fiosAura", fa); m.put("alphaAura", Garras.alphaAura(t)); m.put("passos", pf);
            m.put("braco", Garras.rastroBraco(t)); m.put("alphaBraco", Garras.alphaBraco(t)); m.put("meiaLua", Garras.meiaLua(t));
            m.put("alphaMeiaLua", Garras.alphaMeiaLua(t)); m.put("jorro", Garras.jorro(t)); m.put("manchas", Garras.manchas());
            m.put("alphaMancha", Garras.alphaMancha(t)); m.put("alphaImpacto", Garras.alphaImpacto(t));
            v.put(Double.toString(t), m);
        }
        raiz.put("garras", Map.of("quadros", v, "fim", Garras.FIM, "golpe", Garras.GOLPE, "some", Garras.SOME));
        raiz.put("alvo", KilluaBase.ALVO);
        Files.createDirectories(Path.of("build"));
        try (var w = Files.newBufferedWriter(Path.of("build/killua-base-samples.json"))) { new Gson().toJson(raiz, w); }
    }
}
