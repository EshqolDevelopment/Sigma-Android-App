import io
from contextlib import redirect_stdout

def run(code):
    d = {}
    try:
        f = io.StringIO()
        with redirect_stdout(f):
            exec(code, d)
        return f.getvalue()
    except Exception as e:
        return e
