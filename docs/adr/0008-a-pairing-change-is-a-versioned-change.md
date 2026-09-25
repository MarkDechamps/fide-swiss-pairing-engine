# A pairing change is a versioned change

A reference library is judged by its output, not only its API: a client who upgrades expects the same tournament to get the same pairings, standings and check verdicts. So SemVer here covers **behaviour under unchanged settings** as well as the Java API and the CLI protocol (ADR 0005). A release that can change a pairing, a standing or a check verdict for settings the client did not change is:

- **major** when it changes a default (the edition or Interpretation a Profile uses, a Profile's settings), replaces a fixed reading with another, or removes an edition, system or Interpretation;
- **minor** when it only adds something the client must opt into (a new system, a new edition, a new Interpretation value, a new tie-break), so existing settings pair as before;
- **patch** when it fixes a bug: the old output broke the reading the library already documents, shown by a failing test that cites the article.

A new FIDE edition therefore lands in a minor release as an extra edition, and becomes a Profile's default only in the next major. Every release lists each change that can alter an output under **Pairing changes** in `CHANGELOG.md`, with the article and the systems affected.

## Considered Options

- SemVer on the API only: a patch could silently re-pair live tournaments, which a reference library cannot allow.
- A separate "rules version" beside the library version: two numbers to explain, and the editions already name the rules.
- Calendar versions tied to FIDE editions: FIDE issues editions per chapter at different dates, so one calendar cannot describe them.

## Consequences

Changing a reading costs a major version, which pushes contested readings towards Interpretations (added in a minor, with the old reading as default). Profiles always track the newest edition at the time of their major version. Clients that pin editions explicitly are unaffected by Profile default changes.
