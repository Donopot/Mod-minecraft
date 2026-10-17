"""Access excavation and real-material building acceptance through the actual MCP role."""
import asyncio
import time
from mission_acceptance import connection,call,check,wait


def fixture(rcon,notes,phase):
    text=rcon.cmd('nextcheck '+phase)
    notes.setdefault('next_native_phases',[]).append({'phase':phase,'result':text})
    print('[next-native]',text,flush=True)
    if 'PASS '+phase not in text: raise RuntimeError(text)


async def rollback(session,mid,expected='COMPLETE'):
    out=await call(session,'servitor_mission_control',mission_id=mid,action='rollback')
    if not out.get('ok'): raise RuntimeError(out)
    deadline=time.monotonic()+25
    while time.monotonic()<deadline:
        out=await call(session,'servitor_mission_status',mission_id=mid)
        if out.get('rollback_state')==expected: return out
        await asyncio.sleep(.1)
    raise TimeoutError(out)


async def recover(session,mid):
    out=await call(session,'servitor_mission_control',mission_id=mid,action='deposit')
    if not out.get('ok'): raise RuntimeError(out)
    deadline=time.monotonic()+60
    while time.monotonic()<deadline:
        out=await call(session,'servitor_mission_status',mission_id=mid)
        if not out['worker_active'] and out['carried_items']==0: return out
        await asyncio.sleep(.1)
    raise TimeoutError(out)


def build_spec(key,plan='platform_3x3_v1'):
    return dict(request_id=key,blueprint=plan,material='minecraft:cobblestone',anchor=[50,64,10],source=[44,64,10],origin=[46,64,6])


