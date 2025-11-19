# Dropdown Filter Field - Implementation Summary

## Issue
**Title**: Dropdown with additional input data  
**Issue ID**: (from GitHub issue)

## Problem Statement
Sometimes we want a dropdown which only shows a subset of the fields. Currently, a dropdown field is identified by the `property dropdown="dropdownId"` in the DTO definition in a bon file. The requirement is to add the possibility to specify an additional field (of the same DTO), which, when the dropdown is used in the UI, filters the list of provided fields.

Technically, the UI has to provide the field searchFilter in the LeanSearchRequest. Assuming the additional parameter is a string, the filter would be an instance of UnicodeFilter, with fieldName = (the name of the additional field) and equalsValue its current value.

## Solution Overview
Created a concept and implementation for dropdown filtering with the majority of changes in the t9t-zkui-* maven modules as requested.

## Implementation Details

### 1. New Property Constant
**File**: `t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/util/Constants.java`
- Added `DROPDOWN_FILTER_FIELD` constant to `UiFieldProperties`
- Value: `"dropdownFilterField"`

### 2. Core Filter Support in Dropdown28Db
**File**: `t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/components/dropdown28/db/Dropdown28Db.java`

Changes:
- Added `additionalFilter` field to hold runtime filters
- Added `setAdditionalFilter(SearchFilter)` method to set runtime filters
- Modified `reloadDropDownData()` to combine fixed and additional filters using `AndFilter`
- Added import for `de.jpaw.bonaparte.pojos.api.AndFilter`

Filter combination logic:
```java
if (fixedFilter != null && additionalFilter != null) {
    combinedFilter = new AndFilter(fixedFilter.get(), additionalFilter);
} else if (fixedFilter != null) {
    combinedFilter = fixedFilter.get();
} else if (additionalFilter != null) {
    combinedFilter = additionalFilter;
}
```

### 3. Dropdown Data Field Classes
Enhanced all dropdown data field classes with filter support:

**Files Modified**:
- `DropdownDataField.java`
- `DropdownDbAsStringDataField.java`
- `DropdownDbAsLongDataField.java`
- `DropdownDbAsIntegerDataField.java`

**Changes in Each**:
- Added `filterFieldName` field to store the filter field property
- Added `getFilterFieldName()` method to retrieve the filter field name
- Added `setFilterValue(String, Object)` method to apply filters
- Constructor reads `DROPDOWN_FILTER_FIELD` property from metadata

The `setFilterValue()` method supports:
- `String` values → creates `UnicodeFilter`
- `Long` values → creates `LongFilter`
- `Integer` values → creates `IntFilter`
- `null` values → clears the filter

### 4. Helper Utility Class
**File**: `t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/components/datafields/DropdownFilterHelper.java`

A new utility class that provides:
- `setupAutoFilter()` - manually wire a source field to filter a dropdown
- `setupAutoFilterFromProperty()` - automatically wire based on the `dropdownFilterField` property
- Automatic event listener setup for field change events
- Type-safe filter application

### 5. Documentation
**File**: `docs/dropdown-filter-field.md`

Comprehensive documentation including:
- Overview and how it works
- BON file usage examples
- Programmatic usage examples
- Advanced filtering scenarios
- Backend requirements
- Example use cases
- Limitations and benefits
- Migration notes

## Usage Examples

### BON File Declaration
```bon
class ProductSelection {
    required Unicode(50)                  categoryName;
    required (ProductRef...,ProductDTO)   product  properties dropdown="productId", dropdownFilterField="categoryName";
}
```

### Automatic Wiring
```java
DropdownFilterHelper.setupAutoFilter(
    productDropdownField,  // dropdown to filter
    categoryField,         // source field
    "categoryName",        // field in DTO to filter on
    productSelectionDTO   // data object
);
```

### Direct Filter Application
```java
dropdownField.setFilterValue("categoryName", "Electronics");
```

## Design Decisions

### 1. Minimal Changes Approach
- Changes are confined to t9t-zkui-ce module as requested
- No changes to BON parser or core framework
- Backward compatible - existing dropdowns continue to work unchanged

### 2. Opt-In Feature
- Feature is only active when `dropdownFilterField` property is specified
- No performance impact on dropdowns that don't use filtering
- Applications can choose to use it programmatically or declaratively

### 3. Filter Combination Strategy
- Additional filters are combined with fixed filters using AND logic
- Description filters continue to work independently (post-processing)
- Allows both configuration-driven and runtime filtering

### 4. Type Support
- Started with common types: String, Long, Integer
- Easily extensible to support more types (Date, Boolean, etc.)
- Graceful handling of unsupported types with logging

### 5. Helper Class Pattern
- Provides convenience methods for common scenarios
- Separates filter wiring logic from core dropdown functionality
- Allows applications to choose their integration approach

## Testing Considerations

While a full build requires proprietary dependencies, the implementation has been designed to be testable:

1. **Unit Testing**: Individual methods like `setFilterValue()` can be tested in isolation
2. **Integration Testing**: The `DropdownFilterHelper` can be tested with mock fields and data objects
3. **End-to-End Testing**: Requires backend with LeanSearchRequest handlers that process searchFilter

## Backend Requirements

For the feature to work end-to-end:
1. Backend's `LeanSearchRequest` handlers must support the `searchFilter` parameter
2. Filters must be applied to database queries
3. Only matching `Description` objects should be returned

Note: Most existing LeanSearchRequest handlers in the framework already support this.

## Security Considerations

- No SQL injection risk - filters use parameterized queries through Bonaparte framework
- No XSS risk - all values are properly escaped by ZK framework
- No authentication/authorization bypass - backend handlers apply standard security

## Performance Considerations

- Filtered dropdowns may result in smaller result sets, improving performance
- Additional filter does not add significant overhead to query processing
- Caching strategy remains unchanged - filters bypass cache appropriately

## Future Enhancements

Potential improvements that could be added later:
1. Support for more filter types (ranges, LIKE, IN, etc.)
2. Multiple filter fields (AND/OR combinations)
3. ZUL component for declarative filter field wiring
4. Filter field validation and error handling
5. Support for grouped dropdowns with filtering

## Conclusion

The implementation provides a clean, minimal-change solution that:
- ✅ Solves the stated problem
- ✅ Confines changes to t9t-zkui-* modules as requested
- ✅ Is backward compatible
- ✅ Provides both declarative and programmatic interfaces
- ✅ Includes comprehensive documentation
- ✅ Follows existing code patterns and conventions
- ✅ Is extensible for future enhancements

The feature is ready for integration testing and refinement based on real-world usage.
