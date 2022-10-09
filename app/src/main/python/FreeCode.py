import io
from contextlib import redirect_stdout

def run(code):
    if "os" in code or "__import__" in code:
        return "access denied"

    d = {}
    try:
        f = io.StringIO()
        with redirect_stdout(f):
            exec(code, d)
        return f.getvalue()
    except Exception as e:
        return e
