---
testspace:
title: Recently opened
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Recently opened.

## Menu (display)
- Follow the instructions specified in the `Open a file` or `Partial file opening` test.
- Open the recently opened files view by clicking on the `Recently opened` menu in the contextual menu.
- The view will display the list of recently opened files.

## Button (display)
- Follow the instructions specified in the `Open a file` or `Partial file opening` test.
- Open the recently opened files view by clicking on the `RECENTLY OPENED` button in the home view.
- The view will display the list of recently opened files.

## Open file
- Follow the instructions specified in the `Open a file` or `Partial file opening` test.
- Open the recently opened files view (see `Menu (display)` or `Button (display)`).
- Click on a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.

## Clear list
- Follow the instructions specified in the `Open a file` or `Partial file opening` test.
- Close the file (if it's open).
- Open the recently opened files view (see `Menu (display)` or `Button (display)`).
- Until the list is empty, swipe left to right (or right to left) on an entry to delete it.
- When the list is empty, the view returns to the home view.
- The `RECENTLY OPENED` button must be disabled and the `Recently opened` menu of the contextual menu must be disabled.
