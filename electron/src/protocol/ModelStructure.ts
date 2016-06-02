/**
 * Created by John on 31-05-2016.
 */
import {Node} from "./Node";

export class ModelStructure extends Node {
    static messageType: string = "MODEL";
    rootClass: string;

    getAllVariables() : Array<string> {
        var res = new Array<string>();
        var loop = new Array<Node>();
        this.children.forEach(n => loop.push(n));

        while (loop.length > 0) {
            let n : Node = loop.pop();
            if(n === undefined)
                break;

            n.children.forEach(a => loop.push(a));
            res.push(n.name);
        }

        return res;
    }
}