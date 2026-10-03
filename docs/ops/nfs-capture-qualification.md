# NFS capture — operations qualification

This is the derived operations reading destination for the historical
[NFS capture](../inbox/2026.08.09.23.26.12.md), not an executable runbook.
The decorative vendor logo is omitted: it supplies no storage or network
information. The captured command blocks and persistence sketches are not
copied into this document.

## Routing and authority

The raw capture stays in `docs/inbox/` solely as immutable provenance for the
SHX review corrections. It is unrelated to the SHX IR and is not pending shell
feature work. This local `docs/ops/` note records its qualification and routing;
it does not establish SHX as the owner of deployed storage.

An operational successor belongs with the repository owning the actual service
deployment. That owner and destination must be approved before promotion or
publication of host-specific instructions. No such approval, relocation of the
raw capture, service change or board completion is claimed here.

## Before an operational successor can be used

Carry forward the original capture's
[qualification addendum](../inbox/2026.08.09.23.26.12.md#qualification-addendum--2026-10-03)
and the [SHX constraints](../design/shx1-qualification-constraints.md).

- Verify the actual provider product, region, VPC, export and client topology
  from current authoritative documentation; the capture's historical region
  list and deployment examples do not establish present availability.
- Separate read-only device identification from formatting. The reviewed
  runbook needs affirmative ownership/newness/unmounted-state confirmation
  for the exact device; absence of a signature alone is not permission to
  discard data.
- Verify the deployed NFS versions and protocol settings before relying on a
  port rule. Installation alone does not prove an NFSv4-only service.
- Document the trusted-client UID/GID boundary or the reviewed cryptographic
  identity configuration. The capture's root-squash setting is not evidence
  of non-root client authentication.
- Obtain the owning deployment's authorization for changes and specify
  failure, recovery and verification steps before any execution.

These are unfulfilled operational prerequisites, not deployment evidence. The
later Mongo example also remains an unadmitted persistence sketch; it needs
canonical event/cause identity, uniqueness, retry and conflict proof from its
own upstream contract. Neither example is repaired or implemented here.

## Provenance

Repository: `octave-commons/shx`.
Original source revision: `f6356e91b69d97160151d5b933a0a4f3bf566189`.
Path: `docs/inbox/2026.08.09.23.26.12.md`.
Original byte count: `88637`.
Original-byte SHA256: `48b21892725cf6064a48573641168b959ff5e785e2433ee6153e605172fe9439`.
Qualified source inspected at `7ef838d0912183b3bafd367419cdfbfeee9670a7`.
The original bytes and its existing safe-use addendum are unchanged.

This successor addresses review `4895165651` findings
`f3ac8df18e1d9579e0122ef2` and `eea4ae2d34cb2b70397300fc`.
The index explains why the source remains archived, using the review's explicit
retain-with-reason alternative. Scope comes from card
`a6b112a4-9d6c-4f7d-9bdf-d05bdc690f88` and `shx-kanban-process-inbox`.
