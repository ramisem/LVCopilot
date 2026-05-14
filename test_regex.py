import re

responses = [
"""
Here is your file:
**File: src/com/client/actions/MyAction.java**
```java
public class MyAction {}
```
""",
"""
`File: src/com/client/actions/MyAction2.java`
```java
public class MyAction2 {}
```
""",
"""
File: src/com/client/actions/MyAction3.java
```java
public class MyAction3 {}
```
""",
"""
File path: `src/com/client/actions/MyAction4.java`
```java
public class MyAction4 {}
```
"""
]

# pattern = r'File:\s*([^\n`]+)\s*\n\s*```[a-zA-Z]*\n(.*?)```' # old
pattern = r'(?i)(?:file|filename|file path)[\*:\s]*`?\**([^\n`\*]+)\**`?.*?\n\s*```[a-zA-Z]*\n(.*?)```'

for i, r in enumerate(responses):
    matches = list(re.finditer(pattern, r, re.DOTALL | re.IGNORECASE))
    print(f"Test {i}: {len(matches)} matches")
    for m in matches:
        print(f"  Path: '{m.group(1).strip()}'")
