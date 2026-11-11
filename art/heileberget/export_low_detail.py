"""Derive mobile topology from the same authored art; never simplify collision geometry."""
import bpy,ast,pathlib,json
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
code=(ROOT/'art/heileberget/export_android.py').read_text(encoding='utf-8')
# Load the exporter definitions, without rerunning all regular exports.
tree=ast.parse(code);nodes=[]
for n in tree.body:
    if isinstance(n,(ast.Import,ast.ImportFrom,ast.FunctionDef)):nodes.append(n)
exec(compile(ast.Module(body=nodes,type_ignores=[]),'export helpers','exec'))
DEST=ROOT/'app/src/main/assets/heileberget'
# Props stay at their authored positions; exports never relocate the scene.
updated_outside=export('outside','Heileberget • landscape',1,False)
results=[]
for name,col,cave in [('outside','Heileberget • landscape',False),('cave','Inside the mountain',True),('troll','Rumle • sculpt',False),('arm','Rumle • waving hand',False)]:
    temp=bpy.data.collections.new('Export temporary');bpy.context.scene.collection.children.link(temp)
    for original in bpy.data.collections[col].objects:
        if original.type!='MESH' or original.name.startswith('Fine heather grasses'):continue
        ob=original.copy();temp.objects.link(ob)
        small_gold=original.name.startswith(('Gold ingot','Treasury gold ingot'))
        if (len(ob.data.polygons)>100 or (small_gold and len(ob.data.polygons)>40)) and not original.name.startswith('Meadow •'):
            d=ob.modifiers.new('Mobile LOD','DECIMATE')
            # The small rounded leaf fans became sharp needles at 34%. Keep
            # their silhouettes while simplifying the much larger terrain.
            d.ratio=.65 if small_gold else .72 if original.name.startswith('Birch • soft leaf sprays') else (.34 if name in ('outside','cave','troll') else .42)
            d.use_collapse_triangulate=True
            bpy.context.view_layer.update()
            evaluated=ob.evaluated_get(bpy.context.evaluated_depsgraph_get())
            ob.data=bpy.data.meshes.new_from_object(evaluated,preserve_all_data_layers=True,depsgraph=bpy.context.evaluated_depsgraph_get())
            ob.modifiers.clear();ob['temporary_mesh']=True
    results.append(export(name+'-low',temp.name,0 if name in ('troll','arm') else 1,cave))
    for ob in list(temp.objects):
        data=ob.data if ob.get('temporary_mesh') else None
        bpy.data.objects.remove(ob,do_unlink=True)
        if data:bpy.data.meshes.remove(data)
    bpy.data.collections.remove(temp)
manifest=json.loads((DEST/'manifest.json').read_text(encoding='utf-8'))
manifest['meshes']=[updated_outside if m['name']=='outside' else m for m in manifest['meshes']]
manifest['mobile_meshes']=results
(DEST/'manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('MOBILE_LODS_READY')
