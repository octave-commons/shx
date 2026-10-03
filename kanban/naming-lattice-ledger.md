---
uuid: "shx-kanban-naming-lattice-ledger"
title: "Write resources/structure/naming.edn from the inbox notes"
status: "incoming"
priority: P2
labels: ["tasks","naming","ledger","2sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/naming-lattice-ledger.md"
category: "tasks"
points: 2
---

# Naming lattice as a ledger

`docs/inbox/2026.08.10.00.17.16.md` and `docs/inbox/2026.08.09.23.28.40.md` carry the
canonical role assignments: Keryx, Axxium, Clio, Nomos, Psephisma, Praxis.

Turn into `~/.ημ/resources/structure/naming.edn`, one event per name, so a
rename is an `:amend` and a retired name is a `:retract`.
