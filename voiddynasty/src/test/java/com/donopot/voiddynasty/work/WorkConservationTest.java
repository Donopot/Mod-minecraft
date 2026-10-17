package com.donopot.voiddynasty.work;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.SimpleContainer;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

/** Standalone native Minecraft tests; no player world or game process required. */
public final class WorkConservationTest {
    private static int checks;
    private static void check(boolean ok, String name) {
        if (!ok) throw new AssertionError(name);
        checks++; System.out.println("PASS " + name);
    }
    private static ItemStack stack(Item item, int count) { return new ItemStack(item, count); }
    private static JsonObject spec() {
        return JsonParser.parseString("{\"schema\":1,\"type\":\"GATHER\",\"request_id\":\"unit-1\",\"item\":\"minecraft:diamond\",\"amount\":3,\"min\":[1,64,1],\"max\":[3,64,1],\"destination\":[8,64,1],\"origin\":[0,64,0]}").getAsJsonObject();
    }
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        // Standalone bootstrap has no datapack component binding; explicitly bind fixture defaults.
        for (Item item : BuiltInRegistries.ITEM) item.builtInRegistryHolder().bindComponents(
                DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
        Items.IRON_PICKAXE.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                .set(DataComponents.MAX_STACK_SIZE, 1).set(DataComponents.MAX_DAMAGE, 250)
                .set(DataComponents.DAMAGE, 0).build());
        Items.ENDER_PEARL.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                .set(DataComponents.MAX_STACK_SIZE, 16).build());
        List<ItemStack> full = new ArrayList<>();
        BuiltInRegistries.ITEM.stream().filter(i -> i != Items.AIR && i != Items.DIAMOND)
                .limit(36).forEach(i -> full.add(stack(i, 1)));
        check(WorkInventory.insert(full, 36, List.of(stack(Items.DIAMOND, 1))) == null,
                "36 incompatible partial stacks reject new type without loss");
        check(full.size() == 36 && full.stream().allMatch(s -> s.getCount() == 1), "rejection leaves original unchanged");
        List<ItemStack> merged = WorkInventory.insert(List.of(stack(Items.DIAMOND, 63)), 1, List.of(stack(Items.DIAMOND, 1)));
        check(merged != null && merged.getFirst().getCount() == 64, "compatible partial stack merges");
        check(WorkInventory.insert(List.of(stack(Items.DIAMOND, 63)), 1, List.of(stack(Items.DIAMOND, 2))) == null,
                "partial insertion cannot silently discard overflow");
        check(WorkInventory.insert(List.of(stack(Items.ENDER_PEARL, 16)), 1, List.of(stack(Items.ENDER_PEARL, 1))) == null,
                "native 16 item stack limit respected");
        check(WorkInventory.insert(List.of(), 1, List.of(stack(Items.IRON_PICKAXE, 2))) == null,
                "unstackable tool requires two slots");
        ItemStack named = stack(Items.DIAMOND, 2);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Chantier test"));
        check(WorkInventory.insert(List.of(named), 1, List.of(stack(Items.DIAMOND, 1))) == null,
                "different components do not merge");
        List<ItemStack> input = List.of(named, stack(Items.DIAMOND, 7), stack(Items.EMERALD, 5));
        List<ItemStack> debited = WorkInventory.debit(input, List.of(stack(Items.DIAMOND, 3)));
        check(debited != null && debited.get(1).getCount() == 4 && debited.get(0).getCount() == 2
                && debited.get(2).getCount() == 5, "partial rollback preserves named and unrelated resources");
        check(input.get(1).getCount() == 7, "debit plan does not mutate original");
        check(WorkInventory.debit(input, List.of(stack(Items.DIAMOND, 8))) == null, "missing output refuses rollback");
        check(WorkInventory.debit(input, List.of(stack(Items.DIAMOND, 4), stack(Items.DIAMOND, 4))) == null,
                "repeated receipt requests cannot double count a slot");
        AtomicBoolean restored = new AtomicBoolean(), written = new AtomicBoolean();
        check(!WorkRollback.apply(input, List.of(stack(Items.DIAMOND, 8)), () -> { restored.set(true); return true; },
                after -> written.set(true)) && !restored.get() && !written.get(), "missing resources never restore block");
        check(!WorkRollback.apply(input, List.of(stack(Items.DIAMOND, 1)), () -> false,
                after -> written.set(true)) && !written.get(), "failed block mutation never debits inventory");
        check(WorkRollback.apply(input, List.of(stack(Items.DIAMOND, 1)), () -> true,
                after -> { check(after.get(1).getCount() == 6, "successful rollback debits exact count"); written.set(true); }),
                "successful transaction completes");
        var state = new WorkState(); state.jobName = "test"; state.finished = true;
        state.invSlots.add(named); state.invSlots.add(stack(Items.IRON_PICKAXE, 1));
        state.invSlots.get(1).setDamageValue(91); state.tool = state.invSlots.get(1).copy();
        state.hasDepot = true; state.depots.add(BlockPos.ZERO); state.depots.add(new BlockPos(4, 0, 0));
        state.rollbackStarted = true;
        state.buildingDepot = true; state.depotPlanned = true; state.planX = 7;
        state.depotBuildTicks = 6; state.supplyChests = 62; state.supplySupports = 61;
        var entry = new WorkState.Entry(new BlockPos(1, 2, 3), NbtUtils.writeBlockState(Blocks.DIAMOND_ORE.defaultBlockState()), 7);
        entry.dropsKnown = true; entry.drops = List.of(named.copy()); state.journal.add(entry);
        var ops = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        var encoded = WorkState.CODEC.encodeStart(ops, state).getOrThrow();
        WorkState loaded = WorkState.CODEC.parse(ops, encoded).getOrThrow();
        check(ItemStack.matches(named, loaded.invSlots.getFirst()), "save reload preserves full named stack");
        check(loaded.invSlots.get(1).getDamageValue() == 91 && loaded.tool.getDamageValue() == 91, "save reload preserves tool wear");
        check(loaded.hasDepot && loaded.depots.equals(state.depots), "all depots and origin depot persist");
        check(loaded.journal.getFirst().dropsKnown && ItemStack.matches(named, loaded.journal.getFirst().drops.getFirst()),
                "exact rollback receipt persists");
        check(loaded.rollbackStarted && !loaded.hasJob(), "restart cannot automatically resume mining after rollback");
        check(loaded.buildingDepot && loaded.depotPlanned && loaded.planX == 7 && loaded.depotBuildTicks == 6
                && loaded.supplyChests == 62 && loaded.supplySupports == 61, "construction plan and separate material kit survive reload");
        CompoundTag previousFormat = ((CompoundTag) encoded).copy();
        CompoundTag previousMeta = previousFormat.getCompound("metadata").orElseThrow(); previousMeta.putInt("schema", 2);
        check(!WorkState.CODEC.parse(ops, previousFormat).getOrThrow().needsRecovery(), "existing schema2 jobs remain readable");
        CompoundTag corrupt = ((CompoundTag) encoded).copy(); corrupt.remove("inventory");
        WorkState locked = WorkState.CODEC.parse(ops, corrupt).getOrThrow();
        check(locked.needsRecovery() && !locked.hasJob(), "malformed modern data locks instead of becoming empty legacy inventory");
        check(WorkState.CODEC.encodeStart(ops, locked).getOrThrow().equals(corrupt), "quarantine retains original NBT fields");
        CompoundTag legacy = new CompoundTag(), job = new CompoundTag(); job.putString("name", "legacy");
        ListTag inv = new ListTag(); inv.add(StringTag.valueOf("minecraft:diamond|5")); job.put("inv", inv);
        legacy.put("job", job);
        WorkState migrated = WorkState.CODEC.parse(ops, legacy).getOrThrow();
        check(migrated.invCount() == 5 && migrated.invSlots.getFirst().is(Items.DIAMOND), "legacy counts migrate without deletion");
        inv.add(StringTag.valueOf("missing:unknown|7"));
        check(WorkState.CODEC.parse(ops, legacy).getOrThrow().needsRecovery(), "unresolvable legacy item locks without deleting inventory");
        SimpleContainer destination = new SimpleContainer(2);
        destination.setItem(0, stack(Items.DIAMOND, 5)); destination.setItem(1, stack(Items.EMERALD, 64));
        var transfer = WorkInventory.transfer(List.of(stack(Items.DIAMOND, 7)), destination, s -> s.is(Items.DIAMOND), 3);
        check(transfer.count() == 3 && transfer.bag().getFirst().getCount() == 4 && transfer.container().getFirst().getCount() == 8,
                "mission delivery caps quantity and preserves surplus");
        check(destination.getItem(0).getCount() == 5 && transfer.container().get(1).getCount() == 64, "delivery planning preserves original and unrelated chest contents");
        destination.setItem(0, stack(Items.DIAMOND, 64));
        check(WorkInventory.transfer(List.of(named), destination, s -> true, 2).count() == 0, "full destination transfers nothing without losing named carry");
        destination.setItem(0, ItemStack.EMPTY);
        var nameTransfer = WorkInventory.transfer(List.of(named), destination, s -> true, 1);
        check(ItemStack.isSameItemSameComponents(nameTransfer.container().getFirst(), named)
                && nameTransfer.bag().getFirst().getCount() == 1, "capped delivery retains exact components on both sides");
        WorkMission mission = MissionService.parse(spec());
        check(mission.sameRequest(MissionService.parse(spec())), "mission request identity ignores generated mission UUID");
        JsonObject changed = spec(); changed.addProperty("amount", 4);
        check(!mission.sameRequest(MissionService.parse(changed)), "changed quantity is not an idempotent replay");
        for (String field : List.of("fraction", "huge", "unknown")) {
            JsonObject invalid = spec();
            if (field.equals("fraction")) invalid.addProperty("amount", 1.5);
            if (field.equals("huge")) { invalid.add("min", JsonParser.parseString("[-29999984,-2048,-29999984]")); invalid.add("max", JsonParser.parseString("[29999984,2047,29999984]")); }
            if (field.equals("unknown")) invalid.addProperty("execute", "fill everything");
            boolean rejected = false; try { MissionService.parse(invalid); } catch (RuntimeException expected) { rejected = true; }
            check(rejected, "invalid mission rejected: " + field);
        }
        WorkState missionState = new WorkState(); missionState.jobName = mission.id; missionState.missionId = mission.id;
        missionState.missions.add(mission); mission.status = "DELIVERING"; mission.collected = 5; mission.delivered = 2;
        var missionEncoded = WorkState.CODEC.encodeStart(ops, missionState).getOrThrow();
        WorkState missionLoaded = WorkState.CODEC.parse(ops, missionEncoded).getOrThrow();
        WorkMission resumed = MissionService.active(missionLoaded);
        check(resumed != null && resumed.id.equals(mission.id) && resumed.status.equals("PAUSED") && resumed.reason.equals("SERVER_RESTART"),
                "mission identity persists and restart requires explicit resume");
        check(resumed.collected == 5 && resumed.delivered == 2 && resumed.destination.equals(mission.destination), "partial delivery progress and destination persist");
        missionState.jobName = null; missionState.missionId = ""; mission.status = "SUCCEEDED"; mission.delivered = 3;
        WorkState archived = WorkState.CODEC.parse(ops, WorkState.CODEC.encodeStart(ops, missionState).getOrThrow()).getOrThrow();
        check(archived.missions.size() == 1 && archived.missions.getFirst().status.equals("SUCCEEDED"), "closed request history survives without active job");
        JsonObject excavation=spec();excavation.addProperty("access","excavate");
        check(!MissionService.parse(excavation).sameRequest(MissionService.parse(spec())),"excavation consent is part of request identity");
        BuildPlan platform=new BuildPlan("platform_3x3_v1","minecraft:cobblestone",new BlockPos(20,64,20));
        BuildPlan house=new BuildPlan("shelter_5x5_v1","minecraft:oak_planks",new BlockPos(20,64,20));
        check(platform.blocks().size()==9 && house.blocks().size()==94 && new HashSet<>(house.blocks()).size()==94,"versioned plan counts are exact and positions unique");
        check(!house.blocks().contains(house.anchor().offset(1,1,0)) && !house.blocks().contains(house.anchor().offset(2,2,0)),"house entrance fits physical servitor width");
        JsonObject buildJson=JsonParser.parseString("{\"schema\":1,\"type\":\"BUILD\",\"request_id\":\"build-unit\",\"blueprint\":\"platform_3x3_v1\",\"material\":\"minecraft:cobblestone\",\"anchor\":[20,64,20],\"source\":[15,64,20],\"origin\":[16,64,20]}").getAsJsonObject();
        WorkMission build=MissionService.parse(buildJson);build.status="BUILDING";build.materialsReserved=true;build.collected=9;build.placed=1;build.delivered=1;
        WorkState building=new WorkState();building.jobName=build.id;building.missionId=build.id;building.missions.add(build);building.invSlots.add(stack(Items.COBBLESTONE,8));
        WorkState.Entry placement=new WorkState.Entry(platform.anchor(),NbtUtils.writeBlockState(Blocks.AIR.defaultBlockState()),10);
        placement.placed=NbtUtils.writeBlockState(Blocks.COBBLESTONE.defaultBlockState());placement.dropsKnown=true;placement.drops=List.of(stack(Items.COBBLESTONE,1));building.journal.add(placement);
        WorkState restoredBuild=WorkState.CODEC.parse(ops,WorkState.CODEC.encodeStart(ops,building).getOrThrow()).getOrThrow();
        WorkMission restoredMission=MissionService.active(restoredBuild);
        check(restoredMission.build.equals(platform) && restoredMission.materialsReserved && restoredMission.placed==1 && restoredMission.status.equals("PAUSED"),"build plan, reservation and progress persist and require explicit resume");
        check(restoredBuild.invCount()==8 && restoredBuild.journal.getFirst().placed!=null && restoredBuild.journal.getFirst().drops.getFirst().is(Items.COBBLESTONE),"placement refund receipt and unspent materials survive codecs");
        check(WorkInventory.insert(restoredBuild.invSlots,36,restoredBuild.journal.getFirst().drops).getFirst().getCount()==9,"placement refund restores conserved total without mining loot debit");
        ItemStack namedMaterial=stack(Items.COBBLESTONE,9);namedMaterial.set(DataComponents.CUSTOM_NAME,Component.literal("Keep me"));
        check(WorkInventory.debit(List.of(namedMaterial),List.of(platform.cost(9)))==null,"construction cannot consume named material as plain blocks");
        buildJson.addProperty("material","minecraft:tnt");boolean rejectedBuild=false;try {MissionService.parse(buildJson);}catch(IllegalArgumentException e) {rejectedBuild=true;}
        check(rejectedBuild,"unsafe or unsupported build blocks refused before execution");
        System.out.println("WORK CONSERVATION: " + checks + " checks passed");
    }
}
