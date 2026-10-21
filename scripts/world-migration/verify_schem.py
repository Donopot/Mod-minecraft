#!/usr/bin/env python3
"""Strict structural Sponge v3 check; never validates P1 or downgrade compatibility."""
import argparse
import json
import zlib
from schematic_nbt import load, plain, validate


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('file')
    args = parser.parse_args()
    try:
        _, root = load(args.file)
        result = validate(root)
        metadata = plain(root.value['Schematic']).get('Metadata', {})
        result.update(file=args.file, structure_pass=True,
                      preview_only=metadata.get('VoidDynastyExtractionComplete') == 0,
                      paste_allowed=False)
        print(json.dumps(result, ensure_ascii=False))
        return 0
    except (ValueError, OSError, EOFError, zlib.error) as error:
        print(json.dumps({'file':args.file, 'structure_pass':False, 'error':str(error),
                          'p1_pass':False, 'paste_allowed':False}, ensure_ascii=False))
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
