"""Small strict NBT reader. Unmodified payloads retain their original bytes and types.

Sponge v3: https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-3.md
This validates structure, never Minecraft downgrade compatibility or P1 acceptance.
"""
from dataclasses import dataclass
import gzip
import struct


@dataclass
class Tag:
    kind: int
    value: object
    raw: bytes | None = None


def string_bytes(value):
    # Java's modified UTF-8 encodes UTF-16 code units, including surrogate pairs.
    units = value.encode('utf-16-be', 'surrogatepass')
    encoded = ''.join(chr(int.from_bytes(units[i:i+2], 'big'))
                      for i in range(0, len(units), 2)).encode('utf-8', 'surrogatepass')
    encoded = encoded.replace(b'\0', b'\xc0\x80')
    if len(encoded) > 65535:
        raise ValueError('NBT string too long')
    return struct.pack('>H', len(encoded)) + encoded


class Reader:
    def __init__(self, data):
        self.data, self.pos = data, 0

    def take(self, size):
        if size < 0 or size > len(self.data) - self.pos:
            raise ValueError('Truncated NBT payload or negative length')
        start = self.pos
        self.pos += size
        return self.data[start:self.pos]

    def number(self, fmt):
        return struct.unpack('>' + fmt, self.take(struct.calcsize('>' + fmt)))[0]

    def string(self):
        raw = self.take(self.number('H')).replace(b'\xc0\x80', b'\0')
        units = raw.decode('utf-8', 'surrogatepass')
        return units.encode('utf-16-be', 'surrogatepass').decode('utf-16-be', 'surrogatepass')

    def payload(self, kind, depth=0):
        if depth > 128:
            raise ValueError('NBT nesting too deep')
        start = self.pos
        if kind in range(1, 7):
            value = self.number({1:'b', 2:'h', 3:'i', 4:'q', 5:'f', 6:'d'}[kind])
        elif kind == 7:
            value = self.take(self.number('i'))
        elif kind == 8:
            value = self.string()
        elif kind == 9:
            subtype, size = self.number('B'), self.number('i')
            if size < 0 or size > len(self.data) - self.pos or not 0 <= subtype <= 12 or (size and subtype == 0):
                raise ValueError('Invalid NBT list')
            value = (subtype, [self.payload(subtype, depth+1) for _ in range(size)])
        elif kind == 10:
            value = {}
            while True:
                subtype = self.number('B')
                if subtype == 0:
                    break
                name = self.string()
                if name in value:
                    raise ValueError('Duplicate NBT key: ' + name)
                value[name] = self.payload(subtype, depth+1)
        elif kind in (11, 12):
            size = self.number('i')
            fmt = 'i' if kind == 11 else 'q'
            width = 4 if kind == 11 else 8
            if size < 0 or size > (len(self.data)-self.pos)//width:
                raise ValueError('Invalid NBT array length')
            value = [self.number(fmt) for _ in range(size)]
        else:
            raise ValueError('Unknown NBT tag: ' + str(kind))
        return Tag(kind, value, self.data[start:self.pos])


def decode(data):
    reader = Reader(data)
    if reader.number('B') != 10:
        raise ValueError('NBT root must be a compound')
    name = reader.string()
    root = reader.payload(10)
    if reader.pos != len(data):
        raise ValueError('Trailing bytes after NBT root')
    return name, root


def payload(tag):
    if tag.raw is not None:
        return tag.raw
    if tag.kind in range(1, 7):
        return struct.pack('>' + {1:'b', 2:'h', 3:'i', 4:'q', 5:'f', 6:'d'}[tag.kind], tag.value)
    if tag.kind == 7:
        return struct.pack('>i', len(tag.value)) + tag.value
    if tag.kind == 8:
        return string_bytes(tag.value)
    if tag.kind == 9:
        subtype, children = tag.value
        if any(child.kind != subtype for child in children):
            raise ValueError('Heterogeneous NBT list')
        return struct.pack('>Bi', subtype, len(children)) + b''.join(payload(child) for child in children)
    if tag.kind == 10:
        return b''.join(bytes([child.kind]) + string_bytes(name) + payload(child)
                        for name, child in tag.value.items()) + b'\0'
    if tag.kind in (11, 12):
        fmt = 'i' if tag.kind == 11 else 'q'
        return struct.pack('>i', len(tag.value)) + b''.join(struct.pack('>'+fmt, n) for n in tag.value)
    raise ValueError('Invalid NBT type')


def encode(name, root):
    return b'\x0a' + string_bytes(name) + payload(root)


class NbtDict(dict): pass
class NbtList(list): pass
class NbtInt(int): pass
class NbtFloat(float): pass
class NbtString(str): pass
class NbtBytes(bytes): pass


