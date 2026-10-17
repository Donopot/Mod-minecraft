"""Port only original static layer builders, preserving cube constants and UVs."""
from pathlib import Path
import hashlib,json,re
P=Path(__file__).resolve().parents[1];REF=P.parent/'.runtime/onepiece/asset-source-audit/java-models/net/mcreator/minepiece/client/model'
methods=[];proof=[]
for original,name in [('Modelwooden_spear2','roots'),('Modelmissile','missile')]:
 path=REF/(original+'.java');s=path.read_text(encoding='utf-8')
 body=s.split('public static LayerDefinition createBodyLayer() {',1)[1].split('\n    }',1)[0]
 for a,b in {'m_171576_':'getRoot','m_171599_':'addOrReplaceChild','m_171558_':'create','m_171419_':'offset','m_171423_':'offsetAndRotation','m_171514_':'texOffs','m_171488_':'addBox','m_171480_':'mirror','m_171555_':'mirror','m_171565_':'create'}.items():body=body.replace(a,b)
 assert not re.search(r'\bm_\d+_',body)
 methods.append(' public static LayerDefinition '+name+'(){'+body+'\n }')
 proof.append({'source_class':original,'private_reference_sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'ported_method':name,'cubes':body.count('.addBox('),'constants_unchanged':True})
header='''package net.mcreator.minepiece.client.model;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
/** MinePiece ver15 static mesh builders: identical cube coordinates, pivots, deformation and UVs. */
public final class SourcePrimitiveModels {
 private SourcePrimitiveModels(){}
'''
(P/'src/main/java/net/mcreator/minepiece/client/model/SourcePrimitiveModels.java').write_text(header+'\n'.join(methods)+'\n}\n',encoding='utf-8')
(P/'build/source-java-models-provenance.local.json').write_text(json.dumps(proof,indent=2),encoding='utf-8');print(proof)
