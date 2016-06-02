/**
 * Created by John on 30-05-2016.
 */

export class SerializationHelper {
    static toInstance<T>(obj: T, json: string): T {
        var jsonObj = JSON.parse(json);

        if (typeof (<any>obj)["fromJSON"] === "function") {
            (<any>obj)["fromJSON"](jsonObj);
        }
        else {
            for (var propName in jsonObj) {
                (<any>obj)[propName] = jsonObj[propName]
            }
        }

        return obj;
    }

    static toInstanceObj<T>(obj: T, jsonObj: any): T {
        if (typeof (<any>obj)["fromJSON"] === "function") {
            (<any>obj)["fromJSON"](jsonObj);
        }
        else {
            for (var propName in jsonObj) {
                (<any>obj)[propName] = jsonObj[propName]
            }
        }

        return obj;
    }
}