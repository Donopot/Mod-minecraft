# libs/ — dépendance de compilation non versionnée

Déposer ici `voiddynasty-1.0.0.jar` (produit par `../voiddynasty/gradlew build`) avant de compiler la bridge.
La bridge est compilée **contre** le mod : elle référence `ShipType`, `WeaponService`,
`BoltProjectile`, `LaserBeamEntity`, `ControlPadScreen`, `ShipInfo`, etc.
