Assumptions
  - library must be thread safe
  - no external persistence is needed
  - teams are identified by there unique names
  - we do not need to remember starts times,  just the order
  - interface is as simple as possible

Reasoning
  - I will preferable use java.util.concurrent tools or synchronized keyword
  - Alternatively all request might be queued to avoid race-condition and we could
    have a dedicated thread for picking the tasks from the queue 
  
Trade-offs made
  - heavy synchronization to handle simultaneous interfering score updates
    (i.e. one new update starts when we are in the middle of other). The initial
    solution suggested by AI with the synchronized collection was prone to such a problems

Extra feature in the score board
  - list three teams wth the highest score

