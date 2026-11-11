"""Append the authored bowl used by the gold-washing activity."""
import ast,pathlib,bpy,json
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/export_android.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,(ast.Import,ast.ImportFrom,ast.FunctionDef))],type_ignores=[]),'export helpers','exec'))
DEST=ROOT/'app/src/main/assets/heileberget'
result=export('gold-pan','Gold pan • animated',0)
manifest=json.loads((DEST/'manifest.json').read_text(encoding='utf-8'));manifest['props']=[result]
(DEST/'manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print('GOLD_PAN_EXPORTED')
