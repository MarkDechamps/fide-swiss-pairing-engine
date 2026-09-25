---
name: object-calisthenics
description: Jeff Bay's nine Object Calisthenics rules as a design lens for Java. Use when refactoring or reviewing Java classes for OO design, or when the user mentions object calisthenics, tell-don't-ask, primitive obsession, first-class collections or one-dot-per-line.
---

# Object Calisthenics

Nine design **exercises** (Jeff Bay, *The ThoughtWorks Anthology*; summary by William Durand, 2013).
Bay meant them as a kata: follow them strictly on practice code, then relax. In production code they
are **pressure, not law** — a violation is a smell to justify, not a build failure.

**Stop rule:** stop applying a rule when the next change adds a type, method or indirection
without adding behaviour or protecting an invariant. Calisthenics that only multiply types is
over-engineering.

## The rules

### 1. One level of indentation per method
Nested loops/conditions force mental compilation. Flatten with Extract Method
(`java-refactoring-extract-method`), naming each extracted piece after what it does.

```java
// before: two levels
String board() {
    var buf = new StringBuilder();
    for (int row = 0; row < 10; row++) {
        for (int col = 0; col < 10; col++) {
            buf.append(data[row][col]);
        }
        buf.append("\n");
    }
    return buf.toString();
}

// after: one level per method
String board() {
    var buf = new StringBuilder();
    for (int row = 0; row < 10; row++) {
        appendRow(buf, row);
    }
    return buf.toString();
}

private void appendRow(StringBuilder buf, int row) {
    for (int col = 0; col < 10; col++) {
        buf.append(data[row][col]);
    }
    buf.append("\n");
}
```
Indentation is the symptom; the goal is methods that each do one thing. Collapsing a body onto one
line or dropping braces does not satisfy the rule.

### 2. No `else`
Prefer, in order: **early return / guard clause** → parametrised variable → polymorphism
(State, Strategy, Null Object).

```java
if (!credentials.valid()) {
    return Redirect.toLogin("Bad credentials");
}
return Redirect.toHomepage();
```
A `switch` with pattern matching over a sealed type is **not** polymorphism — it is the opposite
trade-off. Polymorphism makes adding a *type* cheap and adding an *operation* expensive; a sealed
switch makes adding an operation cheap and adding a type expensive (every switch must change).
Choose the sealed switch for closed hierarchies with many operations, polymorphism for open
hierarchies with stable operations.

### 3. Wrap primitives and strings that carry rules
Wrap a primitive or `String` in a value object when it **has an invariant or behaviour**
(`record Rating(int value)` validating its range in the compact constructor), or when two
same-typed values could be confused at a call site (`PlayerId` vs `TournamentId`). Fixes
Primitive Obsession and gives the compiler a name to check.

Don't wrap for its own sake: a value with no rule, no behaviour and no confusion risk stays
primitive. Loop counters and indices are exempt.

### 4. First-class collections — firm rule
Treat this one as a rule, not a hint: flag every raw collection that carries domain logic.
A class holding a collection holds **nothing else**. The wrapper is the home for filtering,
sorting, invariants and queries over the elements (`Players`, `Pairings`, not `List<Player>`
passed around with logic scattered in callers). Never expose the mutable inner collection.
A class that *owns* a first-class collection alongside other fields (a `Round` with a number and
its `Pairings`) is fine — the rule binds the wrapper, not its owner.

### 5. Don't talk to strangers (Law of Demeter)
"One dot per line" is a proxy, not the rule. The rule: a method calls only itself, its fields,
its parameters and objects it creates — it does not **navigate through** one object to reach
another's internals. `order.customer().address().city()` couples the caller to three structures.

Not violations — each call returns a value or the same abstraction, not a stranger's internals:
- fluent APIs, builders, streams, `Optional` chains;
- chains over immutable value objects (`rating.plus(delta).capped()`).

Fix by moving the behaviour to the object that owns the data — **but** don't push presentation or
infrastructure into the domain to get there. Durand's example fixes `loc.current.representation`
by passing a `StringBuilder` into `Piece`; that trades a Demeter violation for rendering leaking
into the model. Better: the domain exposes a meaningful query (`piece.symbol()`) and the renderer
does the rendering.

### 6. Don't abbreviate
Wanting to abbreviate means the name is too long because the thing does too much, or the name is
repeated everywhere (duplication). Split the responsibility; don't shorten the word.
No `mgr`, `ctx`, `tmp`, `calc`. If no decent name exists, the design is wrong.

### 7. Keep all entities small — heuristic
Target: class ≤ 50 lines (tolerate ~150), package ≤ 10 files. Over the limit is a prompt to look
for a hidden concept to extract, not a reason to split arbitrarily.

### 8. No more than two instance variables — heuristic
A class either manages one piece of state or coordinates two collaborators. Reachable only with
rules 3 and 4. Use it as a lens to **discover missing objects** (which fields change together?
which are used by the same methods?), not as a hard cap. Subject to the stop rule.

### 9. Tell, don't ask — no setters, careful getters
The smell is a caller **pulling raw state out to make a decision the object should make**:

```java
// ask (bad): the caller owns the scoring rule
game.setScore(game.getScore() + ENEMY_DESTROYED_SCORE);

// tell (good): the object owns it
game.addScore(ENEMY_DESTROYED_SCORE);
```
- **Setters: no.** State changes go through intention-revealing commands that keep invariants.
- **Queries: yes** (command–query separation). A method returning a value the object computes or
  owns meaningfully (`standing.points()`, `player.isWithdrawn()`) is fine.
- **Accessors at the edges** (record components, DTOs, mapping to views/persistence) are fine.
  Don't drag rendering, serialisation or persistence into the domain to avoid a getter.
- Red flag: `if (x.getA() ... x.getB())` at a call site — move that decision onto `x`.

## Relation to `clean-java`
Mostly aligned (guard clauses, value objects, "prefer predicates on the owning object",
`contract.addLine(line)` over `getLines().add(line)`). Where they differ, **`clean-java` wins**:
- `clean-java` allows a **ternary** when both branches are simple — that is not a Rule 2 violation.
- `clean-java` says use **existing typed accessors** on value objects — reading them is fine under Rule 9.
- `Optional.map()/or()` chains preferred by `clean-java` are exempt from Rule 5.
- Tests may use accessors freely for assertions.

## Applying
- **Refactoring:** work rule by rule, one small step at a time, tests green between steps (`tdd`).
  Start with the cheap, high-yield rules: 4, 2, 1, 9, 3.
- **Reviewing:** for each violation, name the rule and propose the concrete move (extract method,
  introduce value object, move method to data owner, wrap collection). Don't flag exempt cases.
  Report rules 7 and 8 as design hints, never blockers.
- Every suggestion must pass the stop rule: name the behaviour or invariant the change adds.
