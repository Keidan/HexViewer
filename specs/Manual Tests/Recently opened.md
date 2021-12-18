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
   - When opening with the `Partial file opening` function, the start and end sizes are also displayed in hexadecimal.

## Button (display)
- Follow the instructions specified in the `Open a file` or `Partial file opening` test.
- Open the recently opened files view by clicking on the `RECENTLY OPENED` button in the home view.
- The view will display the list of recently opened files.
   - When opening with the `Partial file opening` function, the start and end sizes are also displayed in hexadecimal.

## Open file
- Follow the instructions specified in the `Open a file` or `Partial file opening` test.
- Open the recently opened files view (see `Menu (display)` or `Button (display)`).
- Click on a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.

## Partial reopening of a modified file
- Follow the instructions specified in the `Partial file opening` test and set the `start` offset to what you want and the `end` offset to the file size - 1.
- Close the file.
- Modify the file with an external program, delete the last 3 bytes of the file and save the modification.
- Open `HexViewer`.
- Open the recently opened files view by clicking on the `Recently opened` menu in the contextual menu.
- The file appears with the following message: "The file size has changed, click to re-specify the new size"
- Click on the file.
- The `Partial file opening` view appears.
- Adjust the `end` offset to the file size - 1.
- Validate the choice by clicking on the ticker at the top right.
- The file opens.
- Open the recently opened files view by clicking on the `Recently opened` menu in the contextual menu.
- The message is replaced by the new information.
   - When opening with the `Partial file opening` function, the start and end sizes are also displayed in hexadecimal.

## Clear list
- Follow the instructions specified in the `Open a file` or `Partial file opening` test.
- Close the file (if it's open).
- Open the recently opened files view (see `Menu (display)` or `Button (display)`).
- Until the list is empty, swipe left to right (or right to left) on an entry to delete it.
- When the list is empty, the view returns to the home view.
- The `RECENTLY OPENED` button must be disabled and the `Recently opened` menu of the contextual menu must be disabled.
