---
testspace:
title: Goto address
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Go to address.

## Test
- Follow the instructions specified in the `Hex view` test.
- Open the contextual menu and click on the `Go to address...` menu.
- A new dialog box appears, prompting the user to enter an address number (in hex).
- Enter an invalid address number (e.g. a large number) and click `Ok`.
- The application will reject the value and specify the maximum value that can be entered.
- Enter a valid address number and click `Ok`.
- The application will scroll to the address and play an animation to highlight the selected line.