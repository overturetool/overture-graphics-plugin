/**
 * Created by John on 30-05-2016.
 */
import * as Collections from 'typescript-collections';
import {Point} from "./Point";
import {Points} from "./Points";
import {Guid} from "../utilities/Guid"

export class Plot {
    private _count: number = 0;
    private _data: Collections.Dictionary<string, Points> = new Collections.Dictionary<string, Points>();
    private _shown: boolean = false;
    private _changed: (plot: string) => void;
    private _title: string;
    private _type: PlotType;
    private _id: string;

    constructor(title: string, type: PlotType) {
        this._id = Guid.newGuid();
        this._title = title;
        this._type = type;
    }

    addPoint(variable: string, value: number) {
        var points: Points = this._data.getValue(variable);

        if (points !== undefined) {
            switch (this._type) {
                case PlotType.TwoDimensional:
                case PlotType.List:
                    points.addPoint({
                        x: points.y.length,
                        y: value,
                        z: points.zIndex
                    });
                    break;
                case PlotType.ThreeDimensional:
                    points.addPoint({
                        x: [points.zIndex, points.zIndex + 1],
                        y: [points.y.length, points.y.length],
                        z: [value, value]
                    });
                    break;
            }

            if (this._shown && this._changed != null) {
                this._changed(this._id);
            }
        }
    }

    addVariable(variable: string) {
        var points = new Points();
        points.name = variable;

        switch (this._type) {
            case PlotType.TwoDimensional:
            case PlotType.List:
                points.type = "scatter";
                points.zIndex = 0;
                break;
            case PlotType.ThreeDimensional:
                points.type = "surface";
                points.zIndex = this._data.size();
                points.addPoint({
                    x: [points.zIndex, points.zIndex + 1],
                    y: [0, 0],
                    z: [0, 0]
                });
                break;
        }

        this._data.setValue(variable, points);
    }

    get data(): Collections.Dictionary<string, Points> {
        return this._data;
    }

    get shown(): boolean {
        return this._shown;
    }

    set shown(value: boolean) {
        this._shown = value;
    }

    set changed(value: (plot: string) => void) {
        this._changed = value;
    }

    get title(): string {
        return this._title;
    }

    set title(value: string) {
        this._title = value;
    }

    get type(): PlotType {
        return this._type;
    }

    get id(): string {
        return this._id;
    }
}

export enum PlotType {
    TwoDimensional,
    ThreeDimensional,
    List
}