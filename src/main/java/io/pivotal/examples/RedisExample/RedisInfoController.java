
package io.pivotal.examples.RedisExample;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;




@RestController
public class RedisInfoController {

    private Logger LOG = Logger.getLogger(RedisInfoController.class.getName());
    private Jedis jedis = null;

    @RequestMapping("/ups")
    public String getUserProvidedServiceInfo() {
        String vcap = System.getenv("VCAP_SERVICES");
        LOG.log(Level.WARNING, "VCAP_SERVICES content: " + vcap);


        // now we parse the json in VCAP_SERVICES
        LOG.log(Level.WARNING, "Using GSON to parse the json...");
        JsonElement root = new JsonParser().parse(vcap);
        JsonObject ups = null;
        if (root != null) {
            if (root.getAsJsonObject().has("user-provided")) {
                ups = root.getAsJsonObject().get("user-provided").getAsJsonArray().get(0).getAsJsonObject();
                LOG.log(Level.WARNING, "instance name: " + ups.get("name").getAsString());
            }
            else {
                LOG.log(Level.SEVERE, "ERROR: no redis instance found in VCAP_SERVICES");
            }
        }

        if (ups != null) {
            JsonObject creds = ups.get("credentials").getAsJsonObject();
            return ups.get("name").getAsString() + " / " + creds.get("uri").getAsString() + " / " + creds.get("user").getAsString();
        }
        else return "not found!";
    }

    @RequestMapping("/")
    public RedisInstanceInfo getInfo() {
        LOG.log(Level.WARNING, "Getting Redis Instance Info in Spring controller...");
        // first we need to get the value of VCAP_SERVICES, the environment variable
        // where connection info is stored
        String vcap = System.getenv("VCAP_SERVICES");
        LOG.log(Level.WARNING, "VCAP_SERVICES content: " + vcap);


        // now we parse the json in VCAP_SERVICES
        LOG.log(Level.WARNING, "Using GSON to parse the json...");
        JsonElement root = new JsonParser().parse(vcap);
        JsonObject redis = null;
        if (root != null) {
            if (root.getAsJsonObject().has("redislabs")) {
                redis = root.getAsJsonObject().get("redislabs").getAsJsonArray().get(0).getAsJsonObject();
                LOG.log(Level.WARNING, "instance name: " + redis.get("name").getAsString());
            } else if (root.getAsJsonObject().has("rediscloud")) {
                redis = root.getAsJsonObject().get("rediscloud").getAsJsonArray().get(0).getAsJsonObject();
                LOG.log(Level.WARNING, "instance name: " + redis.get("name").getAsString());
            } else if (root.getAsJsonObject().has("p.redis")) {
                redis = root.getAsJsonObject().get("p.redis").getAsJsonArray().get(0).getAsJsonObject();
                LOG.log(Level.WARNING, "instance name: " + redis.get("name").getAsString());
            }else if (root.getAsJsonObject().has("p-redis")) {
                redis = root.getAsJsonObject().get("p-redis").getAsJsonArray().get(0).getAsJsonObject();
                LOG.log(Level.WARNING, "instance name: " + redis.get("name").getAsString());
            }
            else {
                LOG.log(Level.SEVERE, "ERROR: no redis instance found in VCAP_SERVICES");
            }
        }

        // then we pull out the credentials block and produce the output
        if (redis != null) {
            JsonObject creds = redis.get("credentials").getAsJsonObject();
            RedisInstanceInfo info = new RedisInstanceInfo();
            // if you have DNS enabled:
            info.setHost(creds.get("host").getAsString());
            // if you don't have DNS enabled:
            //info.setHost(creds.getAsJsonArray("ip_list").get(0).getAsString());
            info.setPort(creds.get("port").getAsInt());
            info.setPassword(creds.get("password").getAsString());

            // the object will be json serialized automatically by Spring web - we just need to return it
            return info;
        }
        else return new RedisInstanceInfo();
    }

    @RequestMapping("/set")
    public String setKey(@RequestParam("kn") String key, @RequestParam("kv") String val) {
        LOG.log(Level.WARNING, "Called the key set method, going to set key: " + key + " to val: " + val);

        if (jedis == null || !jedis.isConnected()) {
            jedis = getJedisConnection();
        }
        jedis.set(key, val);

        return "Set key: " + key + " to value: " + val;
    }

    @RequestMapping("/get")
    String getKey(@RequestParam("kn") String key) {
        LOG.log(Level.WARNING, "Called the key get method, going to return val for key: " + key);

        if (jedis == null || !jedis.isConnected()) {
            jedis = getJedisConnection();
        }

        return jedis.get(key);
    }

    @RequestMapping("/kill") 
    void killServer() {
        LOG.log(Level.WARNING, "About to kill the service!");
        System.exit(0);
    }

    private Jedis getJedisConnection() {
        // get our connection info from VCAP_SERVICES
        RedisInstanceInfo info = getInfo();
        Jedis jedis = new Jedis(info.getHost(), info.getPort());
        
        // make the connection
        jedis.connect();

        // authorize with our password
        jedis.auth(info.getPassword());

        return jedis;
    }

}

