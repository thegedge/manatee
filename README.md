# Manatee

Manatee is a lightweight messaging system for Java. Annotations are used to simplify the process
of registering publishers and subscribers. Simply add an instance of a receiver and you're ready
to start sending messages:

```java
final Receiver receiver = new Receiver();
final Sender sender = new Sender();
final MessageDeliverySystem sys = MessageDeliverySystem.getInstance();
 
sys.addReceiver(receiver);
sys.sendMessage(sender, sender.MESSAGE1);
sys.sendMessage(sender, sender.MESSAGE2, "a message parameter", 42);
```

[A more extensive example](//github.com/thegedge/manatee/tree/master/examples/main/java/ca/gedge/manatee).

Manatee is intended to be used within an application that wants a simpler, more lightweight API
than that of the [Java Message Service](http://en.wikipedia.org/wiki/Java_Message_Service) API.
Manatee does not send messages _across the wire_.

# License

Distributed under the [MIT license](//github.com/thegedge/manatee/blob/master/LICENSE)
