# Dropdown with Additional Input Data - Feature Documentation

## Overview

This feature allows dropdowns to be filtered based on the value of another field in the same DTO. This is useful when you want to show only a subset of dropdown options that are relevant to another field's value.

## How It Works

The feature works by:
1. Specifying a filter field name in the BON file using the `dropdownFilterField` property
2. The UI automatically detects this property and makes the dropdown filterable
3. Applications can programmatically set filter values to control what appears in the dropdown
4. The filter is applied server-side via the `LeanSearchRequest` with a `searchFilter` parameter

## Usage

### 1. Basic Usage in BON Files

In your BON file, you can configure dropdown filtering in two ways:

#### Option A: Filter on the Dropdown Field (dropdownFilterField)

Add the `dropdownFilterField` property to the dropdown field to specify which source field should be used for filtering:

```bon
class MyDTO {
    required Unicode(50)           category;          // The filter field
    required (ItemRef...,ItemDTO)  item  properties dropdown="itemId", dropdownFilterField="category"; // Filtered by category
}
```

In this example:
- `category` is the field whose value will be used for filtering
- `item` is the dropdown field that will be filtered
- When `category` is set to a value (e.g., "Electronics"), the `item` dropdown will only show items where the `category` field equals "Electronics"

For advanced cases requiring two independent filters, you can also use `dropdownFilterField2`:

```bon
class MyDTO {
    required Unicode(50)           category;          // Primary filter field
    required Unicode(50)           subcategory;       // Secondary filter field
    required (ItemRef...,ItemDTO)  item  properties dropdown="itemId", 
                                                      dropdownFilterField="category",
                                                      dropdownFilterField2="subcategory";
}
```

#### Option B: Filter on the Source Field (filters / filtersIds)

Alternatively, you can specify filtering behavior on the source field itself, which is more convenient when one field filters multiple dropdowns:

**Using `filters` property:**
```bon
class MyDTO {
    required Unicode(50)           category  properties filters="item,relatedItem"; // Filters these fields
    required (ItemRef...,ItemDTO)  item      properties dropdown="itemId";
    required (ItemRef...,ItemDTO)  relatedItem properties dropdown="itemId";
}
```

**Using `filtersIds` property (filters by dropdown ID):**
```bon
class MyDTO {
    required Unicode(50)           category  properties filtersIds="itemId"; // Filters all dropdowns with this ID
    required (ItemRef...,ItemDTO)  item      properties dropdown="itemId";
    required (ItemRef...,ItemDTO)  relatedItem properties dropdown="itemId";
}
```

**Specifying custom filter field names:**
You can optionally specify which field name in the dropdown's DTO to filter on:
```bon
class MyDTO {
    required Unicode(50)           category  properties filters="item:categoryName,relatedItem:category";
    required (ItemRef...,ItemDTO)  item      properties dropdown="itemId";
    required (ItemRef...,ItemDTO)  relatedItem properties dropdown="itemId";
}
```

In this example:
- The `category` field filters both `item` (using field name `categoryName`) and `relatedItem` (using field name `category`)
- Only one event listener is created on the `category` field, improving efficiency
- The format is `fieldName` or `fieldName:filterFieldName` separated by commas

### 2. Programmatic Usage

#### Direct Filter Application

You can programmatically set filters on any dropdown component:

```java
Dropdown28Db<ItemRef> dropdown = ...; // Your dropdown component
SearchFilter filter = MessagingUtil.createEqualitySearchFilter("category", "Electronics");
dropdown.setAdditionalFilter(filter);  // Primary filter
dropdown.setAdditionalFilter2(filter2); // Secondary filter (if needed)
```

The dropdown supports:
- `setAdditionalFilter(SearchFilter)` - Sets the primary runtime filter
- `setAdditionalFilter2(SearchFilter)` - Sets a secondary runtime filter
- Both filters are combined with AND logic

Supported filter value types for equality filters:
- `String` - Creates a `UnicodeFilter` with `equalsValue`
- `Long` - Creates a `LongFilter` with `equalsValue`
- `Integer` - Creates an `IntFilter` with `equalsValue`
- `Boolean` - Creates a `BooleanFilter` with `booleanValue`
- `null` - Clears the filter

#### Automatic Setup in Form28

When using the standard `Form28` component, filtering is set up automatically based on BON properties:

- **Option A**: Fields with `dropdownFilterField` or `dropdownFilterField2` properties are automatically wired to their source fields
- **Option B**: Fields with `filters` or `filtersIds` properties automatically set up event listeners to filter target dropdowns

The automatic setup happens in the `Form28.onCreate()` method and requires no additional code.

### 3. Advanced Usage - Custom Filters

For more complex filtering scenarios, you can use the lower-level `Dropdown28Db.setAdditionalFilter()` method:

```java
Dropdown28Db<ItemRef> dropdown = ...; // Your dropdown component

// Create a custom filter (e.g., a range filter)
LongFilter filter = new LongFilter("price");
filter.setLowerBound(100L);
filter.setUpperBound(500L);

dropdown.setAdditionalFilter(filter);
```

Or combine multiple filters:

```java
UnicodeFilter categoryFilter = new UnicodeFilter("category");
categoryFilter.setEqualsValue("Electronics");

LongFilter priceFilter = new LongFilter("price");
priceFilter.setUpperBound(1000L);

AndFilter combinedFilter = new AndFilter(categoryFilter, priceFilter);
dropdown.setAdditionalFilter(combinedFilter);
```

## Implementation Details

### Backend Requirements

