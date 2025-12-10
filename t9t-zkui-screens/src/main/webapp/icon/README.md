# Icon Directory Structure

This directory contains icons that can be displayed in grid28 components.

## Usage

To display a field as an icon in grid28:

1. In your DTO definition (Bonaparte .bon file), add a property `icon="path"` to the field
2. Place your icon images in the subdirectory: `icon/path/`
3. Name the icon files using the lowercase value of the field, e.g., `active.png`, `inactive.png`

## Example

If you have a field with property `icon="status"` and the field value is "ACTIVE", 
the system will look for the icon at: `icon/status/active.png`

## Directory Structure

```
icon/
  ├── README.md          (this file)
  └── status/            (example subdirectory for status icons)
      ├── active.png
      ├── inactive.png
      └── pending.png
```

## Supported Formats

- PNG format is recommended
- The field value is automatically converted to lowercase when looking up the icon
