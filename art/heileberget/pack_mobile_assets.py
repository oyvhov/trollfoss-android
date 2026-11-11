"""Pack HBG1 floats into HBG2 GPU-ready vertices, preserving positions exactly.
Normal vectors: signed normalized bytes (one padding byte); RGB: normalized bytes + padding.
Stride is 20 bytes instead of 36. No Blender needed, no geometry/topology changes.
Run after exports. Already packed assets are safe to run again.
"""
import gzip,struct,pathlib,json,hashlib
ROOT=pathlib.Path(__file__).resolve().parents[2]
DEST=ROOT/'app/src/main/assets/heileberget'
results={};max_normal_error=0;max_color_error=0
for path in sorted(DEST.glob('*.hbg.gz')):
    raw=gzip.decompress(path.read_bytes());magic,count,mode=struct.unpack('>4sII',raw[:12])
    if magic==b'HBG1':
        assert len(raw)==12+count*36
        output=bytearray(struct.pack('>4sII',b'HBG2',count,mode))
        for vertex in struct.iter_unpack('<9f',raw[12:]):
            normal=[max(-127,min(127,round(v*127))) for v in vertex[3:6]]
            color=[max(0,min(255,round(v*255))) for v in vertex[6:9]]
            max_normal_error=max(max_normal_error,max(abs(a-b/127) for a,b in zip(vertex[3:6],normal)))
            max_color_error=max(max_color_error,max(abs(a-b/255) for a,b in zip(vertex[6:9],color)))
            output.extend(struct.pack('<3f4b4B',*vertex[:3],*normal,0,*color,255))
        assert len(output)==12+count*20
        # Verify every position survives bit-for-bit and all topology counts stay unchanged.
        for i in range(count):assert raw[12+i*36:24+i*36]==output[12+i*20:24+i*20]
        path.write_bytes(gzip.compress(output,compresslevel=7,mtime=0))
    else:
        assert magic==b'HBG2' and len(raw)==12+count*20
    results[path.name[:-7]]={'format':'HBG2','gpu_bytes':count*20,'asset_bytes':path.stat().st_size,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
manifest=json.loads((DEST/'manifest.json').read_text(encoding='utf-8'))
before=0;after=0
for group in ['meshes','mobile_meshes','characters','creatures','birds','crystals','props']:
    for entry in manifest.get(group,[]):
        packed=results[entry['name']];before+=entry['triangles']*3*36;after+=packed['gpu_bytes'];entry.update(packed)
manifest['vertex_format']='HBG2: float32 XYZ + normalized int8 normal XYZ/pad + uint8 RGB/pad; stride 20'
(DEST/'manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print(json.dumps({'unpacked_gpu_bytes':before,'packed_gpu_bytes':after,'reduction_percent':round((1-after/before)*100,2),'max_normal_component_error':max_normal_error,'max_color_component_error':max_color_error}))
