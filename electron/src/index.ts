'use strict';
import {UpdateValue} from "./protocol/UpdateValue";
import {ModelStructure} from "./protocol/ModelStructure";
import {InitializationController} from "./Initialization";
import {AppMenuHandler} from "./AppMenuHandler";
import {BrowserController} from "./plotBrowser/BrowserController";
import {PlotController} from "./plot/PlotController";
import {SubscriptionClient} from "./protocol/SubscriptionClient";

async function main() {
    // Start WebSocket client
    let client = new SubscriptionClient();
    await client.connect("ws://localhost:8080/subscription");
    let model : ModelStructure = await client.getModelInfo();

    // Initialise controllers
    let title = "TEMPO Plotting Tool ["+ model.rootClass +"]";
    let menuHandler: AppMenuHandler = new AppMenuHandler();
    let browserController: BrowserController = new BrowserController(menuHandler);
    let plotController: PlotController = new PlotController(menuHandler, client, browserController);
    let init = new InitializationController(browserController, plotController, title);
    init.initialize();
    await init.loadCreatePlotView();


    // Setup menu handlers
    menuHandler.openChartView = (id) => {
        init.layout.load("main", "plot/ShowPlotView.html", "", () => {
            plotController.show2DPlotByTitle(id);
        });
    };
    menuHandler.openAddPlotView = async () => {
        await init.loadCreatePlotView();
    };
    menuHandler.runModel = () => {
        client.runModel()
    };
}

main();