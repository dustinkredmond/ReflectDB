### Release Notes
---
###  Important Notes
- Each release will container minor bug fixes, or improvements. API may be marked deprecated in any release version.
- Enhancements and bugfixes will be provided in minor releases. E.g. from 2.0.1 -> 2.0.2
- API marked as deprecated will face removal in the next major release that follows its deprecation. E.g. from 2.9.9 -> 3.0.0

---

| Release (Newest to Oldest)                                     | Notes                                                                                                                                                        |
|----------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [2.0.5](https://www.gserve.org/reflectdb/reflectdb-2.0.5.jar)  | 1. Fixed various bugs with SQLite<br/>2. Begin focusing on MySQL/MariaDB (no new SQLite features added)
| [2.0.4](https://www.gserve.org/reflectdb/reflectdb-2.0.4.jar)  | 1. Ensure queries where cursor position is before first row return null and don't throw SQLException.                                                        |
| [2.0.3](https://www.gserve.org/reflectdb/reflectdb-2.0.3.jar)  | 1. Expand support for SQLite<br/>2. No longer wrap SQLException                                                                                              |
| [2.0.2](https://www.gserve.org/reflectdb/reflectdb-2.0.2.jar)  | 1. Fixed numerous bugs and added unit tests for each API method <br/>2. Queries now return null if ResultSet empty<br/>3. Fixes for SQLite (testing database)|
| [2.0.1](https://www.gserve.org/reflectdb/reflectdb-2.0.1.jar)  | 1. Initial release<br/>2. Only used for development purposes/testing                                                                                        |
