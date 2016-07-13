///<reference path="plotlyjs.d.ts"/>
import {AppMenuHandler} from "../AppMenuHandler"
import {Plot, PlotType} from "../model/Plot";
import {SubscriptionClient} from "../protocol/SubscriptionClient";
import * as Collections from 'typescript-collections';
import {ModelStructure} from "../protocol/ModelStructure";
import {BrowserController} from "../plotBrowser/BrowserController";
import {Configuration} from "../configuration/Configuration";

export class PlotController {
    private _plots = new Collections.Dictionary<string, Plot>();
    private _menuHandler: AppMenuHandler;
    private _subClient: SubscriptionClient;
    private _browserCtrl: BrowserController;
    private _form: W2UI.W2Form;
    private _grid: W2UI.W2Grid;
    private _cfg: Configuration;

    constructor(menuHandler: AppMenuHandler, subClient: SubscriptionClient, browserCtrl: BrowserController, cfg: Configuration) {
        this._menuHandler = menuHandler;
        this._subClient = subClient;
        this._browserCtrl = browserCtrl;
        this._cfg = cfg;
    }

    async addPlotDidMount() {
        if (this._form != null) {
            w2ui["addPlot"].render($('#addPlot')[0]);
            return;
        }

        let self = this;
        let model: ModelStructure = await this._subClient.getModelInfo();
        let vars = model.getAllVariables();

        this._form = $('#addPlot').w2form({
            name: "addPlot",
            header: 'Add Plot',
            fields: [
                { name: 'field_text', type: 'text', required: true },
                {
                    name: 'field_type', type: 'list', required: true,
                    options: { items: [{ id: 0, text: "2D" }, { id: 1, text: "3D" }, { id: 2, text: "List" }] }
                },
                {
                    name: 'field_list', type: 'enum', required: true,
                    options: {
                        openOnFocus: true,
                        items: vars
                    }
                }
            ],
            actions: {
                reset: function () {
                    this.clear();
                },
                save: async function () {
                    var title = this.record["field_text"];
                    var type: number = this.record["field_type"].id;

                    var variableNames = this.record["field_list"];
                    var varNamesStripped = new Array<string>();
                    for (let variable of variableNames) {
                        varNamesStripped.push(variable.text)
                    }

                    await self.createPlot(title, varNamesStripped, <PlotType>type);
                    self._cfg.addPlot(title, varNamesStripped, <PlotType>type);
                    this.clear();
                }
            }
        });
    }

    getExistingVariables(variables: string[]): string[] {
        // Check which of the given variables that exist, and return these
        var existingVariables = variables;//new Array<string>();
        return existingVariables;
    }

    async createPlot(title: string, variables: string[], type: PlotType) {
        // Create plot and add to plot map
        let plot = new Plot(title, type);
        this._plots.setValue(plot.id, plot);

        // Subscribe to variables
        for (let variable of variables) {
            plot.addVariable(variable);

            var result = await this._subClient.subscribe(variable, plot.id, (data: any) => {
                plot.addPoint(variable, +data.value);
                plot.changed = this.updatePlot.bind(this);
            });
            console.log(result);
        }

        // Add to browser
        switch (plot.type) {
            case PlotType.TwoDimensional:
                this._browserCtrl.addPlot("2D", plot.id, title);
                break;
            case PlotType.ThreeDimensional:
                this._browserCtrl.addPlot("3D", plot.id, title);
                break;
            case PlotType.List:
                this._browserCtrl.addPlot("List", plot.id, title);
                break;
        }
    }

    showPlotById(id: string) {
        let plot: Plot = this._plots.getValue(id);
        if (plot == null)
            return;

        // Set shown false for each plot
        for (var i = 0; i < this._plots.size(); i++) {
            this._plots.values()[i].shown = false;
        }

        switch (plot.type) {
            case PlotType.TwoDimensional:
                this.show2DPlot(plot);
                break;
            case PlotType.ThreeDimensional:
                this.show3DPlot(plot);
                break;
            case PlotType.List:
                this.showList(plot);
                break;
            default:
                break;
        }
    }

    updatePlot(title: string) {
        let plot: Plot = this._plots.getValue(title);
        if (plot == null || <HTMLDivElement>document.querySelector("#plot") == undefined)
            return;

        if (plot.type === PlotType.List) {
            this.refreshList(plot)
        }
        else {
            // Retrieve trace
            var tracex = new Array();
            var tracey = new Array();
            var tracez = new Array();
            var indices = new Array();
            var index = 0;
            for (let points of plot.data.values()) {
                tracex.push(points.x);
                tracey.push(points.y);
                tracez.push(points.z);
                indices.push(index++);
            }

            let trace = { x: tracex, y: tracey, z: tracez };

            Plotly.restyle('plot', trace, indices);
        }
    }

    removePlotById(id: string) {
        var plot: Plot = this._plots.getValue(id);
        for (var variable of plot.data.keys()) {
            this._subClient.unsubscribe(variable, id);
        }

        this._plots.remove(id);
        this._browserCtrl.removePlot(id);
    }

    private show2DPlot(plot: Plot) {
        // Retrieve trace
        var traces = new Array();
        for (let points of plot.data.values()) {
            let trace = { x: points.x, y: points.y, z: points.z, type: points.type, name: points.name };
            traces.push(trace);
        }

        // Setup layout and plot
        var layout: any = {
            title: plot.title,
            showlegend: true,
            autosize: true,
            width: 1050,
            height: 750,
            scene: {
                xaxis: { title: 'Time' },
                yaxis: { title: 'Value' }
            }
        };
        Plotly.newPlot('plot', traces, layout, { showLink: false });
        plot.shown = true;
    }

    private show3DPlot(plot: Plot) {
        // Retrieve trace
        var traces = new Array();
        for (let points of plot.data.values()) {
            let trace = { x: points.x, y: points.y, z: points.z, type: points.type, name: points.name };
            traces.push(trace);
        }

        // Setup layout and plot
        var layout: any = {
            title: plot.title,
            showlegend: true,
            autosize: true,
            width: 1050,
            height: 750,
            scene: {
                xaxis: { title: 'Edge #' },
                yaxis: { title: 'Time' },
                zaxis: { title: 'Value' }
            }
        };
        Plotly.newPlot('plot', traces, layout, { showLink: false });
        plot.shown = true;
    }

    private showList(plot: Plot) {
        if (this._grid != null) {
            w2ui["dataList"].render($('#plot')[0]);
            this._grid.clear();
        }
        else {
            this._grid = $('#plot').w2grid({
                name: 'dataList',
                fixedBody: true,
                columns: [
                    { field: 'variableName', caption: 'Variable Name', size: '30%' },
                    { field: 'averageValue', caption: 'Average Value', size: '20%' },
                    { field: 'currentValue', caption: 'Current Value', size: '20%' }
                ]
            });
        }

        var recNum = 1;
        for (let variable of plot.data.values()) {
            this._grid.add({
                recid: recNum++,
                variableName: variable.name,
                averageValue: 0,
                currentValue: 0
            })
        }

        this.refreshList(plot);
        plot.shown = true;
    }

    private refreshList(plot: Plot) {
        for (let record of w2ui['dataList'].records) {
            let value = plot.data.getValue(record.variableName);

            if (value !== undefined && value.getLatest() != undefined) {
                record.currentValue = value.getLatest().y;
                record.averageValue = value.averageValue();
            }
        }

        w2ui['dataList'].refresh();
    }
}