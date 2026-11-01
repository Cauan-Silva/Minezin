"""Report the actual JUnit XML totals in CI; fail if no tests ran."""
from pathlib import Path
import os
import xml.etree.ElementTree as ET

reports = sorted(Path('build/test-results/test').glob('TEST-*.xml'))
totals = dict.fromkeys(('tests', 'failures', 'errors', 'skipped'), 0)
for report in reports:
    root = ET.parse(report).getroot()
    for name in totals:
        totals[name] += int(root.get(name, 0))
message = f"JUnit: {totals['tests']} tests, {totals['failures']} failures, {totals['errors']} errors, {totals['skipped']} skipped"
print(message)
if os.environ.get('GITHUB_STEP_SUMMARY'):
    with open(os.environ['GITHUB_STEP_SUMMARY'], 'a', encoding='utf-8') as summary:
        summary.write('## Java tests\n\n' + message + '\n')
if not totals['tests'] or totals['failures'] or totals['errors']:
    raise SystemExit('JUnit reports are absent or contain failures')
