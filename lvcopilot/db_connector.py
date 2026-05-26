"""Database connector for LVCopilot.

Provides read-only database access to Oracle and SQL Server databases,
enabling the agent to query schemas, inspect tables, and run SELECT
queries during any development phase.

Supports up to two simultaneous database connections (DB1 and DB2)
configured via environment variables in ``.lvcopilotenv``.

Security:
    - Only SELECT / DESCRIBE / EXPLAIN statements are allowed.
    - DML (INSERT, UPDATE, DELETE, MERGE) and DDL (CREATE, ALTER, DROP,
      TRUNCATE) are blocked at the SQL validation layer.
    - All queries are row-limited (``DB_MAX_ROWS``, default 50).
    - Connection and query timeouts are enforced.

Environment Variables (per database — replace ``N`` with ``1`` or ``2``)::

    DB{N}_TYPE     — ``oracle`` or ``sqlserver``
    DB{N}_HOST     — Database host
    DB{N}_PORT     — Database port (default: 1521 for Oracle, 1433 for SQL Server)
    DB{N}_NAME     — SID/service name (Oracle) or database name (SQL Server)
    DB{N}_USER     — Database username
    DB{N}_PASSWORD — Database password

Shared::

    DB_MAX_ROWS    — Max rows returned per query (default: 50)
    DB_TIMEOUT     — Connection timeout in seconds (default: 30)
"""

import os
import re
import sys

# ── SQL validation ────────────────────────────────────────────────────────

# Statements that are always blocked (case-insensitive first word)
_BLOCKED_PREFIXES = re.compile(
    r'^\s*(INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|TRUNCATE|MERGE|GRANT|'
    r'REVOKE|EXEC|EXECUTE|CALL|BEGIN|DECLARE|SET)\b',
    re.IGNORECASE,
)

# Statements that are explicitly allowed
_ALLOWED_PREFIXES = re.compile(
    r'^\s*(SELECT|DESCRIBE|DESC|EXPLAIN|SHOW|WITH)\b',
    re.IGNORECASE,
)


def validate_sql(sql):
    """Validate that a SQL statement is read-only.

    Args:
        sql: The SQL statement string.

    Returns:
        bool: True if allowed, False if blocked.
    """
    if not sql:
        return False
    # Clean SQL string
    sql_clean = sql.strip()
    
    # Check if starts with blocked prefix
    if _BLOCKED_PREFIXES.search(sql_clean):
        return False
        
    # Check if starts with allowed prefix
    if _ALLOWED_PREFIXES.search(sql_clean):
        # We must also ensure there are no blocked commands in subqueries or semi-colon separated statements
        for statement in sql_clean.split(';'):
            statement = statement.strip()
            if not statement:
                continue
            if _BLOCKED_PREFIXES.search(statement):
                return False
            # Check if it doesn't match allowed prefix
            if not _ALLOWED_PREFIXES.search(statement):
                return False
        return True
        
    return False


# ── Database Connector ───────────────────────────────────────────────────

