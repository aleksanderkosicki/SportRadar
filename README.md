Assumptions
  - library must be thread safe
  - no external persistence is needed
  - teams are identified by there unique names
  - start times are implicitly added when match is started
Reasoning
  - I will preferrable use java.util.concurrent tools
Trade-offs made
  - synchronization
