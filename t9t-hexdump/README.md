# t9t-hexdump

A command-line hexadecimal dump utility.

## Description

This module provides a simple hexdump utility that reads input from a file or stdin and displays it in hexadecimal format.

## Building

```bash
mvn clean package
```

## Usage

### Reading from a file:
```bash
java -jar target/t9t-hexdump-9.0-SNAPSHOT.jar <filename>
```

### Reading from stdin:
```bash
echo "Hello World" | java -jar target/t9t-hexdump-9.0-SNAPSHOT.jar
```

Or:
```bash
java -jar target/t9t-hexdump-9.0-SNAPSHOT.jar < input.txt
```

## Output Format

The output format consists of:
- 6-digit hexadecimal file offset
- 1 space
- 8 pairs of hexadecimal bytes (each byte followed by a space)
- 1 additional space
- 8 more pairs of hexadecimal bytes (each byte followed by a space)
- ASCII representation of the bytes (non-printable characters shown as '.')

Example output:
```
000000 48 65 6c 6c 6f 2c 20 57  6f 72 6c 64 21 20 0a    Hello, World! .
```

### Compression Features

To make output more readable for files with repetitive data, the hexdump tool automatically compresses the output:

1. **Lines with identical bytes**: When a line contains exactly 16 identical bytes, the output shows `(all xx)` instead of displaying all hex values:
   ```
   000000 (all ff)
   ```

2. **Consecutive identical lines**: When multiple consecutive lines are identical:
   - The first line is shown normally
   - The second identical line is replaced with `...`
   - Subsequent identical lines (3rd, 4th, etc.) are omitted completely
   - Normal output resumes when a different line is encountered

   Example with 5 identical lines:
   ```
   000000 (all 41 / 'A')
   ...
   000050 41 42 43 44 45 46 47 48  49 4a 4b 4c 4d 4e 4f 50 ABCDEFGHIJKLMNOP
   ```

This compression significantly reduces output size for files with large blocks of repeated data while preserving all essential information.

## Examples

### Example 1: Dump a text file
```bash
echo "Hello, World!" > test.txt
java -jar target/t9t-hexdump-9.0-SNAPSHOT.jar test.txt
```

Output:
```
000000 48 65 6c 6c 6f 2c 20 57  6f 72 6c 64 21 0a       Hello, World!.
```

### Example 2: Dump binary data
```bash
printf '\x00\x01\x02\x03\x04\x05\x06\x07\x08\x09\x0a\x0b\x0c\x0d\x0e\x0f' | \
  java -jar target/t9t-hexdump-9.0-SNAPSHOT.jar
```

Output:
```
000000 00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f ................
```

### Example 3: Dump from stdin
```bash
cat /etc/hostname | java -jar target/t9t-hexdump-9.0-SNAPSHOT.jar
```

### Example 4: Files with repetitive data (demonstrating compression)
```bash
# Create a file with repetitive blocks
printf '\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00' > zeros.bin
printf '\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00' >> zeros.bin
printf '\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00' >> zeros.bin
printf '\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00' >> zeros.bin
printf '\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF' >> zeros.bin

java -jar target/t9t-hexdump-9.0-SNAPSHOT.jar zeros.bin
```

Output (compressed):
```
000000 (all 00)
...
000040 (all ff)
```

Without compression, this would display 5 separate lines. The improved output shows just 3 lines while preserving all information.

## Error Handling

If the specified file does not exist or cannot be read, the utility will print an error message to stderr and exit with status code 1.

Example:
```bash
java -jar target/t9t-hexdump-9.0-SNAPSHOT.jar nonexistent.txt
```

Output:
```
Error: nonexistent.txt (No such file or directory)
```
