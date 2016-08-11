'use strict';
import {RootClassPickerController} from "./configuration/RootClassPickerController";
import {UpdateValue} from "./protocol/UpdateValue";
import {ModelStructure} from "./protocol/ModelStructure";
import {WindowController} from "./WindowController";
import {AppMenuHandler} from "./AppMenuHandler";
import {BrowserController} from "./plotBrowser/BrowserController";
import {PlotController} from "./plot/PlotController";
import {SubscriptionClient} from "./protocol/SubscriptionClient";
import {RunFunctionPickerController} from "./configuration/RunFunctionPickerController";
import {Configuration} from "./configuration/Configuration";
import {menuTemplate, addMacMenu} from "./menu/menuTemplate";
import MenuItemOptions = Electron.MenuItemOptions;
import * as electron from "electron";

let cfg: Configuration = new Configuration();

async function main() {
    setupMenu();

    // Start WebSocket client
    let client = new SubscriptionClient();
    await client.connect("ws://localhost:8080/subscription");

    // Initialise controllers
    let title = "TEMPO Plotting Tool [No root class]";
    let menuHandler: AppMenuHandler = new AppMenuHandler();
    let browserController: BrowserController = new BrowserController(menuHandler);
    let windowCtrl = new WindowController(browserController, title);
    let plotController: PlotController = new PlotController(menuHandler, client, browserController, cfg);
    let rootPickerCtrl: RootClassPickerController = new RootClassPickerController(client, menuHandler, windowCtrl, cfg);
    let runPickerCtrl: RunFunctionPickerController = new RunFunctionPickerController(client, menuHandler, cfg);
    windowCtrl.initialize();

    // Setup menu handlers
    menuHandler.openChartView = (id) => {
        windowCtrl.layout.load("main", "plot/ShowPlotView.html", "", () => {
            plotController.showPlotById(id);
        });
    };
    menuHandler.openAddPlotView = () => {
        windowCtrl.layout.load("main", "plot/CreatePlotView.html", "",
            () => plotController.addPlotDidMount());
    };
    menuHandler.openRootClassPickerView = () => {
        windowCtrl.layout.load("main", "configuration/RootClassPickerView.html", "",
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

    // Callback when config is loaded
    cfg.onLoad = async (cfg: Configuration) => {
        await rootPickerCtrl.setRootClass(cfg.rootClass, rootPickerCtrl);
        runPickerCtrl.setRunFunction(cfg.runFunction);

        for (let key of cfg.plots.keys()) {
            let value = cfg.plots.getValue(key);
            await plotController.createPlot(key, value.variables, value.type);
        }

        menuHandler.openAddPlotView();
    };

    // Set initial main view.
    menuHandler.openRootClassPickerView();

    // On quit, send stop request to websocket server
    window.onbeforeunload = (e: Event) => { 
        client.stop();
    }
}

function setupMenu() {
    // Create template
    var template = <MenuItemOptions[]>menuTemplate;
    template.unshift({
        label: 'File',
        submenu: [
            {
                label: 'Load',
                click: cfg.load.bind(cfg)
            },
            {
                label: 'Save',
                accelerator: 'CmdOrCtrl+S',
                click: cfg.save.bind(cfg)
            }
        ]
    });
    addMacMenu(template);
    
    // Setup menu
    var _menu = electron.remote.Menu.buildFromTemplate(template);
    electron.remote.Menu.setApplicationMenu(_menu);
}

main();