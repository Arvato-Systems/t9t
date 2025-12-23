# Testing Guide: Icon Display in Grid28

## Quick Test Scenario

This guide provides step-by-step instructions to test the icon display feature.

### Scenario 1: Basic Icon Display

#### Step 1: Create Test DTO
Add the following to a Bonaparte file (e.g., in your module's API):

```bonaparte
class TestIconDTO {
    required Ascii(20)      status      properties icon="status";
    required Ascii(50)      description;
}
```

#### Step 2: Create Icon Directory
```bash
mkdir -p t9t-zkui-screens/src/main/webapp/icon/status
```

#### Step 3: Add Test Icons
Create or copy PNG files to the status directory:
- `active.png` - Green checkmark or similar
- `inactive.png` - Gray circle or similar
- `pending.png` - Yellow clock or similar
- `error.png` - Red X or similar

```bash
# Example sizes: 16x16, 24x24, or 32x32 pixels work well
cp /path/to/icons/*.png t9t-zkui-screens/src/main/webapp/icon/status/
```

#### Step 4: Create Grid Configuration
Define a grid that uses the TestIconDTO:
```xml
<!-- In your grid configuration XML -->
<grid id="testIcon.grid" dto="TestIconDTO">
    <columns>
        <column field="status" width="80"/>
        <column field="description" width="200"/>
    </columns>
</grid>
```

#### Step 5: Test Data
Create test data with different status values:
- Record 1: status = "ACTIVE"
- Record 2: status = "INACTIVE"
- Record 3: status = "PENDING"
- Record 4: status = "ERROR"

#### Step 6: View in UI
1. Navigate to the screen that displays the grid
2. Verify that the status column shows icons instead of text
3. Hover over icons to see tooltips with the original value
4. Verify different icons display for different status values

### Scenario 2: Security Test - Invalid Path

#### Test Invalid Characters
Create a field with an invalid icon path to ensure security works:

```bonaparte
class TestSecurityDTO {
    required Ascii(20)      status      properties icon="../../../etc";
}
```

**Expected Result**: 
- System logs warning about invalid icon path
- Field displays as text (not icon)
- No security breach occurs

### Scenario 3: Missing Icon File

#### Test Missing Icon
1. Use a valid icon path: `properties icon="status"`
2. Reference a non-existent icon: field value = "UNKNOWN"
3. No file exists at: `icon/status/unknown.png`

**Expected Result**:
- Browser shows broken image placeholder
- Tooltip still shows "UNKNOWN"
- No application errors

### Scenario 4: Subdirectory Support

#### Test Nested Paths
```bonaparte
class TestNestedDTO {
    required Ascii(20)      flag        properties icon="flags/country";
}
```

Create directory structure:
```bash
mkdir -p t9t-zkui-screens/src/main/webapp/icon/flags/country
cp usa.png icon/flags/country/
cp gbr.png icon/flags/country/
```

**Expected Result**: Icons load from nested subdirectory

### Scenario 5: Boolean Field Icons

#### Test Boolean Checkbox Display
```bonaparte
class TestBooleanDTO {
    required boolean isActive   properties icon;
    optional Boolean isEnabled  properties icon;
    required Ascii(50) description;
}
```

#### Step 1: Create Boolean Icon Directory
```bash
mkdir -p t9t-zkui-screens/src/main/webapp/icon/types/boolean
```

#### Step 2: Add Checkbox Icons
Create or copy checkbox icons:
- `TRUE.png` - Filled/checked checkbox icon
- `FALSE.png` - Empty/unchecked checkbox icon

```bash
# Example sizes: 16x16 or 24x24 pixels work well
cp /path/to/icons/checked.png icon/types/boolean/TRUE.png
cp /path/to/icons/unchecked.png icon/types/boolean/FALSE.png
```

#### Step 3: Test Data
Create test data with different boolean values:
- Record 1: isActive = true, isEnabled = true
- Record 2: isActive = false, isEnabled = false
- Record 3: isActive = true, isEnabled = null

#### Step 4: View in UI
1. Navigate to the screen that displays the grid
2. Verify that boolean fields show checkbox icons instead of text
3. Verify TRUE shows filled checkbox, FALSE shows empty checkbox
4. Verify null values show blank (no icon)
5. Hover over icons to see tooltips with true/false

**Expected Results**:
- `true` values display filled checkbox from `TRUE.png`
- `false` values display empty checkbox from `FALSE.png`
- `null` values (boxed Boolean) display blank/no icon
- Tooltips show "true" or "false"

### Scenario 6: Enum Field Icons

#### Test Enum Icon Display
```bonaparte
class TestEnumDTO {
    required (CommunicationTargetChannelType) channelType properties icon;
    required Ascii(50) description;
}
```

#### Step 1: Create Enum Icon Directory
```bash
# Create directory structure based on PQON
# For com.arvatosystems.t9t.io.CommunicationTargetChannelType
# PQON is: t9t/io/CommunicationTargetChannelType
mkdir -p t9t-zkui-screens/src/main/webapp/icon/enums/t9t/io/CommunicationTargetChannelType
```

#### Step 2: Add Enum Instance Icons
Create or copy icons for each enum instance:
- `NULL.png` - Icon for NULL instance
- `FILE.png` - Icon for FILE instance
- `KAFKA.png` - Icon for KAFKA instance
- `QUEUE.png` - Icon for QUEUE instance
- `TOPIC.png` - Icon for TOPIC instance

```bash
# Example sizes: 16x16 or 24x24 pixels work well
cp /path/to/icons/*.png icon/enums/t9t/io/CommunicationTargetChannelType/
```

#### Step 3: Test Data
Create test data with different enum values:
- Record 1: channelType = FILE
- Record 2: channelType = KAFKA
- Record 3: channelType = QUEUE

#### Step 4: View in UI
1. Navigate to the screen that displays the grid
2. Verify that enum fields show icons instead of text
3. Verify different enum values show different icons
4. Hover over icons to see tooltips with the enum instance name

**Expected Results**:
- `FILE` value displays icon from `FILE.png`
- `KAFKA` value displays icon from `KAFKA.png`
- Enum instance names are kept as-is (UPPERCASE)
- Tooltips show enum instance name

### Verification Checklist

- [ ] Icons display instead of text for fields with icon property
- [ ] Different values show different icons
- [ ] Tooltips show original field values
- [ ] Invalid icon paths fall back to text display
- [ ] Missing icon files show broken image (expected)
- [ ] Security validation prevents directory traversal
- [ ] Nested icon directories work correctly
- [ ] Fields without icon property still display as text
- [ ] Grid sorting works with icon fields
- [ ] Grid filtering works with icon fields
- [ ] Boolean true displays filled checkbox icon
- [ ] Boolean false displays empty checkbox icon
- [ ] Null Boolean values display blank (no icon)
- [ ] Enum values display correct icons based on PQON path
- [ ] Enum instance names preserved (not lowercased)

### Troubleshooting

**Icons not displaying:**
1. Check icon file path matches pattern: `icon/{path}/{lowercase-value}.png`
2. Verify icon files exist in webapp directory
3. Check browser console for 404 errors
4. Verify field value is a String type

**Security warnings in logs:**
1. Check icon path doesn't contain `..` or `\`
2. Verify icon path uses only: `[a-zA-Z0-9_-/]`
3. Verify field values use only: `[a-z0-9_-]` (after lowercase)

**Broken images:**
1. Check file extension is `.png`
2. Verify file name is lowercase version of field value
3. Check file permissions (should be readable)

### Performance Notes

- Icons are loaded by the browser (client-side)
- Each unique icon is cached by the browser
- No server-side performance impact
- Grid rendering performance unchanged

### Browser Compatibility

Tested with:
- Modern browsers (Chrome, Firefox, Safari, Edge)
- ZK framework's standard image handling
- No special browser requirements

---

For more information, see:
- [Usage Documentation](icon-display-usage.md)
- [Implementation Summary](IMPLEMENTATION_SUMMARY.md)
- [Security Summary](SECURITY_SUMMARY.md)
