---
testspace:
title: Partial file opening
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Open a file using the partial opening feature.

## Button
- Open a file by clicking on the `PARTIAL FILE OPENING` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A new activity is displayed with the following fields:
   - `File size`: The total size of the file (static).
   - `Size of the part to open`: The part of the file to open, this field is calculated dynamically (depends on the `Start` and `End` values).
   - `Unit`: The unit to use as a representation of the `Start` and `End` fields.
   - `Input type`: The type of input to use as a representation of the `Start` and `End` fields.
      - When you change this value, the `Start` and `End` fields are modified.
   - `Comment field`: A simple comment.
   - `Start`: The start offset.
   - `End`: The end offset.
- After validation :
   - A dialog box showing the loading progress is displayed.
   - The content of the selected file is displayed.

## Menu
- Open a file by clicking on the `Partial file opening...` menu in the contextual menu.
- The main view disappears.
- A file picker appears.
- Select a file.
- A new activity is displayed with the following fields:
   - `File size`: The total size of the file (static).
   - `Size of the part to open`: The part of the file to open, this field is calculated dynamically (depends on the `Start` and `End` values).
   - `Unit`: The unit to use as a representation of the `Start` and `End` fields.
   - `Input type`: The type of input to use as a representation of the `Start` and `End` fields.
      - When you change this value, the `Start` and `End` fields are modified.
   - `Comment field`: A simple comment.
   - `Start`: The start offset.
   - `End`: The end offset.
- After validation :
   - A dialog box showing the loading progress is displayed.
   - The content of the selected file is displayed.
   
## Open when a file is already open (no change)
- Open a file by clicking on the `Partial file opening...` menu in the contextual menu OR by clicking on the `PARTIAL FILE OPENING` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A new activity is displayed with the following fields:
   - `File size`: The total size of the file (static).
   - `Size of the part to open`: The part of the file to open, this field is calculated dynamically (depends on the `Start` and `End` values).
   - `Unit`: The unit to use as a representation of the `Start` and `End` fields.
   - `Input type`: The type of input to use as a representation of the `Start` and `End` fields.
      - When you change this value, the `Start` and `End` fields are modified.
   - `Comment field`: A simple comment.
   - `Start`: The start offset.
   - `End`: The end offset.
- After validation :
   - A dialog box showing the loading progress is displayed.
   - The content of the selected file is displayed.
- Open a file by clicking on the `Open...` menu in the contextual menu.
- The main view disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.

## Open when a file is already open (With changes (Cancel))
- Open a file by clicking on the `Partial file opening...` menu in the contextual menu OR by clicking on the `PARTIAL FILE OPENING` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A new activity is displayed with the following fields:
   - `File size`: The total size of the file (static).
   - `Size of the part to open`: The part of the file to open, this field is calculated dynamically (depends on the `Start` and `End` values).
   - `Unit`: The unit to use as a representation of the `Start` and `End` fields.
   - `Input type`: The type of input to use as a representation of the `Start` and `End` fields.
      - When you change this value, the `Start` and `End` fields are modified.
   - `Comment field`: A simple comment.
   - `Start`: The start offset.
   - `End`: The end offset.
- After validation :
   - A dialog box showing the loading progress is displayed.
   - The content of the selected file is displayed.
- Click on a line.
- A new window appears.
- Check the `Plain text input` box.
- Check the `Insert mode` box.
- In the bottom area, insert a text (in hexadecimal).
- Place the cursor at the beginning of the line and insert one character.
- The new character(s) will replace the old one(s).
- Validate the changes by clicking on the ticker at the top of the window.
- In the `Hex view`, the modified line(s) will appear in red.
- Open a file by clicking on the `Open...` menu in the contextual menu.
- A dialog box appears, prompting the user to save the changes.
- Click on `Cancel`.
- The dialog box disappears and nothing changes.

## Open when a file is already open (With changes (No))
- Open a file by clicking on the `Partial file opening...` menu in the contextual menu OR by clicking on the `PARTIAL FILE OPENING` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A new activity is displayed with the following fields:
   - `File size`: The total size of the file (static).
   - `Size of the part to open`: The part of the file to open, this field is calculated dynamically (depends on the `Start` and `End` values).
   - `Unit`: The unit to use as a representation of the `Start` and `End` fields.
   - `Input type`: The type of input to use as a representation of the `Start` and `End` fields.
      - When you change this value, the `Start` and `End` fields are modified.
   - `Comment field`: A simple comment.
   - `Start`: The start offset.
   - `End`: The end offset.
- After validation :
   - A dialog box showing the loading progress is displayed.
   - The content of the selected file is displayed.
- Click on a line.
- A new window appears.
- Check the `Plain text input` box.
- Check the `Insert mode` box.
- In the bottom area, insert a text (in hexadecimal).
- Place the cursor at the beginning of the line and insert one character.
- The new character(s) will replace the old one(s).
- Validate the changes by clicking on the ticker at the top of the window.
- In the `Hex view`, the modified line(s) will appear in red.
- Open a file by clicking on the `Open...` menu in the contextual menu.
- A dialog box appears, prompting the user to save the changes.
- Click on `No`.
- The dialog box disappears.
- A file picker appears.
- Select a file.
- A dialog box showing the loading progress is displayed.
- The contents of the selected file are displayed.

## Open when a file is already open (With changes (Yes))
- Open a file by clicking on the `Partial file opening...` menu in the contextual menu OR by clicking on the `PARTIAL FILE OPENING` button in the home view.
- The main view disappears.
- A file picker appears.
- Select a file.
- A new activity is displayed with the following fields:
   - `File size`: The total size of the file (static).
   - `Size of the part to open`: The part of the file to open, this field is calculated dynamically (depends on the `Start` and `End` values).
   - `Unit`: The unit to use as a representation of the `Start` and `End` fields.
   - `Input type`: The type of input to use as a representation of the `Start` and `End` fields.
      - When you change this value, the `Start` and `End` fields are modified.
   - `Comment field`: A simple comment.
   - `Start`: The start offset.
   - `End`: The end offset.
- After validation :
   - A dialog box showing the loading progress is displayed.
   - The content of the selected file is displayed.
- Click on a line.
- A new window appears.
- Check the `Plain text input` box.
- Check the `Insert mode` box.
- In the bottom area, insert a text (in hexadecimal).
- Place the cursor at the beginning of the line and insert one character.
- The new character(s) will replace the old one(s).
- Validate the changes by clicking on the ticker at the top of the window.
- In the `Hex view`, the modified line(s) will appear in red.
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