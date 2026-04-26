import sqlite3
import json
import sys
from collections import defaultdict, Counter

DB = r"C:\Users\ce\AppData\Local\Temp\webshooter-stats.db"
OUT = r"D:\source\webshooter\stats-shooters-by-competition-count.txt"

con = sqlite3.connect(DB)
cur = con.cursor()

competitions_per_user = defaultdict(set)
fullname_per_user = {}
clubs_per_user = defaultdict(Counter)

cur.execute("SELECT userId, userFullname, competitionsId, signupJson FROM results")
for user_id, fullname, comp_id, signup_json in cur:
    competitions_per_user[user_id].add(comp_id)
    fullname_per_user[user_id] = fullname
    try:
        club_name = json.loads(signup_json).get("club", {}).get("name")
    except (json.JSONDecodeError, AttributeError):
        club_name = None
    if club_name:
        clubs_per_user[user_id][club_name] += 1

rows = []
for user_id, comps in competitions_per_user.items():
    count = len(comps)
    name = fullname_per_user.get(user_id, "")
    club = clubs_per_user[user_id].most_common(1)[0][0] if clubs_per_user[user_id] else ""
    rows.append((count, name, club))

rows.sort(key=lambda r: (-r[0], r[1]))

with open(OUT, "w", encoding="utf-8") as f:
    for count, name, club in rows:
        f.write(f"{count} - {name} - {club}\n")

print(f"wrote {len(rows)} rows to {OUT}", file=sys.stderr)
