import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class TempoClient extends WebSocketClient {

    public TempoClient( URI serverUri , Draft draft, Map<String,String> headers) {
        super( serverUri, draft, headers, 10 );
    }

    public TempoClient( URI serverUri , Draft draft ) {
        super( serverUri, draft );
    }

    public TempoClient( URI serverURI ) {
        super( serverURI );
    }

    @Override
    public void onOpen( ServerHandshake handshakedata ) {
        System.out.println( "opened connection" );
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient

        Random rand = new Random(12321);
        int x = 0,y = 0;
        while(true) {
            x--;
            y++;
            send(String.format("{\"x\": %d, \"y\": %d}", x, y%2));

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage( String message ) {
        System.out.println( "received: " + message );
    }

    @Override
    public void onClose( int code, String reason, boolean remote ) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) );
    }

    @Override
    public void onError( Exception ex ) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }

    public static void main( String[] args ) throws URISyntaxException {
        HashMap<String, String> headers = new HashMap<String, String>();
        TempoClient c = new TempoClient( new URI( "ws://localhost:8080/" ), new Draft_10(), new HashMap<String, String>()); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        c.connect();
    }

}