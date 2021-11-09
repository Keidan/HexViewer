---
testspace:
title: Close file
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Close an open file

## Without changes
- Click on the `Close` menu.
- The view returns to the home view.
- The menus below must be enabled:
   - `Open...`
   - `Partial file opening...`
   - `Recently opened...`
   - `Settings`
   - All other menus must be disabled.

## With changes (Cancel)
- Click on the `Close` menu.
- A dialog box appears, prompting the user to save the changes.
- Click on `Cancel`.
- The dialog box disappears and nothing changes.

## With changes (No)
- Click on the `Close` menu.
- A dialog box appears, prompting the user to save the changes.
- Click on `No`.
- The dialog box disappears the view returns to the home view.
- The menus below must be enabled:
   - `Open...`
   - `Partial file opening...`
   - `Recently opened...`
   - `Settings`
   - All other menus must be disabled.
   
   
## With changes (Yes)
- Click on the `Close` menu.
- A dialog box appears, prompting the user to save the changes.
- Click on `Yes`.
- The dialog box disappears.
- A dialog box showing the progress of the saving is displayed (can be stealthy with small files).
- The view returns to the home view and a toast indicating `Save success.` is displayed.
- The menus below must be enabled:
   - `Open...`
   - `Partial file opening...`
   - `Recently opened...`
   - `Settings`
   - All other menus must be disabled.