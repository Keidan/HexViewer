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
      - When you change this value, the `Start` and `End` fields will not be changed.
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
      - When you change this value, the `Start` and `End` fields will not be changed.
   - `Comment field`: A simple comment.
   - `Start`: The start offset.
   - `End`: The end offset.
- After validation :
   - A dialog box showing the loading progress is displayed.
   - The content of the selected file is displayed.