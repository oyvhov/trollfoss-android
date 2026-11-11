import bpy
from mathutils import Vector
s=bpy.context.scene
sky=bpy.data.collections['Sky • painted atmosphere']
for o in sky.objects:
    o.visible_shadow=False;o.visible_diffuse=False;o.visible_glossy=False
    for slot in o.material_slots:
        if slot.material and slot.material.name=='Painted natural albedo':
            m=slot.material.copy();m.name='Sky gradient emission';slot.material=m
            nodes=m.node_tree.nodes;nodes.clear();attr=nodes.new('ShaderNodeVertexColor');attr.layer_name='Albedo'
            e=nodes.new('ShaderNodeEmission');out=nodes.new('ShaderNodeOutputMaterial')
            m.node_tree.links.new(attr.outputs['Color'],e.inputs['Color']);m.node_tree.links.new(e.outputs[0],out.inputs[0])
for o in bpy.data.collections['Heileberget • landscape'].objects:
    if o.name.startswith('Distant blue mountains'):o.visible_shadow=False
s.camera.location=(18,-36,14);s.camera.rotation_euler=(Vector((0,5,5))-s.camera.location).to_track_quat('-Z','Y').to_euler();s.camera.data.lens=33
# The portal blends into the mountainside instead of exposing a black backing rectangle.
o=bpy.data.objects['Deep cave shadow'];o.scale.z=.8;o.location.z-=.8
bpy.data.lights['Evening sun'].energy=3.2
s.view_settings.exposure=.3;s.render.filepath='C:/topa/dist/heileberget-blender/landscape-v3.png'
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
def render_preview():bpy.ops.render.render(write_still=True);return None
bpy.app.timers.register(render_preview,first_interval=1)
print('Lighting and sky corrected')
