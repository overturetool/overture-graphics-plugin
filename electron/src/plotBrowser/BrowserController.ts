//TODO: DUMMY REFERENCE UNTIL CHART MAKES A TYPESCRIPT DEFINITION FILE!

///<reference path="../../typings/globals/github-electron/index.d.ts"/>
///<reference path="../../typings/globals/node/index.d.ts"/>
///<reference path="../../typings/globals/jquery/index.d.ts"/>
///<reference path="../../typings/globals/w2ui/index.d.ts"/>

import {AppEvents} from "../AppEvents";
import {AppMenuHandler} from "../AppMenuHandler";
import * as App from "../App";

export class BrowserController {
    private browser: HTMLDivElement;
    private toolbar: HTMLDivElement;
    private tree: W2UI.W2Sidebar;
    private clickHandlers: Array<(event: JQueryEventObject) => void> = [];
    private dblClickHandlers: Array<(event: JQueryEventObject) => void> = [];

    private menuHandler: AppMenuHandler = null;

    constructor(menuHandler: AppMenuHandler) {
        this.menuHandler = menuHandler;
    }

    initialize() {
        let self = this;
        this.browser = <HTMLDivElement>document.querySelector("#browser");
        this.toolbar = <HTMLDivElement>document.querySelector("#toolbar");
        let remote = require("remote");

        // Setup sidebar with right click menu
        this.tree = $(this.browser).w2sidebar({
            name: 'sidebar',
            menu: [
                { id: "Remove", text: "Remove", icon: 'glyphicon glyphicon-remove' },
            ],
            onMenuClick: (event: any) => {
                let plot: string = event.target + "";
                let menuItem: string = event.menuItem.id;

                if (menuItem === "Remove") {
                    self.menuHandler.removePlot(plot);
                }
            }
        });

        // Add double click handlers
        this.addDblClickHandler((event: JQueryEventObject) => {
            console.info(event);
            let sender: string = event.target + "";

            if (sender != "2D" && sender != "3D" && sender != "List")
                self.menuHandler.openChartView(sender);
        });

        // Setup tree
        this.initTreeParentNodes();
        this.addTreeClickHandlers();

        // Setup toolbar
        this.setupToolbar();
    }

    private setupToolbar() {
        let self = this;

        // Setup toolbar
        let toolbar = $(this.toolbar).w2toolbar({
            name: 'myToolbar',
            items: [
                {
                    type: 'button', id: 'addPlot', caption: 'New', icon: 'glyphicon glyphicon-file'
                },
                { 
                    type: 'button', id: 'runModel', caption: 'Start', icon: 'glyphicon glyphicon-play'
                },
            ]
        });
        toolbar.on('click', (event: JQueryEventObject) => {
            let sender: string = event.target + "";

            if (sender == "runModel")
                self.menuHandler.runModel();
            if (sender == "addPlot")
                self.menuHandler.openAddPlotView();
        });
    };

    private initTreeParentNodes() {
        this.addToplevelNodes([
            {
                id: 'Plots', text: 'Plots', img: 'icon-folder', expanded: true, group: true,
                nodes: [
                    {
                        id: '2D', text: '2D', icon: 'glyphicon glyphicon-folder-open',
                        nodes: []
                    },
                    {
                        id: '3D', text: '3D', icon: 'glyphicon glyphicon-folder-open',
                        nodes: []
                    },
                    {
                        id: 'List', text: 'List', icon: 'glyphicon glyphicon-folder-open',
                        nodes: []
                    }
                ]
            }
        ]);
    }

    addPlot(parent: string, id: string, title: string) {
        let node: any = { id: id, text: title, icon: 'glyphicon glyphicon-stats' };
        this.addNodes(parent, node);
    }

    removePlot(id: string) {
        this.tree.remove(id);
    }

    addToplevelNodes(nodes: Object | Object[]): Object {
        return this.tree.add(nodes);
    }

    addNodes(parentId: string, nodes: Object | Object[]): Object {
        return this.tree.add(parentId, nodes);
    }

    clearAll() {
        let ids: string[] = this.tree.nodes.map((value: any) => {
            return value.id
        });
        this.tree.remove.apply(this.tree, ids);
    }

    addClickHandler(clickHandler: (event: JQueryEventObject) => void) {
        this.clickHandlers.push(clickHandler);
    }

    addDblClickHandler(clickHandler: (event: JQueryEventObject) => void) {
        this.dblClickHandlers.push(clickHandler);
    }

    private addTreeClickHandlers() {
        this.tree.on("dblClick", (event: JQueryEventObject) => {
            //Remove auto expansion on when double clicking

            this.dblClickHandlers.forEach(handler => {
                handler(event);
            })
        });

        this.tree.on("click", (event: JQueryEventObject) => {
            this.clickHandlers.forEach(handler => {
                handler(event);
            })
        });
    }

    getSelectedId(): string {
        return this.tree.selected;
    }
}