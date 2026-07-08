# OSMT API TESTING

---

The OSMT Integration test folder will run API tests against the openapi.yaml file. \
it is important that you keep the openapi.yaml up to date.

## Getting Started

The automated tests uses a combination of bash scripts, newman, docker and Maven. Maven will \
call the tests during any build cycle. You can also call it manually using \
`mvn verify -pl test -Prun-api-tests`. Before getting started there are a few \
prerequisites listed below.

### prerequisites

- Docker
- Newman

Furthermore, you will need to create a `osmt-apitest.env` file under the test folder. There is \
currently an `osmt-apitest.env.example` file you can use as the template.

**Authentication options:**

- **OAuth2 with Okta**: Add your Okta credentials to `osmt-apitest.env` for OAuth2 authentication
- **Single-Auth mode**: Leave Okta credentials empty or as `xxxxxx`. Tests will automatically use the `single-auth` profile. See [Testing Without OAuth2](README.md#testing-without-oauth2) below for details.

### Testing Without OAuth2

**⚠️ SECURITY WARNING**: The `single-auth` profile is intended **ONLY** for local development and CI/CD testing. **DO NOT** use this profile in production environments.

OSMT API tests can run without OAuth2 credentials using the `single-auth` profile. This is useful for:

- CI/CD pipelines without OAuth secrets
- Local testing without OAuth provider setup
- Testing authorization with different roles
- Automated testing in environments without OAuth provider access

**How to use:**

1. Leave Okta credentials empty or as `xxxxxx` in `osmt-apitest.env`
   - The test scripts automatically detect missing OAuth/Okta credentials
   - When credentials are missing, tests automatically use the `single-auth` profile
2. Optionally set test role environment variables:
   ```bash
   export TEST_ROLE=ROLE_Osmt_Admin
   export TEST_USER_NAME=test-user
   export TEST_USER_EMAIL=test@example.com
   ```
   - These can also be set in `osmt-apitest.env` file
   - Test role can be changed per test run to test different authorization scenarios
3. Run tests normally:
   ```bash
   mvn verify -pl test -Prun-api-tests
   ```

The test scripts will automatically detect missing Okta credentials and use the `single-auth` profile.

**How it works:**

- When OAuth credentials are missing, the test scripts set `OSMT_SECURITY_PROFILE=single-auth`
- The Spring Boot application starts with the `single-auth` profile
- Test roles are injected via the `TEST_ROLE` environment variable
- API tests use the `X-Test-Role` header to specify roles for different requests
- Authorization rules are still enforced - tests verify role-based access control works correctly

**Available roles for testing:**

- `ROLE_Osmt_Admin` - Full administrative access (can create, update, publish, delete)
- `ROLE_Osmt_Curator` - Can create and update skills/collections (cannot publish or delete)
- `ROLE_Osmt_View` - Read-only access (can view but not modify)
- `SCOPE_osmt.read` - Basic read scope (for machine-to-machine access)

**Testing different roles:**

You can test different authorization scenarios by setting different roles:

```bash
# Test as Admin
export TEST_ROLE=ROLE_Osmt_Admin
mvn verify -pl test -Prun-api-tests

# Test as Curator
export TEST_ROLE=ROLE_Osmt_Curator
mvn verify -pl test -Prun-api-tests

# Test as View-only
export TEST_ROLE=ROLE_Osmt_View
mvn verify -pl test -Prun-api-tests
```

**Verifying single-auth is active:**

- Check the application logs for "Detected security profile: single-auth"
- Check that OAuth token generation is skipped in test output
- Verify that tests pass without OAuth credentials

### Caveats

In the `osmt-apitest.env` file make sure you escape any special characters or else it will not \
parse correctly when passing the environment variable to newman
