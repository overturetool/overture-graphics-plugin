/**
 * Created by John on 30-05-2016.
 */
import * as Collections from 'typescript-collections';
import {Point} from "./Point";
import {Points} from "./Points";


export class Plot2D {
    private _count: number = 0;
    private _data: Points = new Points();
    private _shown: boolean = false;
    private _changed: (string) => void;
    private _title: string;

    constructor() {
        this._data.type = "scatter";
    }

    addPoint(value: number) {
        this._data.addPoint({x: this._count++, y: value, z: 0});

        if(this._shown && this._changed != null) {
            this._changed(this.title);
        }
    }

    get data():Points {
        return this._data;
    }

    get shown():boolean {
        return this._shown;
    }

    set shown(value:boolean) {
        this._shown = value;
    }

    set changed(value : (string) => void) {
        this._changed = value;
    }

    get title():string {
        return this._title;
    }

    set title(value:string) {
        this._title = value;
    }
}