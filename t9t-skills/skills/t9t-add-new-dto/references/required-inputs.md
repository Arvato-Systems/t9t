# Required inputs for a new t9t DTO

The request should provide, or allow you to determine from existing code:

- the DTO / entity name
- the target functional module (for example `doc`, `io`, `voice`)
- the RTTI value (unique 4-digit suffix used for the surrogate-key based DTO family)
- the natural key field or fields
- the DTO fields
- whether UI wiring is required
- if a screen is required, the target menu section and preferred position
- the ticket / issue number needed for the SQL migration filename

If any of the assumptions below would require guessing, stop and ask:

- unique RTTI selection
- translated field labels
- menu section or screen ordering
- global-tenant access annotations
