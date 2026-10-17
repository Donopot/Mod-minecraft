"""Real MCP stdio -> RCON -> native worker acceptance, only called by the disposable lab."""
import asyncio
from contextlib import asynccontextmanager
import json
import os
from pathlib import Path
import sys
import time
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client


@asynccontextmanager
async def connection(lab):
    params = StdioServerParameters(command=sys.executable, args=[str(Path(__file__).with_name('mcp_vd.py')),'--servitor-only'],
                                   env={**os.environ, 'VD_SERVER_DIR':str(lab)})
    async with stdio_client(params) as streams:
        async with ClientSession(*streams) as session:
            await session.initialize()
            yield session


def check(notes, name, condition):
    notes.setdefault('checks',[]).append({'id':name,'passed':bool(condition)})
    print('[mission-mcp]', 'PASS' if condition else 'FAIL', name, flush=True)
    if not condition: raise AssertionError(name)


async def call(session, tool, **arguments):
    result = await session.call_tool(tool,arguments)
    if result.isError: raise RuntimeError(str(result.content))
    if isinstance(result.structuredContent,dict): return result.structuredContent
    return json.loads(next(c.text for c in result.content if c.type=='text'))


async def wait(session, mission, expected, timeout=45):
    deadline=time.monotonic()+timeout
    while time.monotonic()<deadline:
        out=await call(session,'servitor_mission_status',mission_id=mission)
        if not out.get('ok'): raise RuntimeError(out)
        state=out['mission']['status']
        if state in expected: return out
        if state in {'SUCCEEDED','INCOMPLETE','CANCELLED','PAUSED'}: raise RuntimeError(out)
        await asyncio.sleep(.2)
    raise TimeoutError(out)


def fixture(rcon, notes, phase):
    text=rcon.cmd('missioncheck '+phase)
    notes.setdefault('native_phases',[]).append({'phase':phase,'result':text})
    if 'PASS '+phase not in text: raise RuntimeError(text)


def spec(key, amount=1, item='minecraft:cobblestone', maximum=None):
    return dict(request_id=key,item=item,amount=amount,zone_min=[34,64,4],zone_max=maximum or [38,64,4],
                destination=[42,64,4],origin=[30,64,4],dimension='overworld')


