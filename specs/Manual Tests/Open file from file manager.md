---
testspace:
title: Open file from a file manager
sizes: 0B, ~100kb, ~500kb, ~1Mb, ~5Mb, ~10Mb, ~15Mb
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Open a file from a file manager.\
**<ins>IMPORTANT:</ins> Do the opening test with sizes of {{ spec.sizes }}.**

## Test
- Open a file from a file manager and select HexViewer to edit the file.
   - **<ins>IMPORTANT:</ins> Repeat the operation with a file of {{ spec.sizes }}.**
- The application starts with an activity that displays the following fields:
   - `File size`: The total size of the file (static).
   - `Size of the part to open`: The part of the file to open, this field is calculated dynamically (depends on the `Start` and `End` values).
   - `Unit`: The unit to use as a representation of the `Start` and `End` fields.
   - `Input type`: The type of input to use as a representation of the `Start` and `End` fields.
      - When you change this value, the `Start` and `End` fields are modified.
   - `Comment field`: A simple comment.
   - `Start`: The start offset.
   - `End`: The end offset.- After validation :
- A dialog box showing the loading progress is displayed.
- The content of the selected file is displayed.
- The `Save` menu is disabled.

**Note: If the open area is the entire file, the opening of the file is considered a normal opening (not a partial opening).**
**Note 2: If the file to be opened is empty, the application will open this file WITHOUT the partial view but the `Save` menu will be disabled.**
