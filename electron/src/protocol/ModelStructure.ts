/**
 * Created by John on 31-05-2016.
 */
import {Node} from "./Node";

export class ModelStructure extends Node {
    static messageType: string = "MODEL";
    rootClass: string;
    primitives = ['bool', 
            'real', 
            'nat1', 
            'nat', 
            'int', 
            'rat'];
    listTypes = ['seq of (bool)',
            'seq of (real)', 
            'seq of (nat1)', 
            'seq of (nat)', 
            'seq of (int)', 
            'seq of (rat)',
            'set of (bool)',
            'set of (real)', 
            'set of (nat1)', 
            'set of (nat)', 
            'set of (int)', 
            'set of (rat)'];

    getAllVariables(): Array<{text: string, value: string}> {
        let acceptedTypes = this.listTypes.concat(this.primitives);
        var res = new Array<{text: string, value: string}>();
        var loop = new Array<Node>();
        this.children.forEach(n => loop.push(n));

        while (loop.length > 0) {
            let n: Node = loop.pop();
            if (n === undefined)
                break;

            n.children.forEach(a => loop.push(a));
            
            if(acceptedTypes.find((v: string) => v === n.type) !== undefined) {
                res.push({text: n.name + ": " + n.type, value: n.name});
            }
        }

        return res.sort((a,b) => {
            if(a.value.toUpperCase() > b.value.toUpperCase()) {
                return 1
            }
            if(a.value.toUpperCase() < b.value.toUpperCase()) {
                return -1
            }
            return 0;
        });
    }

    isListType(variable: string) {
        return this.listTypes.find((v: string) => variable.indexOf(v.substr(0,6)) != -1) !== undefined;
    }
}