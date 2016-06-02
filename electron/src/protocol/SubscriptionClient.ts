///<reference path="../../typings/globals/websocket/index.d.ts"/>

import * as Collections from 'typescript-collections';
import {client as WebSocketClient} from "websocket";
import {IMessage} from "websocket";
import {connection} from "websocket";
import {client} from "websocket";
import {SerializationHelper} from "./SerializationHelper";
import {ModelStructure} from "./ModelStructure";
import {Message} from "./Message";
import {Request} from "./Request";
import {Subscription} from "./Subscription";
import {UpdateValue} from "./UpdateValue";

export class SubscriptionClient {
    private wsClient: WebSocketClient;
    private wsConnection: connection;
    private onConnectCb: Function;
    private uri: string;
    private subscriptionMap = new Collections.Dictionary<string, Function>();
    private modelResponseQueue = new Collections.PriorityQueue<any>();

    constructor() {
        this.wsClient = new WebSocketClient();
        this.wsClient.on('connectFailed', this.onConnectFailed.bind(this));
        this.wsClient.on('connect', this.onConnect.bind(this));
    }

    setOnConnect(callback: Function) {
        this.onConnectCb = callback;
    }

    async connect(uri: string) {
        this.wsClient.connect(uri);
        this.uri = uri;

        return new Promise(resolve => this.wsClient.on('connect', resolve));
    }

    onConnect(conn: connection) {
        console.log('WebSocket Client Connected');

        // Setup callbacks
        conn.on('error', this.onError.bind(this));
        conn.on('close', this.onClose.bind(this));
        conn.on('message', this.onMessage.bind(this));

        // Setup periodic ping
        setInterval(() => {this.sendMessage("Ping")},10000);

        if (conn.connected) {
            this.wsConnection = conn;

            if(this.onConnectCb != null) {
                this.onConnectCb();
            }
        }
    }

    onConnectFailed(error: Error) {
        console.log('Connect Error: ' + error.toString());
        // Retry
        this.wsClient.connect(this.uri);
    }

    onError(error: Error) {
        this.wsConnection = null;
        console.log("Error: " + error.toString());
    }

    onClose() {
        this.wsConnection = null;
        console.log('WebSocket Connection Closed');
    }

    onMessage(msg: IMessage) {
        if (msg.type === 'utf8') {
            console.log("Received message: '" + msg.utf8Data + "'");
            var parsed = JSON.parse(msg.utf8Data);

            if(parsed.type === ModelStructure.messageType) {
                this.modelResponseQueue.enqueue(parsed);
            }
            if(parsed.type === UpdateValue.messageType) {
                this.handleUpdateValue(
                    SerializationHelper
                        .toInstanceObj(new UpdateValue(), parsed.data));
            }
        }
    }

    sendMessage(msg: string) {
        if(this.wsConnection != null && this.wsConnection.connected) {
            console.log(msg);
            this.wsConnection.sendUTF(msg);
        }
    }

    async getModelInfo() : Promise<ModelStructure> {
        let msg = new Message<Request>();
        msg.type = Request.messageType;
        msg.data = new Request();
        msg.data.request = Request.GET_MODEL_INFO;
        var queue = this.modelResponseQueue;

        this.sendMessage(JSON.stringify(msg));

        //noinspection TypeScriptValidateTypes
        return new Promise<ModelStructure>(resolve => {
            var id = setInterval(intv, 100);

            function intv() {
                var model = queue.dequeue();
                if (model !== undefined) {
                    clearInterval(id);
                    resolve(SerializationHelper.toInstanceObj(new ModelStructure(), model.data));
                }
            }
        });
    }

    runModel() {
        let msg = new Message<Request>();
        msg.type = Request.messageType;
        msg.data = new Request();
        msg.data.request = Request.RUN_MODEL;

        this.sendMessage(JSON.stringify(msg));
    }

    subscribe(variable: string, callback: Function) {
        let msg = new Message<Subscription>();
        msg.type = Subscription.messageType;
        msg.data = new Subscription();
        msg.data.variableName = variable;

        this.sendMessage(JSON.stringify(msg));
        this.subscriptionMap.setValue(variable, callback);
    }

    handleUpdateValue(val: UpdateValue) {
        let callback : Function = this.subscriptionMap.getValue(val.variableName);

        if(callback != null) {
            callback(val);
        }
    }
}