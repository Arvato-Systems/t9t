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

In your BON file, add the `dropdownFilterField` property to specify which field should be used for filtering:

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

### 2. Programmatic Usage

#### Setting a Filter Value Directly

You can programmatically set a filter on any dropdown field:

```java
DropdownDataField<ItemRef> itemField = ...; // Your dropdown field
String categoryValue = "Electronics";
itemField.setFilterValue("category", categoryValue);
```

Supported filter value types:
- `String` - Creates a `UnicodeFilter` with `equalsValue`
- `Long` - Creates a `LongFilter` with `equalsValue`
- `Integer` - Creates an `IntFilter` with `equalsValue`
- `null` - Clears the filter

#### Using the DropdownFilterHelper

For automatic filtering based on field changes, use the `DropdownFilterHelper`:

```java
import com.arvatosystems.t9t.zkui.components.datafields.DropdownFilterHelper;

// In your form setup code:
IDataField categoryField = ...; // The source field
IDataField itemField = ...;     // The dropdown field to filter
BonaPortable dataObject = ...;   // Your data object

// Set up automatic filtering
DropdownFilterHelper.setupAutoFilter(
    itemField,           // dropdown to filter
    categoryField,       // source field for filter value
    "category",          // field name in the dropdown's DTO to filter on
    dataObject          // data object containing field values
);
```

This will:
- Apply an initial filter based on the current category value
- Listen for changes to the category field
- Automatically update the item dropdown when category changes

#### Automatic Setup from Properties

If you've used the `dropdownFilterField` property in your BON file, you can use:

```java
DropdownFilterHelper.setupAutoFilterFromProperty(
    itemField,           // dropdown to filter (has dropdownFilterField property)
    categoryField,       // source field for filter value
    dataObject          // data object containing field values
);
```

This reads the filter field name from the dropdown's `dropdownFilterField` property.

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
- **Additional Filters**: Set via `setAdditionalFilter(SearchFilter)` - runtime filters like those from filter fields
- **Description Filters**: Set via `setDescriptionFilter(String filterName)` - post-processing filters on results

When both fixed and additional filters are present, they are combined using an `AndFilter`:
```
searchFilter = AndFilter(fixedFilter, additionalFilter)
```

### Supported Dropdown Types

The filter field feature is supported in:
- `DropdownDataField<T>` - Generic dropdown with Ref types
- `DropdownDbAsStringDataField` - Dropdown returning String values
- `DropdownDbAsLongDataField<T>` - Dropdown returning Long values
- `DropdownDbAsIntegerDataField` - Dropdown returning Integer values

## Example Scenario

**Use Case**: An application manages products organized by category. When editing a product, the user first selects a category, then selects a specific product. The product dropdown should only show products from the selected category.

**BON Definition**:
```bon
class ProductSelection {
    required Unicode(50)                  categoryName;
    required (ProductRef...,ProductDTO)   product  properties dropdown="productId", dropdownFilterField="categoryName";
}
```

**Backend**:
The `LeanProductSearchRequest` handler automatically applies the `searchFilter` to filter products by `categoryName`.

**UI**:
```java
// Setup is automatic if using the standard Field28 component
// Or manually:
DropdownFilterHelper.setupAutoFilterFromProperty(
    productDropdownField,
    categoryField,
    productSelectionDTO
);
```

**Result**:
- User selects "Electronics" in category
- Product dropdown reloads and shows only electronics products
- User changes category to "Furniture"  
- Product dropdown reloads and shows only furniture products

## Benefits

1. **Better UX**: Users see only relevant options, reducing confusion
2. **Performance**: Smaller result sets improve UI responsiveness
3. **Data Integrity**: Helps prevent invalid selections
4. **Flexibility**: Works with any field type (string, long, integer)
5. **Minimal Code**: Most scenarios work with just BON property declaration

## Limitations

1. Filter field must be in the same DTO as the dropdown field
2. Only equality filters are created automatically (use custom filters for ranges, etc.)
3. Filter value type must be String, Long, Integer, or null
4. Backend must support the `searchFilter` parameter in its LeanSearchRequest handler

## Migration Notes

This feature is backward compatible:
- Existing dropdowns without `dropdownFilterField` continue to work unchanged
- The feature is opt-in via the property or programmatic API
- No changes required to existing BON files or backend code unless you want to use filtering
