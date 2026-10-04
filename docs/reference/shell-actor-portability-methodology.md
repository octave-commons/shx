# Shell, actor, portability and methodology — derived reader

This note condenses four historical captures. Each opening image is the same
decorative vendor logo, not a diagram of its topic. All four logos are omitted
from this reader surface; the linked original sources remain available with
their provenance. The summaries are design archaeology, not new runtime or
ownership contracts.

## Shell ancestry and names

The [Closh/ancestry capture](../inbox/2026.08.09.22.19.20.md) explores Lisp-aware
shells and compatibility rendering. Its assistant proposes **Alethexx** as a
name for a broader future environment interpreter; that is a naming proposal,
not a ratified rename of this repository's product.

The separate [20.21.57 origin capture](../inbox/2026.08.09.20.21.57.md) records
the user's source-composition/bubbling wish and the response about folding EDN
fragments before rendering. It does not select the Alethexx name.
The current repository still names its EDN shell-config product **envm** in
[AGENTS.md](../../AGENTS.md#two-products-one-repo),
[README.md](../../README.md#envm) and `src/shx/cli.clj`.

Do not infer implemented EDN/bb expression evaluation or a future REPL from the
captured examples. Current `:source` is a string under `src/shx/law/ir.clj`;
the origin capture's qualification addendum names the normalization and
evaluation prerequisites for any future extension.

## Actor vocabulary

The [actor-model capture](../inbox/2026.08.09.23.28.40.md) explores Keryx as
addressed communication, Axxium as identity and Clio as the history substrate.
It distinguishes received messages or commands from durable claims that
something happened. This vocabulary is a captured proposal; it does not
implement an actor runtime or move domain ownership into SHX. Consult the
[qualification constraints](../design/shx1-qualification-constraints.md)
before promoting any of its role or runtime claims.

## Portability

The [portability capture](../inbox/2026.08.09.23.44.17.md) argues for a shared
pure subset, with runtime-specific effects at adapters. Its examples are not
proof that arbitrary `.cljs` works unchanged in every runtime. Each actual
consumer needs the supported-version and compilation/test evidence in its
own build. The captured wildcard policy values remain non-executable
placeholders, as its existing qualification addendum states.

## Methodology, process and procedure

The [methodology capture](../inbox/2026.08.09.23.27.45.md) distinguishes the
principles guiding work, the sequence of work, and instructions for one task.
Read those as vocabulary, not as a new repository workflow or permission to
skip required checks. Current board operations remain Rheos-owned.

## Provenance

Repository: `octave-commons/shx`.
Original source revision for every row:
`f6356e91b69d97160151d5b933a0a4f3bf566189`.
Qualified sources inspected at `7ef838d0912183b3bafd367419cdfbfeee9670a7`.
The hashes bind original captured bytes, excluding subsequent addenda; the
sources and existing addenda are unchanged.

| Original source path | Original bytes | Original-byte SHA256 |
| --- | --- | --- |
| `docs/inbox/2026.08.09.22.19.20.md` | 15446 | `aa948afa45bfe2d9ba72dccae7d5d3bb39d2c3f6f006f8a4be7037b5887ea335` |
| `docs/inbox/2026.08.09.23.28.40.md` | 21073 | `5ba2ebeb6c01bfa67d614eca67220f1eea94a0200cae0db1ebf2814031d5337f` |
| `docs/inbox/2026.08.09.23.44.17.md` | 30015 | `5490c2cd5c17e4c720f7a109f5e24efa9894e3a3bde694d687561a1139628d50` |
| `docs/inbox/2026.08.09.23.27.45.md` | 5032 | `8856a1357e57a167e0d4bae4c2db015d50af777c8d37dac99b658c18df61502e` |
| `docs/inbox/2026.08.09.20.21.57.md` | 1668 | `cd16f3c4154ad5ad17b323c578f5b02df33833a2915776d6b7719ef4aeed2667` |

The four image removals address review `4895165651` finding
`dbf808b35d0d537bfb96961a`, under card
`a6b112a4-9d6c-4f7d-9bdf-d05bdc690f88`. The source/name distinction and
matching index clarification address finding `1d9b29f769532fe858bb3ae8`
without treating an assistant's candidate as ratification.
