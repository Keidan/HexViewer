---
testspace:
title: Open file
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Open a file.

## Button
- Open a file by clicking on the `OPEN` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.

## Menu
- Open a file by clicking on the `Open...` menu in the contextual menu.
- The main view disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.