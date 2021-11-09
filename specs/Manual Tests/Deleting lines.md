---
testspace:
title: Deleting lines
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Deletion of one or more lines.

## Deleting a line
- Follow the instructions specified in the `Hex view` test.
- Long press on a line.
- The title of the window contains the number of selected lines.
- Click on the trash icon at the top right of the window.
- If the file is opened in partial mode, an error message will appear, otherwise the line will be deleted.

## Deleting several lines
- Follow the instructions specified in the `Hex view` test.
- Long press on a line.
- The title of the window contains the number of selected lines.
- Click on one or more lines.
- The title of the window contains the number of selected lines.
- Click on the trash icon at the top right of the window.
- If the file is opened in partial mode, an error message will appear, otherwise the line will be deleted.

## Deleting all rows
- Follow the instructions specified in the `Hex view` test.
- Long press on a line.
- Click on the square icon at the top right of the window.
- All lines will be selected.
- The title of the window contains the number of selected lines.
- Click on the trash icon at the top right of the window.
- If the file is opened in partial mode, an error message will appear, otherwise the line will be deleted.