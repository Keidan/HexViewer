---
testspace:
title: Editing a line
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Editing a line.

## Simple edition without insert mode
- Follow the instructions specified in the `Hex view` test.
- Click on a line.
- A new window appears.
- Uncheck the following boxes:
   - `Plain text input`.
   - `Insert mode`.
- In the bottom area, insert one or more characters (in hexadecimal).
- The `Result` list is updated with the inserted text (in hexadecimal).
- Validate the changes by clicking on the ticker at the top of the window.
- If the file is opened in partial mode, an error message will appear, otherwise the window will close.
- In the `Hex view`, the modified line(s) will appear in red.

Note: If you validate an empty text line, the line(s) will be deleted.

## Simple edition with insert mode
- Follow the instructions specified in the `Hex view` test.
- Click on a line.
- A new window appears.
- Uncheck the `Plain text input` box.
- Check the `Insert mode` box.
- In the bottom area, insert a text (in hexadecimal).
- Place the cursor at the beginning of the line and insert one or more characters (in hexadecimal).
- The new character(s) will replace the old one(s).
- Place the cursor at the end of the line and insert one or more characters (in hexadecimal).
- The new character(s) will be inserted.
- Place the cursor somewhere on the line between two continuous characters and insert one or more characters (in hexadecimal).
- The new character(s) will be inserted.
- The `Result` list is updated with the inserted text (in hexadecimal).
- Validate the changes by clicking on the ticker at the top of the window.
- If the file is opened in partial mode, and you perform the steps that consist in inserting one or more characters at the end of a line, an error message will appear, otherwise the window will close.
- In the `Hex view`, the modified line(s) will appear in red.

Note: If you validate an empty text line, the line(s) will be deleted.

## Plain text edition without insert mode
- Follow the instructions specified in the `Hex view` test.
- Click on a line.
- A new window appears.
- Check the `Plain text input` box.
- Uncheck the `Insert mode` box.
- In the bottom area, insert a text.
- The new character(s) will be inserted by their hexadecimal representation(s).
- The `Result` list is updated with the inserted text (in hexadecimal).
- Validate the changes by clicking on the ticker at the top of the window.
- If the file is opened in partial mode, an error message will appear, otherwise the window will close.
- In the `Hex view`, the modified line(s) will appear in red.

Note: If you validate an empty text line, the line(s) will be deleted.

## Plain text edition with insert mode
- Follow the instructions specified in the `Hex view` test.
- Click on a line.
- A new window appears.
- Check the `Plain text input` box.
- Check the `Insert mode` box.
- In the bottom area, insert a text.
- Place the cursor at the beginning of the line and insert one or more characters.
- The new character(s) will replace the old one(s) by their hexadecimal representation(s).
- Place the cursor at the end of the line and insert one or more characters (in hexadecimal).
- The new character(s) will replace the old one(s) by their hexadecimal representation(s).
- Place the cursor somewhere on the line between two continuous characters and insert one or more characters (in hexadecimal).
- The new character(s) will replace the old one(s) by their hexadecimal representation(s).
- The `Result` list is updated with the inserted text (in hexadecimal).
- Validate the changes by clicking on the ticker at the top of the window.
- If the file is opened in partial mode, and you perform the steps that consist in inserting one or more characters at the end of a line, an error message will appear, otherwise the window will close.
- In the `Hex view`, the modified line(s) will appear in red.

Note: If you validate an empty text line, the line(s) will be deleted.