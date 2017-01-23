Code Inspection example

Inspects code looking for a condition like this:

for (int i = 0 ; 10 > i; i++) {
}

This inspections changes the condition from 10 > i to i < 10, which is more readable.
