# Dependency Upgrade Notes

This file tracks dependencies that were not upgraded during the systematic upgrade process and the reasons why.

## Date: 2025-12-02

### Skipped Upgrades

#### Spring Boot 3.1.2 → 3.3.0
**Status:** Skipped - Test failures

**Attempted:** 2025-12-02

**Issue:**
- Compilation successful
- Simple unit tests pass (e.g., JobCodeBreakoutTest)
- Integration tests fail with Spring context initialization errors
- Error: `ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context`
- Tests using `@SpringBootTest` and Testcontainers fail to initialize
- Root cause appears to be Spring context loading failure in test environment

**Affected Tests:**
- All tests extending `SpringTest` or using `BaseDockerizedTest`
- Tests that require Testcontainers (MySQL, Redis, Elasticsearch)

**Next Steps:**
- Investigate Spring Boot 3.3.0 compatibility with Testcontainers 1.21.3
- May need to upgrade Testcontainers version
- Review Spring Boot 3.3.0 migration guide for breaking changes
- Consider testing with Spring Boot 3.2.x as intermediate step
- Revisit after other dependency upgrades are complete

**Additional Notes:**
- Deprecation warnings observed: `exceptionHandling()` deprecated in Spring Security
- These are warnings, not blockers, but should be addressed

---

## Completed Upgrades

### Java 17 → Java 21 (LTS)
**Status:** ✅ Completed
**Date:** 2025-12-02
**Notes:** All tests passing, build successful

### Kotlin 1.7.21 → 2.2.21 (LTS)
**Status:** ✅ Completed
**Date:** 2025-12-02
**Notes:** All tests passing (155 tests, 0 failures), required code fixes for compatibility

### Log4j 2.17.1 → 2.23.1
**Status:** ✅ Completed
**Date:** 2025-12-02
**Notes:** Security updates, all tests passing

### MySQL Connector Cleanup
**Status:** ✅ Completed
**Date:** 2025-12-02
**Notes:** Removed deprecated mysql-connector-java, using mysql-connector-j only, all tests passing

### Jackson 2.14.1 → 2.17.1
**Status:** ✅ Completed
**Date:** 2025-12-02
**Notes:** All tests passing, JSON serialization verified

### Angular 16 → 18
**Status:** ✅ Completed
**Date:** 2025-12-02
**Notes:** 
- Updated all Angular packages, CLI, and related tools
- Updated RxJS to 7.8.1, TypeScript to 5.4.5, zone.js to 0.14.10
- Fixed deprecated async → waitForAsync in tests
- Fixed TypeScript 5 strictness issues
- Build successful, 689 tests passing (3 pre-existing failures unrelated to upgrade)

---

## Pending Upgrades

The following upgrades are planned but not yet attempted:
- Elasticsearch client 7.17.8 → 8.x (Java API client)
- Docker images (Redis, MySQL versions)
- Node.js 18.18.2 → 20 LTS

