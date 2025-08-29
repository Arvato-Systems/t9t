# TenantCategory Properties in bn files and Annotations in xtend resolvers

## Overview

The `tenantCategory` property is used in bon files for DTOs in the t9t framework that is used to mark DTOs (Data Transfer Objects) to specify how they should be handled in terms of tenant isolation and access control.
The property generates the Java annotation com.arvatosystems.t9t.annotations.TenantCategory.

## Annotation Definition

**Location**: `t9t-annotations-api/src/main/java/com/arvatosystems/t9t/annotations/TenantCategory.java`

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface TenantCategory {
    String value();
}
```

The annotation:
- Has **runtime retention**, meaning it's available at runtime for reflection
- Targets **TYPE** elements (classes, interfaces, enums)
- Takes a **String value** parameter that specifies the tenant category

## Purpose and Function

The `@TenantCategory` annotation is used to mark DTOs to define how they should be treated in multi-tenant scenarios. The annotation value corresponds to entries in the `TenantIsolationCategoryType` enum, which controls data access patterns and tenant isolation behavior.

## Possible Values

Based on the `TenantIsolationCategoryType` enum defined in `/t9t-base-api/src/main/bon/base/types.bon`, the possible values are:

### 1. **"G" - GLOBAL**
- **Description**: No tenantId in table (includes "Special" tables)
- **Behavior**: Data is globally accessible across all tenants
- **Default**: Used when no tenantId column is found in the corresponding JPA entity
- **Usage**: Currently not found in BON files (likely used as default)

### 2. **"I" - ISOLATED** 
- **Description**: Can only access own tenant data
- **Behavior**: Strict tenant isolation - users can only see data from their own tenant
- **Default**: Used when a tenantId column is found in the corresponding JPA entity
- **Example Usage**: 
  - `ApiKeyDTO` - API keys are tenant-specific
  - `SubscriberConfigDTO` - Event subscriber configurations are isolated per tenant
  - `PluginLogDTO` - Plugin logs are kept separate per tenant

### 3. **"D" - ISOLATED_WITH_DEFAULT**
- **Description**: Can access own data + readonly access to "@" (global) tenant
- **Behavior**: Users can access their tenant's data plus read data from the global tenant
- **Activation**: Activated by `@TenantFilterMeOrGlobal` or `@AllCanAccessGlobalTenant` JPA annotations
- **Example Usage**:
  - `AuthModuleCfgDTO` - If configuration missing for tenant, use global configuration
  - `TenantLogoDTO` - If tenant-specific logo missing, use global logo
  - Most configuration DTOs use this pattern for fallback to global defaults

### 4. **"A" - ISOLATED_WITH_ADMIN**
- **Description**: Global admin tenant ("@") has access to all data
- **Behavior**: Regular tenants see only their data, but global admin can see everything
- **Example Usage**:
  - `PasswordBlacklistDTO` - Global admin manages password blacklists across all tenants
  - `SessionDTO` - Global admin can monitor sessions across all tenants
  - `MessageDTO` - Global admin can access message logs from all tenants

### 5. **"E" - ISOLATED_ADMIN_DEFAULT**
- **Description**: Combines categories D and A
- **Behavior**: 
  - Global admin ("@") has read/write access to everything
  - Other tenants can read from global tenant ("@") but only write to their own data
- **Example Usage**:
  - `TenantDTO` - Tenant definitions themselves follow this pattern

### 6. **"S" - SPECIAL**
- **Description**: Anything else / custom behavior
- **Behavior**: Special handling, implementation-specific
- **Usage**: Currently not found in BON files (reserved for special cases)

## Usage in Code

The annotation is primarily used in BON (Business Object Notation) files, which are then processed to generate Java DTOs. The annotation is applied as a property:

```bon
class SomeDTO {
    properties tenantCategory="D";
    // class definition...
}
```

## Runtime Processing

The annotation value is accessed at runtime via reflection using:
```java
String tenantCategory = dtoClass.getProperty("tenantCategory");
TenantIsolationCategoryType category = TenantIsolationCategoryType.factory(tenantCategory);
```

This is used in:
- **UI Components**: `TenantDataField` and `TenantField` use it to determine what tenant options to show in dropdowns
- **JPA Layer**: Controls data filtering and access patterns
- **Request Processing**: Influences how CRUD operations are scoped to tenants

## Statistics from Codebase

Based on the analysis of BON files in the repository:
- **"D" (ISOLATED_WITH_DEFAULT)**: Most common - used for configuration objects and defaults
- **"A" (ISOLATED_WITH_ADMIN)**: Used for administrative data that global admin needs to access
- **"I" (ISOLATED)**: Used for strictly tenant-isolated data like API keys and tenant-specific configurations  
- **"E" (ISOLATED_ADMIN_DEFAULT)**: Used sparingly, mainly for tenant definitions themselves
- **"G" (GLOBAL)** and **"S" (SPECIAL)**: Not found in current BON files but available for special cases

## Related Annotations

The TenantCategory annotation works in conjunction with JPA-specific annotations:
- `@AllCanAccessGlobalTenant` - Corresponds to category "D"
- `@GlobalTenantCanAccessAll` - Related to category "A" behavior
- These annotations are used in JPA entity resolvers to generate appropriate database queries

## Conclusion

The `@TenantCategory` annotation is a crucial part of t9t's multi-tenant architecture, providing a declarative way to specify data access patterns and tenant isolation behavior for DTOs. It enables flexible tenant isolation strategies ranging from complete isolation to various forms of global admin access and default fallback patterns.
