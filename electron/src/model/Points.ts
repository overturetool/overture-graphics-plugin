import {Point} from "./Point";
/**
 * Created by John on 31-05-2016.
 */
export class Points {
    x: Array<number> = new Array<number>();
    y: Array<number> = new Array<number>();
    z: Array<number> = new Array<number>();
    type: string;

    addPoint(pt: Point) {
        this.x.push(pt.x);
        this.y.push(pt.y);
        this.z.push(pt.z);
    }
}