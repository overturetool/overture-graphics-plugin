
import {AppEvents} from "./AppEvents";
import * as App from  "./App"
import {BrowserController} from "./modelBrowser/BrowserController"
import {PlotController} from "./plot/PlotController"
import {AppMenuHandler} from "./AppMenuHandler"
import {SubscriptionClient} from "./protocol/SubscriptionClient";

// constants
var mainViewId: string = "mainView";

class InitializationController {
    layout: W2UI.W2Layout;
    title: HTMLTitleElement;
    mainView: HTMLDivElement;
    
    constructor() {
        $(document).ready(() => this.initialize());
    }
    
    initialize() {
        this.setTitle();
        this.configureLayout();
        this.loadViews();
    }
    
    private configureLayout() {
        let layout: HTMLDivElement = <HTMLDivElement>document.querySelector("#layout");
        var pstyle = 'border: 1px solid #dfdfdf; padding: 5px; background-color: #FFFFFF';
        var topHtml = ""
        this.layout = $(layout).w2layout({
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
        this.title.innerText = "TEMPO Plotting Tool [CurrentModel]";
    }

    private loadViews() {
        this.layout.load("main", "main.html", "", () => {
            /// Switch active tab marker
            $('.navbar li').click(function (e) {
                $('.navbar li.active').removeClass('active');
                var $this = $(this);
                if (!$this.hasClass('active')) {
                    $this.addClass('active');
                }
            });
            this.mainView = (<HTMLDivElement>document.getElementById(mainViewId));
            this.loadPlotView();
        });
        this.layout.load("left", "modelBrowser/BrowserView.html", "", () => {
            browserController.initialize();
        });
    }

    loadPlotView() {
        $(this.mainView).load("plot/PlotView.html", () => plotController.initialize());
    }
};

// Initialise controllers so they persist
let menuHandler: AppMenuHandler = new AppMenuHandler();
var browserController: BrowserController = new BrowserController(menuHandler);
var plotController: PlotController = new PlotController(menuHandler);
var init = new InitializationController();

// Start WebSocket server and forward messages to plotController addPoint
//var wsServer = new WsServer("localhost", 8080);
//wsServer.setOnMessage(plotController.addPoint);
var client = new SubscriptionClient();
client.connect("ws://localhost:8080/subscription");

menuHandler.openChartView = (path) => {
    $(init.mainView).load("plot/PlotView.html", () => {
    });

};
