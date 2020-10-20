import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class WebSocketListener extends WebSocketAdapter {

    // Initialize Logger
    private static Logger LOGGER = null;
    static {
        InputStream stream = SpeedAI.class.getClassLoader().
                getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            LOGGER= Logger.getLogger(SpeedAI.class.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onTextMessage(WebSocket websocket, String message) {
        SpeedAI.handleMessage(websocket, message);
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws WebSocketException {
        if (exception.getMessage().contains("427")) {
            LOGGER.log(Level.SEVERE, "Already connected");
            SpeedAI.shutdown(427);
        } else {
            throw exception;
        }
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        // TODO Handle Connect
    }
}
