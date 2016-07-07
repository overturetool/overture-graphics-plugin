/**
 * Created by John on 05-07-2016.
 */
import * as Collections from 'typescript-collections';

export class SubscriptionMap {
    private subscriptionMap = new Collections.Dictionary<string, Collections.Dictionary<string, Function>>();


    addSubscription(variable: string, subscriber: string, callback: Function) {
        var subscriptionsForVar = this.subscriptionMap.getValue(variable);

        if (subscriptionsForVar !== undefined) {
            subscriptionsForVar.setValue(subscriber, callback);
        }
        else {
            subscriptionsForVar = new Collections.Dictionary<string, Function>();
            subscriptionsForVar.setValue(subscriber, callback);
            this.subscriptionMap.setValue(variable, subscriptionsForVar)
        }
    }

    getSubscriptions(variable: string): Collections.Dictionary<string, Function> {
        return this.subscriptionMap.getValue(variable);
    }

    removeSubscription(variable: string, subscriber: string) {
        var subscriptionsForVar = this.subscriptionMap.getValue(variable);

        if (subscriptionsForVar !== undefined) {
            subscriptionsForVar.remove(subscriber);
        }
    }
}