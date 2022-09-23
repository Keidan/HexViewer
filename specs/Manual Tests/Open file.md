---
testspace:
title: Open file
sizes: 0B, ~100kb, ~500kb, ~1Mb, ~5Mb, ~10Mb, ~15Mb
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Open a file.\
**<ins>IMPORTANT:</ins> Do the opening test with sizes of {{ spec.sizes }}.**

## Button
- Open a file by clicking on the `OPEN` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
   - **<ins>IMPORTANT:</ins> Repeat the operation with a file of {{ spec.sizes }}.**
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.
- If the file to be opened is empty, an edit icon will be available between the search icon and the dots icon.

## Menu
- Open a file by clicking on the `Open...` menu in the contextual menu.
- The main view disappears.
- A file picker appears.
   - **<ins>IMPORTANT:</ins> Repeat the operation with a file of {{ spec.sizes }}.**
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.
- If the file to be opened is empty, an edit icon will be available between the search icon and the dots icon.

## Open when a file is already open (no change)
- Open a file by clicking on the `Open...` menu in the contextual menu OR by clicking on the `OPEN` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.
- Open a file by clicking on the `Open...` menu in the contextual menu.
- The main view disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.

## Open when a file is already open (With changes (Cancel))
- Open a file by clicking on the `Open...` menu in the contextual menu OR by clicking on the `OPEN` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.
- Long press on a line.
- The title of the window contains the number of selected lines.
- Click on the trash icon at the top right of the window.
- Open a file by clicking on the `Open...` menu in the contextual menu.
- A dialog box appears, prompting the user to save the changes.
- Click on `Cancel`.
- The dialog box disappears and nothing changes.

## Open when a file is already open (With changes (No))
- Open a file by clicking on the `Open...` menu in the contextual menu OR by clicking on the `OPEN` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.
- Long press on a line.
- The title of the window contains the number of selected lines.
- Click on the trash icon at the top right of the window.
- Open a file by clicking on the `Open...` menu in the contextual menu.
- A dialog box appears, prompting the user to save the changes.
- Click on `No`.
- The dialog box disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.

## Open when a file is already open (With changes (No))
- Open a file by clicking on the `Open...` menu in the contextual menu OR by clicking on the `OPEN` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.
- Long press on a line.
- The title of the window contains the number of selected lines.
- Click on the trash icon at the top right of the window.
- Open a file by clicking on the `Open...` menu in the contextual menu.
- A dialog box appears, prompting the user to save the changes.
- Click on `Yes`.
- The dialog box disappears.
- A dialog box showing the progress of the saving is displayed (can be stealthy with small files).
- A toast indicating `Save success.` is displayed.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.
