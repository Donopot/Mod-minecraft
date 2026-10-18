"""Physical stairs, exact resource recovery and route invalidation in a disposable world."""
import json
import time


def fixture(rcon,results,phase):
    result=rcon.cmd('navcheck '+phase)
    print('[navigation]',result,flush=True)
    results.append({'phase':'nav-'+phase,'result':result})
    if 'PASS '+phase not in result: raise RuntimeError(result)


def status(rcon):
    text=rcon.cmd('work mission status current')
    return json.loads(text.split('VDMISSION ',1)[1])


def wait(rcon,condition,timeout=150):
    deadline=time.monotonic()+timeout
    while time.monotonic()<deadline:
        state=status(rcon)
        if condition(state): return state
        if state['mission']['status'] in {'PAUSED','INCOMPLETE','CANCELLED'}: raise RuntimeError(state)
        time.sleep(.2)
    raise TimeoutError(state)


def finish(rcon,results,phase):
    wait(rcon,lambda s:s['mission']['status']=='SUCCEEDED')
    fixture(rcon,results,phase)
    fixture(rcon,results,'rollback')
    deadline=time.monotonic()+20
    while time.monotonic()<deadline:
        if status(rcon)['rollback_state']=='COMPLETE': break
        time.sleep(.1)
    else: raise TimeoutError(status(rcon))
    fixture(rcon,results,'restored')


def before_restart(rcon,results):
    fixture(rcon,results,'begin');time.sleep(1);fixture(rcon,results,'reconcile')
    fixture(rcon,results,'oscillation')
    fixture(rcon,results,'guards')
    fixture(rcon,results,'restrictedSetup')
    wait(rcon,lambda s:s['mission']['status']=='PAUSED')
    fixture(rcon,results,'restricted')
    fixture(rcon,results,'upSetup');finish(rcon,results,'upDone')
    fixture(rcon,results,'upExcavateSetup');finish(rcon,results,'upExcavateDone')
    fixture(rcon,results,'downSetup');finish(rcon,results,'downDone')
    fixture(rcon,results,'downSetup')
    wait(rcon,lambda s:s['journal_entries']>=3)
    fixture(rcon,results,'progress');fixture(rcon,results,'changeFloor')
    wait(rcon,lambda s:s['mission']['status']=='PAUSED')
    fixture(rcon,results,'changed');fixture(rcon,results,'resume')
    finish(rcon,results,'downDone')
    fixture(rcon,results,'downSetup')
    wait(rcon,lambda s:s['journal_entries']>=3)
    fixture(rcon,results,'pause')


def after_restart(rcon,results):
    fixture(rcon,results,'persisted');fixture(rcon,results,'resume')
    finish(rcon,results,'downDone')
