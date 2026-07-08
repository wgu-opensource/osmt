# OSMT User Roles

OSMT supports three user roles:

| Role | Authority Name | Description |
|------|----------------|-------------|
| **Admin** | `ROLE_Osmt_Admin` | Full access to modify RSDs and Collections |
| **Curator** | `ROLE_Osmt_Curator` | Can create RSDs and Collections; archive/unarchive; limited to view-only for publish/delete/update |
| **Viewer** | `ROLE_Osmt_View` | Read-only; can view but not modify RSDs or Collections |

**Note:** `SCOPE_osmt.read` is a scope (for machine-to-machine API access), not a user role.

Role names are configurable via `osmt.security.role.*` in `application.properties`; Okta groups must match these values.
