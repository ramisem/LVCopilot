"""Chunked knowledge base retrieval for LVCopilot.

Parses the ``md_files/*.md`` reference docs into logical sections (split on
``## `` headings), auto-extracts keywords from each section, and provides a
retrieval function that returns only the most relevant sections for a given
topic query — avoiding the need to load entire 40-80 KB documents.

No external dependencies are required; keyword matching is done via simple
set-intersection scoring.
"""

import json
import os
import re


# ── Section data structure ────────────────────────────────────────────────

class Section:
    """A single indexed section from a knowledge-base markdown file."""

    __slots__ = (
        "component", "section_id", "title", "keywords",
        "start_line", "end_line", "char_count",
    )

    def __init__(self, component, section_id, title, keywords,
                 start_line, end_line, char_count):
        self.component = component
        self.section_id = section_id
        self.title = title
        self.keywords = keywords
        self.start_line = start_line
        self.end_line = end_line
        self.char_count = char_count

    def to_dict(self):
        return {
            "component": self.component,
            "section_id": self.section_id,
            "title": self.title,
            "keywords": sorted(self.keywords),
            "start_line": self.start_line,
            "end_line": self.end_line,
            "char_count": self.char_count,
        }

    @classmethod
    def from_dict(cls, d):
        return cls(
            component=d["component"],
            section_id=d["section_id"],
            title=d["title"],
            keywords=set(d["keywords"]),
            start_line=d["start_line"],
            end_line=d["end_line"],
            char_count=d["char_count"],
        )


# ── Keyword extraction ───────────────────────────────────────────────────

# Regex patterns for extracting meaningful identifiers from code/text
_JAVA_IDENT = re.compile(r'\b([A-Z][a-zA-Z0-9]+(?:Processor|Handler|Block|Exception|List|Set|Info|Util|Access)?)\b')
_METHOD_CALL = re.compile(r'\.([a-z][a-zA-Z0-9]+)\s*\(')
_HEADING_WORDS = re.compile(r'[A-Za-z][A-Za-z0-9_]+')
_TABLE_HEADER_CELL = re.compile(r'\|\s*\*\*?`?([^|`*]+)`?\*?\*?\s*\|')

# Common stop-words that are not useful as keywords
_STOP_WORDS = {
    "the", "and", "for", "you", "use", "this", "that", "with", "from",
    "are", "not", "can", "but", "all", "your", "its", "has", "have",
    "will", "when", "how", "what", "which", "each", "any", "new",
    "see", "also", "must", "should", "may", "example", "string",
    "int", "void", "public", "private", "protected", "static",
    "return", "import", "package", "class", "interface", "extends",
    "implements", "override", "throws", "null", "true", "false",
}


def _extract_keywords(text):
    """Extract meaningful keywords from a block of markdown/code text.

    Returns a set of lowercase keyword strings.
    """
    keywords = set()

    # Java class / type names (PascalCase)
    for m in _JAVA_IDENT.finditer(text):
        kw = m.group(1).lower()
        if len(kw) > 2 and kw not in _STOP_WORDS:
            keywords.add(kw)

    # Method calls (camelCase after a dot)
    for m in _METHOD_CALL.finditer(text):
        kw = m.group(1).lower()
        if len(kw) > 3 and kw not in _STOP_WORDS:
            keywords.add(kw)

    # Words from heading text
    for m in _HEADING_WORDS.finditer(text.split('\n')[0] if '\n' in text else text):
        kw = m.group(0).lower()
        if len(kw) > 2 and kw not in _STOP_WORDS:
            keywords.add(kw)

    return keywords


def _extract_heading_keywords(heading_text):
    """Extract keywords specifically from a section heading line."""
    keywords = set()
    for m in _HEADING_WORDS.finditer(heading_text):
        kw = m.group(0).lower()
        if len(kw) > 2 and kw not in _STOP_WORDS:
            keywords.add(kw)
    return keywords


# ── Index building ────────────────────────────────────────────────────────

def _slugify(title):
    """Convert a heading title to a URL-style slug for section IDs."""
    slug = re.sub(r'[^a-z0-9]+', '-', title.lower()).strip('-')
    return slug


def parse_sections(component, filepath):
    """Parse a markdown file into sections split on ``## `` headings.

    Args:
        component: Component name (e.g., 'action', 'ajax').
        filepath: Absolute path to the markdown file.

    Returns:
        list[Section]: Parsed sections with keywords and line ranges.
    """
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    sections = []
    current_title = None
    current_start = 0
    current_lines = []

    for i, line in enumerate(lines):
        # Detect ## headings (not # or ###)
        if line.startswith('## '):
            # Save previous section
            if current_title is not None:
                content = ''.join(current_lines)
                kw = _extract_heading_keywords(current_title)
                kw |= _extract_keywords(content)
                sections.append(Section(
                    component=component,
                    section_id=_slugify(current_title),
                    title=current_title,
                    keywords=kw,
                    start_line=current_start,
                    end_line=i - 1,
                    char_count=len(content),
                ))

            current_title = line.lstrip('#').strip()
            current_start = i
            current_lines = [line]
        else:
            current_lines.append(line)

    # Last section
    if current_title is not None:
        content = ''.join(current_lines)
        kw = _extract_heading_keywords(current_title)
        kw |= _extract_keywords(content)
        sections.append(Section(
            component=component,
            section_id=_slugify(current_title),
            title=current_title,
            keywords=kw,
            start_line=current_start,
            end_line=len(lines) - 1,
            char_count=len(content),
        ))

    return sections