For filtering to work, the backend's `LeanSearchRequest` handler must:
1. Accept and process the `searchFilter` parameter
2. Apply the filter to the database query
3. Return only matching `Description` objects

This is typically handled automatically by the framework's search request handlers.

### Filter Combination

The `Dropdown28Db` component supports:
- **Fixed Filters**: Set via `setFixedFilter(String filterName)` - typically configured filters
- **Additional Filters**: Set via `setAdditionalFilter(SearchFilter)` - primary runtime filter (e.g., from `dropdownFilterField`)
- **Additional Filters 2**: Set via `setAdditionalFilter2(SearchFilter)` - secondary runtime filter (e.g., from `dropdownFilterField2`)
- **Description Filters**: Set via `setDescriptionFilter(String filterName)` - post-processing filters on results

When multiple filters are present, they are combined using an `AndFilter`:
```
searchFilter = AndFilter(fixedFilter, additionalFilter, additionalFilter2)
```

### Supported Dropdown Types

The filter field feature is supported in:
- `DropdownDataField<T>` - Generic dropdown with Ref types
- `DropdownDbAsStringDataField` - Dropdown returning String values
- `DropdownDbAsLongDataField<T>` - Dropdown returning Long values
- `DropdownDbAsIntegerDataField` - Dropdown returning Integer values

All dropdown data fields extend `AbstractDropdownDataField` which provides:
- `getFilterFieldName()` - Returns the primary filter field property
- `getFilterFieldName2()` - Returns the secondary filter field property

### Automatic Wiring in Form28

The `Form28` component automatically sets up dropdown filters in its `onCreate()` method:

1. **Dropdown-based properties**: Scans all registered fields for `dropdownFilterField` and `dropdownFilterField2` properties
2. **Source-based properties**: Scans all fields for `filters` and `filtersIds` properties
3. **Event listeners**: Creates efficient event listeners to update filters when source field values change
4. **Multiple dropdowns**: When using `filters` or `filtersIds`, a single event listener can update multiple dropdowns

The automatic wiring handles:
- Finding source and target fields by name
- Parsing property values (supports `fieldName` or `fieldName:filterFieldName` format)
- Creating appropriate equality filters based on field values
- Setting up ZK event listeners for field changes

## Example Scenario

**Use Case**: An application manages products organized by category and subcategory. When editing a product, the user first selects a category and subcategory, then selects a specific product. The product dropdown should only show products matching both filters.

**Approach 1: Using dropdownFilterField on the dropdown**
```bon
class ProductSelection {
    required Unicode(50)                  categoryName;
    required Unicode(50)                  subcategoryName;
    required (ProductRef...,ProductDTO)   product  properties dropdown="productId", 
                                                             dropdownFilterField="categoryName",
                                                             dropdownFilterField2="subcategoryName";
}
```

**Approach 2: Using filters on the source fields**
```bon
class ProductSelection {
    required Unicode(50)                  categoryName    properties filters="product:categoryName";
    required Unicode(50)                  subcategoryName properties filters="product:subcategoryName";
    required (ProductRef...,ProductDTO)   product         properties dropdown="productId";
}
```

**Approach 3: Using filtersIds (when multiple dropdowns share the same ID)**
```bon
class ProductSelection {
    required Unicode(50)                  categoryName    properties filtersIds="productId:categoryName";
    required Unicode(50)                  subcategoryName properties filtersIds="productId:subcategoryName";
    required (ProductRef...,ProductDTO)   product         properties dropdown="productId";
    required (ProductRef...,ProductDTO)   alternateProduct properties dropdown="productId";
}
```

**Backend**:
The `LeanProductSearchRequest` handler automatically applies the `searchFilter` to filter products by `categoryName` and `subcategoryName`.

**UI**:
Setup is automatic when using the standard `Form28` component. No additional code needed.

**Result**:
- User selects "Electronics" in category → product dropdowns reload
- User selects "Laptops" in subcategory → product dropdowns reload again
- Product dropdowns now show only laptops in the electronics category
- Both primary and alternate product dropdowns are filtered if using Approach 3

## Benefits

1. **Better UX**: Users see only relevant options, reducing confusion
2. **Performance**: Smaller result sets improve UI responsiveness
3. **Data Integrity**: Helps prevent invalid selections
4. **Flexibility**: Works with any field type (string, long, integer, boolean)
5. **Minimal Code**: Most scenarios work with just BON property declaration
6. **Multiple Approaches**: Choose between dropdown-centric (`dropdownFilterField`) or source-centric (`filters`, `filtersIds`) configuration
7. **Efficiency**: Source-centric approach with `filters`/`filtersIds` creates fewer event listeners when one field filters multiple dropdowns
8. **Dual Filtering**: Support for two independent filters via `dropdownFilterField` and `dropdownFilterField2`

## Limitations

1. The automatic wiring only works within a single `Form28` - filter and dropdown fields must be in the same form
2. Only equality filters are created automatically (use custom filters for ranges, complex logic, etc.)
3. Filter value type must be String, Long, Integer, Boolean, or null
4. Backend must support the `searchFilter` parameter in its LeanSearchRequest handler
5. The `filters` property requires fields to be dropdown fields (not bandbox or other types)
6. The `filtersIds` property filters all dropdowns with matching IDs that don't have their own `dropdownFilterField` specified

## Migration Notes

This feature is backward compatible:
- Existing dropdowns without `dropdownFilterField` continue to work unchanged
- The feature is opt-in via the property or programmatic API
- No changes required to existing BON files or backend code unless you want to use filtering
