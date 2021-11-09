---
testspace:
title: Undo
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Action undo.

## Test
- Follow the instructions specified in the `Edit single line`, `Edit multiple line` and/or `Remove line` test.
- Open the contextual menu and click on the `left arrow` menu.
- The application will undo the previous changes.
- If the button is not disabled (so there is still something to undo), you can try the previous 2 steps again.
