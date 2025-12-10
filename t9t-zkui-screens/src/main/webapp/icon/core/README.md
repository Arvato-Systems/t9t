# Boolean Icon Directory

This directory contains checkbox icons for boolean fields with the `icon` property.

## Expected Files

### For Booleans
- `TRUE.png` - Icon for checked/true boolean values (filled checkbox)
- `FALSE.png` - Icon for unchecked/false boolean values (empty checkbox)

### For Ascii fields
- `EMPTY.png` - Icon for empty (but not null) strings, if specified by property `emptyIcon` (without argument)
- `BADEMPTY.png` - Icon for empty strings, if specified by property `emptyIcon="basename"` where basename is invalid
- `BADVALUE.png` - Icon for field values which do not fit into the allowed pattern

## Usage

In your DTO definition (Bonaparte .bon file), add the `icon` property to a boolean field:

```bonaparte
class ExampleDTO {
    required boolean active properties icon;
    optional Boolean enabled properties icon;
}
```

When `icon` property is present on a boolean field, the grid will display:
- For `true`: icon from `icon/core/TRUE.png`
- For `false`: icon from `icon/core/FALSE.png`
- For `null` (boxed Boolean): blank/no icon

## Icon Requirements

- Format: PNG
- Recommended size: 16x16 or 24x24 pixels
- Style: Checkbox appearance (filled vs. empty)
