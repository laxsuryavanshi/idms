# Architecture Decision Record (ADR) - Index

This directory contains Architecture Decision Records (ADRs) for the IDMS project. ADRs document significant architectural decisions made during the project's development.

## What is an ADR?

An Architecture Decision Record (ADR) captures an important architectural decision made along with its context and consequences. ADRs help teams:

- **Document decisions**: Maintain a history of why certain decisions were made
- **Share knowledge**: Communicate architectural decisions to team members
- **Track evolution**: Understand how the architecture has evolved over time
- **Avoid repetition**: Prevent revisiting already-decided issues
- **Onboard developers**: Help new team members understand architectural choices

## ADR Format

We use a standardized format for all ADRs. See [ADR_TEMPLATE.md](ADR_TEMPLATE.md) for the template.

Each ADR includes:

1. **Context**: The situation and problem
2. **Decision**: What was decided
3. **Consequences**: Positive and negative outcomes
4. **Alternatives**: Other options considered

## ADR Lifecycle

- **Proposed**: ADR is under discussion
- **Accepted**: Decision has been approved and implemented
- **Deprecated**: Decision is no longer relevant
- **Superseded**: Replaced by another ADR

## List of ADRs

| ADR #                                     | Title                                                   | Status   | Date       |
| ----------------------------------------- | ------------------------------------------------------- | -------- | ---------- |
| [001](./ADR-001-adopt-spring-modulith.md) | Adopt Spring Modulith for Modular Monolith Architecture | Proposed | 2026-01-11 |

## Creating a New ADR

1. Copy the [ADR_TEMPLATE.md](ADR_TEMPLATE.md) file
2. Name it `ADR-XXX-short-title.md` (e.g., `ADR-004-use-redis-for-caching.md`)
3. Fill in all sections
4. Submit for review via pull request
5. Update this index after acceptance

## Guidelines for Writing ADRs

### When to Create an ADR

Create an ADR for decisions that:

- Are difficult to reverse (e.g., database choice, framework selection)
- Have significant impact on the system
- Involve trade-offs between competing concerns
- Require consensus among team members
- Need to be communicated to stakeholders

### When NOT to Create an ADR

Don't create an ADR for:

- Trivial decisions (e.g., variable naming)
- Implementation details that don't affect architecture
- Decisions that are easily reversible
- Standard industry practices with no alternatives

### Writing Tips

- **Be concise**: Keep it brief but comprehensive
- **Be factual**: State facts, not opinions (unless explaining reasoning)
- **Be specific**: Avoid vague statements
- **Include context**: Explain why the decision matters
- **List alternatives**: Show that you considered other options
- **Document trade-offs**: Be honest about negatives

## References

- [ADR GitHub Organization](https://adr.github.io/)
- [Documenting Architecture Decisions by Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
