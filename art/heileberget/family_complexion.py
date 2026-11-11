"""A soft painted flush for Eira and Eilev; the continuous face mesh stays intact."""
import ast,bpy,pathlib,math
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
base=rgb('F4C6A2');rose=rgb('E99B94')
for who in ['eira','eilev']:
    col=bpy.data.collections['Children • '+who+'-body']
    face=next(o for o in col.objects if 'soft oval face' in o.name)
    attr=face.data.color_attributes.get('Albedo') or face.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in face.data.vertices:
        p=face.matrix_basis@v.co
        cheeks=math.exp(-((abs(p.x)-.174)/.077)**2-((p.z-1.485)/.060)**2)
        front=max(0,min(1,(-p.y-.09)/.11))
        t=.38*cheeks*front
        attr.data[v.index].color=(*[linear(a*(1-t)+b*t) for a,b in zip(base,rose)],1)
    face.data.materials.clear();face.data.materials.append(bpy.data.materials['Painted natural albedo'])
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('SOFT_PAINTED_COMPLEXION_READY; no geometry added')
