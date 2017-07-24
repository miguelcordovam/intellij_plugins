Show Rest Services Plugin
--------------------------

This plugin will create a tool window with all the REST services found in your project. 
It will search in all classes that are @Controller and @RestController annotated.
Just click on Tools > Show Rest Services and you will get the results. 
Then you can just click on the services and it will take you to the method in the Editor.

Features
---------
- Right-click any service and select one of two options: 'Copy REST Url' or 'Copy cURL'. If method is GET, the url will include the query string ( ?param1=val1&param2=val2...)


Planned Features
----------------
- Export REST services on different formats such  as html, csv, txt
- Update the tree when services are commented out or deleted.

