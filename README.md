# ludo-planung-timefold

Planning tool for shift scheduling at the Ludothek.

Using [Timefold](https://timefold.ai/) (formerly OptaPlanner), a schedule is created as well as possible to plan people
according their availabilities.

**Hard constraints**

* People only work on days they are available

**Soft constraints (strong)**

* Ideal load (e.g. 2 shifts a month) according to people's preferences

**Soft constraints (weak)**

* Shifts per person should be well distributed
* People can define "unwanted" days of week (Monday, Tuesday etc.) which will be avoided if possible

