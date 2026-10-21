"""Disposable native brain/work bridge and real MCP acceptance; never enables an LLM."""
import asyncio
import json
import time
from mission_acceptance import connection, call, check


def phase(rcon, results, name):
    response=rcon.cmd('braincheck '+name)
    print('[brain]',response,flush=True)
    results.append({'phase':'brain_'+name,'result':response})
    if 'PASS '+name not in response: raise AssertionError(response)


async def before_restart(rcon, lab, results, notes):
    for name in ['setup','validation','bag']:
        phase(rcon,results,name)
    async with connection(lab) as session:
        names={tool.name for tool in (await session.list_tools()).tools}
        check(notes,'restricted_servitor_tools',{'servitor_find','servitor_observe','servitor_mission_wait','servitor_gather','servitor_mission_control'} <= names and all(name.startswith('servitor_') for name in names))
        found=await call(session,'servitor_find',item='cobblestone',zone_min=[34,64,4],zone_max=[38,64,4],limit=1)
        if not found.get('ok'): raise AssertionError(found)
        check(notes,'real_mcp_find',found['matches_total']==5 and len(found['matches'])==1 and found['truncated'])
        unknown=await call(session,'servitor_find',item='diamond',zone_min=[100000,64,100000],zone_max=[100001,64,100001])
        if not unknown.get('ok'): raise AssertionError(unknown)
        check(notes,'real_mcp_unknown_area',not unknown['coverage']['complete'] and not unknown['conclusive_absence'])
        rejected=await session.call_tool('servitor_observe',dict(zone_min=[True,64,4],zone_max=[38,64,4]))
        check(notes,'sdk_rejects_boolean_coordinate',rejected.isError)
        phase(rcon,results,'longObservation')
        large=await call(session,'servitor_find',item='diamond',zone_min=[48,68,0],zone_max=[51,68,15],limit=64)
        check(notes,'real_rcon_multiframe_json',large.get('matches_total')==64 and len(large['matches'])==64 and len(json.dumps(large,separators=(',',':')))>4096)
        phase(rcon,results,'submit')
        current=await call(session,'servitor_mission_status',mission_id='current')
        notes['brain_id']=current['mission']['id']
        waited=await call(session,'servitor_mission_wait',mission_id=notes['brain_id'],timeout_seconds=0)
        check(notes,'real_wait_observes_pause',waited['wait_state']=='paused' and not waited['worker_active'])


async def after_restart(rcon,lab,results,notes):
    phase(rcon,results,'restart')
    async with connection(lab) as session:
        # The native fixture resumed exactly the same companion and mission UUID.
        deadline=time.monotonic()+60
        while time.monotonic()<deadline:
            status=await call(session,'servitor_mission_wait',mission_id=notes['brain_id'],timeout_seconds=2)
            if status.get('wait_state')=='settled':break
            if status.get('wait_state')!='pending':raise AssertionError(status)
        check(notes,'real_wait_verified_delivery',status.get('wait_state')=='settled' and status['mission']['status']=='SUCCEEDED' and status['mission']['delivered_total']==2)
        phase(rcon,results,'success')
        deadline=time.monotonic()+10
        while time.monotonic()<deadline:
            status=await call(session,'servitor_mission_status',mission_id=notes['brain_id'])
            if status.get('rollback_state')=='COMPLETE':break
            await asyncio.sleep(.1)
        check(notes,'real_rollback_verified',status.get('rollback_state')=='COMPLETE')
        phase(rcon,results,'rollback')
        phase(rcon,results,'pathSetup')
        await asyncio.sleep(25)
        phase(rcon,results,'pathCheck')
