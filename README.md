# ludo-planung-timefold

Planning tool for shift scheduling at the Ludothek.

Using [Timefold](https://timefold.ai/) (formerly OptaPlanner), a schedule is created as well as possible to plan people
according their availabilities.

**Hard constraints:**

* People only work on days they are available
* There are always two people working together in a shift
* No person is working twice a week

**Soft constraints, will be optimized:**

* Each person can indicate how many shifts per month they want and should get the according amount of shifts
* Shifts per person should be well distributed
