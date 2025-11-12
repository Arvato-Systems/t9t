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
