package org.overturetool.plotting.server;

import com.google.gson.Gson;
import org.junit.Test;
import org.overture.interpreter.values.Value;
import org.overturetool.plotting.RunModel;
import org.overturetool.plotting.client.SubscriptionClient;
import org.overturetool.plotting.handlers.RequestHandler;
import org.overturetool.plotting.interpreter.TempoRemoteControl;
import org.overturetool.plotting.protocol.Message;
import org.overturetool.plotting.protocol.Request;
import org.overturetool.plotting.protocol.Subscription;

import java.io.File;
import java.util.concurrent.Semaphore;

public class SubscriptionServiceTest {

    SubscriptionService svc;
    SubscriptionClient client = new SubscriptionClient();
    String dest = "ws://localhost:8080/subscription";
    Gson gson = new Gson();

    @Test
    public void testServerTempoRemoteCtrlRequest() throws Exception {
        Semaphore sem = new Semaphore(1);
        sem.acquire();
        final TempoRemoteControl remote = new TempoRemoteControl(controller -> sem.release());
        Thread t = new Thread(() -> {
            try
            {
                RunModel.runWithRemoteConsole(new File("src/test/resources/test3".replace('/', File.separatorChar)), remote);

            } catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();

        // Wait for interpreter initialization
        sem.acquire();

        // Send request
        Message<Request> msg = new Message<Request>();
        msg.data = new Request("GetModelInfo");
        msg.type = Request.messageType;
        String serialized = gson.toJson(msg);

        client.connect(dest);
        client.sendMessage(serialized);

        // Wait to receive message
        Thread.sleep(1000);
    }

    @Test
    public void testServerTempoRemoteCtrlSubscription() throws Exception {
        // Initialize semaphore
        Semaphore sem = new Semaphore(1);
        sem.acquire();

        final TempoRemoteControl remote = new TempoRemoteControl(controller -> sem.release());
        Thread t = new Thread(() -> {
            try
            {
                RunModel.runWithRemoteConsole(new File("src/test/resources/test3".replace('/', File.separatorChar)), remote);

            } catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();

        // Wait for interpreter initialization
        sem.acquire();

        // Send subscription
        Message<Subscription> msg = new Message();
        msg.type = Subscription.messageType;
        msg.data = new Subscription();
        msg.data.variableName = "var_real";
        String serialized = gson.toJson(msg);

        client.connect(dest);
        client.sendMessage(serialized);

        Thread.sleep(1000);

        // Start model
        Message<Request> rq = new Message<>();
        rq.type = Request.messageType;
        rq.data = new Request();
        rq.data.request = Request.RUN_MODEL;
        serialized = gson.toJson(rq);
        client.sendMessage(serialized);

        // Wait to receive message
        Thread.sleep(1000);
    }
}