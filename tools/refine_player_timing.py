"""Retiming for attacks with delayed server impact; preserves existing long cinematics."""
import copy,json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/arcanecontrol/player_animation'

def retime(animation,impact):
    end=animation['animation_length']
    for bone,channels in animation['bones'].items():
        frames=channels.get('rotation')
        if not isinstance(frames,dict) or len(frames)<3:continue
        if str(impact) in frames and str(round(impact+.1,3)) in frames and frames[str(impact)]==frames[str(round(impact+.1,3))]:continue
        ordered=sorted(frames.items(),key=lambda x:float(x[0]))
        wind=copy.deepcopy(ordered[1][1]);hit=copy.deepcopy(ordered[2][1]);neutral=copy.deepcopy(ordered[-1][1])
        if bone=='body':wind=copy.deepcopy(ordered[0][1]);hit=copy.deepcopy(ordered[1][1])
        def frame(v,ease):
            v=copy.deepcopy(v);v['easing']=ease;return v
        frames={'0':frame(ordered[0][1],'easeInQuad'),str(round(impact-.1,3)):frame(wind,'easeInQuad'),
                str(impact):frame(hit,'linear'),str(round(impact+.1,3)):frame(hit,'linear'),
                str(round(min(end-.1,impact+.25),3)):frame(hit,'easeOutQuad'),str(end):frame(neutral,'easeInOutSine')}
        channels['rotation']=frames

def refine_asta(animations):
    for tipo,tick in [(300,6),(301,8),(302,10),(304,12),(305,10),(306,18),(307,8)]:retime(animations[f'asta_{tipo}'],tick/20)

def main():
    p=ROOT/'asta.json';d=json.loads(p.read_text(encoding='utf-8'));refine_asta(d['animations']);p.write_bytes((json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode('utf-8'))
    p=ROOT/'cid.json';d=json.loads(p.read_text(encoding='utf-8'))
    # The slash impacts at tick 6; hold the wind-up until tick 4, then freeze for two ticks.
    retime(d['animations']['cid_1'],.3)
    p.write_bytes((json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode('utf-8'))

if __name__=='__main__':main()
