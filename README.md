# IB_tim12

## Dependency check ##
> analyzed with owasp

**com.google.api-client**: Updated to 2.2.0

**spring-boot-starter-data-jdbc**: CVE-2023-20863 - it is possible for a user to provide a specially crafted SpEL expression that may cause a denial-of-service (DoS) condition. In our case, this part of dependency isn't being used.

**spring-boot-starter-security**: CVE-2023-20883 - there is potential for a denial-of-service (DoS) attack if Spring MVC is used together with a reverse proxy cache. In our case, this part of dependency isn't being used.

Strong logging system is implemented in any case
