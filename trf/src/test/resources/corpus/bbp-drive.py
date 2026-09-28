import random, subprocess, sys, os, math
BBP = sys.argv[1]; out_dir = sys.argv[2]; count = int(sys.argv[3]); seed0 = int(sys.argv[4])
mode = sys.argv[5] if len(sys.argv) > 5 else "baku"

def line001(sr, rating, points, cells):
    l = list("001 %4d      %-33s %4d" % (sr, "Player%04d" % sr, rating))
    s = ''.join(l).ljust(80) + ("%4.1f" % points).rjust(4) + " " * 7
    for (opp, col, res) in cells:
        s += "%4s %s %s  " % ("0000" if opp == 0 else "%4d" % opp, col, res)
    return s.rstrip()

def run(tid):
    rnd = random.Random(seed0 + tid)
    n = rnd.randint(10, 60); rounds = rnd.randint(5, min(11, n - 1))
    ratings = sorted([rnd.randint(1400, 2600) for _ in range(n)], reverse=True)
    cells = {i: [] for i in range(1, n + 1)}; pts = {i: 0.0 for i in range(1, n + 1)}
    xxa = []
    if mode == "baku":
        ga = 2 * math.ceil(n / 4); acc = math.ceil(rounds / 2); full = math.ceil(acc / 2)
        vals = [1.0] * full + [0.5] * (acc - full)
        for i in range(1, ga + 1):
            xxa.append("XXA %4d" % i + "".join(" %4.1f" % v for v in vals))
    else:
        for i in range(1, n + 1):
            if rnd.random() < 0.3:
                vals = [rnd.choice([0.0, 0.5, 1.0, 1.0, 2.0]) for _ in range(rounds)]
                xxa.append("XXA %4d" % i + "".join(" %4.1f" % v for v in vals))
    retired = set()
    path = os.path.join(out_dir, "a%d.trf" % tid)
    for r in range(1, rounds + 1):
        absent = {}
        for i in range(1, n + 1):
            if i in retired: absent[i] = 'Z'
            elif rnd.random() < 0.03: absent[i] = rnd.choice(['H', 'Z'])
            elif rnd.random() < 0.01: retired.add(i); absent[i] = 'Z'
        def write(extra):
            lines = ["012 Accelerated %d" % tid, "XXR %d" % rounds, "XXC white1"]
            for i in range(1, n + 1):
                c = list(cells[i])
                p = pts[i]
                if extra and i in absent:
                    c.append((0, '-', absent[i])); p += 0.5 if absent[i] == 'H' else 0
                lines.append(line001(i, ratings[i - 1], p, c))
            lines += xxa
            open(path, 'w').write("\r".join(lines) + "\r")
        write(True)
        res = subprocess.run([BBP, "--dutch", path, "-p", path + ".out"], capture_output=True, text=True)
        if res.returncode != 0:
            os.remove(path); return False
        pairs = [l.split() for l in open(path + ".out").read().split("\n")[1:] if l.strip()]
        os.remove(path + ".out")
        for i, kind in absent.items():
            cells[i].append((0, '-', kind)); pts[i] += 0.5 if kind == 'H' else 0
        for w, b in pairs:
            w = int(w); b = int(b)
            if b == 0:
                cells[w].append((0, '-', 'U')); pts[w] += 1; continue
            x = rnd.random()
            if x < 0.04: rw, rb = '+', '-'
            elif x < 0.07: rw, rb = '-', '+'
            elif x < 0.08: rw, rb = '-', '-'
            else:
                d = ratings[w - 1] - ratings[b - 1]
                e = 1 / (1 + 10 ** (-d / 400)); y = rnd.random()
                rw, rb = ('1', '0') if y < e - 0.15 else (('=', '=') if y < e + 0.15 else ('0', '1'))
            cells[w].append((b, 'w', rw)); cells[b].append((w, 'b', rb))
            pts[w] += {'1': 1, '+': 1, '=': 0.5}.get(rw, 0); pts[b] += {'1': 1, '+': 1, '=': 0.5}.get(rb, 0)
    write(False)
    return True

ok = sum(run(t) for t in range(count))
print(ok, "tournaments")
