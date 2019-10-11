# Redis Enterprise for PCF Java Sample App

This project demonstrates how to use Redis for PCF to create a Redis Enterprise service instance and connect to it without using Spring Cloud Connectors. If you are building a Spring Boot app you may wish to investigate that approach here: https://cloud.spring.io/spring-cloud-connectors/

Cloud Foundry sets certain configuration values such as service dependency connection information in an environment variable called VCAP_SERVICES inside the container where your app is running. This sample app parses VCAP_SERVICES to pull out the necessary connection information from there and then connects to Redis if it can.

This app needs to be pushed to Cloud Foundry. It won't work on your local machine.

### How to deploy
##### Create the Redis Enterprise service instance
First, you need to create a Redis Enterprise service instance. You'll need to determine what the Redis Enterprise service is called in your PCF installation's service marketplace. To do this, do "cf marketplace". It's likely that you'll encounter one or more of the following three values:
* <code>redislabs</code>: this is the Redis Enterprise service provided by Redis Labs to people using Pivotal Web Services. It provide HA, durable, and geo-redundant Redis services.
* <code>rediscloud</code>: this is the Redis service provided by Redis Labs to people using Pivotal Web Services. 
* <code>p.redis</code>: this is the new on-demand-brokered Redis for PCF service (provided by pivotal). This service provides a single shard non-HA Redis service.

Once you've determined the name of the service and the name of the plan, create an instance. For example, do <br><code>cf create-service redislabs small-redis my-sample-redis</code><br> or <br><code>cf create-service redislabs small-redis my-sample-redis</code><br>


##### Clone, build, and push
Clone this repo by doing <br> <code>git clone https://github.com/Redislabs-Solution-Architects/RedisForPCF-Java-Example.git</code>

Build the project by doing<br><code>mvn package</code>

Push to PCF by doing<br><code>cf push</code>

##### Try app in unbound state
If the above steps were successful, the app will be running on Cloud Foundry but the Redis service instance you created in the first step is not yet bound to the app. In this state, you can try the app's endpoints (detailed below) to see how things work in this state. As you have not bound Redis, you will not be able to see any service credentials or write any keys to the store.

##### Bind the service instance
Binding a service instance to an app tells that app how to connect to the service instance. To bind your service instance to your app (assuming you used the same app and service instance names; if not, change as appropriate), do<br><code>cf bind-service redisexample my-sample-redis</code>. <br> Once this is done, restart your app. This is necessary because changes in the container environment (such as the addition of the Redis service instance connection credentials) are not reflected inside the Java process environment until it is restarted.<br><code>cf restart redisexample</code>

##### Try app in bound state
Now you can test out the app and ensure that it can retrieve connection info, connect to your Redis service instance, and write/read keys.

### Endpoints to try
##### /
If you visit https://redisexample.yourappsdomain.example.com/ you should see information about your Redis service instance. This method does not attempt to connect to the Redis service instance. If no service is bound, you will see an error message.

##### /set?kn=KEYNAME&kv=KEYVALUE
The method at https://redisexample.yourappsdomain.example.com/set will set a key in your bound Redis service instance. Make a <code>GET</code> request; it takes two params supplied on the query string. <code>kn</code> should be the name of the key you want to set and <code>kv</code> should be the value of the key you want to set. On success, it returns a success message. If no service is bound, you will see an error message.

##### /get?kn=KEYNAME
The method at https://redisexample.yourappsdomain.example.com/get will get a key in your bound Redis service instance. Make a <code>GET</code> request; it takes one param supplied on the query string. <code>kn</code> should be the name of the key you want to get the value for. On success, the value of <code>kn</code> will be returned. If no service is bound, you will see an error message.

