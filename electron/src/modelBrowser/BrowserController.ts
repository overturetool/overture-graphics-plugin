//TODO: DUMMY REFERENCE UNTIL CHART MAKES A TYPESCRIPT DEFINITION FILE!

///<reference path="../../typings/globals/github-electron/index.d.ts"/>
///<reference path="../../typings/globals/node/index.d.ts"/>
///<reference path="../../typings/globals/jquery/index.d.ts"/>
///<reference path="../../typings/globals/w2ui/index.d.ts"/>

import {AppEvents} from "../AppEvents";
import * as App from  "../App";
import {AppMenuHandler} from "../AppMenuHandler";


export class BrowserController {
    private browser: HTMLDivElement;
    private tree: W2UI.W2Sidebar;
    private clickHandlers: Array<(event: JQueryEventObject) => void> = [];
    private dblClickHandlers: Array<(event: JQueryEventObject) => void> = [];

    private menuHandler: AppMenuHandler = null;

    constructor(menuHandler: AppMenuHandler) {
        this.menuHandler = menuHandler;
    }

    initialize() {
        let _this2 = this;
        this.browser = <HTMLDivElement>document.querySelector("#browser");
        let remote = require("remote");

        this.tree = $(this.browser).w2sidebar({
            name: 'sidebar',
             menu: [
                {id: "Add", text: "Add", icon: 'glyphicon glyphicon-add'},
                {id: "Remove", text: "Remove", icon: 'glyphicon glyphicon-remove'},
            ]
        });

        this.addDblClickHandler((event: JQueryEventObject) => {
            console.info(event);

            if ((event.target + "").indexOf('coe.json') >= 0) {
                _this2.menuHandler.openChartView(event.target + "");
            }
        });
        
        // Setup tree
        this.exampleOfInitTreeNodes();
        this.addTreeClickHandlers();
        
        // Listen to IPC events of model changes
        var ipc = require('electron').ipcRenderer;
        ipc.on(AppEvents.PROJECT_CHANGED, function (event, arg) {
            //TODO: Set tree view browser
        });
    }

    /*private buildProjectStructor(level: number, root: Container): any {

        let _this = this;
        var items: any[] = [];
        let contentProvider: ContentProvider = new ContentProvider();

        contentProvider.getChildren(root).forEach((value: Container, index: number, array: Container[]) => {

            var name = value.name;
            if (name.indexOf('.') > 0) {
                name = name.substring(0, name.indexOf('.'));
            }

            var item: any = new Object();
            item.id = value.filepath;
            item.text = name;
            item.expanded = true

            if (level == 0)
                item.group = true;


            switch (value.type) {
                case ContainerType.Folder:
                    {
                        item.img = 'icon-folder';
                        item.nodes = _this.buildProjectStructor(level + 1, value);
                        break;
                    };
                case ContainerType.FMU:
                    {
                        item.img = 'icon-page';
                        break;
                    };
                case ContainerType.MultiModelConfig:
                    {
                        item.img = 'glyphicon glyphicon-briefcase';
                        break;
                    };
                case ContainerType.CoeConfig:
                    {
                        item.img = 'glyphicon glyphicon-copyright-mark';
                        break;
                    };
                case ContainerType.SysMLExport:
                    {
                        item.img = 'glyphicon glyphicon-tasks';
                        break;
                    };

            }

            items.push(item);
        });

        console.info(items);
        return items;
    }*/

    private exampleOfInitTreeNodes() {
        this.addToplevelNodes([
            {
                id: 'Plots', text: 'Plots', img: 'icon-folder', expanded: true, group: true,
                nodes: [{
                    id: 'Plot-2d', text: '2D', icon: 'glyphicon glyphicon-folder-open',
                    nodes: [
                        {
                            id: 'Plot-2d-1', text: 'Edge 1', icon: 'glyphicon glyphicon-file'
                        },
                        {
                            id: 'Plot-2d-2', text: 'Edge 2', icon: 'glyphicon glyphicon-file'
                        },
                        {
                            id: 'Plot-2d-3', text: 'Edge 3', icon: 'glyphicon glyphicon-file'

                        }
                    ]
                },
                {
                    id: 'Plot-3d', text: '3D', icon: 'glyphicon glyphicon-folder-open',
                    nodes: [
                        {
                            id: 'Plot-3d-1', text: 'Edge 1-3', icon: 'glyphicon glyphicon-file'
                        }]
                }
            ]}
        ]);
    }

    private exampleOfAddingNode() {
        let node: any = { id: 'fmu-waterTank', text: 'Water tank fmu', img: 'icon-folder' };
        let parent = "FMUs";
        this.addNodes(parent, node);
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
            event.preventDefault();
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