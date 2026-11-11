"""Separate original eye geometry and add tiny closed lids for natural blinks.

The open eye collection stays nested in the original body so Blender previews
remain intact. Android exports the two alternatives separately. Idempotent.
"""
import ast,bpy,math,pathlib
from mathutils import Vector
from math import sin,cos,pi,sqrt
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
lid=bpy.data.materials.get('Family • soft eyelid') or mat('Family • soft eyelid','806858')
for who in ['eira','eilev','oyvind']:
    prefix='Family • ' if who=='oyvind' else 'Children • '
    body=bpy.data.collections[prefix+who+'-body']
    eyes=bpy.data.collections.get(prefix+who+'-eyes-open')
    if eyes is None:
        eyes=bpy.data.collections.new(prefix+who+'-eyes-open');body.children.link(eyes)
    starts=('Oyvind eye white','Oyvind iris','Oyvind pupil','Oyvind eye glint') if who=='oyvind' else ('Eye white','Iris','Pupil','Eye sparkle')
    for ob in list(body.objects):
        if ob.name.startswith(starts):eyes.objects.link(ob);body.objects.unlink(ob)
    assert len(eyes.objects)==8,(who,len(eyes.objects))
    current=bpy.data.collections.get(prefix+who+'-eyes-closed')
    if current is None:current=bpy.data.collections.new(prefix+who+'-eyes-closed')
    current.use_fake_user=True
    for ob in list(current.objects):bpy.data.objects.remove(ob,do_unlink=True)
    adult=who=='oyvind'
    cx=.107 if adult else .117;cy=-.008 if adult else -.012
    cz=1.60 if adult else 1.56;rx=.292 if adult else .327;ry=.25 if adult else .277;rz=.335 if adult else .365
    ez=1.627 if adult else 1.583;width=.051 if adult else .061
    for sign in [-1,1]:
        points=[]
        for j in range(13):
            t=-1+j/6;x=sign*cx+t*width;z=ez-.014*(1-t*t)
            y=cy-ry*sqrt(max(.01,1-(x/rx)**2-((z-cz)/rz)**2))-.006
            points.append((x,y,z))
        tube(who+' • relaxed eyelid',points,[.004+.002*sin(pi*j/12) for j in range(13)],lid,8)
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('FAMILY_BLINKS_READY')
