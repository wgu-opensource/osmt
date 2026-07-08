# Phase 1: Fix Count Queries and Null-Safe findById

## Scope

Fix `SyncQueryHelpers.kt`: align count queries with find query predicates, and
make raw JDBC entity loading null-safe. Add tests for count accuracy.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file.
- Place more abstract things, entry points, and tests first.
- Place helper utility functions at the bottom of files.
- Keep related functionality grouped together.
- Any temporary code should have a TODO comment.

## Implementation Details

### 1. Fix `countSkillsUpdatedSinceRaw`

Replace the 1-second window with the exact composite cursor predicate:

```kotlin
private fun countSkillsUpdatedSinceRaw(
    watermarkDate: LocalDateTime,
    watermarkId: Long,
): Int {
    val sql =
        """
        SELECT COUNT(*) FROM RichSkillDescriptor
        WHERE ((updateDate > ?) OR (updateDate = ? AND id > ?))
        AND (publishDate IS NOT NULL)
        """.trimIndent()
    val ts = Timestamp.valueOf(watermarkDate)
    return transaction {
        val conn = TransactionManager.current()
            .connection.connection as java.sql.Connection
        conn.prepareStatement(sql).use { ps ->
            ps.setTimestamp(1, ts)
            ps.setTimestamp(2, ts)
            ps.setLong(3, watermarkId)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }
    }
}
```

### 2. Fix `countCollectionsUpdatedSinceRaw`

Same change:

```kotlin
private fun countCollectionsUpdatedSinceRaw(
    watermarkDate: LocalDateTime,
    watermarkId: Long,
): Int {
    val sql =
        """
        SELECT COUNT(*) FROM Collection
        WHERE ((updateDate > ?) OR (updateDate = ? AND id > ?))
        AND (status IN ('Published', 'Archived'))
        """.trimIndent()
    val ts = Timestamp.valueOf(watermarkDate)
    return transaction {
        val conn = TransactionManager.current()
            .connection.connection as java.sql.Connection
        conn.prepareStatement(sql).use { ps ->
            ps.setTimestamp(1, ts)
            ps.setTimestamp(2, ts)
            ps.setLong(3, watermarkId)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }
    }
}
```

### 3. Null-safe `findById` in raw queries

In `findSkillsUpdatedSinceRaw`, replace:

```kotlin
return ids.map {
    RichSkillDescriptorDao.findById(EntityID(it, RichSkillDescriptorTable))!!
}
```

With:

```kotlin
return ids.mapNotNull { id ->
    RichSkillDescriptorDao.findById(
        EntityID(id, RichSkillDescriptorTable),
    ).also { dao ->
        if (dao == null) {
            log.warn("Skill id={} found by cursor query but missing on load", id)
        }
    }
}
```

Same change in `findCollectionsUpdatedSinceRaw` for `CollectionDao`.

Note: `SyncQueryHelpers.kt` is a top-level file with no class. Add a
top-level logger:

```kotlin
private val log = LoggerFactory.getLogger("SyncQueryHelpers")
```

### 4. Tests

In `SyncQueryHelpersTest.kt`, add:

```kotlin
@Test
fun `countSkillsUpdatedSince matches find count after partial sync`() {
    val skills = (1..5).map { createPublishedSkill() }
    val batch1 = findSkillsUpdatedSince(null, null, 3)
    assertThat(batch1).hasSize(3)

    val last = batch1.maxWithOrNull(
        compareBy<RichSkillDescriptorDao> { it.updateDate }
            .thenBy { it.id.value },
    )!!
    val remaining = findSkillsUpdatedSince(
        last.updateDate, last.id.value, 100,
    )
    val count = countSkillsUpdatedSince(
        last.updateDate, last.id.value,
    )
    assertThat(count)
        .describedAs("count should equal remaining find size")
        .isEqualTo(remaining.size)
}
```

## Validate

```bash
sdk env install && cd api && mvn test -pl . -Dtest="SyncQueryHelpersTest,SyncServiceTest"
```

Fix any warnings or failures before proceeding.
