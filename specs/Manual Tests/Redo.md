---
testspace:
title: Redo
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Action redo.

## Test
- Follow the instructions specified in the `Undo` test.
- Open the contextual menu and click on the `right arrow` menu.
- The application will redo the previous changes.
- If the button is not disabled (so there is still something to redo), you can try the previous 2 steps again.