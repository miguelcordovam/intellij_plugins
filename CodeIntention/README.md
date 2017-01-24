This intention will attempt to format an inline if-statement. For example, if you have something like this:

String result = "";
if (result.isEmpty()) {result = "Empty";} else {result = "Not Empty";}

This intention will transform it into a block if-statement. You need to place cursor on "if" and the intention will pop up.
