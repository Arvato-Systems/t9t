# Icon Display in Grid28 - Usage Documentation

## Overview

The Grid28 component now supports displaying field values as icons instead of text. This is useful for status indicators, flags, or any other visual representations.

## How to Use

### Step 1: Define the Icon Property in Your DTO

In your Bonaparte (.bon) file, add the `icon="path"` property to the field you want to display as an icon:

```bonaparte
class MyDataDTO {
    required Ascii(20)      status      properties icon="status";
    optional Ascii(20)      type        properties icon="types";
}
```

The `icon` property value specifies the subdirectory under `/icon/` where the icons are located.

### Step 2: Create the Icon Directory Structure

Create a subdirectory in the webapp/icon directory matching the path specified in your DTO:

```
t9t-zkui-screens/src/main/webapp/icon/
├── status/
│   ├── active.png
│   ├── inactive.png
│   ├── pending.png
│   └── error.png
└── types/
    ├── document.png
    ├── image.png
    └── video.png
```

### Step 3: Name Your Icon Files

Icon files should be named using the **lowercase** value of the field:
- If field value is "ACTIVE", the icon file should be `active.png`
- If field value is "Pending", the icon file should be `pending.png`
- If field value is "Error", the icon file should be `error.png`

**Note:** The system automatically converts the field value to lowercase when looking up the icon.

## Example

Given this DTO definition:

```bonaparte
class OrderDTO {
    required Ascii(20)      orderStatus     properties icon="status";
}
```

And this directory structure:

```
icon/
└── status/
    ├── active.png
    ├── shipped.png
    └── delivered.png
```

When the grid displays an order with `orderStatus = "ACTIVE"`, it will automatically:
1. Convert "ACTIVE" to "active"
2. Look for the icon at `icon/status/active.png`
3. Display the icon instead of the text "ACTIVE"

## Requirements

- **Field Type**: Works with Ascii/String fields, Boolean fields, and Enum fields
  - **String fields**: Use `properties icon="path"` to specify icon directory
  - **Boolean fields**: Use `properties icon` (no path needed, uses predefined `types/boolean/` directory)
  - **Enum fields**: Use `properties icon` (no path needed, uses predefined `enums/PQON/` directory structure)
- **Icon Format**: PNG format is recommended
- **Icon Property**: Must be defined in the DTO metadata
- **File Naming**: 
  - For String fields: Icon files must be named in lowercase matching the field value
  - For Boolean fields: Use `TRUE.png` and `FALSE.png`
  - For Enum fields: Use the exact enum instance name (typically UPPERCASE, e.g., `FILE.png`, `KAFKA.png`)
- **Security**: Icon paths and values are validated to prevent directory traversal attacks
  - Icon paths can only contain: alphanumeric characters, hyphens (`-`), underscores (`_`), and forward slashes (`/`)
  - Icon values can only contain: lowercase alphanumeric characters, hyphens (`-`), and underscores (`_`)

## Boolean Field Support

Boolean fields can also be displayed as icons (checkbox-style):

```bonaparte
class MyDataDTO {
    required boolean isActive   properties icon;
    optional Boolean isEnabled  properties icon;
}
```

Icons are automatically loaded from:
- `icon/types/boolean/TRUE.png` - Filled checkbox for true values
- `icon/types/boolean/FALSE.png` - Empty checkbox for false values
- Null values (for boxed Boolean) display as blank (no icon)

## Enum Field Support

Enum fields (including xenums) can be displayed as icons:

```bonaparte
class MyDataDTO {
    required (CommunicationTargetChannelType) channelType properties icon;
    required (CommunicationTargetChannelXType) channelXType properties icon;  // xenum
}
```

Icons are automatically loaded from:
- `icon/enums/PQON/INSTANCE_NAME.png`
- For Bonaparte enums (BonaEnum), PQON is obtained via the `ret$PQON()` method
- For xenums (XEnum), PQON is obtained from the base enum via `getBaseEnum().ret$PQON()`
- Example: For enum `com.arvatosystems.t9t.io.CommunicationTargetChannelType`, PQON is `t9t.io.CommunicationTargetChannelType`, converted to path `t9t/io/CommunicationTargetChannelType`
- Instance names are kept as-is (typically UPPERCASE): `NULL.png`, `FILE.png`, `KAFKA.png`, etc.

## Notes

- If the icon file is not found, the browser will display a broken image placeholder (tooltip will show the original value)
- If the icon path or value contains invalid characters, the field will display as text instead
- The icon path is relative to the webapp root
- Multiple fields can use the same icon directory or different ones
- Icons are displayed in the grid cells where the field value would normally appear
- Tooltip is set to the original field value for accessibility
