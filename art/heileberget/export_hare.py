"""Turn an authored woodland hare into a reusable animated Android character."""
import bpy,ast,pathlib,json,math
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/export_android.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,(ast.Import,ast.ImportFrom,ast.FunctionDef))],type_ignores=[]),'export helpers','exec'))
DEST=ROOT/'app/src/main/assets/heileberget'
outside=bpy.data.collections['Heileberget • landscape']
hare=bpy.data.collections.get('Woodland hare • animated')
if hare is None:
    hare=bpy.data.collections.new('Woodland hare • animated')
    bpy.data.scenes['Heileberget'].collection.children.link(hare)
    pieces=[o for o in outside.objects if o.name.startswith('Harmony • hare')]
    selected=[o for o in pieces if o.location.x<0]
    body=next(o for o in selected if o.name.startswith('Harmony • hare body'))
    ground=body.location.z-.3
    for o in selected:
        outside.objects.unlink(o);hare.objects.link(o)
        o.location-=Vector((-15,-6,ground))
    for o in pieces:
        if o not in selected:bpy.data.objects.remove(o,do_unlink=True)
    # Editable instances remain in the Blender landscape; native Android draws the same mesh.
    east_ground=1.65+2+.22*math.sin(17*.44)*math.cos(-5*.3)
    for i,(x,y,z) in enumerate([(-15,-6,ground),(17,5,east_ground)]):
        ob=bpy.data.objects.new('Hare preview instance %d'%i,None);outside.objects.link(ob)
        ob.instance_type='COLLECTION';ob.instance_collection=hare;ob.location=(x,y,z)
    bpy.data.scenes['Heileberget'].view_layers[0].layer_collection.children[hare.name].exclude=True
# Keep existing preview instances in sync when exporting an already extracted hare.
east=bpy.data.objects.get('Hare preview instance 1')
if east:east.location.z=1.65+2+.22*math.sin(17*.44)*math.cos(-5*.3)
result=export('hare',hare.name,0,False)
manifest=json.loads((DEST/'manifest.json').read_text(encoding='utf-8'))
manifest['creatures']=[result]
(DEST/'manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('HARE_EXPORTED')