async def before_restart(rcon, lab, notes):
    fixture(rcon,notes,'setup')
    async with connection(lab) as session:
        names={t.name for t in (await session.list_tools()).tools}
        check(notes,'actual_mcp_tool_discovery',names=={'servitor_capabilities','servitor_gather','servitor_mission_status','servitor_mission_control','servitor_build','servitor_build_estimate'})
        caps=await call(session,'servitor_capabilities')
        check(notes,'server_reports_actual_capabilities',caps['ok'] and caps['mission_types']==['GATHER','BUILD'] and 'minecraft:diamond' in caps['gather_items'])
        invalid=await session.call_tool('servitor_gather',spec('bad-boolean',amount=True))
        check(notes,'sdk_rejects_boolean_quantity',invalid.isError)
        unsupported=await call(session,'servitor_gather',**spec('unsupported',item='minecraft:nether_star'))
        check(notes,'unsupported_capability_refused',not unsupported['ok'] and unsupported['error']=='INVALID_SPEC')
        request=spec('mcp-first',amount=2); notes['first_request']=request
        created=await call(session,'servitor_gather',**request); mid=created['mission']['id']; notes['first_id']=mid
        check(notes,'acceptance_is_not_completion',created['ok'] and not created['mission']['goal_met'] and not created['replayed'])
        replay=await call(session,'servitor_gather',**request)
        check(notes,'same_request_reuses_mission',replay['ok'] and replay['replayed'] and replay['mission']['id']==mid)
        changed=await call(session,'servitor_gather',**{**request,'amount':3})
        check(notes,'changed_request_key_conflicts',not changed['ok'] and changed['error']=='REQUEST_CONFLICT')
        busy=await call(session,'servitor_gather',**spec('busy-dont-enqueue'))
        check(notes,'second_request_cannot_overwrite_worker',not busy['ok'] and busy['error']=='WORKER_BUSY')
        completed=await wait(session,mid,{'SUCCEEDED'})
        check(notes,'actual_goal_completion',completed['mission']['delivered_total']==2 and completed['mission']['goal_met'])
        fixture(rcon,notes,'success')
        await call(session,'servitor_mission_control',mission_id=mid,action='rollback')
        deadline=time.monotonic()+10
        while time.monotonic()<deadline:
            rollback=await call(session,'servitor_mission_status',mission_id=mid)
            if rollback.get('rollback_state')=='COMPLETE': break
            await asyncio.sleep(.1)
        check(notes,'typed_rollback_has_verified_completion',rollback.get('rollback_state')=='COMPLETE' and not rollback['mission']['goal_met'])
        fixture(rcon,notes,'rolledBack')
        check(notes,'close_completed_mission',(await call(session,'servitor_mission_control',mission_id=mid,action='close'))['ok'])

        fixture(rcon,notes,'fullSource')
        full=await call(session,'servitor_gather',**spec('mcp-full')); fid=full['mission']['id']
        paused=await wait(session,fid,{'PAUSED'})
        check(notes,'full_destination_is_pause_not_success',paused['mission']['reason']=='DESTINATION_FULL' and not paused['mission']['goal_met'])
        fixture(rcon,notes,'full'); fixture(rcon,notes,'freeSlot')
        check(notes,'resume_paused_mission',(await call(session,'servitor_mission_control',mission_id=fid,action='resume'))['ok'])
        await wait(session,fid,{'SUCCEEDED'}); fixture(rcon,notes,'recovered')
        await call(session,'servitor_mission_control',mission_id=fid,action='close')

        fixture(rcon,notes,'fullSource')
        cancel=await call(session,'servitor_gather',**spec('mcp-cancel')); cid=cancel['mission']['id']
        await wait(session,cid,{'PAUSED'}); fixture(rcon,notes,'full')
        cancelled=await call(session,'servitor_mission_control',mission_id=cid,action='cancel')
        check(notes,'cancel_is_persistent_terminal_state',cancelled['mission']['status']=='CANCELLED' and not cancelled['mission']['goal_met'])
        fixture(rcon,notes,'cancelled')
        refused=await call(session,'servitor_mission_control',mission_id=cid,action='close')
        check(notes,'close_cannot_discard_carried_resources',not refused['ok'] and refused['error']=='CLOSE_REFUSED')
        await call(session,'servitor_mission_control',mission_id=cid,action='deposit')
        deadline=time.monotonic()+40
        while time.monotonic()<deadline:
            status=await call(session,'servitor_mission_status',mission_id=cid)
            if not status['worker_active'] and status['carried_items']==0: break
            await asyncio.sleep(.2)
        fixture(rcon,notes,'deposited')
        check(notes,'typed_cancel_recovery_completed',not status['worker_active'] and status['carried_items']==0)
        await call(session,'servitor_mission_control',mission_id=cid,action='close')

        fixture(rcon,notes,'emptySource')
        empty=await call(session,'servitor_gather',**spec('mcp-empty',amount=10)); eid=empty['mission']['id']
        incomplete=await wait(session,eid,{'INCOMPLETE'})
        check(notes,'exhaustion_has_honest_outcome',not incomplete['mission']['goal_met'] and incomplete['mission']['reason']=='SOURCE_EXHAUSTED')
        fixture(rcon,notes,'incomplete'); await call(session,'servitor_mission_control',mission_id=eid,action='close')

        fixture(rcon,notes,'copperSource')
        copper=await call(session,'servitor_gather',**spec('mcp-copper',item='minecraft:raw_copper',maximum=[35,64,4])); oid=copper['mission']['id']
        overshoot=await wait(session,oid,{'SUCCEEDED'})
        check(notes,'multidrop_goal_is_capped',overshoot['mission']['delivered_total']==1 and overshoot['mission']['collected_total']>=2)
        fixture(rcon,notes,'surplus'); await call(session,'servitor_mission_control',mission_id=oid,action='close')

        fixture(rcon,notes,'fullSource')
        request=spec('mcp-restart'); notes['restart_request']=request
        restart=await call(session,'servitor_gather',**request); rid=restart['mission']['id']; notes['restart_id']=rid
        await wait(session,rid,{'PAUSED'}); fixture(rcon,notes,'full')


async def after_restart(rcon, lab, notes):
    fixture(rcon,notes,'persisted')
    async with connection(lab) as session:
        replay=await call(session,'servitor_gather',**notes['restart_request'])
        check(notes,'restart_replay_keeps_same_identity',replay['ok'] and replay['replayed'] and replay['mission']['id']==notes['restart_id'])
        check(notes,'restart_keeps_actual_partial_counters',replay['mission']['status']=='PAUSED' and replay['mission']['collected_total']==1 and replay['mission']['delivered_total']==0)
        old=await call(session,'servitor_gather',**notes['first_request'])
        check(notes,'closed_request_replay_never_starts_new_work',old['ok'] and old['replayed'] and old['mission']['id']==notes['first_id'] and not old['attached'])
        fixture(rcon,notes,'freeSlot')
        await call(session,'servitor_mission_control',mission_id=notes['restart_id'],action='resume')
        result=await wait(session,notes['restart_id'],{'SUCCEEDED'})
        check(notes,'restart_resume_delivers_exactly_once',result['mission']['collected_total']==1 and result['mission']['delivered_total']==1)
        fixture(rcon,notes,'recovered')
        await call(session,'servitor_mission_control',mission_id=notes['restart_id'],action='close')
    print('MISSION MCP END checks='+str(len(notes['checks']))+' failed='+str(sum(not x['passed'] for x in notes['checks'])),flush=True)
