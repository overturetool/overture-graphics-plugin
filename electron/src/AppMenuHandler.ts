
import {Navigation} from "./Navigation";
export class AppMenuHandler implements Navigation {
    openChartView: (id: string) => void;
    openAddPlotView: () => void;
    openRootClassPickerView: () => void;
    openRunFunctionPickerView: () => void;
    removePlot: (id: string) => void;
    runModel: () => void;
}