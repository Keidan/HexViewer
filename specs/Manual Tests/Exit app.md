---
testspace:
title: Exit the application
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Exit the application by pressing the back button.

## Without changes
- Press the back button twice.
- The application closes.

## With changes (Cancel)
- Press the back button twice.
- A dialog box appears, prompting the user to save the changes.
- Click on `Cancel`.
- The dialog box disappears and nothing changes.

## With changes (No)
- Press the back button twice.
- A dialog box appears, prompting the user to save the changes.
- Click on `No`.
- The application closes.
   
   
## With changes (Yes)
- Press the back button twice.
- A dialog box appears, prompting the user to save the changes.
- Click on `Yes`.
- The dialog box disappears.
- A dialog box showing the progress of the saving is displayed (can be stealthy with small files).
- A toast indicating `Save success.` is displayed.
- The application closes.