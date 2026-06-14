# Architecture

> This document evolves alongside the codebase.

## Overview

The app follows a single-module clean architecture with three layers:

- **data** — remote API access, DTOs, mappers, and repository implementations.
- **domain** — framework-agnostic models and the business logic (rating and currency
  conversion), exposed through repository interfaces.
- **presentation** — Compose UI and ViewModels, following an MVI pattern.

Dependencies point inward: presentation depends on domain; data implements domain
interfaces. The domain layer has no Android dependencies.

## Presentation (MVI)

Each screen exposes a single immutable UI state via `StateFlow`, and receives user actions
as intents. The UI is a pure function of the current state. State is modelled as a sealed
type so that loading, content, empty, and error are mutually exclusive by construction.

## Data

API responses are modelled as nullable DTOs (a tolerant boundary), and mapped into clean
domain models that are safe for the UI to consume. Mapping validates and defaults missing
or malformed values so that a single bad record never breaks a whole screen.

## Dependency injection

Hilt provides dependencies throughout. ViewModels depend on repository interfaces rather
than concrete implementations, which keeps the layers decoupled and the logic testable.
