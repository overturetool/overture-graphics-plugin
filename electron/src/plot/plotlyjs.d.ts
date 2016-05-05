declare var Plotly : PlotlyJS

interface PlotlyJS {
    newPlot(div: String, data: Object, layout: Object): void;
    newPlot(div: String, data: Object, layout: Object, options: Object): void;
    restyle(div: String, data: Object): void;
    restyle(div: String, data: Object, options: Object): void;
}