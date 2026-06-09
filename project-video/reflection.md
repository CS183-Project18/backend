# Reflection

## Version 1 Weakness

Version 1 established the product concept, frontend interactions and basic
backend APIs, but the frontend prototype and backend were not fully integrated.
The prototype also lacked semantic retrieval, a mobile layout and governance
workflows.

## Version 2 Design Choice

Version 2 uses the Spring Boot backend as the public API boundary. Semantic and
image retrieval are delegated to an internal Python AI service, while MySQL
remains the persistent source of post and interaction data. This separation
keeps AI model code independent from the main application API.

## Evidence of Improvement

The Version 2 demo shows a backend-connected interface, richer posting and
interaction flows, administration features and a mobile layout. Repository
comparison also shows broader API and automated-test coverage. These counts
describe scope and verification coverage rather than search speed or accuracy.

## Trade-off

Version 2 is more capable but has greater setup cost. The AI models are large
and can take several minutes to download and load on first startup. Docker
Compose improves reproducibility, and SQL fallback allows text search to remain
available when the AI service cannot be reached.

## Future Improvement

Future work should measure semantic-search relevance with a labelled query set,
record startup and response-time metrics on consistent hardware, improve model
caching and add more end-to-end browser tests.
