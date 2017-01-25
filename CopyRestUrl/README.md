This plugin will help developers copy a url from a method inside a Spring Controller. For example, let's say we have this controller:

@Controller
@RequestMapping("/plugin/test/demo")
public class TestController {


    @RequestMapping (value = "/hello", method = RequestMethod.GET)
    @ResponseBody
    public Object hi (@RequestParam String name) {
        return "Hello " + name;
    }


    private String concat (String one, String two) {
        return one + two;
    }

}


If you right-click on "hi" method, you will see an option to "Copy REST url", and it will copy "/plugin/test/demo/hello" to the clipboard.

The plugin will only be available for methods with RequestMapping annotation.

Pending tasks:
--------------
   - Add option to include hosts, so that the url is complete and ready to paste into browser or Postman.
   - If it is GET method, add parameters as "?param1=X&param2=X..."
