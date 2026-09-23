# Local-markdown issue tracker (wayfinder)

- Each effort lives in its own folder; `map.md` is the map (label `wayfinder:map`).
- Every other `NN-*.md` file in the folder is a child ticket of that map; its filename is its id.
- Frontmatter fields:
  - `title`: the ticket's name (refer to tickets by this name)
  - `labels`: e.g. `[wayfinder:research]`
  - `status`: `open` | `closed`
  - `assignee`: empty = unclaimed; set it to claim the ticket before any work
  - `blocked_by`: list of ticket ids; a ticket is unblocked when every one of them is `closed`
- **Frontier** = `status: open`, unblocked, and no `assignee`.
- A resolution is appended as a `## Resolution` section (the "resolution comment"), then `status: closed`.
