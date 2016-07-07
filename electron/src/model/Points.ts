import {Point} from "./Point";

/**
 * Created by John on 31-05-2016.
 */
export class Points {
    x: Array<any> = new Array<any>();
    y: Array<any> = new Array<any>();
    z: Array<any> = new Array<any>();
    type: string;
    zIndex: number;
    name: string;

    addPoint(pt: Point) {
        this.x.push(pt.x);
        this.y.push(pt.y);
        this.z.push(pt.z);
    }

    getLatest(): Point {
        if (this.x.length > 0) {
            return {
                x: <number | number[]>this.x[this.x.length - 1],
                y: <number | number[]>this.y[this.y.length - 1],
                z: <number | number[]>this.z[this.z.length - 1]
            };
        }
        else {
            return undefined;
        }
    }

    averageValue(): number {
        var sumValue = 0;
        for (let num of <number[]>this.y) {
            sumValue += num;
        }
        return sumValue/this.y.length;
    }
}