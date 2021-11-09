---
testspace:
title: Editing multiple lines
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Editing multiple lines.

## Test
- Follow the instructions specified in the `Hex view` test.
- Long press on a line.
- Select multiple non-continuous lines.
- The title of the window contains the number of selected lines.
- Click on the pencil icon at the top right of the window.
- An error message appears.
- Now make a continuous selection and confirm the change to editing by clicking on the pencil icon at the top right of the window.
- The title of the window contains the number of selected lines.
- Then go to the `Editing a line` test.