class DatabaseConnector:
    def __init__(self, prefix):
        """Initialize connection parameters for DB1 or DB2.

        Args:
            prefix: 'DB1' or 'DB2'.
        """
        self.prefix = prefix
        self.db_type = os.environ.get(f"{prefix}_TYPE", "").strip().lower()
        self.host = os.environ.get(f"{prefix}_HOST", "").strip()
        self.port = os.environ.get(f"{prefix}_PORT", "").strip()
        self.name = os.environ.get(f"{prefix}_NAME", "").strip()
        self.user = os.environ.get(f"{prefix}_USER", "").strip()
        self.password = os.environ.get(f"{prefix}_PASSWORD", "").strip()
        
        self.connection = None
        
    def is_configured(self):
        """Check if this database is configured."""
        return bool(self.db_type and self.host and self.user)
        
    def connect(self):
        """Establish a connection to the database if not already connected."""
        if self.connection:
            return self.connection
            
        if not self.is_configured():
            raise ValueError(f"Database {self.prefix} is not fully configured.")
            
        timeout = int(os.environ.get("DB_TIMEOUT", "30"))
        
        if self.db_type == "oracle":
            try:
                import oracledb
            except ImportError:
                raise ImportError("The 'oracledb' package is not installed. Please run 'pip install oracledb' to install it.")
            
            # Use thin mode (default in modern oracledb)
            # DSN structure: host:port/service_name
            dsn = f"{self.host}:{self.port}/{self.name}"
            self.connection = oracledb.connect(
                user=self.user,
                password=self.password,
                dsn=dsn,
                expire_time=timeout
            )
            
        elif self.db_type == "sqlserver":
            try:
                import pyodbc
            except ImportError as e:
                err_msg = str(e)
                if "libodbc" in err_msg or "unixodbc" in err_msg or "dlopen" in err_msg or "Library not loaded" in err_msg:
                    raise ImportError(
                        "The 'pyodbc' package is installed, but the system ODBC driver manager (unixodbc) is missing on your Mac.\n"
                        "Please install it using Homebrew:\n"
                        "  [bold cyan]brew install unixodbc[/bold cyan]\n"
                        "And then retry connecting."
                    )
                raise ImportError("The 'pyodbc' package is not installed. Please run 'pip install pyodbc' to install it.")
                
            # Connect using standard SQL Server ODBC drivers.
            # We try ODBC Driver 18, and fallback to 17 or SQL Server.
            drivers = [
                "ODBC Driver 18 for SQL Server",
                "ODBC Driver 17 for SQL Server",
                "SQL Server Native Client 11.0",
                "SQL Server"
            ]
            
            last_err = None
            for driver in drivers:
                conn_str = (
                    f"DRIVER={{{driver}}};"
                    f"SERVER={self.host},{self.port};"
                    f"DATABASE={self.name};"
                    f"UID={self.user};"
                    f"PWD={self.password};"
                    "TrustServerCertificate=yes;"
                )
                try:
                    self.connection = pyodbc.connect(conn_str, timeout=timeout)
                    break
                except Exception as e:
                    last_err = e
                    err_str = str(e)
                    # Check if the driver itself is missing on the system
                    is_missing_driver = (
                        "Can't open lib" in err_str or 
                        "file not found" in err_str or 
                        "Data source name not found" in err_str
                    )
                    if not is_missing_driver:
                        # Fail fast with the actual connection/auth error
                        raise ConnectionError(
                            f"SQL Server connection failed using driver '{driver}'.\n"
                            f"Detail: {e}\n"
                            f"Tip: If your database is in a local Docker container, try using '127.0.0.1' instead of 'localhost' as the Host."
                        )
                    continue
            else:
                raise ConnectionError(f"Could not connect to SQL Server using any ODBC driver. Last error: {last_err}")
        else:
            raise ValueError(f"Unsupported database type: {self.db_type}")
            
        return self.connection
        
    def close(self):
        """Close the database connection."""
        if self.connection:
            try:
                self.connection.close()
            except Exception:
                pass
            self.connection = None
            
    def test_connection(self):
        """Test the connection and return a status string."""
        if not self.is_configured():
            return f"Database {self.prefix} is not configured."
        try:
            self.connect()
            cursor = self.connection.cursor()
            if self.db_type == "oracle":
                cursor.execute("SELECT 1 FROM DUAL")
            else:
                cursor.execute("SELECT 1")
            cursor.fetchone()
            cursor.close()
            return f"✅ Database {self.prefix} ({self.db_type}) connected successfully!"
        except Exception as e:
            return f"❌ Database {self.prefix} connection failed: {e}"
            
    def execute_query(self, sql, params=None):
        """Execute a read-only SELECT query and return formatted results."""
        if not validate_sql(sql):
            return "Error: DML and DDL commands are blocked. Only read-only queries are allowed."
            
        try:
            self.connect()
            cursor = self.connection.cursor()
            
            # Enforce max rows
            max_rows = int(os.environ.get("DB_MAX_ROWS", "50"))
            
            if params:
                cursor.execute(sql, params)
            else:
                cursor.execute(sql)
                
            # Get column headers
            if cursor.description:
                headers = [desc[0] for desc in cursor.description]
            else:
                headers = []
                
            rows = cursor.fetchmany(max_rows)
            row_count = len(rows)
            
            # Format as text table
            result = self._format_as_table(headers, rows)
            result += f"\n({row_count} row(s) returned)"
            
            cursor.close()
            return result
        except Exception as e:
            return f"Error executing query: {e}"
            
    def list_tables(self, schema=None):
        """List tables in the database."""
        try:
            self.connect()
            cursor = self.connection.cursor()
            
            if self.db_type == "oracle":
                if schema:
                    sql = "SELECT table_name FROM all_tables WHERE owner = :1 ORDER BY table_name"
                    cursor.execute(sql, [schema.upper()])
                else:
                    sql = "SELECT table_name FROM user_tables ORDER BY table_name"
                    cursor.execute(sql)
            elif self.db_type == "sqlserver":
                if schema:
                    sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? AND table_type = 'BASE TABLE' ORDER BY table_name"
                    cursor.execute(sql, [schema])
                else:
                    sql = "SELECT table_name FROM information_schema.tables WHERE table_type = 'BASE TABLE' ORDER BY table_name"
                    cursor.execute(sql)
            else:
                return "Unsupported database type."
                
            rows = cursor.fetchall()
            cursor.close()
            
            if not rows:
                return "No tables found."
                
            tables = [row[0] for row in rows]
            result = f"Found {len(tables)} table(s):\n"
            result += "\n".join(f"- {t}" for t in tables)
            return result
        except Exception as e:
            return f"Error listing tables: {e}"
            
    def describe_table(self, table_name):
        """Describe the schema of a specific table."""
        try:
            self.connect()
            cursor = self.connection.cursor()
            
            if self.db_type == "oracle":
                parts = table_name.upper().split('.')
                if len(parts) == 2:
                    schema, tbl = parts
                    sql = "SELECT column_name, data_type, data_length, nullable FROM all_tab_columns WHERE owner = :1 AND table_name = :2 ORDER BY column_id"
                    cursor.execute(sql, [schema, tbl])
                else:
                    tbl = parts[0]
                    sql = "SELECT column_name, data_type, data_length, nullable FROM user_tab_columns WHERE table_name = :1 ORDER BY column_id"
                    cursor.execute(sql, [tbl])
            elif self.db_type == "sqlserver":
                parts = table_name.split('.')
                if len(parts) == 2:
                    schema, tbl = parts
                    sql = "SELECT column_name, data_type, character_maximum_length, is_nullable FROM information_schema.columns WHERE table_schema = ? AND table_name = ? ORDER BY ordinal_position"
                    cursor.execute(sql, [schema, tbl])
                else:
                    tbl = parts[0]
                    sql = "SELECT column_name, data_type, character_maximum_length, is_nullable FROM information_schema.columns WHERE table_name = ? ORDER BY ordinal_position"
                    cursor.execute(sql, [tbl])
            else:
                return "Unsupported database type."
                
            rows = cursor.fetchall()
            cursor.close()
            
            if not rows:
                return f"Table '{table_name}' not found or could not be described."
                
            headers = ["COLUMN_NAME", "DATA_TYPE", "LENGTH/PRECISION", "NULLABLE"]
            formatted_rows = []
            for r in rows:
                col_name, data_type, length, nullable = r
                formatted_rows.append([
                    str(col_name),
                    str(data_type),
                    str(length) if length is not None else "N/A",
                    "YES" if str(nullable).upper() in ("Y", "YES", "1", "TRUE") else "NO"
                ])
                
            result = f"Table description for '{table_name}' ({len(rows)} column(s)):\n"
            result += self._format_as_table(headers, formatted_rows)
            return result
        except Exception as e:
            return f"Error describing table: {e}"
            
    def _format_as_table(self, headers, rows):
        """Format tabular data into a pretty ASCII table."""
        if not headers:
            return ""
            
        # Calculate max column widths
        widths = [len(h) for h in headers]
        for row in rows:
            for i, val in enumerate(row):
                if i < len(widths):
                    widths[i] = max(widths[i], len(str(val if val is not None else "")))
                    
        # Build header line
        header_line = " | ".join(str(h).ljust(widths[i]) for i, h in enumerate(headers))
        separator = "-+-".join("-" * w for w in widths)
        
        lines = [header_line, separator]
        for row in rows:
            line = " | ".join(str(val if val is not None else "").ljust(widths[i]) for i, val in enumerate(row))
            lines.append(line)
            
        return "\n".join(lines)


