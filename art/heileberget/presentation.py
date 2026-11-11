"""Two named, editable Blender scenes and actual rendered previews."""
import bpy,pathlib,hashlib,json
from mathutils import Vector
outside=bpy.data.scenes['Heileberget']
CAVE=bpy.data.collections['Inside the mountain'];OUTSIDE=bpy.data.collections['Heileberget • landscape'];STUDIO=bpy.data.collections['Cameras & light']
for c in [CAVE,OUTSIDE,bpy.data.collections['Tarn • water']]:c.hide_render=False;c.hide_viewport=False
outside.view_layers[0].layer_collection.children[CAVE.name].exclude=True
cave=bpy.data.scenes.get('Trollhola') or bpy.data.scenes.new('Trollhola')
for c in [CAVE,STUDIO]:
    if c.name not in cave.collection.children:cave.collection.children.link(c)
cave.camera=bpy.data.objects['Cave camera'];cave.world=outside.world.copy()
cave.world.node_tree.nodes['Background'].inputs[0].default_value=(.3,.25,.20,1)
cave.world.node_tree.nodes['Background'].inputs[1].default_value=.15
cave.render.engine='CYCLES';cave.cycles.device='GPU';cave.cycles.samples=48;cave.cycles.use_denoising=True
cave.render.resolution_x=1600;cave.render.resolution_y=1000;cave.render.resolution_percentage=100
cave.view_settings.view_transform='AgX';cave.view_settings.look='AgX - Medium High Contrast';cave.view_settings.exposure=.25
cave.render.image_settings.file_format='PNG';cave.render.filepath='C:/topa/dist/heileberget-blender/cave-final.png'
bpy.context.window.scene=cave
for a in bpy.context.screen.areas:
    if a.type=='VIEW_3D':
        a.spaces.active.region_3d.view_perspective='CAMERA';a.spaces.active.overlay.show_overlays=False
        a.spaces.active.shading.type='MATERIAL'
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
def preview():bpy.ops.render.render(write_still=True);return None
bpy.app.timers.register(preview,first_interval=1)
print('Two editable scenes: Heileberget and Trollhola; cave shown')
