
import {AppEvents} from "./AppEvents";
import {BrowserController} from "./plotBrowser/BrowserController"
import {PlotController} from "./plot/PlotController"
import {AppMenuHandler} from "./AppMenuHandler"
import {SubscriptionClient} from "./protocol/SubscriptionClient";
import * as App from "./App";
import {RootClassPickerController} from "./configuration/RootClassPickerController";

// constants
const mainViewId: string = "mainView";

export class WindowController {
    layout: W2UI.W2Layout;
    private _title: HTMLTitleElement;
    private _titleText: string;
    private _browserCtrl: BrowserController;

    constructor(browserCtrl: BrowserController,
        title: string) {
        this._titleText = title;
        this._browserCtrl = browserCtrl;
    }

    get titleText(): string {
        return this._titleText;
    }

    set titleText(value: string) {
        this._titleText = value;
        this.setTitle();
    }

    initialize() {
        this.setTitle();
        this.configureLayout();
        this.loadViews();
    }

    getMainView(): HTMLDivElement {
        return <HTMLDivElement>document.getElementById(mainViewId);
    }

    private configureLayout() {
        let l: HTMLDivElement = <HTMLDivElement>document.querySelector("#layout");
        var pstyle = 'border: 1px solid #dfdfdf; padding: 5px; background-color: #FFFFFF';
        var topHtml = ""
        this.layout = $(l).w2layout({
            name: 'layout',
            padding: 4,
            panels: [
                { type: 'left', size: 200, resizable: true, style: pstyle, overflow: 'auto' },
                { type: 'main', style: pstyle, overflow: 'auto' },
            ]
        });
    }

    private setTitle() {
        //Set the title to the project name
        this._title = <HTMLTitleElement>document.querySelector('title');
        this._title.innerText = this._titleText;
    }

    private loadViews() {
        // Load main view
        this.layout.load("main", "main.html", "");

        // Load browser in left view
        this.layout.load("left", "plotBrowser/BrowserView.html", "", () => {
            this._browserCtrl.initialize();
        });
    }
};