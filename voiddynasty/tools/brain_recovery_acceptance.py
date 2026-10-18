"""Recovery of a companion's personal bag after death, dimension change and restart.

Deterministic native phases only: no LLM endpoint is contacted. Each phase runs inside the
disposable lab server (see BrainRecoveryServerTest) and must print ``PASS <phase>``.
"""


def phase(rcon, results, name):
    response = rcon.cmd('braincheckrecovery ' + name)
    print('[recovery]', response, flush=True)
    results.append({'phase': 'recovery_' + name, 'result': response})
    if 'PASS ' + name not in response:
        raise AssertionError(response)
    return response


def before_restart(rcon, lab, results, notes):
    # Death leaves exactly one ledger entry with the exact items; a new companion can take it
    # back; a full bag refuses without moving anything; a dimension change follows the items.
    for name in ['setup', 'death', 'restore', 'atomic', 'dimension', 'stash']:
        phase(rcon, results, name)


def after_restart(rcon, lab, results, notes):
    # The ledger written before the stop must come back identical, then restore once, never twice.
    phase(rcon, results, 'restart')
