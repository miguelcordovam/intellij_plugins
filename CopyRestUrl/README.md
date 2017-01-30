This plugin will help developers copy a url from a method inside a Spring Controller. For example, let's say we have this controller:

    @Controller
    @RequestMapping("/plugin/test/demo")
    public class TestController {

        @RequestMapping (value = "/hello", method = RequestMethod.GET)
        @ResponseBody
        public Object helloWorld (@RequestParam String name) {
            return "Hello " + name;
        }


        private String concat (String one, String two) {
            return one + two;
        }

    }

Features:
---------
- If you right-click on "helloWorld" method, you will see an option to "Copy REST url", and it will copy "http://localhost:8080/plugin/test/demo/hello" to the clipboard.
- If your application is using Spring-Boot and you have an application.properties file, it will look for these 2 keys: server.port, server.contextPath. And if they are found, it will add those values to the final url.
- The plugin will only be available for methods with RequestMapping annotation.
- Works for any http method.
- For GET methods, it  will include query string on the url, like this: "?param1=X&param2=X..."

Pending tasks:
-------------- 
 - Add tests
