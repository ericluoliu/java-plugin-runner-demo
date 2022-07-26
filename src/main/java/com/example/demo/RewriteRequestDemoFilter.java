package com.example.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.filter.PluginFilter;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
import org.springframework.stereotype.Component;

@Component
public class RewriteRequestDemoFilter implements
        PluginFilter {

    @Override
    public String name() {
        /* It is recommended to keep the name of the filter the same as the class name.
         Configure the filter to be executed on apisix's routes in the following format
         curl http://127.0.0.1:9080/apisix/admin/routes/1 -H 'X-API-KEY: edd1c9f034335f136f87ad84b625c8f1' -X PUT -d '
        {
            "uri": "/*",
            "plugins": {
                "ext-plugin-pre-req": {
                    "conf": [{
                        "name": "RewriteRequestDemoFilter",
                        "value": "{\"rewrite_path\":\"\", \"conf_header_name\":\"joe\", \"conf_header_value\":\"1\", \"conf_arg_name\":\"george\", \"conf_arg_value\":\"2\"}"
                    }]
                }
            },
            "upstream": {
                "nodes": {
                    "127.0.0.1:8081": 1
                },
                "type": "roundrobin"
            }

        }'



        {
            "uri": "/*",
            "plugins": {
                "ext-plugin-pre-req": {
                    "conf": [{
                        "name": "RewriteRequestDemoFilter",
                        "value": "{\"rewrite_path\":\"bob\", \"conf_header_name\":\"joe\", \"conf_header_value\":\"1\", \"conf_arg_name\":\"george\", \"conf_arg_value\":\"2\"}"
                    }]
                }
            },
            "upstream": {
                "nodes": {
                    "127.0.0.1:8080": 1
                },
                "type": "roundrobin"
            }

        }

        {
            "uri": "/hello",
            "plugins": {
                "ext-plugin-pre-req": {
                    "conf": [{
                        "name": "RewriteRequestDemoFilter",
                        "value": "bar"
                    }]
                }
            },
            "upstream": {
                "nodes": {
                    "127.0.0.1:1980": 1
                },
                "type": "roundrobin"
            }
        }

        The value of name in the configuration corresponds to the value of return here.
         */

        return "RewriteRequestDemoFilter";
    }

    @Override
    public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
        /*
         * If the conf you configured is of type json, you can convert it to Map or json.
         */
        System.out.println("BRUH123");
        String configStr = request.getConfig(this);
        Gson gson = new Gson();
        Map<String, Object> conf = new HashMap<>();
        conf = gson.fromJson(configStr, conf.getClass());

        /*
         * You can use the parameters in the configuration.
         */

        // note: the path to the rewrite must start with '/'
        request.setPath((String) conf.get("rewrite_path"));
        request.setHeader((String) conf.get("conf_header_name"), (String) conf.get("conf_header_value"));
        /* note: The value of the parameter is currently a string type.
                 If you need the json type, you need the upstream service to parse the string value to json.
                 For example, if the arg is set as below
                 request.setArg("new arg", "{\"key1\":\"value1\",\"key2\":2}");
                 The arg received by the upstream service will be as below
                 "new arg": "{\"key1\":\"value1\",\"key2\":2}"
         */
        request.setArg((String) conf.get("conf_arg_name"), (String) conf.get("conf_arg_value"));

        /*
         * You can fetch the Nginx variables, and the request body
         */
        String remoteAddr = request.getVars("remote_addr");
        String serverPort = request.getVars("server_port");
        String body = request.getBody();

        System.out.println("BRUH");

        chain.filter(request, response);
    }

    /**
     * If you need to fetch some Nginx variables in the current plugin, you will need to declare them in this function.
     * @return a list of Nginx variables that need to be called in this plugin
     */
    @Override
    public List<String> requiredVars() {
        List<String> vars = new ArrayList<>();
        vars.add("remote_addr");
        vars.add("server_port");
        return vars;
    }

    /**
     * If you need to fetch request body in the current plugin, you will need to return true in this function.
     */
    @Override
    public Boolean requiredBody() {
        return true;
    }
}