# Combat foundation regression scenarios

These are required checks, not a report that they have run.

## Cheap isolated checks

Compile `CombatFoundationPolicyTest.java` with production `DamageCallStack.java` and
`CombatHitPolicy.java` into an OS temporary directory, then run its `main`.
This checks sequential calls, bounded nesting, exceptions, thread isolation and the explicit
damage/absorption outcome policy. It does not prove that Mixins are woven or that entities render.

## Loader and real-server checks

1. Load `LivingEntity`, `ServerPlayer` and `SkillProjectileEntity` with the full installed pack in
   an isolated instance. Require successful transformation of the exact `hurtServer` descriptor.
   There must be one wrapping hook on the base method; the ServerPlayer override is not wrapped.
2. On ordinary high-health mobs, apply more than eight sequential hits and kill more than eight
   different mobs. Also repeat refused damage against an invulnerable mob. A subsequent normal hit
   must still work. For true nested recursion, the fourth same-key family call must be rejected;
   another independent hit after unwinding must work.
3. For FIRE, MAGMA, ICE, LIGHTNING and PHYSICAL sources assert `getEntity() == caster` and, for a
   projectile, `getDirectEntity() == projectile`. Their original DamageType must remain intact.
   Repeat PvP-disabled/team-protected, Haki, kill credit and protected-claim cases.
4. A health-losing or absorption-consuming offensive impact applies its declared effect and recoil.
   A refused hit, shield-only block or immunity that zeroes damage applies neither. Test actual
   damage calls rather than `computeDamageMagnification` alone; Yami's stateful absorption must run
   once per hit. Gomu blocks PHYSICAL projectiles, and attributed ICE/MAGMA stay nonphysical.
5. Standalone zero-damage declared statuses and `areaEffect` remain independent effect paths.
   A zero-damage visual-only projectile must neither knock back nor create an elemental status.
6. Shoot and use melee in all four cardinal directions and diagonals. Recoil must point away from
   the caster/along the projectile, with the declared strength; strength zero adds no motion.
7. Verify real-world projectile animation over multiple frames and from a second client. Procedure
   controllers must keep advancing while their animation name is unchanged; idle must not compete
   with a procedure on the same bones. This requires an observed client, not the standalone test.

Do not infer coverage of 213 techniques from this common-layer regression set.
