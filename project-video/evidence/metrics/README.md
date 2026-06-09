# Version Comparison Method

The comparison uses the repository tags below:

- V1 commit: `771481e432f269a8cd17b7393a9c84faff8ada97`
- V2 commit: `334cdc3c7fb27d3be56e86039756e4d7dbf95b93`

## Counting Rules

- Tracked files were counted from each tag with `git ls-tree`.
- HTTP endpoints were counted from Spring controller mapping annotations.
- Automated test files include Java and Python test source files stored in each
  tagged version.
- Feature rows were checked against tagged source code and the recorded demos.

These figures describe implementation scope and verification coverage. They are
not benchmarks of response time, search accuracy or runtime performance.
