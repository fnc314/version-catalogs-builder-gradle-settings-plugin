# Repository Architectural Plans

This directory holds architecture, system design, and migration plans for the repository.

---

## Plan Invariants (MANDATORY)

Every architecture plan created by any agent must strictly adhere to these paradigms:

1. **Target-Derived Location**:
   - Multi-module, cross-subsystem, or repository-wide plans -> `.agents/plans/`
2. **Date Subdirectory**: Plans must be saved in a `YYYY-MM-DD/` folder matching the creation date.
3. **24-Hour Timestamp Prefix**: Filenames must be formatted as `HHMMSS_<plan-name>.md` using a 24-hour clock (`00`-`23` for `HH`).
4. **Registry Synchronization**: Register the plan in the table below with its relative path (`./YYYY-MM-DD/HHMMSS_<plan-name>.md`), status, and scope.
5. **Mandatory Opening Frontmatter**: Start every plan from [`TEMPLATE.md`](./TEMPLATE.md) (`Status`, `Date`, `Target` block); `Status` must match the registry row.

---

### Repository-Wide Plans

| Plan | Status | Scope / Focus Area |
| :--- | :--- | :--- |
| [`004500_version-catalogs-builder-3-0-0-scope-reduction.md`](./2026-11-26/004500_version-catalogs-builder-3-0-0-scope-reduction.md) | Completed | Refocus version-catalogs-builder to version catalogs only for 3.0.0: strip repo handlers, preview flags, toolBuilds; flatten API to `catalog(name)` and `catalog(name, from)`; add @DslMarker; and establish agent planning infrastructure |
