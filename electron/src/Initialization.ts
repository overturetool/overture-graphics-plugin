
import {AppEvents} from "./AppEvents";
import {BrowserController} from "./plotBrowser/BrowserController"
import {PlotController} from "./plot/PlotController"
import {AppMenuHandler} from "./AppMenuHandler"
import {SubscriptionClient} from "./protocol/SubscriptionClient";
import * as App from "./App";
import {RootClassPickerController} from "./configuration/RootClassPickerController";

// constants
const mainViewId: string = "mainView";

export class InitializationController {
    layout: W2UI.W2Layout;
    title: HTMLTitleElement;
    browserCtrl: BrowserController;
    plotCtrl: PlotController;
    titleText: string;
    private rootPickerCtrl: RootClassPickerController;

    constructor(browserCtrl: BrowserController, plotCtrl: PlotController, rootPickerCtrl: RootClassPickerController, title: string) {
        this.rootPickerCtrl = rootPickerCtrl;
        this.titleText = title;
        this.browserCtrl = browserCtrl;
        this.plotCtrl = plotCtrl;
    }
    
    initialize() {
        this.setTitle();
        this.configureLayout();
        this.loadViews();
    }
    
    private configureLayout() {
        let l: HTMLDivElement = <HTMLDivElement>document.querySelector("#layout");
        var pstyle = 'border: 1px solid #dfdfdf; padding: 5px; background-color: #FFFFFF';
        var topHtml = ""
        this.layout = $(l).w2layout({
            name: 'layout',
            padding: 4,
            panels: [
                { type: 'left', size: 200, resizable: true, style: pstyle },
                { type: 'main', style: pstyle },
            ]
        });
    }
    
    private setTitle() {
        //Set the title to the project name
        this.title = <HTMLTitleElement>document.querySelector('title');
        this.title.innerText = this.titleText;//;
    }

    private loadViews() {
        // Load main view
        this.layout.load("main", "main.html", "");

        // Load browser in left view
        this.layout.load("left", "plotBrowser/BrowserView.html", "", () => {
            this.browserCtrl.initialize();
        });
    }

    loadPlotView() {
        this.layout.load("main", "plot/ShowPlotView.html", "",
            () => this.plotCtrl.initialize());
    }

    loadCreatePlotView() {
        this.layout.load("main","plot/CreatePlotView.html", "",
            () => this.plotCtrl.add2DPlotDidMount());
    }

    loadRootClassPickerView() {
        this.layout.load("main","configuration/RootClassPickerView.html", "",
            () => this.rootPickerCtrl.didMount());
    }

    getMainView() : HTMLDivElement {
        return <HTMLDivElement>document.getElementById(mainViewId);
    }
};