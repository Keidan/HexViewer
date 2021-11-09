---
testspace:
title: Hexadecimal view
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Display of hexadecimal text content.

## Without line numbers
- Follow the instructions specified in the `Open a file` or `Partial file opening` test.
- Uncheck the `Plain text` menu box.
- Uncheck the `Line numbers` menu box.
- The view will display the text in hexadecimal.

## With line numbers
- Follow the instructions specified in the `Open a file` or `Partial file opening` test.
- Uncheck the `Plain text` menu box.
- Check the `Line numbers` menu box.
- The view will display the text in hexadecimal with the byte address (rows and columns).
