---
name: clean-java
description: Use when writing Java code. Opinionated clean code standards, principles, and examples for maintainable Java domain-driven design.
applyTo: "**/*.java"
---
# TDD
- We want to work in short loops. Always write a failing test first and make it pass with the simplest possible code.
- Use the test skill for those tests

# Clean Java Development Skill

This skill defines how Java code should be written, reviewed, and refactored in this repository.
It enforces clean domain-driven design, strong encapsulation, and maintainable code structure.

---

# Clean Code Examples

Short before/after snippets for common refactorings.

##var
// Prefer var instead of explicit types to reduce noise
var customer = repository.findCustomer(id);

##final
Don't use final for local variables and method parameters. It's just noise.

##imports

//Bad are wildcard imports
import static eu.domain.model.opco.Opco.*;

//Good are specific imports
import static eu.domain.model.opco.Opco.of;

//Use static imports
//Bad
Assertions.assertThat(storedInvoice.getId()).isEqualTo(CLONED_INVOICE_ID);
//GOOD
assertThat(storedInvoice.getId()).isEqualTo(CLONED_INVOICE_ID);

## Comments
- Avoid comments in the code by making the code self-explanatory. If you find yourself writing a comment, ask if you can refactor the code to express the same idea more clearly.
// Bad
// creates a query to fetch contracts
Query query = em.createQuery("select c from Contract c");

// Good
createFetchContractsQuery();

## Naming

// Bad
loadContractsForUi();
calculateDiscountVersion2();

// Good
fetchActiveContracts();
calculateDiscount();

## Encapsulation

// Bad
contract.getLines().add(line);

// Good
contract.addLine(line);

## Null handling

Avoid the use of null where possible, and prefer Optional for return types in the model.

// Bad
if (customer != null) {
process(customer);
}

// Good
var customer = repository.findCustomer(id);

// use primitives like boolean or int instead of Boolean or Integer when possible to avoid nulls

## Invariants

// Bad
Contract contract = new Contract();
contract.setStartDate(start);
contract.setEndDate(end);

// Good
var contract = Contract.startingOn(start, endingOn(end));


## Behavior over interaction

// Bad
verify(repository).save(any());

// Good
assertThat(repository.findById(id)).hasValueSatisfying(contract ->
assertThat(contract.isFinalized()).isTrue());

## Framework leakage

// Bad
@Entity
public class Contract {

    @Autowired
    DiscountService service;
}

// Good
public class Contract {

    private final DiscountPolicy discountPolicy;
}

## Helper extraction

// Bad
if (config.isLocationBased()) {
return resolve(context)
.orElseGet(() -> customerRepository
.findBranchContactInfo(...)
.orElse(fallback));
}
return customerRepository.findBranchContactInfo(...).orElse(fallback);

// Good
if (config.isLocationBased()) {
return resolve(context)
.orElseGet(() -> getBranchContactInfo(context));
}
return getBranchContactInfo(context);

---

# Clean Code Principles

Core rules for intention-revealing, maintainable Java code.

## Principles

- Clarity over cleverness
- Domain language over technical language
- Encapsulation over exposure
- Behavior over data
- Explicit over implicit
- Small methods with clear intent
- No speculative design
- Low-complexity code

## Naming and comments
- use var where possible
- Names describe what, never how
- Never name methods after callers or UI concerns
- If something is hard to name, the design is wrong
- Comments should explain why, not what

## Design

- State is private
- Collections are not exposed mutably
- Behavior lives in the owning object
- Objects should be valid after construction
- Model absence explicitly or fail fast
- Remove null guards when they hide a guaranteed invariant

## Architecture

- UI → Application → Domain
- Domain depends on nothing
- No framework annotations in the domain
- No persistence concerns in entities
- Reject anemic domain models

## Refactoring signals

- Extract helper methods when logic repeats
- Extract when a method grows hard to scan
- Prefer one intent per method
- Use orElseGet for fallback chains when it keeps the flow readable

## Review stance

- Call out design smells explicitly
- Propose cleaner alternatives
- Reject “it works” as justification
- Reject framework-driven design
- Reject DTOs leaking into the domain
- Reject overengineering

---

# Coding Conventions

Repo-wide style rules.

## Formatting

- Use 4 spaces, no tabs
- Keep lines within 120 characters
- Always use var for local variables, unless the inferred type is genuinely unclear from context


## Java conventions

- Prefer constructor injection over field injection
- Use final for fields where possible (not for parameters)
- Never return or accept null: model absence with Optional for return values; for parameters, avoid the need for absence (overloads, required values) rather than accepting Optional
- Use typed value objects instead of raw String parameters when possible
- Prefer Optional.map() / Optional.or() over presence checks
- Extract complex lambdas into named methods
- Add Javadoc for public methods when it adds value

## Method structure

- A variable assigned inside try/catch but declared outside is a smell
- Remove else after return
- Use a ternary when both branches are simple
- Split distinct branches into private methods with intention-revealing names

## Encapsulation

- Prefer predicates on the owning object over exposing internals
- When a check recurs at call sites, add a named method on the owner

## Value objects

- Prefer Opco, CustomerAccountId, and similar types over raw strings
- Use existing typed accessors instead of extracting raw values first
- New code should use value objects directly instead of .value()

## Logging and errors

- Use SLF4J with Lombok @Slf4j
- Log with the right severity level
- Use specific exception types with meaningful messages
- Avoid catching generic Exception

## Git hygiene
- Do not commit local-only config files such as testcontainers.properties