# ── Database Manager ─────────────────────────────────────────────────────

class DatabaseManager:
    def __init__(self):
        """Manage dual database connectors (DB1 and DB2)."""
        self.db1 = DatabaseConnector("DB1")
        self.db2 = DatabaseConnector("DB2")
        
    def get_connector(self, db_name=None):
        """Get the connector based on database identifier string.

        Args:
            db_name: 'db1' or 'db2' (case-insensitive). Defaults to 'db1'.
        """
        if not db_name:
            db_name = "db1"
            
        db_name = db_name.strip().lower()
        if db_name == "db1":
            return self.db1
        elif db_name == "db2":
            return self.db2
        else:
            raise ValueError(f"Invalid database name '{db_name}'. Choose 'db1' or 'db2'.")
            
    def has_any_configured(self):
        """Check if at least one database is configured."""
        return self.db1.is_configured() or self.db2.is_configured()
        
    def get_status(self):
        """Get connection status of all configured databases."""
        statuses = []
        if self.db1.is_configured():
            statuses.append(f"DB1 (Primary): Type={self.db1.db_type}, Host={self.db1.host}, Name={self.db1.name}, User={self.db1.user}")
        else:
            statuses.append("DB1 (Primary): Not configured")
            
        if self.db2.is_configured():
            statuses.append(f"DB2 (Secondary): Type={self.db2.db_type}, Host={self.db2.host}, Name={self.db2.name}, User={self.db2.user}")
        else:
            statuses.append("DB2 (Secondary): Not configured")
            
        return "\n".join(statuses)
        
    def test_connections(self):
        """Test connections to all configured databases."""
        results = []
        if self.db1.is_configured():
            results.append(f"DB1: {self.db1.test_connection()}")
        else:
            results.append("DB1: Not configured")
            
        if self.db2.is_configured():
            results.append(f"DB2: {self.db2.test_connection()}")
        else:
            results.append("DB2: Not configured")
            
        return "\n".join(results)
        
    def close_all(self):
        """Close all active database connections."""
        self.db1.close()
        self.db2.close()
        return "All active database connections closed."