def build_index(md_files_dir):
    """Build the knowledge index from all markdown files in a directory.

    Args:
        md_files_dir: Path to the ``md_files/`` directory.

    Returns:
        dict: Mapping of component name → list of Section objects.
    """
    index = {}
    if not os.path.isdir(md_files_dir):
        return index

    for filename in sorted(os.listdir(md_files_dir)):
        if not filename.endswith('.md'):
            continue
        component = filename.replace('.md', '')
        filepath = os.path.join(md_files_dir, filename)
        sections = parse_sections(component, filepath)
        index[component] = sections

    return index


def save_index(index, output_path):
    """Serialize the index to a JSON file.

    Args:
        index: dict of component → list[Section].
        output_path: Path to write the JSON index file.
    """
    serializable = {}
    for component, sections in index.items():
        serializable[component] = [s.to_dict() for s in sections]

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(serializable, f, indent=2)


def load_index(index_path):
    """Load a previously saved index from JSON.

    Args:
        index_path: Path to the JSON index file.

    Returns:
        dict: component → list[Section].
    """
    with open(index_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    index = {}
    for component, section_dicts in data.items():
        index[component] = [Section.from_dict(d) for d in section_dicts]

    return index


# ── Retrieval ─────────────────────────────────────────────────────────────

def retrieve_sections(index, component, topic, max_sections=3):
    """Retrieve the most relevant sections for a component + topic query.

    Scores each section by keyword overlap with the query topic words
    and returns the top N sections.

    Args:
        index: The knowledge index (dict of component → list[Section]).
        component: Component name (e.g., 'action').
        topic: Free-text topic string (e.g., 'QueryProcessor SELECT DataSet').
        max_sections: Maximum number of sections to return.

    Returns:
        list[Section]: Top matching sections, sorted by relevance score (descending).
    """
    sections = index.get(component, [])
    if not sections:
        return []

    # Tokenize the query topic into keywords
    query_keywords = set()
    for word in re.findall(r'[A-Za-z][A-Za-z0-9_]+', topic):
        kw = word.lower()
        if len(kw) > 2 and kw not in _STOP_WORDS:
            query_keywords.add(kw)

    if not query_keywords:
        # If no meaningful keywords, return the first N sections
        return sections[:max_sections]

    # Score each section by keyword overlap
    scored = []
    for section in sections:
        overlap = len(section.keywords & query_keywords)
        if overlap > 0:
            # Normalize by query size to prefer focused matches
            score = overlap / len(query_keywords)
            
            # Give a significant boost if query keywords are in the section title
            title_words = {w.lower() for w in re.findall(r'[A-Za-z][A-Za-z0-9_]+', section.title)}
            title_overlap = len(title_words & query_keywords)
            if title_overlap > 0:
                score += 10.0 * (title_overlap / len(query_keywords))
                
            scored.append((score, section))

    # Sort by score descending, then by original order (start_line) for ties
    scored.sort(key=lambda x: (-x[0], x[1].start_line))

    return [s for _, s in scored[:max_sections]]


def get_section_content(md_files_dir, component, section):
    """Read the actual content of a section from the source markdown file.

    Args:
        md_files_dir: Path to the ``md_files/`` directory.
        component: Component name (e.g., 'action').
        section: Section object with start_line / end_line.

    Returns:
        str: The section content, or an error message if unreadable.
    """
    filepath = os.path.join(md_files_dir, f"{component}.md")
    if not os.path.isfile(filepath):
        return f"[Error: Could not find {component}.md]"

    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
    except Exception as e:
        return f"[Error reading {component}.md: {e}]"

    start = max(0, section.start_line)
    end = min(len(lines), section.end_line + 1)
    return ''.join(lines[start:end])


# ── Convenience: ensure index exists ─────────────────────────────────────

def ensure_index(base_dir):
    """Ensure the knowledge index exists, building it if needed.

    Args:
        base_dir: The lvcopilot package base directory (contains md_files/).

    Returns:
        tuple: (index dict, md_files_dir path).
    """
    md_files_dir = os.path.join(base_dir, 'md_files')
    index_path = os.path.join(md_files_dir, 'knowledge_index.json')

    if os.path.isfile(index_path):
        # Check if any md file is newer than the index
        index_mtime = os.path.getmtime(index_path)
        needs_rebuild = False
        for f in os.listdir(md_files_dir):
            if f.endswith('.md'):
                if os.path.getmtime(os.path.join(md_files_dir, f)) > index_mtime:
                    needs_rebuild = True
                    break
        if not needs_rebuild:
            return load_index(index_path), md_files_dir

    # Build and save
    index = build_index(md_files_dir)
    if index:
        save_index(index, index_path)
    return index, md_files_dir
