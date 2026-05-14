import re

lines = [
    "Here is **src/com/MyAction.java**",
    "File: src/com/MyAction.java",
    "Created app.js.",
    "`app/index.js`",
    "Some text with ~folder/file.txt",
    "/absolute/path/to/file.py",
    "File: /src/com/test.java"
]
path_pattern = r'([a-zA-Z0-9_\-\./\\]+\.[a-zA-Z0-9]{2,})'

for l in lines:
    m = re.search(path_pattern, l)
    if m:
        print(f"'{l}' -> '{m.group(1)}'")
    else:
        print(f"'{l}' -> NONE")
