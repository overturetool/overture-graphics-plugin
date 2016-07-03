import {RootClassPickerController} from "./configuration/RootClassPickerController";
'use strict';
import {UpdateValue} from "./protocol/UpdateValue";
import {ModelStructure} from "./protocol/ModelStructure";
import {InitializationController} from "./Initialization";
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
    //let title = "TEMPO Plotting Tool ["+ model.rootClass +"]";
    let title = "TEMPO Plotting Tool [No root class]";
    let menuHandler: AppMenuHandler = new AppMenuHandler();
    let browserController: BrowserController = new BrowserController(menuHandler);
    let plotController: PlotController = new PlotController(menuHandler, client, browserController);
    let rootPickerCtrl: RootClassPickerController = new RootClassPickerController(client, menuHandler);
    let runPickerCtrl: RunFunctionPickerController = new RunFunctionPickerController(client, menuHandler);
    let init = new InitializationController(browserController, plotController, rootPickerCtrl, title);
    init.initialize();


    // Setup menu handlers
    menuHandler.openChartView = (id) => {
        init.layout.load("main", "plot/ShowPlotView.html", "", () => {
            plotController.show2DPlotByTitle(id);
        });
    };
    menuHandler.openAddPlotView = async () => {
        await init.loadCreatePlotView();
    };
    menuHandler.openRootClassPickerView = async () => {
        await init.loadRootClassPickerView();
    };
    menuHandler.openRunFunctionPickerView = () => {
        init.layout.load("main", "configuration/RunFunctionPickerView.html", "", async () => {
            await runPickerCtrl.didMount();
        });
    };
    menuHandler.runModel = () => {
        client.runModel()
    };

    // Set initial main view.
    await init.loadRootClassPickerView();
}

main();