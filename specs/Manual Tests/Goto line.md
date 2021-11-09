---
testspace:
title: Goto line
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Go to line.

## Test
- Follow the instructions specified in the `Plain view` test.
- Open the contextual menu and click on the `Go to line...` menu.
- A new dialog box appears, prompting the user to enter a line number.
- Enter an invalid line number (e.g. a large number) and click `Ok`.
- The application will reject the value and specify the maximum value that can be entered.
- Enter a valid line number and click `Ok`.
- The application will scroll to the line and play an animation to highlight the selected line.