def plain(tag):
    """Values usable by the extractor, with the original tag attached for lossless BE copy."""
    if tag.kind == 10:
        value = NbtDict({key:plain(child) for key, child in tag.value.items()})
    elif tag.kind == 9:
        value = NbtList(plain(child) for child in tag.value[1])
    elif tag.kind in (11, 12):
        value = NbtList(tag.value)
    elif tag.kind == 7:
        value = NbtBytes(tag.value)
    elif tag.kind == 8:
        value = NbtString(tag.value)
    elif tag.kind in (5, 6):
        value = NbtFloat(tag.value)
    else:
        value = NbtInt(tag.value)
    value.nbt_tag = tag
    return value


def varints(data):
    value = shift = 0
    for byte in data:
        if shift == 28 and byte > 7:
            raise ValueError('Palette varint exceeds signed int range')
        value |= (byte & 127) << shift
        if byte & 128:
            shift += 7
            if shift >= 35:
                raise ValueError('Palette varint too long')
        else:
            yield value
            value = shift = 0
    if shift:
        raise ValueError('Truncated palette varint')


def load(path):
    with gzip.open(path, 'rb') as source:
        name, root = decode(source.read())
    # WorldEdit writes an unnamed root containing the Schematic compound.
    if 'Schematic' not in root.value or root.value['Schematic'].kind != 10:
        raise ValueError('Sponge v3 requires a root Schematic compound; regenerate legacy previews')
    return name, root


def validate(root):
    schematic = root.value['Schematic']
    d = plain(schematic)
    required = {'Version':3, 'DataVersion':3, 'Width':2, 'Height':2, 'Length':2, 'Blocks':10}
    for key, kind in required.items():
        if key not in schematic.value or schematic.value[key].kind != kind:
            raise ValueError('Missing or mistyped Sponge v3 field: ' + key)
    if d['Version'] != 3 or d['DataVersion'] <= 0:
        raise ValueError('Invalid Sponge version or DataVersion')
    dimensions = [d[key] & 65535 for key in ('Width','Height','Length')]
    if not all(dimensions):
        raise ValueError('Zero schematic dimension')
    offset = schematic.value.get('Offset')
    if offset is not None and (offset.kind != 11 or len(offset.value) != 3):
        raise ValueError('Offset must be an int array of length 3')
    volume = dimensions[0] * dimensions[1] * dimensions[2]
    result = {'dimensions':dimensions, 'volume':volume, 'block_entities':0, 'entities':0,
              'has_biomes':'Biomes' in d, 'p1_pass':False}
    for container in ('Blocks', 'Biomes'):
        if container not in schematic.value:
            continue
        c = schematic.value[container]
        if c.kind != 10 or c.value.get('Palette', Tag(0,None)).kind != 10 or c.value.get('Data', Tag(0,None)).kind != 7:
            raise ValueError('Invalid ' + container + ' container')
        palette = c.value['Palette'].value
        indices = [tag.value for tag in palette.values()]
        if not palette or any(tag.kind != 3 or tag.value < 0 for tag in palette.values()) or len(set(indices)) != len(indices):
            raise ValueError('Invalid or duplicate palette index')
        allowed = set(indices)
        count = 0
        for index in varints(c.value['Data'].value):
            if index not in allowed:
                raise ValueError('Undefined palette index: ' + str(index))
            count += 1
        if count != volume:
            raise ValueError('Wrong ' + container + ' voxel count')
    for key, holder, position_kind in (('BlockEntities', schematic.value['Blocks'], 11), ('Entities', schematic, 9)):
        entries = holder.value.get(key)
        if entries is None:
            continue
        if entries.kind != 9 or (entries.value[1] and entries.value[0] != 10):
            raise ValueError('Invalid ' + key + ' list')
        for entry in entries.value[1]:
            fields = entry.value
            if fields.get('Id', Tag(0,None)).kind != 8 or not fields['Id'].value:
                raise ValueError('Missing ' + key + ' Id')
            position = fields.get('Pos', Tag(0,None))
            if position.kind != position_kind:
                raise ValueError('Invalid ' + key + ' position type')
            coordinates = position.value if position_kind == 11 else position.value[1]
            if len(coordinates) != 3 or (position_kind == 9 and position.value[0] != 6):
                raise ValueError('Invalid ' + key + ' position size')
            if position_kind == 11 and any(not 0 <= p < limit for p,limit in zip(coordinates,dimensions)):
                raise ValueError('Block entity outside schematic')
            if 'Data' in fields and fields['Data'].kind != 10:
                raise ValueError('Invalid ' + key + ' Data')
            # Old extractor flattened the BE inventory; v3 readers ignore those fields.
            if set(fields) - {'Id','Pos','Data'}:
                raise ValueError('Unexpected flattened ' + key + ' fields; expected Data compound')
        result['block_entities' if key == 'BlockEntities' else 'entities'] = len(entries.value[1])
    return result
