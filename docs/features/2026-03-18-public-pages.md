# Public Pages

Unauthenticated users can now access the primary search interface for RSDs,
categories, and collections. This previously required authentication.

Visitors can browse published and archived content, search and filter by
keyword / category / job code, view detail pages, and export data as CSV or
XLSX — including collection exports, library/search exports, and single-skill
downloads from a skill detail page — all without logging in. Drafts and deleted
items are never shown. Creating, editing, publishing, or deleting content still requires
authentication, as do My Workspace, Sync, and admin features.

## Navigation

The top navbar shows **Skills**, **Collections**, and **Categories** to
everyone. Visitors see a **Login** link; authenticated users see
My Workspace, Sync, and Logout instead.

Links route public users to read-only views:

- Category names on skill detail pages link to the category page
- Skill names on a category page link to the public skill detail (not the
  manage/edit page)
- Collection names link to the public collection detail

## Pages available without login

| URL                   | Page                                 |
|-----------------------|--------------------------------------|
| `/`                   | Redirects to `/skills`               |
| `/skills`             | Skills library (published only)      |
| `/skills/search`      | Search results                       |
| `/skills/{uuid}`      | Skill detail                         |
| `/collections`        | Collections library                  |
| `/collections/{uuid}` | Collection detail                    |
| `/categories`         | Category library                     |
| `/categories/{id}`    | Category detail with its skills list |

## Configuration

Two flags in `application.properties` (or as env vars) control public access:

| Flag                       | Default | Effect                             |
|----------------------------|---------|------------------------------------|
| `app.allowPublicLists`     | `true`  | List and detail pages are public   |
| `app.allowPublicSearching` | `true`  | Search, filter, and type-ahead are public |

When either flag is `false`, the corresponding requests require
authentication (unauthenticated requests get a 401).

`app.publicKeywordLimit` (default 1000) caps how many skills are scanned
when building keyword suggestions for visitors. Larger values give more
complete type-ahead at the cost of slower responses.

## API endpoints (reference)

The public UI is backed by these unauthenticated API routes.

**Skills** — `GET /api/{v2,v3}/skills/{uuid}`,
`POST /api/{v2,v3}/search/skills`,
`POST /api/{v2,v3}/skills/filter`

**Collections** — `GET /api/{v2,v3}/collections/{uuid}`,
`POST /api/{v2,v3}/search/collections`,
`POST /api/{v2,v3}/collections/{uuid}/skills`,
`GET /api/{v2,v3}/collections/{uuid}/csv`,
`GET /api/{v2,v3}/collections/{uuid}/xlsx`

**Categories** — `GET /api/v3/categories`,
`GET /api/v3/categories/{id}`,
`GET /api/v3/categories/{id}/skills`,
`POST /api/v3/categories/{id}/skills`

**Search metadata** — `GET /api/{v2,v3}/search/keywords`,
`GET /api/{v2,v3}/search/jobcodes`

**Export (custom skill lists, v3)** — `POST /api/v3/export/skills/csv`,
`POST /api/v3/export/skills/xlsx` (body: `ApiSearch`, e.g. UUID list for one
skill). Still gated by `app.allowPublicSearching`; anonymous callers only see
published/archived skills in the export.

**Task results** — `GET /api/{v2,v3}/results/text/{uuid}`,
`GET /api/{v2,v3}/results/media/{uuid}`
