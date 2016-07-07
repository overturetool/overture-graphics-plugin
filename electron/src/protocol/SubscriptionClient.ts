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
import {Response} from "./Response";
import {SubscriptionMap} from "./SubscriptionMap";

export class SubscriptionClient {
    private wsClient: WebSocketClient;
    private wsConnection: connection;
    private onConnectCb: Function;
    private uri: string;
    private subscriptionMap = new SubscriptionMap();
    private responseQueue = new Collections.PriorityQueue<any>();
    private modelResponseQueue = new Collections.PriorityQueue<any>();
    private classInfoQueue = new Collections.PriorityQueue<any>();
    private functionInfoQueue = new Collections.PriorityQueue<any>();
    private runMethod: string;

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
        setInterval(() => { this.sendMessage("Ping") }, 10000);

        if (conn.connected) {
            this.wsConnection = conn;

            if (this.onConnectCb != null) {
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

            if (parsed.type === "RESPONSE") {
                this.responseQueue.enqueue(parsed);
            }
            if (parsed.type === Response.functionInfo) {
                this.functionInfoQueue.enqueue(parsed);
            }
            if (parsed.type === Response.classInfo) {
                this.classInfoQueue.enqueue(parsed);
            }
            if (parsed.type === Response.modelInfo) {
                this.modelResponseQueue.enqueue(parsed);
            }
            if (parsed.type === UpdateValue.messageType) {
                this.handleUpdateValue(
                    SerializationHelper
                        .toInstanceObj(new UpdateValue(), parsed.data));
            }
        }
    }

    sendMessage(msg: string) {
        if (this.wsConnection != null && this.wsConnection.connected) {
            console.log(msg);
            this.wsConnection.sendUTF(msg);
        }
    }

    setRunFunction(fct: string) {
        this.runMethod = fct;
    }

    async getModelInfo(): Promise<ModelStructure> {
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

    async getClassInfo(): Promise<Array<string>> {
        let msg = new Message<Request>();
        msg.type = Request.messageType;
        msg.data = new Request();
        msg.data.request = Request.GET_CLASS_INFO;
        var queue = this.classInfoQueue;

        this.sendMessage(JSON.stringify(msg));

        //noinspection TypeScriptValidateTypes
        return new Promise<Array<string>>(resolve => {
            var id = setInterval(intv, 100);

            function intv() {
                var model = queue.dequeue();
                if (model !== undefined) {
                    clearInterval(id);
                    resolve(<Array<string>>model.data);
                }
            }
        });
    }

    async getFunctionInfo(): Promise<Array<string>> {
        let msg = new Message<Request>();
        msg.type = Request.messageType;
        msg.data = new Request();
        msg.data.request = Request.GET_FUNCTION_INFO;
        var queue = this.functionInfoQueue;

        this.sendMessage(JSON.stringify(msg));

        //noinspection TypeScriptValidateTypes
        return new Promise<Array<string>>(resolve => {
            var id = setInterval(intv, 100);

            function intv() {
                var model = queue.dequeue();
                if (model !== undefined) {
                    clearInterval(id);
                    resolve(<Array<string>>model.data);
                }
            }
        });
    }

    async setRootClass(rootClass: string): Promise<string> {
        let msg = new Message<Request>();
        msg.type = Request.messageType;
        msg.data = new Request();
        msg.data.request = Request.SET_ROOT_CLASS;
        msg.data.parameter = rootClass;
        var queue = this.responseQueue;

        this.sendMessage(JSON.stringify(msg));

        //noinspection TypeScriptValidateTypes
        return new Promise<string>(resolve => {
            var id = setInterval(intv, 100);

            function intv() {
                var model = queue.dequeue();
                if (model !== undefined) {
                    clearInterval(id);
                    resolve(<string>model.data);
                }
            }
        });
    }

    runModel() {
        let msg = new Message<Request>();
        msg.type = Request.messageType;
        msg.data = new Request();
        msg.data.request = Request.RUN_MODEL;
        msg.data.parameter = this.runMethod;

        this.sendMessage(JSON.stringify(msg));
    }

    async subscribe(variable: string, subscriber: string, callback: Function) : Promise<string> {
        var self = this;

        if(this.subscriptionMap.getSubscriptions(variable) === undefined) {
            let msg = new Message<Subscription>();
            msg.type = Subscription.messageType;
            msg.data = new Subscription();
            msg.data.variableName = variable;

            this.sendMessage(JSON.stringify(msg));
            var queue = this.responseQueue;

            return new Promise<string>(resolve => {
                var id = setInterval(intv, 100);

                function intv() {
                    var model = queue.dequeue();
                    if (model !== undefined) {
                        clearInterval(id);
                        self.subscriptionMap.addSubscription(variable, subscriber, callback);
                        resolve(<string>model.data);
                    }
                }
            });
        }

        return new Promise<string>(resolve => {
                var id = setInterval(intv, 100);

                function intv() {
                    clearInterval(id);
                    self.subscriptionMap.addSubscription(variable, subscriber, callback);
                    resolve("OK");
                }
            });
    }

    unsubscribe(variable: string, subscriber: string) {
        this.subscriptionMap.removeSubscription(variable, subscriber);
    }

    handleUpdateValue(val: UpdateValue) {
        let callbacks = this.subscriptionMap.getSubscriptions(val.variableName);

        if (callbacks !== undefined) {
            for (let callback of callbacks.values()) {
                callback(val);
            }
        }
    }
}