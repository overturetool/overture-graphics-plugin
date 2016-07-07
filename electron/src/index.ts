import {RootClassPickerController} from "./configuration/RootClassPickerController";
'use strict';
import {UpdateValue} from "./protocol/UpdateValue";
import {ModelStructure} from "./protocol/ModelStructure";
import {WindowController} from "./WindowController";
import {AppMenuHandler} from "./AppMenuHandler";
import {BrowserController} from "./plotBrowser/BrowserController";
import {PlotController} from "./plot/PlotController";
import {SubscriptionClient} from "./protocol/SubscriptionClient";
import {RunFunctionPickerController} from "./configuration/RunFunctionPickerController";

async function main() {
    // Start WebSocket client
    let client = new SubscriptionClient();
    await client.connect("ws://localhost:8080/subscription");
    //let model : ModelStructure = await client.getModelInfo();

    // Initialise controllers
    let title = "TEMPO Plotting Tool [No root class]";
    let menuHandler: AppMenuHandler = new AppMenuHandler();
    let browserController: BrowserController = new BrowserController(menuHandler);
    let windowCtrl = new WindowController(browserController, title);
    let plotController: PlotController = new PlotController(menuHandler, client, browserController);
    let rootPickerCtrl: RootClassPickerController = new RootClassPickerController(client, menuHandler, windowCtrl);
    let runPickerCtrl: RunFunctionPickerController = new RunFunctionPickerController(client, menuHandler);
    windowCtrl.initialize();

    // Setup menu handlers
    menuHandler.openChartView = (id) => {
        windowCtrl.layout.load("main", "plot/ShowPlotView.html", "", () => {
            plotController.showPlotById(id);
        });
    };
    menuHandler.openAddPlotView = () => {
        windowCtrl.layout.load("main","plot/CreatePlotView.html", "",
            () => plotController.addPlotDidMount());
    };
    menuHandler.openRootClassPickerView = () => {
        windowCtrl.layout.load("main","configuration/RootClassPickerView.html", "",
            () => rootPickerCtrl.didMount());
    };
    menuHandler.openRunFunctionPickerView = () => {
        windowCtrl.layout.load("main", "configuration/RunFunctionPickerView.html", "", async () => {
            await runPickerCtrl.didMount();
        });
    };
    menuHandler.runModel = () => {
        client.runModel()
    };
    menuHandler.removePlot = (id: string) => {
        plotController.removePlotById(id);
    };

    // Set initial main view.
    menuHandler.openRootClassPickerView();
}

main();