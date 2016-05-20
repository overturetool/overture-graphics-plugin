///<reference path="../../typings/globals/websocket/index.d.ts"/>

import {client as WebSocketClient} from "websocket";
import {IMessage} from "websocket";
import {connection} from "websocket";
import {client} from "websocket";

export class SubscriptionClient {
    private wsClient: WebSocketClient
    private wsConnection: connection

    constructor() {
        this.wsClient = new WebSocketClient();
        this.wsClient.on('connectFailed', this.onConnectFailed.bind(this));
        this.wsClient.on('connect', this.onConnect.bind(this));
    }

    connect(uri: string) {
        this.wsClient.connect(uri);
    }

    onConnect(conn: connection) {
        console.log('WebSocket Client Connected');

        // Setup callbacks
        conn.on('error', this.onError.bind(this));
        conn.on('close', this.onClose.bind(this));
        conn.on('message', this.onMessage.bind(this));

        if (conn.connected) {
            this.wsConnection = conn;
            this.sendMessage();
        }
    }

    onConnectFailed(error: Error) {
        console.log('Connect Error: ' + error.toString());
    }

    onError(error: Error) {
        this.wsConnection = null;
        console.log("Connection Error: " + error.toString());
    }

    onClose() {
        this.wsConnection = null;
        console.log('WebSocket Connection Closed');
    }

    onMessage(msg: IMessage) {
        if (msg.type === 'utf8') {
            console.log("Received message: '" + msg.utf8Data + "'");
            console.log(JSON.parse(msg.utf8Data));
        }
    }

    sendMessage() {
        if(this.wsConnection != null && this.wsConnection.connected) {
            console.log('Send request: {"type": "REQ","data": {"request":"GetModelInfo"}}');
            this.wsConnection.sendUTF('{"type": "REQ","data": {"request":"GetModelInfo"}}');
        }
    }
}