# Enum Icon Directory

This directory contains icons for enum fields with the `icon` property.

## Directory Structure

Icons for enum fields are organized by the enum's Partially Qualified Object Name (PQON):

```
enums/
└── PQON/
    ├── INSTANCE_NAME_1.png
    ├── INSTANCE_NAME_2.png
    └── INSTANCE_NAME_3.png
```

## PQON (Partially Qualified Object Name)

For **Bonaparte enums** (BonaEnum), the PQON is automatically obtained using the `ret$PQON()` method, which provides the correct partially qualified name.

For **xenums** (XEnum), the PQON is obtained from the base enum using `getBaseEnum().ret$PQON()`.

For **standard Java enums**, the full class name is used with dots replaced by slashes.

**Example for Bonaparte enum**:
- Full class name: `com.arvatosystems.t9t.io.CommunicationTargetChannelType`
- PQON (via `ret$PQON()`): `t9t.io.CommunicationTargetChannelType`
- Icon path: `t9t/io/CommunicationTargetChannelType` (dots replaced with slashes)
- Result: `enums/t9t/io/CommunicationTargetChannelType/`

**Example for xenum**:
- Xenum: `CommunicationTargetChannelXType` (extends `CommunicationTargetChannelType`)
- PQON obtained from base enum: `t9t.io.CommunicationTargetChannelType`
- Same icon path as the base enum

## Usage

In your DTO definition (Bonaparte .bon file), add the `icon` property to an enum field:

```bonaparte
class ExampleDTO {
    required (CommunicationTargetChannelType) channelType properties icon;
    required (CommunicationTargetChannelXType) channelXType properties icon;  // xenum uses base enum icons

}
```

## Example

For the enum `CommunicationTargetChannelType` in package `com.arvatosystems.t9t.io`:

**PQON**: `t9t/io/CommunicationTargetChannelType`

**Icon paths**:
```
icon/enums/t9t/io/CommunicationTargetChannelType/
├── NULL.png
├── FILE.png
├── KAFKA.png
├── QUEUE.png
└── TOPIC.png
```

When the grid displays a field with `channelType = FILE`, it will look for:
`icon/enums/t9t/io/CommunicationTargetChannelType/FILE.png`

## File Naming

- Instance names are kept as-is (typically UPPERCASE)
- No lowercase conversion for enum instances
- File extension: `.png`

## Icon Requirements

- Format: PNG
- Recommended size: 16x16 or 24x24 pixels
- Name must exactly match the enum instance name (case-sensitive)