async def before_restart(rcon,lab,notes,skip_access=False):
    async with connection(lab) as session:
        if not skip_access:
            fixture(rcon,notes,'accessSetup')
            created=await call(session,'servitor_gather',request_id='mcp-tunnel',item='minecraft:diamond',amount=1,
                               zone_min=[34,64,18],zone_max=[48,65,30],destination=[44,64,10],origin=[30,64,24],access='excavate')
            check(notes,'access_mission_accepted',created.get('ok') and created['mission']['access']=='excavate')
            mid=created['mission']['id']
            fixture(rcon,notes,'accessGuards')
            done=await wait(session,mid,{'SUCCEEDED'},timeout=150)
            check(notes,'hidden_ore_delivered_via_bounded_access',done['mission']['access_blocks_mined']>0 and done['mission']['delivered_total']==1)
            fixture(rcon,notes,'accessDone')
            result=await rollback(session,mid)
            check(notes,'access_rollback_complete',result['rollback_state']=='COMPLETE')
            fixture(rcon,notes,'accessRollback')
            await call(session,'servitor_mission_control',mission_id=mid,action='close')
    
        fixture(rcon,notes,'buildSetup');fixture(rcon,notes,'noMaterials')
        request=build_spec('mcp-platform')
        fixture(rcon,notes,'occupySite')
        occupied=await call(session,'servitor_build',**{**request,'request_id':'mcp-site-refused'})
        check(notes,'occupied_build_site_rejected_before_reservation',not occupied['ok'] and occupied['error']=='BUILD_SITE_OCCUPIED')
        fixture(rcon,notes,'clearSite')
        estimate=await call(session,'servitor_build_estimate',**{k:v for k,v in request.items() if k!='request_id'})
        check(notes,'build_estimate_read_only',estimate.get('ok') and estimate['required']==9 and estimate['site_problem']=='')
        created=await call(session,'servitor_build',**request)
        check(notes,'build_submission_is_not_success',created.get('ok') and created['mission']['type']=='BUILD' and not created['mission']['goal_met'])
        mid=created['mission']['id']
        paused=await wait(session,mid,{'PAUSED'})
        check(notes,'missing_materials_pause_without_partial_debit',paused['mission']['reason']=='MATERIALS_MISSING')
        fixture(rcon,notes,'missing');fixture(rcon,notes,'refill')
        await call(session,'servitor_mission_control',mission_id=mid,action='resume')
        done=await wait(session,mid,{'SUCCEEDED'},timeout=80);fixture(rcon,notes,'platformDone')
        replay=await call(session,'servitor_build',**request)
        check(notes,'build_replay_cannot_debit_materials_twice',replay.get('replayed') and replay['mission']['id']==mid and replay['mission']['placed_total']==9)
        changed=await call(session,'servitor_build',**{**request,'blueprint':'shelter_5x5_v1'})
        check(notes,'build_request_conflict_is_rejected',not changed['ok'] and changed['error']=='REQUEST_CONFLICT')
        await rollback(session,mid);fixture(rcon,notes,'buildRollback')
        refused=await call(session,'servitor_mission_control',mission_id=mid,action='close')
        check(notes,'refunded_material_cannot_be_discarded',not refused['ok'])
        await recover(session,mid);fixture(rcon,notes,'recovered')
        await call(session,'servitor_mission_control',mission_id=mid,action='close')

        fixture(rcon,notes,'buildSetup')
        partial=await call(session,'servitor_build',**build_spec('mcp-cancelled-build'))
        mid=partial['mission']['id'];deadline=time.monotonic()+40
        while time.monotonic()<deadline:
            status=await call(session,'servitor_mission_status',mission_id=mid)
            if status['mission']['placed_total']>=1: break
            if status['mission']['status']=='PAUSED': raise RuntimeError(status)
            await asyncio.sleep(.05)
        else: raise TimeoutError(status)
        cancelled=await call(session,'servitor_mission_control',mission_id=mid,action='cancel')
        check(notes,'partial_build_cancel_preserves_reservation',cancelled['mission']['status']=='CANCELLED' and not cancelled['mission']['goal_met'])
        fixture(rcon,notes,'buildCancelled')
        await recover(session,mid);fixture(rcon,notes,'cancelCarryRecovered')
        await rollback(session,mid);fixture(rcon,notes,'cancelBuildRolledBack')
        await recover(session,mid);fixture(rcon,notes,'recovered')
        check(notes,'partial_build_reconciled_after_deposit_then_rollback',True)
        await call(session,'servitor_mission_control',mission_id=mid,action='close')

        fixture(rcon,notes,'buildSetup')
        request=build_spec('mcp-house-restart','shelter_5x5_v1')
        estimate=await call(session,'servitor_build_estimate',**{k:v for k,v in request.items() if k!='request_id'})
        check(notes,'house_estimate_accounts_for_usable_entrance',estimate.get('ok') and estimate['required']==94)
        created=await call(session,'servitor_build',**request)
        if not created.get('ok'): raise RuntimeError(created)
        mid=created['mission']['id'];notes['house_request']=request;notes['house_id']=mid
        deadline=time.monotonic()+60
        while time.monotonic()<deadline:
            progress=await call(session,'servitor_mission_status',mission_id=mid)
            if progress['mission']['placed_total']>=3: break
            if progress['mission']['status']=='PAUSED': raise RuntimeError(progress)
            await asyncio.sleep(.1)
        else: raise TimeoutError(progress)
        paused=await call(session,'servitor_mission_control',mission_id=mid,action='pause')
        notes['house_placed']=paused['mission']['placed_total']
        fixture(rcon,notes,'housePartial')


async def after_restart(rcon,lab,notes):
    fixture(rcon,notes,'housePersisted')
    async with connection(lab) as session:
        mid=notes['house_id']
        replay=await call(session,'servitor_build',**notes['house_request'])
        check(notes,'build_restart_preserves_identity_and_progress',replay.get('replayed') and replay['mission']['id']==mid and replay['mission']['placed_total']==notes['house_placed'])
        await call(session,'servitor_mission_control',mission_id=mid,action='resume')
        done=await wait(session,mid,{'SUCCEEDED'},timeout=150);fixture(rcon,notes,'houseDone')
        check(notes,'restarted_house_matches_observed_goal',done['mission']['placed_total']==94 and done['mission']['goal_met'])
        fixture(rcon,notes,'tamper')
        failed=await rollback(session,mid,'FAILED');fixture(rcon,notes,'tampered')
        check(notes,'tampered_build_never_refunds_or_removes_external_block',failed['rollback_state']=='FAILED' and failed['carried_items']==0)
        fixture(rcon,notes,'repair')
        await rollback(session,mid);fixture(rcon,notes,'buildRollback')
        await recover(session,mid);fixture(rcon,notes,'recovered')
        await call(session,'servitor_mission_control',mission_id=mid,action='close')
    print('EXTENDED MCP END checks='+str(len(notes['checks']))+' failed='+str(sum(not c['passed'] for c in notes['checks'])),flush=True)
